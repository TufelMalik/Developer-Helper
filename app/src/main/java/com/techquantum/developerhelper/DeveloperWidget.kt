package com.techquantum.developerhelper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast

class DeveloperWidget : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val item = DebugSwitch.entries.firstOrNull { it.name == intent.action } ?: return
        if (!DebugSettings.hasPermission(context)) {
            Toast.makeText(context, "Open Developer Helper for ADB setup", Toast.LENGTH_SHORT).show()
            context.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else if (!DebugSettings.toggle(context, item)) {
            Toast.makeText(context, "Android did not allow this change", Toast.LENGTH_SHORT).show()
        }
        refreshAll(context)
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.developer_widget)
            val viewIds = listOf(R.id.developer_options, R.id.wireless_debugging, R.id.usb_debugging)
            DebugSwitch.entries.forEachIndexed { index, item ->
                val state = if (!DebugSettings.hasPermission(context)) "SETUP"
                    else if (!DebugSettings.supported(item)) "N/A"
                    else if (DebugSettings.enabled(context, item)) "ON" else "OFF"
                views.setTextViewText(viewIds[index], "${item.title}  ·  $state")
                val intent = Intent(context, DeveloperWidget::class.java).setAction(item.name)
                val pending = PendingIntent.getBroadcast(context, index, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(viewIds[index], pending)
            }
            manager.updateAppWidget(id, views)
        }
    }

    companion object {
        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, DeveloperWidget::class.java))
            if (ids.isNotEmpty()) DeveloperWidget().onUpdate(context, manager, ids)
        }
    }
}
