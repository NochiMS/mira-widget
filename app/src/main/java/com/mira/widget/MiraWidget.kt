package com.mira.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.util.Calendar

class MiraWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_UPDATE = "com.mira.widget.UPDATE"

        // Mira'nın doğum tarihi: 19 Mart 2026
        private const val BIRTH_YEAR  = 2026
        private const val BIRTH_MONTH = 3   // Mart
        private const val BIRTH_DAY   = 19

        /**
         * Toplam aya göre gelişim emojisi — 10 yıllık harita
         */
        private fun getStageEmoji(totalMonths: Int): String = when {
            totalMonths < 1   -> "🌸"  //  0– 1 ay  : Doğum, nazik yeni hayat
            totalMonths < 2   -> "🌱"  //  1– 2 ay  : Yenidoğan, dünyayı keşfediyor
            totalMonths < 4   -> "🌼"  //  2– 4 ay  : İlk gülüşler, sosyal gelişim
            totalMonths < 6   -> "🦋"  //  4– 6 ay  : Kelebek, motor dönüşümü
            totalMonths < 9   -> "🧸"  //  6– 9 ay  : Peluş, oturma ve oyun keşfi
            totalMonths < 12  -> "🧭"  //  9–12 ay  : Pusula, emekleme ve keşif dönemi
            totalMonths < 18  -> "👣"  // 12–18 ay  : Ayak izleri, ilk adımlar
            totalMonths < 24  -> "💬"  // 18–24 ay  : Konuşma balonu, dil patlaması
            totalMonths < 36  -> "🎨"  //  2– 3 yaş : Palet, hayal gücü ve sembolik oyun
            totalMonths < 48  -> "🔍"  //  3– 4 yaş : Büyüteç, "neden?" çağı
            totalMonths < 60  -> "🌈"  //  4– 5 yaş : Gökkuşağı, okul öncesi renkli öğrenme
            totalMonths < 72  -> "📚"  //  5– 6 yaş : Kitaplar, ilkokul başlangıcı
            totalMonths < 96  -> "🎯"  //  6– 8 yaş : Hedef, okul çağı becerileri
            else              -> "🌟"  //  8–10 yaş : Parlayan yıldız, kişilik oluşuyor
        }

        /**
         * Doğum tarihinden itibaren geçen kesin yıl, ay, gün hesabı.
         * Örnek: 15 Nis 2026 → 0y 0a 27g
         *        19 Nis 2026 → 0y 1a 0g
         *        5 May 2026  → 0y 1a 16g
         */
        private fun calculateAge(today: LocalDate): Triple<Int, Int, Int> {
            var years  = today.year       - BIRTH_YEAR
            var months = today.monthValue - BIRTH_MONTH
            var days   = today.dayOfMonth - BIRTH_DAY

            if (days < 0) {
                months--
                // Önceki ayın kaç günlük olduğunu hesapla (şubat/artık yıl dahil)
                val lastDayOfPrevMonth = today.withDayOfMonth(1).minusDays(1)
                days += lastDayOfPrevMonth.dayOfMonth
            }
            if (months < 0) {
                years--
                months += 12
            }
            return Triple(years, months, days)
        }

        fun buildViews(context: Context): RemoteViews {
            val today = LocalDate.now()
            val (years, months, days) = calculateAge(today)
            val totalMonths = years * 12 + months
            val emoji   = getStageEmoji(totalMonths)
            val ageText = "${years}y  ${months}a  ${days}g"
            val views = RemoteViews(context.packageName, R.layout.widget_mira)
            views.setTextViewText(R.id.tv_emoji, emoji)
            views.setTextViewText(R.id.tv_age, ageText)
            return views
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, MiraWidget::class.java))
            if (ids.isEmpty()) return
            manager.updateAppWidget(ids, buildViews(context))
        }

        /**
         * Bir sonraki gece yarısı için alarm kur.
         * setWindow() — 30 dakika esneklik, ekstra izin gerektirmez, pil dostu.
         */
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
            alarm.setWindow(
                AlarmManager.RTC_WAKEUP,
                midnight.timeInMillis,
                30 * 60 * 1000L,  // 30 dakikalık pencere
                pi
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Doğrudan gelen ID'leri kullan — getAppWidgetIds() race condition yok
        appWidgetManager.updateAppWidget(appWidgetIds, buildViews(context))
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

    // Widget ilk eklendiğinde
    override fun onEnabled(context: Context) {
        updateAllWidgets(context)
        try { scheduleMidnightUpdate(context) } catch (_: Exception) { }
    }

    // Son widget kaldırıldığında alarmı iptal et
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
