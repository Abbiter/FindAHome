package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.components.BrandLogo
import com.example.nestore_15.data.model.RegistrationRole
import com.example.nestore_15.ui.components.EmailField
import com.example.nestore_15.ui.components.NameField
import com.example.nestore_15.ui.components.PasswordField
import com.example.nestore_15.ui.components.PhoneField
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.components.SecondaryGreenButton
import com.example.nestore_15.ui.theme.ButtonShape
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.viewmodel.RegisterFieldErrors

@Composable
fun RegisterScreen(
    selectedRole: RegistrationRole,
    onRoleSelected: (RegistrationRole) -> Unit,
    fieldErrors: RegisterFieldErrors?,
    isSubmitting: Boolean,
    onSubmit: (fullName: String, phone: String, email: String, password: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.BackgroundSoft)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandLogo(
            fillMaxWidthFraction = 0.55f,
            squareAspect = true,
            modifier = Modifier.padding(bottom = 12.dp, start = 24.dp, end = 24.dp)
        )
        Text(
            "Create your account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = FindAHomeColors.PrimaryDarkBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Join as a student or home provider.",
            style = MaterialTheme.typography.bodyMedium,
            color = FindAHomeColors.TextSecondary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            RoleChip(
                text = "Student",
                selected = selectedRole == RegistrationRole.STUDENT,
                onClick = { onRoleSelected(RegistrationRole.STUDENT) },
                modifier = Modifier.weight(1f),
                isPrimary = true
            )
            RoleChip(
                text = "Home Provider",
                selected = selectedRole == RegistrationRole.HOME_PROVIDER,
                onClick = { onRoleSelected(RegistrationRole.HOME_PROVIDER) },
                modifier = Modifier.weight(1f),
                isPrimary = false
            )
        }

        Spacer(Modifier.height(20.dp))

        Card(
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(24.dp)) {
                NameField(
                    fullName,
                    { fullName = it },
                    isError = fieldErrors?.let { it.fullNameRequired || it.fullNameTooShort } == true
                )
                Spacer(Modifier.height(16.dp))
                PhoneField(phone, { phone = it })
                Spacer(Modifier.height(16.dp))
                EmailField(
                    email,
                    { email = it },
                    isError = fieldErrors?.let { it.emailRequired || it.emailInvalid } == true
                )
                Spacer(Modifier.height(16.dp))
                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    visible = passwordVisible,
                    onToggle = { passwordVisible = !passwordVisible }
                )
                Spacer(Modifier.height(24.dp))
                PrimaryOrangeButton(
                    text = if (isSubmitting) "Creating…" else "Create Account",
                    onClick = { onSubmit(fullName, phone, email, password) },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        SecondaryGreenButton(text = "Back to Sign In", onClick = onBack, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun RoleChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean
) {
    if (selected) {
        if (isPrimary) {
            PrimaryOrangeButton(text, onClick, modifier)
        } else {
            SecondaryGreenButton(text, onClick, modifier)
        }
    } else {
        androidx.compose.material3.OutlinedButton(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = ButtonShape,
            border = androidx.compose.foundation.BorderStroke(1.dp, FindAHomeColors.ImageBorder.copy(alpha = 0.4f))
        ) {
            Text(text, color = FindAHomeColors.TextSecondary)
        }
    }
}
