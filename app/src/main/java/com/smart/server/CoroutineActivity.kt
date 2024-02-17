package com.smart.server

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import android.util.Log

class CoroutineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)

        coroutine()
    }

    fun coroutine() {
        CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {  //액티비티가 활성화되어 있는 동안 무한 반복
                val html = CoroutineScope(Dispatchers.Default).async {
                    longPollRequest()
                }.await()

                updateUI(html) // 서버 응답을 UI에 반영
//                delay(5000)
//                Log.d("오류확인", "안녕하세요")
            }
        }
    }

    // 네트워크 통신을 수행하는 함수
    suspend fun longPollRequest(): String {
        val client = OkHttpClient.Builder().build()
        val req = Request.Builder().url("https://www.google.com").build()
        return try {
            client.newCall(req).execute().use {
                        response -> return if (response.body != null) {
                        response.body!!.string()
                    }
                    else{
                        "body null"
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error occurred: ${e.message}"
        }
    }

    fun updateUI(html: String) {
        val mTextMain = findViewById<TextView>(R.id.mTextMain)
        mTextMain.text = html
    }
}