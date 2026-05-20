package com.example.nestore_15.data.repository

import android.net.Uri

/**
 * Profile/verification uploads — Storage bypass for assignment builds.
 * Returns local markers so Firestore fields indicate "on file" without hosting files.
 */
class UserRepository {

    private companion object {
        const val LOCAL_PROFILE_MARKER = "local://profile_selected"
        const val LOCAL_VERIFICATION_MARKER = "local://verification_on_file"
        const val LOCAL_OWNERSHIP_MARKER = "local://ownership_on_file"
    }

    suspend fun uploadProfilePhoto(userId: String, uri: Uri): String {
        uri // picker used; no cloud upload in assignment mode
        return LOCAL_PROFILE_MARKER
    }

    suspend fun uploadVerificationDocument(userId: String, uri: Uri): String {
        uri
        return LOCAL_VERIFICATION_MARKER
    }

    suspend fun uploadOwnershipProof(userId: String, uri: Uri): String {
        uri
        return LOCAL_OWNERSHIP_MARKER
    }
}
