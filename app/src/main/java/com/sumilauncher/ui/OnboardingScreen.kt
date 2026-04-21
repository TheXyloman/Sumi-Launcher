package com.sumilauncher.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumilauncher.ui.components.ActionPill

@Composable
fun OnboardingScreen(
    uiState: LauncherUiState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onContinue: () -> Unit,
) {
    val context = LocalContext.current
    val roleManager = remember { context.getSystemService(RoleManager::class.java) }
    val launcherRoleRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        onRefresh()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 28.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Sumi Launcher",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Set Sumi as the home app and enable notifications access.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            ChecklistItem(
                title = "Default launcher",
                isComplete = uiState.setupState.isDefaultLauncher,
                actionLabel = if (uiState.setupState.isDefaultLauncher) "Ready" else "Set launcher",
                onAction = {
                    if (!uiState.setupState.isDefaultLauncher) {
                        requestLauncherRole(context, roleManager) { intent ->
                            launcherRoleRequest.launch(intent)
                        }
                    }
                },
                tag = "check_default_launcher",
            )
            ChecklistItem(
                title = "Notifications swipe-down",
                isComplete = uiState.setupState.isAccessibilityEnabled,
                actionLabel = if (uiState.setupState.isAccessibilityEnabled) "Ready" else "Enable access",
                onAction = {
                    context.startActivity(
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                },
                tag = "check_accessibility",
            )
        }

        ActionPill(
            label = "Continue",
            enabled = uiState.setupState.isDefaultLauncher && uiState.setupState.isAccessibilityEnabled,
            onClick = onContinue,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .semantics { testTag = "continue_setup" },
        )
    }
}

@Composable
private fun ChecklistItem(
    title: String,
    isComplete: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    tag: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(14.dp)
            .semantics { testTag = tag },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = if (isComplete) "Enabled" else "Needs setup",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        ActionPill(
            label = actionLabel,
            enabled = !isComplete,
            onClick = onAction,
        )
    }
}

private fun requestLauncherRole(
    context: Context,
    roleManager: RoleManager?,
    launchIntent: (Intent) -> Unit,
) {
    if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
        launchIntent(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
    } else {
        context.startActivity(
            Intent(Settings.ACTION_HOME_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
