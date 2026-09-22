package com.tirhal.ai.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector
import com.tirhal.ai.R

sealed class Screen(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    object ClientsTravelers : Screen("clients_travelers", R.string.nav_clients_travelers, Icons.Default.People)
    object TripsBookings : Screen("trips_bookings", R.string.nav_trips_bookings, Icons.Default.ConfirmationNumber)
    object TravelerTracking : Screen("traveler_tracking", R.string.nav_traveler_tracking, Icons.Default.TrackChanges)
    object DigitalMarketing : Screen("digital_marketing", R.string.nav_digital_marketing, Icons.Default.Campaign)
    object OperationsCenter : Screen("operations_center", R.string.nav_operations_center, Icons.Default.Analytics)
    object AICenter : Screen("ai_center", R.string.nav_ai_center, Icons.Default.Psychology)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)

    companion object {
        val navItems = listOf(
            Home,
            ClientsTravelers,
            TripsBookings,
            TravelerTracking,
            DigitalMarketing,
            OperationsCenter,
            AICenter,
            Settings
        )
    }
}
