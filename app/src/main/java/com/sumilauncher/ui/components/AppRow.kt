package com.sumilauncher.ui.components

import android.content.ComponentName
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.sumilauncher.model.LauncherApp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppRow(
    app: LauncherApp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    iconSize: Dp = 34.dp,
    minHeight: Dp = 62.dp,
    labelTextStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppIcon(
            app = app,
            size = iconSize,
        )
        Text(
            text = app.label,
            style = labelTextStyle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailingContent?.invoke(this)
    }
}

@Composable
fun ActionPill(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .widthIn(min = 56.dp)
            .border(
                width = 1.dp,
                color = if (enabled) MaterialTheme.colorScheme.outline
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = when {
                    label.length >= 12 -> 11.sp
                    label.length >= 8 -> 12.sp
                    else -> 13.sp
                },
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
            ),
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AppIcon(
    app: LauncherApp,
    size: Dp,
) {
    val context = LocalContext.current
    val squircleShape = RoundedCornerShape(percent = 28)
    val icon = remember(app.packageName, app.activityName) {
        runCatching {
            context.packageManager
                .getActivityIcon(ComponentName(app.packageName, app.activityName))
                .toBitmap(width = 96, height = 96)
                .asImageBitmap()
        }.getOrNull()
    }

    if (icon == null) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(squircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline),
        )
        return
    }

    Image(
        bitmap = icon,
        contentDescription = null,
        modifier = Modifier
            .size(size)
            .clip(squircleShape),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.secondary,
        modifier = modifier.padding(vertical = 4.dp),
    )
}

@Composable
fun RowSpacer() {
    Spacer(modifier = Modifier.size(8.dp))
}
