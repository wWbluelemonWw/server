package com.smart.server

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.Map


class MakeRoom : Activity() {
    lateinit var title_num: EditText
    lateinit var subtitle_num: EditText
    lateinit var person_num: EditText
    lateinit var region_num: EditText
    lateinit var roomCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_makeroom)

        title_num = findViewById(R.id.title_num)
        subtitle_num = findViewById(R.id.subtitle_num)
        person_num = findViewById(R.id.person_num)
        region_num = findViewById(R.id.region_num)

        val save: Button = findViewById(R.id.save)
        save.setOnClickListener {
            saveData()
        }

    }

    private fun saveData() {
        roomCode = generateRandomString(8)
        var title = title_num.text.toString()
        var subtitle = subtitle_num.text.toString()
        var person = person_num.text.toString()
        var region = region_num.text.toString()

        restful(title, subtitle, person, region, roomCode)

        val intent = Intent(this, com.smart.server.Map::class.java)
        val myCode : String = MakeRoom().generateRandomString(8)
        intent.putExtra("person", person)
        intent.putExtra("myCode", myCode)
        intent.putExtra("roomCode", roomCode)

        startActivity(intent)
    }

    private fun restful(title: String, subtitle: String, person: String, region: String, roomCode: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
        var retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000/")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
            .build()

        var MakeRoomService = retrofit.create(MakeRoomService::class.java)

        MakeRoomService.requestLogin(title,subtitle,person,region,roomCode).enqueue(object : Callback<Rooming> {        //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                override fun onResponse(call: Call<Rooming>, response: Response<Rooming>) {
                }

                override fun onFailure(call: Call<Rooming>, t: Throwable) {
                }
            })
    }

    fun generateRandomString(length: Int): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }
}