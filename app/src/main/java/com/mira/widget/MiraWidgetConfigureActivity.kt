package com.mira.widget

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup

class MiraWidgetConfigureActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_configure)

        val prefs = getSharedPreferences(MiraWidget.PREFS, MODE_PRIVATE)

        // Mevcut font tercihini yükle
        val rgFont = findViewById<RadioGroup>(R.id.rg_font)
        when (prefs.getString("font", "medium")) {
            "small"   -> rgFont.check(R.id.rb_small)
            "large"   -> rgFont.check(R.id.rb_large)
            "xlarge"  -> rgFont.check(R.id.rb_xlarge)
            "xxlarge" -> rgFont.check(R.id.rb_xxlarge)
            else      -> rgFont.check(R.id.rb_medium)
        }

        // Mevcut renk tercihini yükle
        val rgColor = findViewById<RadioGroup>(R.id.rg_color)
        when (prefs.getString("color", "gold")) {
            "white"    -> rgColor.check(R.id.rb_white)
            "purple"   -> rgColor.check(R.id.rb_purple)
            "teal"     -> rgColor.check(R.id.rb_teal)
            "blue"     -> rgColor.check(R.id.rb_blue)
            "darkgray" -> rgColor.check(R.id.rb_darkgray)
            else       -> rgColor.check(R.id.rb_gold)
        }

        // Mevcut arka plan tercihini yükle
        val rgBg = findViewById<RadioGroup>(R.id.rg_background)
        if (prefs.getString("bg", "dark") == "transparent") {
            rgBg.check(R.id.rb_transparent)
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val font = when (rgFont.checkedRadioButtonId) {
                R.id.rb_small   -> "small"
                R.id.rb_large   -> "large"
                R.id.rb_xlarge  -> "xlarge"
                R.id.rb_xxlarge -> "xxlarge"
                else            -> "medium"
            }
            val bg = if (rgBg.checkedRadioButtonId == R.id.rb_transparent) "transparent" else "dark"

            val color = when (rgColor.checkedRadioButtonId) {
                R.id.rb_white    -> "white"
                R.id.rb_purple   -> "purple"
                R.id.rb_teal     -> "teal"
                R.id.rb_blue     -> "blue"
                R.id.rb_darkgray -> "darkgray"
                else             -> "gold"
            }

            prefs.edit()
                .putString("font", font)
                .putString("bg", bg)
                .putString("color", color)
                .apply()

            // Tüm widgetları güncelle
            MiraWidget.updateAllWidgets(this)

            finish()
        }
    }
}
