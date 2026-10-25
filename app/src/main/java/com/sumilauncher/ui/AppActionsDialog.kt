package com.sumilauncher.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.sumilauncher.model.LauncherApp
import com.sumilauncher.ui.components.*

@Composable
fun AppActionsDialog(app: LauncherApp, pinned: Boolean, onDismiss: () -> Unit, onPin: () -> Unit,
    onRename: () -> Unit, onHide: () -> Unit, onInfo: () -> Unit) {
    SumiDialog(app.label, onDismiss) {
        if (app.originalLabel != app.label) Text(app.originalLabel, style = MaterialTheme.typography.bodyMedium)
        if (!app.available) Text("This app is currently unavailable. Its place on your home is saved.", style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        TextAction(if (pinned) "Remove from home" else "Add to home", Modifier.fillMaxWidth().testTag("action_pin"), onPin)
        TextAction("Rename", Modifier.fillMaxWidth().testTag("action_rename"), onRename)
        TextAction("Hide app", Modifier.fillMaxWidth().testTag("action_hide"), onHide)
        TextAction("App information ↗", Modifier.fillMaxWidth(), onInfo)
        ActionPill("Close", Modifier.fillMaxWidth(), onClick = onDismiss)
    }
}

@Composable
fun RenameAppDialog(app: LauncherApp, customName: String?, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by rememberSaveable(app.componentKey) { mutableStateOf(customName ?: app.originalLabel) }
    val focus = remember { FocusRequester() }
    SumiDialog("A name of your own", onDismiss) {
        Text("Leave blank to use “${app.originalLabel}”.", style = MaterialTheme.typography.bodyMedium)
        BasicTextField(name, { name = it }, singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onSave(name) }),
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).border(1.dp, MaterialTheme.colorScheme.outline).padding(14.dp)
                .focusRequester(focus).testTag("rename_input").semantics { contentDescription = "App name" })
        ActionPill("Save name", Modifier.fillMaxWidth().testTag("save_name"), onClick = { onSave(name) })
        TextAction("Cancel", Modifier.fillMaxWidth(), onDismiss)
    }
    LaunchedEffect(Unit) { focus.requestFocus() }
}
