package com.aimp.compose.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.aimp.compose.MainActivity
import com.aimp.compose.R

class MusicWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val intent = Intent(context, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_music)
            views.setOnClickPendingIntent(R.id.widgetPlay, pending)
            views.setOnClickPendingIntent(R.id.widgetPrev, pending)
            views.setOnClickPendingIntent(R.id.widgetNext, pending)
            manager.updateAppWidget(id, views)
        }
    }
}
