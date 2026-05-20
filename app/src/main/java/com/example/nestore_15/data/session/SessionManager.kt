package com.example.nestore_15.data.session

import android.content.Context
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.preferences.UserPreferences
import com.example.nestore_15.data.repository.toFirestoreMap
import com.example.nestore_15.data.repository.toUserFromDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SessionManager(
    context: Context,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val userPreferences = UserPreferences(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val currentUserFlow: StateFlow<User?> = authStateChanges()
            .flatMapLatest { firebaseUser ->
                if (firebaseUser == null) {
                    flowOf<User?>(null)
                } else {
                    observeUserProfile(firebaseUser)
                }
            }
            .onEach { user ->
                if (user == null) {
                    userPreferences.clearUser()
                } else {
                    userPreferences.saveUser(user)
                }
            }
            .distinctUntilChanged()

            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = null
            )

    val isLoggedIn: Flow<Boolean> = currentUserFlow.map { it != null }.distinctUntilChanged()
    val isVerified: Flow<Boolean> = currentUserFlow.map { it?.isVerified == true }.distinctUntilChanged()
    val userRole: Flow<UserRole?> = currentUserFlow.map { it?.role }.distinctUntilChanged()

    fun getCurrentUser(): Flow<User?> = currentUserFlow

    suspend fun saveUser(user: User) {
        firestore.collection("users").document(user.id).set(user.toFirestoreMap(), SetOptions.merge()).await()
        userPreferences.saveUser(user)
    }

    suspend fun clearSession() {
        firebaseAuth.signOut()
        userPreferences.clearUser()
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Resolves the signed-in user for gated screens. The in-memory [currentUserFlow] can briefly
     * emit null while Firestore hydrates; we also fall back to DataStore after a short wait so
     * provider child activities match [ProviderHomeActivity] behaviour (uid present ⇒ not logged out).
     */
    suspend fun awaitCurrentUser(): User? {
        val uid = getCurrentUserId() ?: return null
        val fromHot = withTimeoutOrNull(800L) {
            getCurrentUser().first { it != null && it.id == uid }
        }
        if (fromHot != null) return fromHot
        val cached = userPreferences.currentUser.first().takeIf { it?.id == uid }
        if (cached != null) return cached
        return withTimeoutOrNull(20_000L) {
            getCurrentUser().first { it != null && it.id == uid }
        }
    }

    private fun authStateChanges(): Flow<FirebaseUser?> {
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser).isSuccess
            }
            firebaseAuth.addAuthStateListener(listener)
            trySend(firebaseAuth.currentUser).isSuccess
            awaitClose { firebaseAuth.removeAuthStateListener(listener) }
        }
    }

    private fun observeUserProfile(firebaseUser: FirebaseUser): Flow<User?> {
        return callbackFlow {
            val docRef = firestore.collection("users").document(firebaseUser.uid)
            val registration: ListenerRegistration = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null).isSuccess
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    trySend(null).isSuccess
                    return@addSnapshotListener
                }
                trySend(snapshot.toUserFromDocument(firebaseUser)).isSuccess
            }

            awaitClose { registration.remove() }
        }
    }

}
