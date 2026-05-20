package com.example.nestore_15.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nestore_15.R
import com.example.nestore_15.data.model.User
import com.example.nestore_15.data.util.ListingImageResolver
import com.example.nestore_15.data.model.UserRole
import com.example.nestore_15.data.model.VerificationStatus
import com.example.nestore_15.ui.components.FindAHomeCenterTopBar
import com.example.nestore_15.ui.components.FindAHomeTopAppBar
import com.example.nestore_15.ui.components.FullScreenLoading
import com.example.nestore_15.ui.components.OutlinedBlueButton
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.components.SecondaryGreenButton
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.viewmodel.ProfileUiState

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onEditProfile: () -> Unit,
    onVerify: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
    ) {
        if (showBack) {
            FindAHomeCenterTopBar(title = "My Profile", onBack = onBack)
        } else {
            FindAHomeTopAppBar(title = "My Profile")
        }

        when (uiState) {
            ProfileUiState.Loading -> FullScreenLoading("Loading profile…")
            ProfileUiState.Error -> Text(
                "Unable to load profile",
                modifier = Modifier.padding(24.dp),
                color = FindAHomeColors.ErrorRed
            )
            is ProfileUiState.Success -> ProfileContent(
                user = uiState.user,
                onEditProfile = onEditProfile,
                onVerify = onVerify,
                onLogout = onLogout,
                onChangePassword = onChangePassword
            )
        }
    }
}

@Composable
private fun ProfileContent(
    user: User,
    onEditProfile: () -> Unit,
    onVerify: () -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit
) {
    val status = user.effectiveVerificationStatus()
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (user.photoUrl.isNotBlank() && ListingImageResolver.isRemote(user.photoUrl)) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.profile_placeholder),
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    user.displayNameOrEmail(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = FindAHomeColors.TextSecondary)
                Text(
                    when (user.role) {
                        UserRole.STUDENT -> "Student"
                        UserRole.PROVIDER -> "Home Provider"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = FindAHomeColors.OrangeAccent,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        ProfileInfoCard("Verification", verificationDetail(status))
        Spacer(Modifier.height(12.dp))
        ProfileInfoCard("Full name", user.fullName.ifBlank { "—" })
        ProfileInfoCard("Phone", user.phone.ifBlank { "—" })

        if (user.role == UserRole.STUDENT) {
            ProfileInfoCard("Institution", user.studentInstitution.ifBlank { "—" })
            ProfileInfoCard("Student ID", user.studentId.ifBlank { "—" })
        } else {
            ProfileInfoCard("Business", user.providerBusinessName.ifBlank { "—" })
            ProfileInfoCard("Address", user.providerContactAddress.ifBlank { "—" })
        }

        Spacer(Modifier.height(20.dp))
        PrimaryOrangeButton("Edit Profile", onEditProfile, Modifier.fillMaxWidth())
        if (status != VerificationStatus.VERIFIED) {
            Spacer(Modifier.height(8.dp))
            SecondaryGreenButton("Submit Verification", onVerify, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(8.dp))
        OutlinedBlueButton("Change Password", onChangePassword, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedBlueButton("Log Out", onLogout, Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileInfoCard(label: String, value: String) {
    Card(
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = FindAHomeColors.TextSecondary)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

private fun verificationDetail(status: VerificationStatus): String = when (status) {
    VerificationStatus.VERIFIED -> "Verified — full access enabled"
    VerificationStatus.PENDING_REVIEW -> "Pending review"
    VerificationStatus.NOT_SUBMITTED -> "Not submitted"
}
