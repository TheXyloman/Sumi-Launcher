package com.sumilauncher.ui

import android.app.role.RoleManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.sumilauncher.platform.startActivitySafely

@Composable
fun rememberHomeRoleRequest(onRefresh: () -> Unit, onError: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    val result = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { onRefresh() }
    return {
        val roleManager = context.getSystemService(RoleManager::class.java)
        try {
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                result.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME))
            } else if (!context.startActivitySafely(Intent(Settings.ACTION_HOME_SETTINGS))) {
                onError("Home settings could not be opened on this device.")
            }
        } catch (e: android.content.ActivityNotFoundException) {
            onError("Home settings could not be opened on this device.")
        } catch (e: SecurityException) {
            onError("Home settings could not be opened on this device.")
        }
    }
}
