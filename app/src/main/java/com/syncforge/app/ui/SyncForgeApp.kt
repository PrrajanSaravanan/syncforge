package com.syncforge.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.syncforge.app.data.repository.SyncForgeRepository
import com.syncforge.app.ui.components.ForgeSnackbar
import com.syncforge.app.ui.navigation.BottomTab
import com.syncforge.app.ui.navigation.ForgeBottomBar
import com.syncforge.app.ui.navigation.Routes
import com.syncforge.app.ui.navigation.authEnter
import com.syncforge.app.ui.navigation.authExit
import com.syncforge.app.ui.navigation.modalEnter
import com.syncforge.app.ui.navigation.modalExit
import com.syncforge.app.ui.navigation.popEnter
import com.syncforge.app.ui.navigation.popExit
import com.syncforge.app.ui.navigation.pushEnter
import com.syncforge.app.ui.navigation.pushExit
import com.syncforge.app.ui.navigation.tabEnter
import com.syncforge.app.ui.navigation.tabExit
import com.syncforge.app.ui.screens.activity.ActivityScreen
import com.syncforge.app.ui.screens.ai.AiAssistantScreen
import com.syncforge.app.ui.screens.analytics.AnalyticsScreen
import com.syncforge.app.ui.screens.auth.LoginScreen
import com.syncforge.app.ui.screens.auth.SignupScreen
import com.syncforge.app.ui.screens.chat.ChatScreen
import com.syncforge.app.ui.screens.dashboard.DashboardScreen
import com.syncforge.app.ui.screens.kanban.KanbanScreen
import com.syncforge.app.ui.screens.notifications.NotificationsScreen
import com.syncforge.app.ui.screens.profile.ProfileScreen
import com.syncforge.app.ui.screens.projects.ProjectDetailScreen
import com.syncforge.app.ui.screens.projects.ProjectsScreen
import com.syncforge.app.ui.screens.settings.SettingsScreen
import com.syncforge.app.ui.screens.splash.SplashScreen
import com.syncforge.app.ui.screens.team.TeamScreen
import com.syncforge.app.ui.theme.Space
import com.syncforge.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun SyncForgeApp(app: AppViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val currentTab = BottomTab.fromRoute(currentRoute)
    val toast by app.toast.collectAsStateWithLifecycle()

    val repo = SyncForgeRepository.get()
    val channels by repo.channels.collectAsStateWithLifecycle()
    val chatUnread = channels.sumOf { it.unread }

    // Snackbars self-dismiss; there is no action to wait on.
    LaunchedEffect(toast?.id) {
        if (toast != null) {
            delay(2_800)
            app.consumeToast()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        ForgeNavHost(
            navController = navController,
            app = app,
            modifier = Modifier.fillMaxSize(),
        )

        // Bottom bar only exists for the five top-level tabs.
        AnimatedVisibility(
            visible = currentTab != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ForgeBottomBar(
                current = currentTab,
                chatBadge = chatUnread,
                onSelect = { tab ->
                    if (tab.route != currentRoute) {
                        navController.navigate(tab.route) {
                            popUpTo(Routes.DASHBOARD) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        }

        ForgeSnackbar(
            toast = toast,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = Space.md)
                .padding(bottom = if (currentTab != null) 96.dp else 28.dp),
        )
    }
}

@Composable
private fun ForgeNavHost(
    navController: NavHostController,
    app: AppViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier,
        enterTransition = { pushEnter() },
        exitTransition = { pushExit() },
        popEnterTransition = { popEnter() },
        popExitTransition = { popExit() },
    ) {
        // ── Onboarding ───────────────────────────────────────────────────────
        composable(
            Routes.SPLASH,
            exitTransition = { authExit() },
        ) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(
            Routes.LOGIN,
            enterTransition = { authEnter() },
            exitTransition = { authExit() },
        ) {
            LoginScreen(
                onSignIn = {
                    app.signIn()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGuest = {
                    app.continueAsGuest()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onSignUp = { navController.navigate(Routes.SIGNUP) },
                onMessage = app::showToast,
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onCreated = {
                    app.signIn()
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onMessage = app::showToast,
            )
        }

        // ── Bottom-tab destinations ──────────────────────────────────────────
        composable(
            Routes.DASHBOARD,
            enterTransition = { tabEnter() },
            exitTransition = { tabExit() },
        ) {
            DashboardScreen(
                onOpenProject = { navController.navigate(Routes.projectDetail(it)) },
                onOpenKanban = { navController.navigate(Routes.kanban(it)) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onOpenActivity = { navController.navigate(Routes.ACTIVITY) },
                onOpenAi = { navController.navigate(Routes.AI) },
                onOpenTeam = { navController.navigate(Routes.TEAM) },
                onOpenProjects = {
                    navController.navigate(Routes.PROJECTS) {
                        popUpTo(Routes.DASHBOARD) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onMessage = app::showToast,
            )
        }

        composable(
            Routes.PROJECTS,
            enterTransition = { tabEnter() },
            exitTransition = { tabExit() },
        ) {
            ProjectsScreen(
                onOpenProject = { navController.navigate(Routes.projectDetail(it)) },
                onOpenKanban = { navController.navigate(Routes.kanban(null)) },
                onMessage = app::showToast,
            )
        }

        composable(
            Routes.CHAT,
            enterTransition = { tabEnter() },
            exitTransition = { tabExit() },
        ) {
            ChatScreen(onMessage = app::showToast)
        }

        composable(
            Routes.ANALYTICS,
            enterTransition = { tabEnter() },
            exitTransition = { tabExit() },
        ) {
            AnalyticsScreen(
                onOpenActivity = { navController.navigate(Routes.ACTIVITY) },
                onMessage = app::showToast,
            )
        }

        composable(
            Routes.PROFILE,
            enterTransition = { tabEnter() },
            exitTransition = { tabExit() },
        ) {
            ProfileScreen(
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenActivity = { navController.navigate(Routes.ACTIVITY) },
                onOpenTeam = { navController.navigate(Routes.TEAM) },
                onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onSignOut = {
                    app.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onMessage = app::showToast,
            )
        }

        // ── Detail / stack destinations ──────────────────────────────────────
        composable(
            route = Routes.PROJECT_DETAIL,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType }),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId").orEmpty()
            ProjectDetailScreen(
                projectId = projectId,
                onBack = { navController.popBackStack() },
                onOpenKanban = { navController.navigate(Routes.kanban(projectId)) },
                onMessage = app::showToast,
            )
        }

        composable(
            route = Routes.KANBAN,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) { entry ->
            val projectId = entry.arguments?.getString("projectId")?.takeIf { it.isNotBlank() }
            KanbanScreen(
                initialProjectId = projectId,
                onBack = { navController.popBackStack() },
                onMessage = app::showToast,
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = { navController.popBackStack() },
                onOpenProject = { navController.navigate(Routes.projectDetail(it)) },
                onMessage = app::showToast,
            )
        }

        composable(Routes.ACTIVITY) {
            ActivityScreen(
                onBack = { navController.popBackStack() },
                onOpenProject = { navController.navigate(Routes.projectDetail(it)) },
            )
        }

        composable(
            Routes.AI,
            enterTransition = { modalEnter() },
            exitTransition = { modalExit() },
            popExitTransition = { modalExit() },
        ) {
            AiAssistantScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.TEAM) {
            TeamScreen(
                onBack = { navController.popBackStack() },
                onMessage = app::showToast,
            )
        }

        composable(
            Routes.SETTINGS,
            enterTransition = { modalEnter() },
            exitTransition = { modalExit() },
        ) {
            SettingsScreen(
                app = app,
                onBack = { navController.popBackStack() },
                onSignOut = {
                    app.signOut()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }
    }
}
