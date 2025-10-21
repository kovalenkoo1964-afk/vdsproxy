package com.yourapp.vdsproxy

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "VDS Proxy"
            textSize = 20f
            setPadding(32, 32, 32, 32)
        })
    }
}
