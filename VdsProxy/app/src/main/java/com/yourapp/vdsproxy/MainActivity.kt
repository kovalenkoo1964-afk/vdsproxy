package com.yourapp.vdsproxy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import android.widget.TextView
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = "VDS Proxy • Demo"
            textSize = 20f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        setContentView(tv)

        tv.post {
            Snackbar.make(tv, "App started", Snackbar.LENGTH_SHORT).show()
        }
    }
}
