package com.example.nestore_15.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.nestore_15.R
import com.example.nestore_15.ui.theme.FindAHomeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindAHomeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = false,
    showMenu: Boolean = false,
    showNotifications: Boolean = false,
    onBack: () -> Unit = {},
    onMenu: () -> Unit = {},
    onNotifications: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    Column(modifier = modifier) {
        TopAppBar(
            modifier = Modifier.shadow(4.dp),
            title = {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(end = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                    Text(
                        text = title,
                        color = FindAHomeColors.TextOnPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            navigationIcon = {
                when {
                    showBack -> IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FindAHomeColors.TextOnPrimary
                        )
                    }
                    showMenu -> IconButton(onClick = onMenu) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = FindAHomeColors.TextOnPrimary)
                    }
                }
            },
            actions = {
                if (showNotifications) {
                    IconButton(onClick = onNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = FindAHomeColors.TextOnPrimary)
                    }
                }
                actions()
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = FindAHomeColors.PrimaryDarkBlue,
                titleContentColor = FindAHomeColors.TextOnPrimary,
                navigationIconContentColor = FindAHomeColors.TextOnPrimary,
                actionIconContentColor = FindAHomeColors.TextOnPrimary
            )
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(FindAHomeColors.OrangeAccent)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindAHomeCenterTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        CenterAlignedTopAppBar(
            title = {
                Text(title, color = FindAHomeColors.TextOnPrimary, fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = FindAHomeColors.TextOnPrimary)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = FindAHomeColors.PrimaryDarkBlue
            )
        )
        Box(Modifier.fillMaxWidth().height(2.dp).background(FindAHomeColors.OrangeAccent))
    }
}

@Composable
fun VerificationDot(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(10.dp)
            .shadow(2.dp, androidx.compose.foundation.shape.CircleShape)
            .background(color, androidx.compose.foundation.shape.CircleShape)
    )
}

@Composable
fun HeaderActionRow(
    onNotifications: () -> Unit,
    onProfile: () -> Unit,
    verificationColor: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNotifications) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = FindAHomeColors.TextOnPrimary)
        }
        Box {
            IconButton(onClick = onProfile) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = FindAHomeColors.TextOnPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
            VerificationDot(
                color = verificationColor,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}
