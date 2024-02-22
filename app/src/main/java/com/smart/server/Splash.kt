package com.smart.server

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity


class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceStare: Bundle?) {
        super.onCreate(savedInstanceStare)
        setContentView(R.layout.splash_activity)
        val handler = Handler()
        handler.postDelayed(Runnable {
            val intent = Intent(applicationContext, Log::class.java)
            startActivity(intent)
            finish()
        }, 1500) // 1초 있다 메인액티비티로
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}