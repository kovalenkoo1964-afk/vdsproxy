package com.yourapp.vdsproxy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val etHost = findViewById<EditText>(R.id.etHost)
    val etPort = findViewById<EditText>(R.id.etPort)
    val etUser = findViewById<EditText>(R.id.etUser)
    val etRemote = findViewById<EditText>(R.id.etRemote)
    val etPem = findViewById<EditText>(R.id.etPem)
    val cbAuto = findViewById<CheckBox>(R.id.cbAutostart)
    val tv = findViewById<TextView>(R.id.tvStatus)

    val sp = getSharedPreferences("vds", 0)
    etHost.setText(sp.getString("host",""))
    etPort.setText(sp.getInt("port",22).toString())
    etUser.setText(sp.getString("user","andgate"))
    etRemote.setText(sp.getInt("rport",11011).toString())
    etPem.setText(sp.getString("key",""))
    cbAuto.isChecked = sp.getBoolean("autostart", true)

    findViewById<Button>(R.id.btnSave).setOnClickListener {
      sp.edit()
        .putString("host", etHost.text.toString().trim())
        .putInt("port", etPort.text.toString().toIntOrNull() ?: 22)
        .putString("user", etUser.text.toString().trim())
        .putInt("rport", etRemote.text.toString().toIntOrNull() ?: 11011)
        .putString("key", etPem.text.toString())
        .putBoolean("autostart", cbAuto.isChecked)
        .apply()
      Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
    }

    findViewById<Button>(R.id.btnStart).setOnClickListener {
      ContextCompat.startForegroundService(this, Intent(this, ProxyService::class.java))
      tv.text = "Status: starting…"
    }
    findViewById<Button>(R.id.btnStop).setOnClickListener {
      stopService(Intent(this, ProxyService::class.java))
      tv.text = "Status: stopped"
    }
  }
}
