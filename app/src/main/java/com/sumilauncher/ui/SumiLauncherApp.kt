package com.sumilauncher.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sumilauncher.SumiApplication
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.platform.*
import com.sumilauncher.ui.components.*
import com.sumilauncher.ui.theme.SumiTheme

private object Routes {
    const val Bootstrap = "bootstrap"
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Drawer = "drawer"
    const val Edit = "edit"
    const val Settings = "settings"
    const val Hidden = "hidden"
}

@Composable
fun SumiLauncherApp(homeRequest: Int = 0) {
    val context = LocalContext.current
    val application = context.applicationContext as SumiApplication
    val viewModel: LauncherViewModel = viewModel(factory = LauncherViewModel.Factory(application, application.container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var selectedApp by remember { mutableStateOf<LauncherApp?>(null) }
    var renaming by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var notificationSetup by remember { mutableStateOf(false) }
    fun open(route: String) { nav.navigate(route) { launchSingleTop = true } }
    fun back() { if (!nav.popBackStack()) open(Routes.Home) }
    fun launch(app: LauncherApp) {
        if (!context.launchApp(app)) {
            message = "${app.label} is unavailable right now. Its saved settings will be kept."
            viewModel.retryApps()
        }
    }
    fun actions(app: LauncherApp) { renaming = false; selectedApp = app }
    DisposableEffect(lifecycle, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshSetupState() }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(viewModel) { viewModel.errors.collect { message = it } }
    LaunchedEffect(state.loadError) { state.loadError?.let { message = it } }
    LaunchedEffect(homeRequest, state.isLoaded) {
        if (homeRequest > 0 && state.isLoaded && state.onboardingComplete &&
            nav.currentDestination?.route !in listOf(Routes.Bootstrap, Routes.Onboarding)) {
            selectedApp = null; message = null; notificationSetup = false
            nav.navigate(Routes.Home) {
                popUpTo(nav.graph.id) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
    SumiTheme {
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars).imePadding()) {
                NavHost(nav, Routes.Bootstrap,
                    enterTransition = { EnterTransition.None }, exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None }, popExitTransition = { ExitTransition.None }) {
                    composable(Routes.Bootstrap) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sumi", style = MaterialTheme.typography.headlineLarge)
                        }
                        LaunchedEffect(state.isLoaded) {
                            if (state.isLoaded) nav.navigate(if (state.onboardingComplete) Routes.Home else Routes.Onboarding) {
                                popUpTo(Routes.Bootstrap) { inclusive = true }
                            }
                        }
                    }
                    composable(Routes.Onboarding) {
                        OnboardingScreen(state, onRefresh = viewModel::refreshSetupState, onContinue = viewModel::completeOnboarding,
                            onError = { message = it })
                        LaunchedEffect(state.onboardingComplete) {
                            if (state.onboardingComplete) nav.navigate(Routes.Home) { popUpTo(Routes.Onboarding) { inclusive = true } }
                        }
                    }
                    composable(Routes.Home) {
                        HomeScreen(state.pinnedApps, onLaunchApp = ::launch, onOpenDrawer = { open(Routes.Drawer) },
                            onOpenNotifications = { if (!NotificationShadeController.openNotifications()) notificationSetup = true },
                            onEdit = { open(Routes.Edit) }, onAppActions = ::actions,
                            showIcons = state.showIcons, iconRevision = state.iconRevision, homeRequest = homeRequest)
                    }
                    composable(Routes.Drawer) {
                        DrawerScreen(state.allApps, onLaunchApp = ::launch, onClose = ::back, onAppActions = ::actions,
                            showIcons = state.showIcons, iconRevision = state.iconRevision)
                    }
                    composable(Routes.Edit) {
                        EditScreen(state.allApps, state.pinnedApps, onTogglePinned = viewModel::setPinned,
                            onMoveUp = viewModel::movePinnedUp, onMoveDown = viewModel::movePinnedDown,
                            onDone = ::back, onSettings = { open(Routes.Settings) }, onAppActions = ::actions,
                            showIcons = state.showIcons, iconRevision = state.iconRevision)
                    }
                    composable(Routes.Settings) {
                        SettingsScreen(state, ::back, viewModel::setShowIcons, { open(Routes.Hidden) },
                            viewModel::refreshSetupState, viewModel::retryApps, { message = it })
                    }
                    composable(Routes.Hidden) {
                        HiddenAppsScreen(state.hiddenApps, state.showIcons, state.iconRevision, { viewModel.setHidden(it, false) }, ::back)
                    }
                }
            }
            selectedApp?.let { app ->
                if (renaming) RenameAppDialog(app, state.customNames[app.componentKey], onDismiss = { selectedApp = null }, onSave = {
                    viewModel.renameApp(app, it); selectedApp = null
                }) else AppActionsDialog(app, app.componentKey in state.pinnedKeys, onDismiss = { selectedApp = null },
                    onPin = { viewModel.setPinned(app, app.componentKey !in state.pinnedKeys); selectedApp = null },
                    onRename = { renaming = true }, onHide = { viewModel.setHidden(app, true); selectedApp = null },
                    onInfo = {
                        if (!context.openAppInfo(app)) message = "App information could not be opened."
                        selectedApp = null
                    })
            }
            if (notificationSetup) SumiDialog("Notifications, if you like", onDismiss = { notificationSetup = false }) {
                Text("Allow Sumi’s accessibility service to open the notification shade. This is optional and you can keep using your device’s usual notification gesture.", style = MaterialTheme.typography.bodyMedium)
                ActionPill("Set up shortcut", Modifier.fillMaxWidth(), onClick = {
                    notificationSetup = false
                    if (!context.startActivitySafely(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))) message = "Accessibility settings could not be opened."
                })
                TextAction("Not now", Modifier.fillMaxWidth(), onClick = { notificationSetup = false })
            }
            message?.let { text -> SumiDialog("A small interruption", onDismiss = { message = null }) {
                Text(text, style = MaterialTheme.typography.bodyLarge)
                ActionPill("Close", Modifier.fillMaxWidth(), onClick = { message = null })
            } }
        }
    }
}
