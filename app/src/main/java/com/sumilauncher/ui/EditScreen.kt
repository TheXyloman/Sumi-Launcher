package com.sumilauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.components.ActionPill
import com.sumilauncher.ui.components.AppRow
import com.sumilauncher.ui.components.RowSpacer
import com.sumilauncher.ui.components.SectionLabel

@Composable
fun EditScreen(
    allApps: List<LauncherApp>,
    pinnedApps: List<LauncherApp>,
    modifier: Modifier = Modifier,
    onTogglePinned: (LauncherApp, Boolean) -> Unit,
    onMoveUp: (LauncherApp) -> Unit,
    onMoveDown: (LauncherApp) -> Unit,
    onDone: () -> Unit,
) {
    val pinnedKeys = pinnedApps.mapTo(mutableSetOf()) { it.componentKey }

    BackHandler(onBack = onDone)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 22.dp)
            .semantics { testTag = "edit_screen" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Edit apps",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                ActionPill(
                    label = "Done",
                    enabled = pinnedApps.isNotEmpty(),
                    onClick = onDone,
                    modifier = Modifier.semantics { testTag = "edit_done" },
                )
            }
        }
        item {
            SectionLabel(text = "Pinned")
        }
        if (pinnedApps.isEmpty()) {
            item {
                Text(
                    text = "Pick at least one app for the home list.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.semantics { testTag = "edit_empty_state" },
                )
            }
        } else {
            items(
                count = pinnedApps.size,
                key = { index -> "pinned:${pinnedApps[index].componentKey}" },
            ) { index ->
                val app = pinnedApps[index]
                AppRow(
                    app = app,
                    onClick = {},
                    onLongClick = null,
                    modifier = Modifier.semantics {
                        testTag = "edit_pinned_${app.componentKey}"
                    },
                ) {
                    ActionPill(
                        label = "Up",
                        enabled = index > 0,
                        onClick = { onMoveUp(app) },
                        modifier = Modifier.semantics {
                            testTag = "move_up_${app.componentKey}"
                        },
                    )
                    RowSpacer()
                    ActionPill(
                        label = "Down",
                        enabled = index < pinnedApps.lastIndex,
                        onClick = { onMoveDown(app) },
                        modifier = Modifier.semantics {
                            testTag = "move_down_${app.componentKey}"
                        },
                    )
                    RowSpacer()
                    ActionPill(
                        label = "Remove",
                        onClick = { onTogglePinned(app, false) },
                        modifier = Modifier.semantics {
                            testTag = "remove_${app.componentKey}"
                        },
                    )
                }
            }
        }
        item {
            SectionLabel(text = "All apps", modifier = Modifier.padding(top = 10.dp))
        }
        items(
            count = allApps.size,
            key = { index -> "all:${allApps[index].componentKey}" },
        ) { index ->
            val app = allApps[index]
            val isPinned = app.componentKey in pinnedKeys
            AppRow(
                app = app,
                onClick = { onTogglePinned(app, !isPinned) },
                modifier = Modifier.semantics {
                    testTag = "edit_all_${app.componentKey}"
                },
            ) {
                ActionPill(
                    label = if (isPinned) "Added" else "Add",
                    enabled = !isPinned,
                    onClick = { onTogglePinned(app, true) },
                    modifier = Modifier.semantics {
                        testTag = "toggle_${app.componentKey}"
                    },
                )
            }
        }
    }
}
