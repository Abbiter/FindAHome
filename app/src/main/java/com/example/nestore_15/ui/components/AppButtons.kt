package com.example.nestore_15.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.animation.pressScale
import com.example.nestore_15.ui.theme.ButtonShape
import com.example.nestore_15.ui.theme.FindAHomeColors

@Composable
fun PrimaryOrangeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scaleModifier = Modifier.pressScale(pressed)
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .then(scaleModifier),
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = FindAHomeColors.OrangeAccent,
            contentColor = FindAHomeColors.TextOnPrimary,
            disabledContainerColor = FindAHomeColors.OrangeAccent.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        interactionSource = interaction
    ) {
        Text(text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryGreenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scaleModifier = Modifier.pressScale(pressed)
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp).then(scaleModifier),
        enabled = enabled,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = FindAHomeColors.GreenAccent,
            contentColor = FindAHomeColors.TextOnPrimary
        ),
        interactionSource = interaction
    ) {
        Text(text)
    }
}

@Composable
fun OutlinedBlueButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        shape = ButtonShape,
        border = BorderStroke(1.5.dp, FindAHomeColors.PrimaryDarkBlue),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = FindAHomeColors.PrimaryDarkBlue
        )
    ) {
        Text(text)
    }
}

@Composable
fun TextLinkButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = FindAHomeColors.PrimaryDarkBlue)
    }
}
