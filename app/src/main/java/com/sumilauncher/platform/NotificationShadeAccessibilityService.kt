package com.sumilauncher.platform

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import java.lang.ref.WeakReference

class NotificationShadeAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        NotificationShadeController.attach(this)
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        NotificationShadeController.detach(this)
        super.onDestroy()
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        NotificationShadeController.detach(this)
        return super.onUnbind(intent)
    }
}

object NotificationShadeController {
    private var serviceReference = WeakReference<NotificationShadeAccessibilityService>(null)

    fun attach(service: NotificationShadeAccessibilityService) {
        serviceReference = WeakReference(service)
    }

    fun detach(service: NotificationShadeAccessibilityService) {
        if (serviceReference.get() == service) {
            serviceReference.clear()
        }
    }

    fun openNotifications(): Boolean {
        return serviceReference.get()
            ?.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            ?: false
    }

    fun isEnabled(context: Context): Boolean {
        val componentName = ComponentName(context, NotificationShadeAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()

        return enabledServices
            .split(':')
            .any { it.equals(componentName.flattenToString(), ignoreCase = true) }
    }
}
