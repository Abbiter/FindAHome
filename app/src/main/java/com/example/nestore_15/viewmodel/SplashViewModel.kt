package com.example.nestore_15.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.session.SessionManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

sealed class SplashDestination {
    object Login : SplashDestination()
    data class Home(val role: UserRole) : SplashDestination()
}

sealed class SplashUiState {
    data object Loading : SplashUiState()
    data class Ready(val destination: SplashDestination) : SplashUiState()
    data class ConnectivityIssue(val message: String) : SplashUiState()
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)

    private val _uiState = MutableLiveData<SplashUiState>(SplashUiState.Loading)
    val uiState: LiveData<SplashUiState> = _uiState

    fun startStartupFlow() {
        viewModelScope.launch {
            _uiState.value = SplashUiState.Loading
            runStartup().fold(
                onSuccess = { dest -> _uiState.value = SplashUiState.Ready(dest) },
                onFailure = { e ->
                    _uiState.value = SplashUiState.ConnectivityIssue(
                        e.message ?: "Unable to reach services."
                    )
                }
            )
        }
    }

    private suspend fun runStartup(): Result<SplashDestination> = withContext(Dispatchers.IO) {
        runCatching {
            FirebaseApp.getInstance()
            FirebaseAuth.getInstance()

            val firestoreReady = verifyFirestoreResponsive()
            if (!firestoreReady) {
                error("Connection issue. Check your network and try again.")
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                return@runCatching SplashDestination.Login
            }

            val role = withTimeoutOrNull(12_000) {
                sessionManager.userRole.filterNotNull().first()
            } ?: UserRole.STUDENT

            SplashDestination.Home(role)
        }
    }

    /**
     * Confirms Firestore is reachable. Permission-denied still means the backend responded.
     */
    private suspend fun verifyFirestoreResponsive(): Boolean =
        withTimeoutOrNull(10_000) {
            try {
                FirebaseFirestore.getInstance()
                    .collection("listings")
                    .limit(1)
                    .get()
                    .await()
                true
            } catch (e: FirebaseFirestoreException) {
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            } catch (_: Exception) {
                false
            }
        } ?: false
}
