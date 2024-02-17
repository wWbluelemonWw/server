package com.smart.server

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {    //해당 액티비티가 처음 실행될 때 한번 수행하는 곳(초기화)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val room: Button = findViewById(R.id.room)
        val set: Button = findViewById(R.id.set)

        room.setOnClickListener {
            // 다음 화면으로 이동하기 위한 인텐트 객체 생성
            val intent = Intent(this, Room::class.java)
            startActivity(intent)

        }
        set.setOnClickListener {
            // 다음 화면으로 이동하기 위한 인텐트 객체 생성
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)

        }
    }
}