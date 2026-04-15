package com.mira.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup

class MiraWidgetConfigureActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.widget_configure)

        val prefs = getSharedPreferences(MiraWidget.PREFS, MODE_PRIVATE)

        // Mevcut font tercihini yükle
        val rgFont = findViewById<RadioGroup>(R.id.rg_font)
        when (prefs.getString("font_$appWidgetId", "medium")) {
            "small" -> rgFont.check(R.id.rb_small)
            "large" -> rgFont.check(R.id.rb_large)
            else    -> rgFont.check(R.id.rb_medium)
        }

        // Mevcut arka plan tercihini yükle
        val rgBg = findViewById<RadioGroup>(R.id.rg_background)
        if (prefs.getString("bg_$appWidgetId", "dark") == "transparent") {
            rgBg.check(R.id.rb_transparent)
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val font = when (rgFont.checkedRadioButtonId) {
                R.id.rb_small -> "small"
                R.id.rb_large -> "large"
                else          -> "medium"
            }
            val bg = if (rgBg.checkedRadioButtonId == R.id.rb_transparent) "transparent" else "dark"

            prefs.edit()
                .putString("font_$appWidgetId", font)
                .putString("bg_$appWidgetId", bg)
                .apply()

            AppWidgetManager.getInstance(this)
                .updateAppWidget(appWidgetId, MiraWidget.buildViews(this, appWidgetId))

            finish()
        }
    }
}
