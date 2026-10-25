package com.sumilauncher.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.components.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date

@Composable
fun HomeScreen(
    pinnedApps: List<LauncherApp>,
    modifier: Modifier = Modifier,
    onLaunchApp: (LauncherApp) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenNotifications: () -> Unit,
    onEdit: () -> Unit,
    onAppActions: ((LauncherApp) -> Unit)? = null,
    showIcons: Boolean = false,
    iconRevision: Int = 0,
    homeRequest: Int = 0,
) {
    BackHandler {}
    Column(modifier.fillMaxSize().padding(horizontal = 28.dp).padding(top = 28.dp, bottom = 8.dp).testTag("home_screen")) {
        DateTimeHeader(onEdit, onOpenDrawer, onOpenNotifications)
        SectionLabel("Your everyday", Modifier.padding(top = 16.dp))
        PagedAppList(pinnedApps, modifier = Modifier.weight(1f), showIcons = showIcons, iconRevision = iconRevision,
            labelStyle = MaterialTheme.typography.titleLarge, resetKey = homeRequest, tagPrefix = "pinned_app",
            emptyTitle = "A little space for you.", emptyDescription = "Keep the apps you reach for. Leave room for everything else.",
            emptyAction = { ActionPill("Add apps", modifier = Modifier.testTag("add_apps"), onClick = onEdit) },
            onClick = onLaunchApp, onLongClick = onAppActions ?: { onEdit() }, onSwipeLeft = onOpenDrawer)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextAction("All apps", modifier = Modifier.testTag("open_drawer"), onClick = onOpenDrawer)
            TextAction("Customize", modifier = Modifier.testTag("open_customize"), onClick = onEdit)
        }
    }
}

@Composable
private fun DateTimeHeader(onLongPress: () -> Unit, onOpenDrawer: () -> Unit, onOpenNotifications: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val now by produceState(System.currentTimeMillis(), lifecycle, context) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val changes = Channel<Unit>(Channel.CONFLATED)
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) { changes.trySend(Unit) }
            }
            ContextCompat.registerReceiver(context, receiver, IntentFilter().apply {
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_LOCALE_CHANGED)
            }, ContextCompat.RECEIVER_NOT_EXPORTED)
            try {
                while (isActive) {
                    value = System.currentTimeMillis()
                    withTimeoutOrNull(60_000L - (value % 60_000L)) { changes.receive() }
                }
            } finally {
                context.unregisterReceiver(receiver)
                changes.close()
            }
        }
    }
    val time = DateFormat.getTimeFormat(context).format(Date(now))
    val date = DateFormat.format("EEEE, d MMMM", now).toString()
    Column(Modifier.fillMaxWidth().testTag("date_time_header")
        .einkSwipes(onLeft = onOpenDrawer, onDown = onOpenNotifications)
        .combinedClickable(interactionSource = remember { MutableInteractionSource() }, indication = null,
            onClick = {}, onLongClick = onLongPress)) {
        Text(date.uppercase(), style = MaterialTheme.typography.labelMedium)
        Text(time, style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(top = 7.dp, bottom = 18.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}
