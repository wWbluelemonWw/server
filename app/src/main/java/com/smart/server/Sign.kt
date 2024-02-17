package com.smart.server

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Sign : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"

        val signup: Button = findViewById(R.id.signup)
        val ID: EditText = findViewById(R.id.ID)
        val Password: EditText = findViewById(R.id.Password)

        var retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000/")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
            .build()

        var loginService = retrofit.create(LoginService::class.java)        //retrofit 객체를 만든 다음 create를 통해 서비스를 올려주면 loginService가 앞에서 정의한 INPUT OUTPUT을 가지고 서버를 호출할 수 있는 서비스 인터페이스가 된다.

        signup.setOnClickListener{
            val LS = '2'
            var textId = ID.text.toString()
            var textPw = Password.text.toString()

            loginService.requestLogin(LS, textId, textPw).enqueue(object : Callback<Login>{     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                override fun onResponse(call: Call<Login>, response: Response<Login>) {     //응답값을 response.body로 받아옴
                    //웹 통신에 성공했을 때 실행. 응답값을 받아옴.
                    var login = response.body()     //code, msg

                    if (login?.code == "0000") {
                        val dialog = AlertDialog.Builder(this@Sign)
                        dialog.setTitle("알림!")
                        dialog.setMessage("가입 성공")
                        dialog.show()

                        val intent = Intent(applicationContext, Log::class.java)
                        startActivity(intent)
                    } else {
                        val dialog = AlertDialog.Builder(this@Sign)
                        dialog.setTitle("알림!")
                        dialog.setMessage("가입 실패")
                        dialog.show()
                    }

                }

                override fun onFailure(call: Call<Login>, t: Throwable) {
                    //웹 통신에 실패했을 때 실행
                    val dialog = AlertDialog.Builder(this@Sign)
                    dialog.setTitle("실패!")
                    dialog.setMessage("통신에 실패했습니다.")
                    dialog.show()
                }

            })
        }
    }
}