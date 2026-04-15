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

        // Font boyutu
        val rgFont = findViewById<RadioGroup>(R.id.rg_font)
        when (prefs.getString("font", "medium")) {
            "small"   -> rgFont.check(R.id.rb_small)
            "large"   -> rgFont.check(R.id.rb_large)
            "xlarge"  -> rgFont.check(R.id.rb_xlarge)
            "xxlarge" -> rgFont.check(R.id.rb_xxlarge)
            else      -> rgFont.check(R.id.rb_medium)
        }

        // Mira ismi rengi
        val rgNameColor = findViewById<RadioGroup>(R.id.rg_name_color)
        when (prefs.getString("color_name", "gold")) {
            "white"    -> rgNameColor.check(R.id.rb_name_white)
            "purple"   -> rgNameColor.check(R.id.rb_name_purple)
            "teal"     -> rgNameColor.check(R.id.rb_name_teal)
            "blue"     -> rgNameColor.check(R.id.rb_name_blue)
            "darkgray" -> rgNameColor.check(R.id.rb_name_darkgray)
            else       -> rgNameColor.check(R.id.rb_name_gold)
        }

        // Yaş metni rengi
        val rgAgeColor = findViewById<RadioGroup>(R.id.rg_age_color)
        when (prefs.getString("color_age", "white")) {
            "gold"     -> rgAgeColor.check(R.id.rb_age_gold)
            "purple"   -> rgAgeColor.check(R.id.rb_age_purple)
            "teal"     -> rgAgeColor.check(R.id.rb_age_teal)
            "blue"     -> rgAgeColor.check(R.id.rb_age_blue)
            "darkgray" -> rgAgeColor.check(R.id.rb_age_darkgray)
            else       -> rgAgeColor.check(R.id.rb_age_white)
        }

        // Emoji konumu
        val rgAlign = findViewById<RadioGroup>(R.id.rg_align)
        when (prefs.getString("align", "left")) {
            "center" -> rgAlign.check(R.id.rb_align_center)
            "right"  -> rgAlign.check(R.id.rb_align_right)
            else     -> rgAlign.check(R.id.rb_align_left)
        }

        // Arka plan
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
            val nameColor = when (rgNameColor.checkedRadioButtonId) {
                R.id.rb_name_white    -> "white"
                R.id.rb_name_purple   -> "purple"
                R.id.rb_name_teal     -> "teal"
                R.id.rb_name_blue     -> "blue"
                R.id.rb_name_darkgray -> "darkgray"
                else                  -> "gold"
            }
            val ageColor = when (rgAgeColor.checkedRadioButtonId) {
                R.id.rb_age_gold     -> "gold"
                R.id.rb_age_purple   -> "purple"
                R.id.rb_age_teal     -> "teal"
                R.id.rb_age_blue     -> "blue"
                R.id.rb_age_darkgray -> "darkgray"
                else                 -> "white"
            }
            val align = when (rgAlign.checkedRadioButtonId) {
                R.id.rb_align_center -> "center"
                R.id.rb_align_right  -> "right"
                else                 -> "left"
            }
            val bg = if (rgBg.checkedRadioButtonId == R.id.rb_transparent) "transparent" else "dark"

            prefs.edit()
                .putString("font",       font)
                .putString("color_name", nameColor)
                .putString("color_age",  ageColor)
                .putString("align",      align)
                .putString("bg",         bg)
                .apply()

            MiraWidget.updateAllWidgets(this)
            finish()
        }
    }
}
