package com.sumilauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.components.AppRow

@Composable
fun DrawerScreen(
    apps: List<LauncherApp>,
    modifier: Modifier = Modifier,
    onLaunchApp: (LauncherApp) -> Unit,
    onClose: () -> Unit,
) {
    var horizontalDrag by remember { mutableFloatStateOf(0f) }

    BackHandler(onBack = onClose)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { horizontalDrag = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        horizontalDrag += dragAmount
                        if (horizontalDrag >= 120f) {
                            horizontalDrag = 0f
                            onClose()
                        }
                    },
                )
            }
            .padding(horizontal = 16.dp, vertical = 22.dp)
            .semantics { testTag = "drawer_screen" },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "All apps",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            items(
                count = apps.size,
                key = { index -> apps[index].componentKey },
            ) { index ->
                val app = apps[index]
                AppRow(
                    app = app,
                    onClick = { onLaunchApp(app) },
                    modifier = Modifier.semantics {
                        testTag = "drawer_app_${app.componentKey}"
                    },
                )
            }
        }
    }
}
