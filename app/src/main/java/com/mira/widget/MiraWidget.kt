package com.mira.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.TypedValue
import android.widget.RemoteViews
import java.time.LocalDate
import java.util.Calendar

class MiraWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_UPDATE = "com.mira.widget.UPDATE"
        const val PREFS = "mira_prefs"

        private const val BIRTH_YEAR  = 2026
        private const val BIRTH_MONTH = 3
        private const val BIRTH_DAY   = 19
        private const val ROLLOVER_HOUR   = 11
        private const val ROLLOVER_MINUTE = 30

        private fun getStageEmoji(totalMonths: Int): String = when {
            totalMonths < 1   -> "🌸"
            totalMonths < 2   -> "🌱"
            totalMonths < 4   -> "🌼"
            totalMonths < 6   -> "🦋"
            totalMonths < 9   -> "🧸"
            totalMonths < 12  -> "🧭"
            totalMonths < 18  -> "👣"
            totalMonths < 24  -> "💬"
            totalMonths < 36  -> "🎨"
            totalMonths < 48  -> "🔍"
            totalMonths < 60  -> "🌈"
            totalMonths < 72  -> "📚"
            totalMonths < 96  -> "🎯"
            else              -> "🌟"
        }

        private fun effectiveToday(): LocalDate {
            val now = Calendar.getInstance()
            val h = now.get(Calendar.HOUR_OF_DAY)
            val m = now.get(Calendar.MINUTE)
            val before = h < ROLLOVER_HOUR || (h == ROLLOVER_HOUR && m < ROLLOVER_MINUTE)
            val today = LocalDate.now()
            return if (before) today.minusDays(1) else today
        }

        private fun calculateAge(today: LocalDate): Triple<Int, Int, Int> {
            var years  = today.year       - BIRTH_YEAR
            var months = today.monthValue - BIRTH_MONTH
            var days   = today.dayOfMonth - BIRTH_DAY
            if (days < 0) {
                months--
                days += today.withDayOfMonth(1).minusDays(1).dayOfMonth
            }
            if (months < 0) { years--; months += 12 }
            return Triple(years, months, days)
        }

        private fun colorFor(prefs: android.content.SharedPreferences, key: String, default: String) =
            when (prefs.getString(key, default)) {
                "white"    -> 0xFFFFFFFF.toInt()
                "purple"   -> 0xFFC47FFF.toInt()
                "teal"     -> 0xFF00D4B8.toInt()
                "blue"     -> 0xFF5BA8FF.toInt()
                "darkgray" -> 0xFF7A7A95.toInt()
                "pink"     -> 0xFFFF90C0.toInt()
                "orange"   -> 0xFFFFA060.toInt()
                "mint"     -> 0xFF70FFD0.toInt()
                "yellow"   -> 0xFFFFEE55.toInt()
                "red"      -> 0xFFFF6B6B.toInt()
                "cyan"     -> 0xFF60E0FF.toInt()
                "lime"     -> 0xFFAAFF60.toInt()
                "peach"    -> 0xFFFFB07A.toInt()
                "lavender" -> 0xFFD4AAFF.toInt()
                "silver"   -> 0xFFCCCCCC.toInt()
                else       -> 0xFFE8C87A.toInt()  // gold
            }

        fun buildViews(context: Context): RemoteViews {
            val today = effectiveToday()
            val (years, months, days) = calculateAge(today)
            val totalMonths = years * 12 + months
            val autoEmoji = getStageEmoji(totalMonths)
            val ageText   = "${years}y  ${months}a  ${days}g"

            val prefs    = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val align    = prefs.getString("align", "left") ?: "left"
            val isDouble = align == "double"

            val emojiSp = prefs.getInt("font_emoji", 28).coerceIn(8, 80).toFloat()
            val nameSp  = prefs.getInt("font_name",  19).coerceIn(8, 60).toFloat()
            val ageSp   = prefs.getInt("font_age",   16).coerceIn(8, 50).toFloat()

            val emoji = if (prefs.getString("emoji_mode", "auto") == "custom")
                prefs.getString("emoji_custom", autoEmoji) ?: autoEmoji
            else autoEmoji

            val layoutId = if (isDouble) R.layout.widget_mira_double else R.layout.widget_mira

            val views = RemoteViews(context.packageName, layoutId)
            views.setTextViewText(R.id.tv_emoji, emoji)
            views.setTextViewText(R.id.tv_age, ageText)
            if (isDouble) views.setTextViewText(R.id.tv_emoji_right, emoji)

            views.setTextViewTextSize(R.id.tv_emoji, TypedValue.COMPLEX_UNIT_SP, emojiSp)
            views.setTextViewTextSize(R.id.tv_name,  TypedValue.COMPLEX_UNIT_SP, nameSp)
            views.setTextViewTextSize(R.id.tv_age,   TypedValue.COMPLEX_UNIT_SP, ageSp)
            if (isDouble) views.setTextViewTextSize(R.id.tv_emoji_right, TypedValue.COMPLEX_UNIT_SP, emojiSp)

            // Emoji hizalaması
            if (!isDouble) {
                val density = context.resources.displayMetrics.density
                val scale   = context.resources.displayMetrics.scaledDensity
                val cellDp  = (emojiSp * 1.8f).toInt()
                val cellPx  = (cellDp * density).toInt()
                val emojiPx = (emojiSp * scale).toInt()
                val leftPx  = when (align) {
                    "right_cell"  -> maxOf(0, cellPx - emojiPx - (4 * density).toInt())
                    "center_cell" -> maxOf(0, (cellPx - emojiPx) / 2)
                    else          -> 0
                }
                views.setViewPadding(R.id.tv_emoji, leftPx, 0, 0, 0)
            }

            // Renkler
            views.setTextColor(R.id.tv_name, colorFor(prefs, "color_name", "gold"))
            views.setTextColor(R.id.tv_age,  colorFor(prefs, "color_age",  "white"))

            // Arka plan — drawable ile yuvarlak köşe korunur
            when (prefs.getString("bg", "dark")) {
                "transparent" -> views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
                "black"    -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_black)
                "navy"     -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_navy)
                "green"    -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_green)
                "purple"   -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_purple)
                "rose"     -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_rose)
                "teal"     -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_teal)
                "orange"   -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_orange)
                "gray"     -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_gray)
                "crimson"  -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_crimson)
                "midnight" -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_midnight)
                "forest"   -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_forest)
                "amber"    -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_amber)
                "slate"    -> views.setInt(R.id.widget_root, "setBackgroundResource", R.drawable.widget_bg_slate)
            }

            return views
        }

        fun updateAllWidgets(context: Context) {
            try {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(ComponentName(context, MiraWidget::class.java))
                if (ids.isEmpty()) return
                val views = try { buildViews(context) }
                            catch (_: Throwable) { RemoteViews(context.packageName, R.layout.widget_mira) }
                manager.updateAppWidget(ids, views)
            } catch (_: Throwable) { }
        }

        fun scheduleMidnightUpdate(context: Context) {
            scheduleNextRollover(context)
            MiraUpdateWorker.schedule(context)
        }

        private fun scheduleNextRollover(context: Context) {
            val alarm  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MiraWidget::class.java).apply { action = ACTION_UPDATE }
            val pi     = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, ROLLOVER_HOUR)
                set(Calendar.MINUTE, ROLLOVER_MINUTE)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            try {
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pi)
                } else {
                    alarm.setExact(AlarmManager.RTC_WAKEUP, target.timeInMillis, pi)
                }
            } catch (_: SecurityException) {
                alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, target.timeInMillis, pi)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val views = try { buildViews(context) }
                    catch (_: Throwable) { RemoteViews(context.packageName, R.layout.widget_mira) }
        try { appWidgetManager.updateAppWidget(appWidgetIds, views) } catch (_: Throwable) { }
        try { scheduleMidnightUpdate(context) } catch (_: Throwable) { }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_UPDATE,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                updateAllWidgets(context)
                try { scheduleMidnightUpdate(context) } catch (_: Throwable) { }
                try { MiraUpdateWorker.schedule(context) } catch (_: Throwable) { }
            }
        }
    }

    override fun onEnabled(context: Context) {
        updateAllWidgets(context)
        try { scheduleMidnightUpdate(context) } catch (_: Throwable) { }
        try { MiraUpdateWorker.schedule(context) } catch (_: Throwable) { }
    }

    override fun onDisabled(context: Context) {
        val alarm  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MiraWidget::class.java).apply { action = ACTION_UPDATE }
        val pi     = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(pi)
    }
}
