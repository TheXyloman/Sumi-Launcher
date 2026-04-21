package com.sumilauncher.ui

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.components.AppRow
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

@Composable
fun HomeScreen(
    pinnedApps: List<LauncherApp>,
    modifier: Modifier = Modifier,
    onLaunchApp: (LauncherApp) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenNotifications: () -> Unit,
    onEdit: () -> Unit,
) {
    BackHandler(enabled = true) {}

    val listState = rememberLazyListState()
    var pullDistance by remember { mutableFloatStateOf(0f) }
    var horizontalDrag by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember(listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput) {
                    return Offset.Zero
                }

                val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                if (atTop && available.y > 0f) {
                    pullDistance += available.y
                    if (pullDistance >= 120f) {
                        pullDistance = 0f
                        onOpenNotifications()
                    }
                } else if (available.y < 0f) {
                    pullDistance = 0f
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: androidx.compose.ui.unit.Velocity,
                available: androidx.compose.ui.unit.Velocity,
            ): androidx.compose.ui.unit.Velocity {
                pullDistance = 0f
                return androidx.compose.ui.unit.Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { horizontalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        horizontalDrag += dragAmount
                        if (horizontalDrag <= -120f) {
                            horizontalDrag = 0f
                            onOpenDrawer()
                        }
                    },
                )
            }
            .nestedScroll(nestedScrollConnection)
            .semantics { testTag = "home_screen" },
    ) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 22.dp),
        ) {
            item {
                DateTimeHeader(
                    onLongPress = onEdit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            items(
                count = pinnedApps.size,
                key = { index -> pinnedApps[index].componentKey },
            ) { index ->
                val app = pinnedApps[index]
                AppRow(
                    app = app,
                    onClick = { onLaunchApp(app) },
                    onLongClick = onEdit,
                    iconSize = 41.dp,
                    minHeight = 68.dp,
                    labelTextStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 19.sp,
                        letterSpacing = 0.sp,
                    ),
                    modifier = Modifier.semantics {
                        testTag = "pinned_app_${app.componentKey}"
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DateTimeHeader(
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val timeFormatter = remember(context) { DateFormat.getTimeFormat(context) }
    val dateFormatter = remember(context) { DateFormat.getMediumDateFormat(context) }
    var now by remember { mutableStateOf(ZonedDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            val current = ZonedDateTime.now()
            now = current
            val millisUntilNextMinute = 60_000L - ((current.second * 1_000L) + (current.nano / 1_000_000L))
            delay(millisUntilNextMinute.coerceAtLeast(1_000L))
        }
    }

    Column(
        modifier = modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .combinedClickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = onLongPress,
            )
            .semantics { testTag = "date_time_header" },
    ) {
        Text(
            text = dateFormatter.format(java.util.Date.from(now.toInstant())),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = timeFormatter.format(java.util.Date.from(now.toInstant())),
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Long press to edit",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
