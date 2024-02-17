package com.smart.server

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log


class Room : Activity() {
    private lateinit var re_room: RecyclerView
    private lateinit var profileList: ArrayList<Profiles>
    private lateinit var adapter: ProfileAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        re_room = findViewById(R.id.re_room)
        profileList = ArrayList()
        adapter = ProfileAdapter(profileList, this, mDelayHandler)

        for (i in 1..profileList.size) {
            profileList.add(Profiles(null, null, null, null, null))
        }
        re_room.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        re_room.setHasFixedSize(true)
        re_room.adapter = adapter

        val makeroom: Button = findViewById(R.id.makeroom)
        waitGuest()

        makeroom.setOnClickListener {
            // 다음 화면으로 이동하기 위한 인텐트 객체 생성
            mDelayHandler.removeCallbacksAndMessages(null)
            val intent = Intent(this, MakeRoom::class.java)
            startActivity(intent)
        }

    }

   val mDelayHandler: Handler by lazy {
        Handler()
    }

    private fun waitGuest(){
        mDelayHandler.postDelayed(::showGuest, 3000) // 3초 후에 showGuest 함수를 실행한다.
    }

    private fun showGuest() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"

        // 실제 반복하는 코드를 여기에 적는다
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
            .build()

        val RoomService = retrofit.create(RoomService::class.java)
        val no = "0"

        RoomService.requestLogin(no).enqueue(object : Callback<MRoom> {        //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
            override fun onResponse(call: Call<MRoom>, response: Response<MRoom>) {
                val room = response.body()     //title, subtitle, person, region, roomcode
                Log.d("통신 성공", "성공" + room!!.data[0])
                profileList.clear()

                val size = room.data.size
                    for (i in 0 until size) {
                        profileList.add(Profiles(room.data[i].title, room.data[i].subtitle, room.data[i].person, room.data[i].region, room.data[i].roomcode))
                        adapter.notifyDataSetChanged()
                    }

                }


            override fun onFailure(call: Call<MRoom>, t: Throwable) {
                Log.d("통신 실패", "실퍠")
            }
        })

        waitGuest() // 코드 실행뒤에 계속해서 반복하도록 작업한다.
    }
    override fun onDestroy() {
        super.onDestroy()
        mDelayHandler.removeCallbacksAndMessages(null) // Handler의 모든 콜백을 제거하여 반복을 중지합니다.
    }

}