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

        fun buildViews(context: Context, appWidgetId: Int): RemoteViews {
            val today = LocalDate.now()
            val (years, months, days) = calculateAge(today)
            val totalMonths = years * 12 + months
            val emoji   = getStageEmoji(totalMonths)
            val ageText = "${years}y  ${months}a  ${days}g"

            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

            // Font boyutuna göre ayrı layout seç (setTextViewTextSize MIUI'de çalışmıyor)
            val layoutId = when (prefs.getString("font_$appWidgetId", "medium")) {
                "small"   -> R.layout.widget_mira_small
                "large"   -> R.layout.widget_mira_large
                "xlarge"  -> R.layout.widget_mira_xlarge
                "xxlarge" -> R.layout.widget_mira_xxlarge
                else      -> R.layout.widget_mira
            }

            val views = RemoteViews(context.packageName, layoutId)
            views.setTextViewText(R.id.tv_emoji, emoji)
            views.setTextViewText(R.id.tv_age, ageText)

            // Arka plan tercihi
            if (prefs.getString("bg_$appWidgetId", "dark") == "transparent") {
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
            }

            // Widgete tıklayınca ayar ekranı aç
            val settingsIntent = Intent(context, MiraWidgetConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val settingsPi = PendingIntent.getActivity(
                context, appWidgetId, settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, settingsPi)

            return views
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MiraWidget::class.java))
            if (ids.isEmpty()) return
            for (id in ids) {
                manager.updateAppWidget(id, buildViews(context, id))
            }
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

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildViews(context, id))
        }
        try { scheduleMidnightUpdate(context) } catch (_: Exception) { }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_UPDATE,
            Intent.ACTION_BOOT_COMPLETED -> {
                updateAllWidgets(context)
                try { scheduleMidnightUpdate(context) } catch (_: Exception) { }
            }
        }
    }

    override fun onEnabled(context: Context) {
        updateAllWidgets(context)
        try { scheduleMidnightUpdate(context) } catch (_: Exception) { }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) {
            editor.remove("bg_$id")
            editor.remove("font_$id")
        }
        editor.apply()
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
