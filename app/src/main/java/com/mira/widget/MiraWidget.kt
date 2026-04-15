package com.mira.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
                else       -> 0xFFE8C87A.toInt()  // gold
            }

        fun buildViews(context: Context): RemoteViews {
            val today = LocalDate.now()
            val (years, months, days) = calculateAge(today)
            val totalMonths = years * 12 + months
            val autoEmoji = getStageEmoji(totalMonths)
            val ageText   = "${years}y  ${months}a  ${days}g"

            val prefs  = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val align  = prefs.getString("align", "left") ?: "left"
            val isDouble = align == "double"

            // Emoji içeriği — otomatik veya özel seçim
            val emoji = if (prefs.getString("emoji_mode", "auto") == "custom")
                prefs.getString("emoji_custom", autoEmoji) ?: autoEmoji
            else autoEmoji

            // Font + hizalamaya göre layout
            val layoutId = when (prefs.getString("font", "medium")) {
                "small"   -> if (isDouble) R.layout.widget_mira_small_double   else R.layout.widget_mira_small
                "large"   -> if (isDouble) R.layout.widget_mira_large_double   else R.layout.widget_mira_large
                "xlarge"  -> if (isDouble) R.layout.widget_mira_xlarge_double  else R.layout.widget_mira_xlarge
                "xxlarge" -> if (isDouble) R.layout.widget_mira_xxlarge_double else R.layout.widget_mira_xxlarge
                else      -> if (isDouble) R.layout.widget_mira_double         else R.layout.widget_mira
            }

            val views = RemoteViews(context.packageName, layoutId)
            views.setTextViewText(R.id.tv_emoji, emoji)
            views.setTextViewText(R.id.tv_age, ageText)
            if (isDouble) views.setTextViewText(R.id.tv_emoji_right, emoji)

            // Emoji hizalaması (çift modda gerekmez)
            if (!isDouble) {
                val gravity = when (align) {
                    "right_cell"  -> 21   // Gravity.RIGHT | CENTER_VERTICAL
                    "center_cell" -> 17   // Gravity.CENTER
                    else          -> 19   // Gravity.LEFT | CENTER_VERTICAL
                }
                views.setInt(R.id.tv_emoji, "setGravity", gravity)
            }

            // Renkler
            views.setTextColor(R.id.tv_name, colorFor(prefs, "color_name", "gold"))
            views.setTextColor(R.id.tv_age,  colorFor(prefs, "color_age",  "white"))

            // Arka plan
            if (prefs.getString("bg", "dark") == "transparent") {
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
            }

            return views
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MiraWidget::class.java))
            if (ids.isEmpty()) return
            manager.updateAppWidget(ids, buildViews(context))
        }

        fun scheduleMidnightUpdate(context: Context) {
            val alarm  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MiraWidget::class.java).apply { action = ACTION_UPDATE }
            val pi     = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val midnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnight.timeInMillis, pi)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetManager.updateAppWidget(appWidgetIds, buildViews(context))
        try { scheduleMidnightUpdate(context) } catch (_: Exception) { }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_UPDATE, Intent.ACTION_BOOT_COMPLETED -> {
                updateAllWidgets(context)
                try { scheduleMidnightUpdate(context) } catch (_: Exception) { }
            }
        }
    }

    override fun onEnabled(context: Context) {
        updateAllWidgets(context)
        try { scheduleMidnightUpdate(context) } catch (_: Exception) { }
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
