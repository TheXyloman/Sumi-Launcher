package com.sumilauncher.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun Modifier.einkSwipes(
    onLeft: (() -> Unit)? = null,
    onRight: (() -> Unit)? = null,
    onUp: (() -> Unit)? = null,
    onDown: (() -> Unit)? = null,
): Modifier {
    val left = rememberUpdatedState(onLeft)
    val right = rememberUpdatedState(onRight)
    val up = rememberUpdatedState(onUp)
    val down = rememberUpdatedState(onDown)
    val threshold = with(LocalDensity.current) { 56.dp.toPx() }
    return pointerInput(threshold) {
        awaitEachGesture {
            val first = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val origin = first.position
            var horizontal: Boolean? = null
            var cancelled = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == first.id } ?: break
                if (event.changes.count { it.pressed } > 1) cancelled = true
                val delta = change.position - origin
                if (horizontal == null && (abs(delta.x) > viewConfiguration.touchSlop || abs(delta.y) > viewConfiguration.touchSlop)) {
                    val isHorizontal = abs(delta.x) > abs(delta.y)
                    val supported = if (isHorizontal) left.value != null || right.value != null else up.value != null || down.value != null
                    if (supported) horizontal = isHorizontal else cancelled = true
                }
                // Consume the release as well: a swipe must not become a row tap.
                if (horizontal != null) change.consume()
                if (!change.pressed) {
                    if (!cancelled) {
                        when (horizontal) {
                            true -> if (delta.x <= -threshold) left.value?.invoke() else if (delta.x >= threshold) right.value?.invoke()
                            false -> if (delta.y <= -threshold) up.value?.invoke() else if (delta.y >= threshold) down.value?.invoke()
                            null -> Unit
                        }
                    }
                    break
                }
            }
        }
    }
}
