package com.example.nestore_15.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.theme.FindAHomeColors
import com.example.nestore_15.ui.theme.InputShape

@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = FindAHomeColors.TextSecondary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (hint.isNotEmpty()) Text(hint, color = FindAHomeColors.TextSecondary.copy(alpha = 0.6f)) },
            shape = InputShape,
            singleLine = true,
            isError = isError,
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password"
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { onImeAction() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FindAHomeColors.CardSurface,
                unfocusedContainerColor = FindAHomeColors.CardSurface,
                focusedBorderColor = FindAHomeColors.OrangeAccent,
                unfocusedBorderColor = FindAHomeColors.ImageBorder.copy(alpha = 0.4f)
            )
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = FindAHomeColors.ErrorRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun EmailField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier, isError: Boolean = false) {
    LabeledTextField(
        label = "Email address",
        value = value,
        onValueChange = onValueChange,
        hint = "student@ub.ac.bw",
        modifier = modifier,
        keyboardType = KeyboardType.Email,
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        isError = isError
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    LabeledTextField(
        label = "Password",
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        isPassword = true,
        passwordVisible = visible,
        onTogglePassword = onToggle,
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        imeAction = ImeAction.Done
    )
}

@Composable
fun NameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    LabeledTextField(
        label = "Full name",
        value = value,
        onValueChange = onValueChange,
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        modifier = modifier,
        isError = isError
    )
}

@Composable
fun PhoneField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    LabeledTextField(
        label = "Phone",
        value = value,
        onValueChange = onValueChange,
        keyboardType = KeyboardType.Phone,
        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
        modifier = modifier
    )
}
