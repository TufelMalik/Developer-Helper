package com.techquantum.developerhelper

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var buttons: List<Button>
    private lateinit var note: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val padding = (20 * resources.displayMetrics.density).toInt()
        val gap = (12 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(padding, padding, padding, padding)
        }
        buttons = DebugSwitch.entries.map { item ->
            Button(this).apply {
                isAllCaps = false
                setOnClickListener {
                    if (!DebugSettings.hasPermission(this@MainActivity)) {
                        openDeveloperSettings()
                    } else if (!DebugSettings.toggle(this@MainActivity, item)) {
                        Toast.makeText(this@MainActivity, "Android did not allow this change", Toast.LENGTH_SHORT).show()
                    }
                    refresh()
                    DeveloperWidget.refreshAll(this@MainActivity)
                }
                root.addView(this, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { bottomMargin = gap })
            }
        }
        note = TextView(this).apply { gravity = Gravity.CENTER }
        root.addView(note)
        setContentView(root)
        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::buttons.isInitialized) refresh()
    }

    private fun refresh() {
        DebugSwitch.entries.forEachIndexed { index, item ->
            val supported = DebugSettings.supported(item)
            buttons[index].isEnabled = supported
            val state = if (DebugSettings.hasPermission(this)) {
                if (DebugSettings.enabled(this, item)) "ON" else "OFF"
            } else "SETUP"
            buttons[index].text = "${item.title}  ·  $state"
        }
        note.text = if (DebugSettings.hasPermission(this)) {
            "Tap a switch to change it"
        } else {
            "One time ADB setup required:\nadb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
        }
    }

    private fun openDeveloperSettings() {
        try { startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) }
        catch (_: Exception) { Toast.makeText(this, "Developer settings unavailable", Toast.LENGTH_SHORT).show() }
    }
}
