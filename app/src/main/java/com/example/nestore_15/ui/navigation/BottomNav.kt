package com.example.nestore_15.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.nestore_15.ui.theme.BottomNavShape
import com.example.nestore_15.ui.theme.FindAHomeColors

enum class StudentTab(val label: String) {
    HOME("Explore"),
    FAVORITES("Saved"),
    MESSAGES("Inbox"),
    PROFILE("Profile")
}

private data class NavItem(
    val tab: StudentTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val useGreenWhenSelected: Boolean = false
)

private val studentNavItems = listOf(
    NavItem(StudentTab.HOME, Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(StudentTab.FAVORITES, Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, useGreenWhenSelected = true),
    NavItem(StudentTab.MESSAGES, Icons.Filled.Chat, Icons.Outlined.Chat),
    NavItem(StudentTab.PROFILE, Icons.Filled.Person, Icons.Outlined.Person, useGreenWhenSelected = true)
)

@Composable
fun FloatingBottomNavBar(
    selectedTab: StudentTab,
    onTabSelected: (StudentTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .shadow(12.dp, BottomNavShape),
        shape = BottomNavShape,
        color = FindAHomeColors.PrimaryDarkBlue
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            studentNavItems.forEach { item ->
                NavBarItem(
                    item = item,
                    selected = item.tab == selectedTab,
                    onClick = { onTabSelected(item.tab) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint by animateColorAsState(
        targetValue = when {
            selected -> if (item.useGreenWhenSelected) FindAHomeColors.GreenAccent
            else FindAHomeColors.OrangeAccent
            else -> FindAHomeColors.NavUnselected
        },
        animationSpec = tween(250),
        label = "navTint"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = tween(250),
        label = "navScale"
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .clip(CircleShape)
                .background(if (selected) tint.copy(alpha = 0.18f) else Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.tab.label,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            if (selected) {
                Text(
                    item.tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
