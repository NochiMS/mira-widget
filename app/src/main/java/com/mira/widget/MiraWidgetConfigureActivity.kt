package com.mira.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup

class MiraWidgetConfigureActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.widget_configure)

        // Mevcut tercihi yükle
        val prefs = getSharedPreferences("mira_prefs", MODE_PRIVATE)
        val current = prefs.getString("bg_$appWidgetId", "dark")
        val rg = findViewById<RadioGroup>(R.id.rg_background)
        if (current == "transparent") {
            rg.check(R.id.rb_transparent)
        }

        findViewById<Button>(R.id.btn_add).setOnClickListener {
            val bg = if (rg.checkedRadioButtonId == R.id.rb_transparent) "transparent" else "dark"

            prefs.edit().putString("bg_$appWidgetId", bg).apply()

            val manager = AppWidgetManager.getInstance(this)
            manager.updateAppWidget(appWidgetId, MiraWidget.buildViews(this, appWidgetId))

            try { MiraWidget.scheduleMidnightUpdate(this) } catch (_: Exception) { }

            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, result)
            finish()
        }
    }
}
