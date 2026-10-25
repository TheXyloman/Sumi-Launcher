package com.sumilauncher.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    Row(modifier.fillMaxWidth().heightIn(min = 56.dp).border(1.dp, MaterialTheme.colorScheme.outline).padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(value, onValueChange, singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            modifier = Modifier.weight(1f).padding(vertical = 12.dp).testTag("search_apps").semantics { contentDescription = "Search apps" },
            decorationBox = { input -> Box { if (value.isEmpty()) Text("Search apps", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary); input() } })
        if (value.isNotEmpty()) IconActionButton(Icons.Default.Close, "Clear search", onClick = { onValueChange("") }) else Spacer(Modifier.width(14.dp))
    }
}
