package com.smart.server

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.Map


class MakeRoom : Activity() {
    lateinit var title_num: EditText
    lateinit var region_num: Spinner
    lateinit var city_num: Spinner
    lateinit var person_num: EditText
    lateinit var roomCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_makeroom)

        title_num = findViewById(R.id.title_num)
        person_num = findViewById(R.id.person_num)
        region_num = findViewById(R.id.region_num)
        city_num = findViewById(R.id.city_num)
        val items_region = resources.getStringArray(R.array.region_array)
        val items_Seoul = resources.getStringArray(R.array.Seoul_array)
        val items_Busan = resources.getStringArray(R.array.Busan_array)

        val Adapter_region = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items_region)
        val Adapter_Seoul = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items_Seoul)
        val Adapter_Busan = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items_Busan)

        region_num.adapter = Adapter_region

        region_num.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
                when (position) {
                    0 -> {
                        city_num.adapter = Adapter_Seoul
                        city_num.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                                when (position){
                                    0 -> {
                                        person_num.setText("1")
                                    }
                                    1 -> {
                                        person_num.setText("2")
                                    }
                                    2 -> {
                                        person_num.setText("3")
                                    }
                                    3 -> {
                                        person_num.setText("4")
                                    }
                                    4 -> {
                                        person_num.setText("5")
                                    }
                                    5 -> {
                                        person_num.setText("6")
                                    }
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {

                            }
                        }
                    }

                    1 -> {
                        city_num.adapter = Adapter_Busan
                        city_num.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {

                            }
                        }
                    }
                    //...
                    else -> {

                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        val save: Button = findViewById(R.id.save)
        save.setOnClickListener {
            saveData()
        }

    }

    private fun saveData() {
        roomCode = generateRandomString(8)
        val title = title_num.text.toString()
        val region = region_num.selectedItem.toString()
        val city = city_num.selectedItem.toString()
        val person = person_num.text.toString()

        restful(title, region, city, person, roomCode)

        val intent = Intent(this, com.smart.server.Map::class.java)
        val myCode : String = MakeRoom().generateRandomString(8)
        intent.putExtra("person", person)
        intent.putExtra("myCode", myCode)
        intent.putExtra("roomCode", roomCode)

        startActivity(intent)
    }

    private fun restful(title: String, region: String, city: String, person: String, roomCode: String) {
        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
        var retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000/")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
            .build()

        var MakeRoomService = retrofit.create(MakeRoomService::class.java)

        MakeRoomService.requestLogin(title,region,city,person,roomCode).enqueue(object : Callback<Rooming> {        //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
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