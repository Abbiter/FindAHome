package com.example.nestore_15.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.components.BrandLogo
import com.example.nestore_15.ui.components.EmailField
import com.example.nestore_15.ui.components.OutlinedBlueButton
import com.example.nestore_15.ui.components.PasswordField
import com.example.nestore_15.ui.components.PrimaryOrangeButton
import com.example.nestore_15.ui.components.PulsingLoader
import com.example.nestore_15.ui.theme.CardShape
import com.example.nestore_15.ui.theme.FindAHomeColors

@Composable
fun LoginScreen(
    isLoading: Boolean,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FindAHomeColors.PrimaryDarkBlue)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        BrandLogo(
            fillMaxWidthFraction = 0.65f,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            colors = CardDefaults.cardColors(containerColor = FindAHomeColors.CardSurface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Sign in",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = FindAHomeColors.PrimaryDarkBlue
                )
                Text(
                    "Welcome back to the campus hub.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FindAHomeColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 28.dp)
                )

                EmailField(email, { email = it })
                Spacer(Modifier.height(20.dp))
                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    visible = passwordVisible,
                    onToggle = { passwordVisible = !passwordVisible }
                )
                Spacer(Modifier.height(28.dp))

                if (isLoading) {
                    PulsingLoader()
                } else {
                    PrimaryOrangeButton(
                        text = "Sign In",
                        onClick = { onLogin(email, password) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedBlueButton(
                    text = "Create Account",
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
