package com.yourapp.vdsproxy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(c: Context, i: Intent) {
    val sp = c.getSharedPreferences("vds", 0)
    if (sp.getBoolean("autostart", true)) {
      ContextCompat.startForegroundService(c, Intent(c, ProxyService::class.java))
    }
  }
}
