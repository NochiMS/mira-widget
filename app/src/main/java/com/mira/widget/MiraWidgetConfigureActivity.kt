package com.mira.widget

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView

class MiraWidgetConfigureActivity : Activity() {

    private var selectedCustomEmoji: String = ""
    private var selectedEmojiView: TextView? = null

    private val emojiList = listOf(
        "🌸","🌱","🌼","🦋","🧸","🧭","👣",
        "💬","🎨","🔍","🌈","📚","🎯","🌟",
        "👶","🍼","🎀","💕","❤️","🫶","🥰",
        "⭐","🌙","☀️","🦄","🐰","🐻","🐼",
        "🐸","🦊","🐝","🌺","🌻","🌷","🍓",
        "🎈","🎉","🎁","🎂","🍭","🧁","🍬",
        "💎","🔮","🌊","🦅","✨","💫","👑",
        "🦩","🎵","🎶","🎠","🎪","🏵️","🌍"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.widget_configure)

        val prefs = getSharedPreferences(MiraWidget.PREFS, MODE_PRIVATE)

        // Emoji modu
        val rgEmojiMode = findViewById<RadioGroup>(R.id.rg_emoji_mode)
        val isCustom = prefs.getString("emoji_mode", "auto") == "custom"
        if (isCustom) rgEmojiMode.check(R.id.rb_emoji_custom)
        selectedCustomEmoji = prefs.getString("emoji_custom", "") ?: ""

        // Emoji grid oluştur
        buildEmojiGrid(prefs)

        // Font boyutu
        val rgFont = findViewById<RadioGroup>(R.id.rg_font)
        when (prefs.getString("font", "medium")) {
            "small"   -> rgFont.check(R.id.rb_small)
            "large"   -> rgFont.check(R.id.rb_large)
            "xlarge"  -> rgFont.check(R.id.rb_xlarge)
            "xxlarge" -> rgFont.check(R.id.rb_xxlarge)
            else      -> rgFont.check(R.id.rb_medium)
        }

        // Mira rengi
        val rgNameColor = findViewById<RadioGroup>(R.id.rg_name_color)
        when (prefs.getString("color_name", "gold")) {
            "white"    -> rgNameColor.check(R.id.rb_name_white)
            "purple"   -> rgNameColor.check(R.id.rb_name_purple)
            "teal"     -> rgNameColor.check(R.id.rb_name_teal)
            "blue"     -> rgNameColor.check(R.id.rb_name_blue)
            "darkgray" -> rgNameColor.check(R.id.rb_name_darkgray)
            else       -> rgNameColor.check(R.id.rb_name_gold)
        }

        // Yaş rengi
        val rgAgeColor = findViewById<RadioGroup>(R.id.rg_age_color)
        when (prefs.getString("color_age", "white")) {
            "gold"     -> rgAgeColor.check(R.id.rb_age_gold)
            "purple"   -> rgAgeColor.check(R.id.rb_age_purple)
            "teal"     -> rgAgeColor.check(R.id.rb_age_teal)
            "blue"     -> rgAgeColor.check(R.id.rb_age_blue)
            "darkgray" -> rgAgeColor.check(R.id.rb_age_darkgray)
            else       -> rgAgeColor.check(R.id.rb_age_white)
        }

        // Hizalama
        val rgAlign = findViewById<RadioGroup>(R.id.rg_align)
        when (prefs.getString("align", "left")) {
            "right_cell"  -> rgAlign.check(R.id.rb_align_right_cell)
            "center_cell" -> rgAlign.check(R.id.rb_align_center)
            "double"      -> rgAlign.check(R.id.rb_align_double)
            else          -> rgAlign.check(R.id.rb_align_left)
        }

        // Arka plan
        val rgBg = findViewById<RadioGroup>(R.id.rg_background)
        when (prefs.getString("bg", "dark")) {
            "black"       -> rgBg.check(R.id.rb_bg_black)
            "navy"        -> rgBg.check(R.id.rb_bg_navy)
            "green"       -> rgBg.check(R.id.rb_bg_green)
            "purple"      -> rgBg.check(R.id.rb_bg_purple)
            "rose"        -> rgBg.check(R.id.rb_bg_rose)
            "transparent" -> rgBg.check(R.id.rb_transparent)
            else          -> rgBg.check(R.id.rb_dark)
        }

        // Kaydet
        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val emojiMode = if (
                rgEmojiMode.checkedRadioButtonId == R.id.rb_emoji_custom
                && selectedCustomEmoji.isNotEmpty()
            ) "custom" else "auto"

            val font = when (rgFont.checkedRadioButtonId) {
                R.id.rb_small   -> "small";  R.id.rb_large -> "large"
                R.id.rb_xlarge  -> "xlarge"; R.id.rb_xxlarge -> "xxlarge"
                else            -> "medium"
            }
            val nameColor = when (rgNameColor.checkedRadioButtonId) {
                R.id.rb_name_white -> "white"; R.id.rb_name_purple -> "purple"
                R.id.rb_name_teal  -> "teal";  R.id.rb_name_blue -> "blue"
                R.id.rb_name_darkgray -> "darkgray"; else -> "gold"
            }
            val ageColor = when (rgAgeColor.checkedRadioButtonId) {
                R.id.rb_age_gold   -> "gold";   R.id.rb_age_purple -> "purple"
                R.id.rb_age_teal   -> "teal";   R.id.rb_age_blue -> "blue"
                R.id.rb_age_darkgray -> "darkgray"; else -> "white"
            }
            val align = when (rgAlign.checkedRadioButtonId) {
                R.id.rb_align_right_cell -> "right_cell"
                R.id.rb_align_center     -> "center_cell"
                R.id.rb_align_double     -> "double"
                else                     -> "left"
            }
            val bg = when (rgBg.checkedRadioButtonId) {
                R.id.rb_bg_black  -> "black"
                R.id.rb_bg_navy   -> "navy"
                R.id.rb_bg_green  -> "green"
                R.id.rb_bg_purple -> "purple"
                R.id.rb_bg_rose   -> "rose"
                R.id.rb_transparent -> "transparent"
                else -> "dark"
            }

            prefs.edit()
                .putString("emoji_mode",   emojiMode)
                .putString("emoji_custom", selectedCustomEmoji)
                .putString("font",         font)
                .putString("color_name",   nameColor)
                .putString("color_age",    ageColor)
                .putString("align",        align)
                .putString("bg",           bg)
                .apply()

            MiraWidget.updateAllWidgets(this)
            finish()
        }
    }

    private fun buildEmojiGrid(prefs: android.content.SharedPreferences) {
        val container = findViewById<LinearLayout>(R.id.emoji_grid_container)
        val rgEmojiMode = findViewById<RadioGroup>(R.id.rg_emoji_mode)
        var currentRow: LinearLayout? = null
        val currentCustom = prefs.getString("emoji_custom", "") ?: ""
        val isCustom = prefs.getString("emoji_mode", "auto") == "custom"

        emojiList.forEachIndexed { index, emoji ->
            if (index % 7 == 0) {
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                container.addView(row)
                currentRow = row
            }

            val tv = TextView(this).apply {
                text = emoji
                textSize = 22f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(4, 10, 4, 10)

                if (isCustom && emoji == currentCustom) {
                    setBackgroundColor(0x44E8C87A.toInt())
                    selectedEmojiView = this
                }

                setOnClickListener {
                    selectedEmojiView?.setBackgroundColor(Color.TRANSPARENT)
                    setBackgroundColor(0x44E8C87A.toInt())
                    selectedEmojiView = this
                    selectedCustomEmoji = emoji
                    // Özel seçim radio'sunu otomatik işaretle
                    rgEmojiMode.check(R.id.rb_emoji_custom)
                }
            }
            currentRow?.addView(tv)
        }
    }
}
