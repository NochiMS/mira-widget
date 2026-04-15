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
        private const val PREFS         = "mira_prefs"

        // Mira'nın doğum tarihi: 19 Mart 2026
        private const val BIRTH_YEAR  = 2026
        private const val BIRTH_MONTH = 3
        private const val BIRTH_DAY   = 19

        private fun getStageEmoji(totalMonths: Int): String = when {
            totalMonths < 1   -> "🌸"  //  0– 1 ay  : Doğum
            totalMonths < 2   -> "🌱"  //  1– 2 ay  : Yenidoğan
            totalMonths < 4   -> "🌼"  //  2– 4 ay  : İlk gülüşler
            totalMonths < 6   -> "🦋"  //  4– 6 ay  : Motor dönüşümü
            totalMonths < 9   -> "🧸"  //  6– 9 ay  : Oturma ve oyun
            totalMonths < 12  -> "🧭"  //  9–12 ay  : Keşif dönemi
            totalMonths < 18  -> "👣"  // 12–18 ay  : İlk adımlar
            totalMonths < 24  -> "💬"  // 18–24 ay  : Dil patlaması
            totalMonths < 36  -> "🎨"  //  2– 3 yaş : Hayal gücü
            totalMonths < 48  -> "🔍"  //  3– 4 yaş : Neden çağı
            totalMonths < 60  -> "🌈"  //  4– 5 yaş : Okul öncesi
            totalMonths < 72  -> "📚"  //  5– 6 yaş : İlkokul
            totalMonths < 96  -> "🎯"  //  6– 8 yaş : Okul çağı
            else              -> "🌟"  //  8–10 yaş : Parlayan yıldız
        }

        private fun calculateAge(today: LocalDate): Triple<Int, Int, Int> {
            var years  = today.year       - BIRTH_YEAR
            var months = today.monthValue - BIRTH_MONTH
            var days   = today.dayOfMonth - BIRTH_DAY

            if (days < 0) {
                months--
                val lastDayOfPrevMonth = today.withDayOfMonth(1).minusDays(1)
                days += lastDayOfPrevMonth.dayOfMonth
            }
            if (months < 0) {
                years--
                months += 12
            }
            return Triple(years, months, days)
        }

        fun buildViews(context: Context, appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID): RemoteViews {
            val today = LocalDate.now()
            val (years, months, days) = calculateAge(today)
            val totalMonths = years * 12 + months
            val emoji   = getStageEmoji(totalMonths)
            val ageText = "${years}y  ${months}a  ${days}g"

            val views = RemoteViews(context.packageName, R.layout.widget_mira)
            views.setTextViewText(R.id.tv_emoji, emoji)
            views.setTextViewText(R.id.tv_age, ageText)

            // Arka plan tercihi
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val bg = prefs.getString("bg_$appWidgetId", "dark")
            if (bg == "transparent") {
                views.setInt(R.id.widget_root, "setBackgroundColor", Color.TRANSPARENT)
            }

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
            // setAndAllowWhileIdle: Doze modunu deler, özel izin gerekmez
            alarm.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                midnight.timeInMillis,
                pi
            )
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

    // Widget silinince o ID'nin tercihini temizle
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (id in appWidgetIds) editor.remove("bg_$id")
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
