package com.sumilauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sumilauncher.SumiApplication
import com.sumilauncher.model.LauncherApp

@Composable
fun AppRow(
    app: LauncherApp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    showIcons: Boolean = false,
    iconRevision: Int = 0,
    labelTextStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val ink = MaterialTheme.colorScheme.onSurface
    val foreground = if (pressed) Color.White else ink
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp)
            .semantics { if (!app.available) stateDescription = "Unavailable" }
            .background(if (pressed) ink else Color.White)
            .combinedClickable(interactionSource = interaction, indication = null, role = Role.Button,
                onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showIcons) AppIcon(app, iconRevision)
        Text(app.label, style = labelTextStyle, color = foreground,
            maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        CompositionLocalProvider(LocalContentColor provides foreground) { trailingContent?.invoke(this) }
    }
}

@Composable
private fun AppIcon(app: LauncherApp, revision: Int) {
    val context = LocalContext.current
    val cache = (context.applicationContext as? SumiApplication)?.container?.iconCache
    val icon by produceState<android.graphics.Bitmap?>(null, app.componentKey, revision) {
        value = null
        value = cache?.load(app)
    }
    val grayscale = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
        if (icon != null) {
            Image(icon!!.asImageBitmap(), contentDescription = null, colorFilter = grayscale, modifier = Modifier.fillMaxSize())
        } else {
            Text(app.label.take(1).uppercase(), style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.border(1.dp, LocalContentColor.current).padding(horizontal = 7.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun ActionPill(
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val inverted = selected || pressed
    val ink = MaterialTheme.colorScheme.onSurface
    Box(modifier.heightIn(min = 48.dp).widthIn(min = 48.dp)
        .background(if (inverted) ink else Color.White)
        .border(if (enabled) 1.dp else 0.dp, if (enabled) ink else Color.Transparent)
        .clickable(enabled = enabled, role = Role.Button, interactionSource = interaction, indication = null, onClick = onClick)
        .padding(horizontal = 14.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.labelLarge,
            color = if (inverted) Color.White else if (enabled) ink else Color(0xFF666666))
    }
}

@Composable
fun TextAction(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(modifier.heightIn(min = 48.dp)
        .background(if (pressed) MaterialTheme.colorScheme.onSurface else Color.White)
        .clickable(role = Role.Button, interactionSource = interaction, indication = null, onClick = onClick)
        .padding(horizontal = 4.dp, vertical = 12.dp), contentAlignment = Alignment.CenterStart) {
        Text(label, style = MaterialTheme.typography.labelLarge,
            color = if (pressed) Color.White else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun IconActionButton(icon: ImageVector, contentDescription: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val ink = MaterialTheme.colorScheme.onSurface
    Box(modifier.size(48.dp).background(if (pressed) ink else Color.White)
        .clickable(enabled = enabled, role = Role.Button, interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription, tint = if (pressed) Color.White else if (enabled) ink else Color(0xFF888888), modifier = Modifier.size(22.dp))
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary, modifier = modifier.padding(vertical = 8.dp))
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        TextAction("← Back", onClick = onBack)
        trailing?.invoke()
    }
    Text(title, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 12.dp, bottom = 20.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}
