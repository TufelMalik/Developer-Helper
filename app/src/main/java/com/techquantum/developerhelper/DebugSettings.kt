package com.techquantum.developerhelper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

internal enum class DebugSwitch(val title: String, val key: String) {
    DEVELOPER("Developer options", Settings.Global.DEVELOPMENT_SETTINGS_ENABLED),
    WIRELESS("Wireless debugging", "adb_wifi_enabled"),
    USB("USB debugging", Settings.Global.ADB_ENABLED)
}

internal object DebugSettings {
    fun hasPermission(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    fun supported(item: DebugSwitch): Boolean =
        item != DebugSwitch.WIRELESS || Build.VERSION.SDK_INT >= 30

    fun enabled(context: Context, item: DebugSwitch): Boolean =
        Settings.Global.getInt(context.contentResolver, item.key, 0) == 1

    fun toggle(context: Context, item: DebugSwitch): Boolean {
        if (!hasPermission(context) || !supported(item)) return false
        val next = if (enabled(context, item)) 0 else 1
        return try {
            Settings.Global.putInt(context.contentResolver, item.key, next) &&
                Settings.Global.getInt(context.contentResolver, item.key, -1) == next
        } catch (_: SecurityException) {
            false
        }
    }
}
