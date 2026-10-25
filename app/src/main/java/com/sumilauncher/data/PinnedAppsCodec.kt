package com.sumilauncher.data

import com.sumilauncher.model.PinnedApp
import java.util.Base64

object PinnedAppsCodec {
    private const val ENTRY_SEPARATOR = "\n"
    private const val FIELD_SEPARATOR = "\t"
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encode(apps: List<PinnedApp>): String {
        return apps
            .sortedBy { it.sortOrder }
            .joinToString(ENTRY_SEPARATOR) { app ->
                listOf(
                    encodeField(app.packageName),
                    encodeField(app.activityName),
                    encodeField(app.label),
                    app.sortOrder.toString(),
                ).joinToString(FIELD_SEPARATOR)
            }
    }

    fun decode(raw: String): List<PinnedApp> {
        if (raw.isBlank()) {
            return emptyList()
        }

        return raw
            .lineSequence()
            .mapNotNull { line ->
                val parts = line.split(FIELD_SEPARATOR)
                if (parts.size != 4) {
                    return@mapNotNull null
                }

                val sortOrder = parts[3].toIntOrNull() ?: return@mapNotNull null
                runCatching {
                    PinnedApp(
                        packageName = decodeField(parts[0]),
                        activityName = decodeField(parts[1]),
                        label = decodeField(parts[2]),
                        sortOrder = sortOrder,
                    )
                }.getOrNull()
            }
            .sortedBy { it.sortOrder }
            .toList()
    }

    private fun encodeField(value: String): String {
        return encoder.encodeToString(value.toByteArray(Charsets.UTF_8))
    }

    private fun decodeField(value: String): String {
        return decoder.decode(value).decodeToString()
    }
}
