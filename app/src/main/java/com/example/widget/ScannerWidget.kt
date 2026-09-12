package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.MainActivity

class ScannerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScannerWidget()
}

class ScannerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.primaryContainer)
                        .padding(16.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan Medical Report",
                        style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer)
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "Tap to open scanner",
                        style = TextStyle(color = GlanceTheme.colors.onPrimaryContainer)
                    )
                }
            }
        }
    }
}
