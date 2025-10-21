package com.yourapp.vdsproxy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class ProxyService : Service() {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  @Volatile private var running = false

  override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
    startForegroundNotif()
    if (!running) { running = true; scope.launch { loop() } }
    return START_STICKY
  }

  override fun onDestroy() { running = false; scope.cancel(); SSHReverse.close(); Socks5.stop(); super.onDestroy() }
  override fun onBind(i: android.content.Intent?): IBinder? = null

  private fun startForegroundNotif() {
    val ch = "vdsproxy"
    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= 26) nm.createNotificationChannel(
      NotificationChannel(ch, "VDS Proxy", NotificationManager.IMPORTANCE_MIN)
    )
    val n = NotificationCompat.Builder(this, ch)
      .setSmallIcon(android.R.drawable.stat_sys_upload_done)
      .setContentTitle("VDS Proxy")
      .setContentText("Работает")
      .setOngoing(true)
      .build()
    startForeground(1, n)
  }

  private suspend fun loop() {
    val sp = getSharedPreferences("vds", 0)
    val cfg = AppConfig(
      host = sp.getString("host","")!!,
      port = sp.getInt("port",22),
      user = sp.getString("user","andgate")!!,
      privateKeyPem = sp.getString("key","")!!,
      localSocksPort = 1080,
      remotePortOnVds = sp.getInt("rport",11011)
    )
    while (running) {
      try {
        Socks5.ensure(1080)
        SSHReverse.ensure(this, cfg)
        delay(15_000)
      } catch (_: Throwable) {
        delay(5_000)
      }
    }
  }
}
