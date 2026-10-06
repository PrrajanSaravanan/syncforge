package com.syncforge.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SpaceDashboard
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"

    const val DASHBOARD = "dashboard"
    const val PROJECTS = "projects"
    const val CHAT = "chat"
    const val ANALYTICS = "analytics"
    const val PROFILE = "profile"

    const val PROJECT_DETAIL = "project/{projectId}"
    const val KANBAN = "kanban?projectId={projectId}"
    const val NOTIFICATIONS = "notifications"
    const val ACTIVITY = "activity"
    const val AI = "ai"
    const val TEAM = "team"
    const val SETTINGS = "settings"

    fun projectDetail(id: String) = "project/$id"
    fun kanban(projectId: String? = null) = "kanban?projectId=${projectId ?: ""}"
}

/** The five bottom-bar destinations. Kept at five to respect the nav limit. */
enum class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    Dashboard(Routes.DASHBOARD, "Home", Icons.Rounded.SpaceDashboard, Icons.Outlined.SpaceDashboard),
    Projects(Routes.PROJECTS, "Projects", Icons.Rounded.Folder, Icons.Outlined.FolderOpen),
    Chat(Routes.CHAT, "Chat", Icons.Rounded.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    Analytics(Routes.ANALYTICS, "Insights", Icons.Rounded.BarChart, Icons.Outlined.BarChart),
    Profile(Routes.PROFILE, "Profile", Icons.Rounded.Person, Icons.Outlined.Person);

    companion object {
        fun fromRoute(route: String?): BottomTab? = entries.firstOrNull { it.route == route }
    }
}
