package com.sumilauncher.ui

import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sumilauncher.SumiApplication
import com.sumilauncher.platform.NotificationShadeController
import com.sumilauncher.platform.launchApp
import com.sumilauncher.ui.theme.SumiTheme

private object Routes {
    const val Bootstrap = "bootstrap"
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Drawer = "drawer"
    const val Edit = "edit"
}

@Composable
fun SumiLauncherApp() {
    val context = LocalContext.current
    val application = context.applicationContext as SumiApplication
    val viewModel: LauncherViewModel = viewModel(
        factory = LauncherViewModel.Factory(
            application = application,
            container = application.container,
        ),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSetupState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val pinnedLaunchableApps = uiState.pinnedApps.mapNotNull { pinnedApp ->
        uiState.allApps.firstOrNull { it.componentKey == pinnedApp.componentKey }
    }

    SumiTheme {
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            NavHost(
                navController = navController,
                startDestination = Routes.Bootstrap,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                composable(Routes.Bootstrap) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    )
                    LaunchedEffect(uiState.isLoaded, uiState.onboardingComplete, pinnedLaunchableApps.size) {
                        if (!uiState.isLoaded) {
                            return@LaunchedEffect
                        }
                        val destination = when {
                            !uiState.onboardingComplete -> Routes.Onboarding
                            pinnedLaunchableApps.isEmpty() -> Routes.Edit
                            else -> Routes.Home
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.Bootstrap) { inclusive = true }
                        }
                    }
                }

                composable(Routes.Onboarding) {
                    OnboardingScreen(
                        uiState = uiState,
                        onRefresh = viewModel::refreshSetupState,
                        onContinue = {
                            viewModel.setOnboardingComplete(true)
                            navController.navigate(
                                if (pinnedLaunchableApps.isEmpty()) Routes.Edit else Routes.Home,
                            ) {
                                popUpTo(Routes.Onboarding) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable(Routes.Home) {
                    LaunchedEffect(uiState.onboardingComplete, pinnedLaunchableApps.size) {
                        when {
                            !uiState.onboardingComplete -> navController.navigate(Routes.Onboarding) {
                                launchSingleTop = true
                            }
                            pinnedLaunchableApps.isEmpty() -> navController.navigate(Routes.Edit) {
                                launchSingleTop = true
                            }
                        }
                    }
                    HomeScreen(
                        pinnedApps = pinnedLaunchableApps,
                        onLaunchApp = { app -> context.launchApp(app) },
                        onOpenDrawer = {
                            navController.navigate(Routes.Drawer) {
                                launchSingleTop = true
                            }
                        },
                        onOpenNotifications = {
                            val opened = NotificationShadeController.openNotifications()
                            if (!opened) {
                                viewModel.setOnboardingComplete(false)
                                navController.navigate(Routes.Onboarding) {
                                    launchSingleTop = true
                                }
                            }
                        },
                        onEdit = {
                            navController.navigate(Routes.Edit) {
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable(Routes.Drawer) {
                    DrawerScreen(
                        apps = uiState.allApps,
                        onLaunchApp = { app -> context.launchApp(app) },
                        onClose = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Routes.Home) {
                                    launchSingleTop = true
                                }
                            }
                        },
                    )
                }

                composable(Routes.Edit) {
                    EditScreen(
                        allApps = uiState.allApps,
                        pinnedApps = pinnedLaunchableApps,
                        onTogglePinned = { app, enabled -> viewModel.setPinned(app, enabled) },
                        onMoveUp = { app -> viewModel.movePinnedUp(app.componentKey) },
                        onMoveDown = { app -> viewModel.movePinnedDown(app.componentKey) },
                        onDone = {
                            if (pinnedLaunchableApps.isNotEmpty()) {
                                if (!navController.popBackStack()) {
                                    navController.navigate(Routes.Home) {
                                        popUpTo(Routes.Edit) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}
