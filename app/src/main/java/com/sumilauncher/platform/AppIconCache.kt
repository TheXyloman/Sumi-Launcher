package com.sumilauncher.platform

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import com.sumilauncher.model.LauncherApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppIconCache(context: Context) {
    private val packageManager = context.applicationContext.packageManager
    private val cache = object : LruCache<String, Bitmap>(4 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }
    private var generation = 0

    suspend fun load(app: LauncherApp): Bitmap? = withContext(Dispatchers.IO) {
        val version: Int
        synchronized(cache) {
            cache.get(app.componentKey)?.let { return@withContext it }
            version = generation
        }
        val bitmap = runCatching {
            packageManager.getActivityIcon(ComponentName(app.packageName, app.activityName)).toBitmap(96, 96)
        }.getOrNull()
        synchronized(cache) {
            if (bitmap != null && version == generation) cache.put(app.componentKey, bitmap)
        }
        bitmap
    }

    fun invalidate(packageName: String) = synchronized(cache) {
        generation++
        cache.snapshot().keys.filter { it.startsWith("$packageName/") }.forEach(cache::remove)
    }
}
