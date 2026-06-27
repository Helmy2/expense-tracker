package com.expense.tracker.shared.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar as M3NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail as M3NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.expense.tracker.shared.designsystem.AppTheme

data class NavigationDestination(
    val label: String,
    val icon: @Composable () -> Unit = { Text("○") },
    val selectedIcon: @Composable () -> Unit = icon
)

@Composable
fun NavigationBar(
    destinations: List<NavigationDestination>,
    selectedDestination: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    M3NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        destinations.forEachIndexed { index, dest ->
            NavigationBarItem(
                selected = index == selectedDestination,
                onClick = { onDestinationSelected(index) },
                icon = if (index == selectedDestination) dest.selectedIcon else dest.icon,
                label = { Text(text = dest.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun NavigationRail(
    destinations: List<NavigationDestination>,
    selectedDestination: Int,
    onDestinationSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable ColumnScope.() -> Unit)? = null
) {
    M3NavigationRail(
        modifier = modifier,
        header = header,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        destinations.forEachIndexed { index, dest ->
            NavigationRailItem(
                selected = index == selectedDestination,
                onClick = { onDestinationSelected(index) },
                icon = if (index == selectedDestination) dest.selectedIcon else dest.icon,
                label = { Text(text = dest.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun NavigationBarPreview() {
    AppTheme {
        NavigationBar(
            destinations = listOf(
                NavigationDestination("Home", icon = { Icon(Icons.Default.Home, contentDescription = "Home") }),
                NavigationDestination("Search", icon = { Icon(Icons.Default.Search, contentDescription = "Search") }),
                NavigationDestination("Profile", icon = { Icon(Icons.Default.Person, contentDescription = "Profile") })
            ),
            selectedDestination = 0,
            onDestinationSelected = {}
        )
    }
}
