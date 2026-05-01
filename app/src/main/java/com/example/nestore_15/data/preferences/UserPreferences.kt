package com.example.nestore_15.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userDataStore by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    private object Keys {
        val userId = stringPreferencesKey("user_id")
        val userEmail = stringPreferencesKey("user_email")
        val userRole = stringPreferencesKey("user_role")
        val isVerified = booleanPreferencesKey("is_verified")
        val fullName = stringPreferencesKey("full_name")
        val phone = stringPreferencesKey("phone")
        val photoUrl = stringPreferencesKey("photo_url")
        val verificationStatus = stringPreferencesKey("verification_status")
        val studentInstitution = stringPreferencesKey("student_institution")
        val providerBusinessName = stringPreferencesKey("provider_business_name")
        val studentBudgetMax = doublePreferencesKey("student_budget_max")
    }

    val currentUser: Flow<User?> =
        context.userDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences())
                else throw exception
            }
            .map { preferences ->
                preferences.toUserOrNull()
            }

    suspend fun saveUser(user: User) {
        context.userDataStore.edit { prefs ->
            prefs[Keys.userId] = user.id
            prefs[Keys.userEmail] = user.email
            prefs[Keys.userRole] = user.role.name
            prefs[Keys.isVerified] = user.isVerified
            prefs[Keys.fullName] = user.fullName
            prefs[Keys.phone] = user.phone
            prefs[Keys.photoUrl] = user.photoUrl
            prefs[Keys.verificationStatus] = user.verificationStatus.name
            prefs[Keys.studentInstitution] = user.studentInstitution
            prefs[Keys.providerBusinessName] = user.providerBusinessName
            if (user.studentBudgetMax != null) {
                prefs[Keys.studentBudgetMax] = user.studentBudgetMax
            } else {
                prefs.remove(Keys.studentBudgetMax)
            }
        }
    }

    suspend fun clearUser() {
        context.userDataStore.edit { it.clear() }
    }

    private fun Preferences.toUserOrNull(): User? {
        val id = this[Keys.userId] ?: return null
        val email = this[Keys.userEmail] ?: return null
        val roleValue = this[Keys.userRole] ?: return null
        val role = runCatching { UserRole.valueOf(roleValue) }.getOrNull() ?: return null
        val isVerified = this[Keys.isVerified] ?: false
        val storedVs = runCatching {
            VerificationStatus.valueOf(this[Keys.verificationStatus] ?: "")
        }.getOrDefault(VerificationStatus.NOT_SUBMITTED)
        val verificationStatus = if (isVerified) VerificationStatus.VERIFIED else storedVs
        return User(
            id = id,
            email = email,
            role = role,
            isVerified = isVerified,
            fullName = this[Keys.fullName].orEmpty(),
            phone = this[Keys.phone].orEmpty(),
            photoUrl = this[Keys.photoUrl].orEmpty(),
            verificationStatus = verificationStatus,
            studentInstitution = this[Keys.studentInstitution].orEmpty(),
            providerBusinessName = this[Keys.providerBusinessName].orEmpty(),
            studentBudgetMax = this[Keys.studentBudgetMax]
        )
    }
}
