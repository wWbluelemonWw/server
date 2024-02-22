@file:JvmName("LoginKt")

package com.smart.server

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.motion.widget.Debug
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import android.widget.ImageButton

class Log : AppCompatActivity() {
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var locationManager: LocationManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"

        //GPS 사용 설정
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 경우 권한 요청 다이얼로그 표시
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // 권한이 이미 있는 경우 위치 정보를 가져올 수 있습니다.
            Debug.getLocation()
        }

        val button: Button = findViewById(R.id.button)
        val sign: Button = findViewById(R.id.sign)
        val setset: ImageButton = findViewById(R.id.setset)
        val editText: EditText = findViewById(R.id.editText)
        val editText2: EditText = findViewById(R.id.editText2)

        Log.d("확인용","http://$IPnum:8000/")
        var retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000/")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
            .build()

        var loginService = retrofit.create(LoginService::class.java)        //retrofit 객체를 만든 다음 create를 통해 서비스를 올려주면 loginService가 앞에서 정의한 INPUT OUTPUT을 가지고 서버를 호출할 수 있는 서비스 인터페이스가 된다.

        button.setOnClickListener{
            val LS = '1'
            var textId = editText.text.toString()
            var textPw = editText2.text.toString()

            loginService.requestLogin(LS, textId, textPw).enqueue(object : Callback<Login>{     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                override fun onResponse(call: Call<Login>, response: Response<Login>) {     //응답값을 response.body로 받아옴
                    //웹 통신에 성공했을 때 실행. 응답값을 받아옴.
                    var login = response.body()     //code, msg

                    if (login?.code == "1001") {
                        val dialog = AlertDialog.Builder(this@Log)
                        dialog.setTitle("알림!")
                        dialog.setMessage("로그인 성공")
                        dialog.show()

                        val intent = Intent(applicationContext, Main::class.java)
                        startActivity(intent)
                    } else {
                        val dialog = AlertDialog.Builder(this@Log)        //대괄호 안에 있어서 this@MainActivity 사용
                        dialog.setTitle("알림!")
                        dialog.setMessage("로그인 실패")
                        dialog.show()
                    }
                }

                override fun onFailure(call: Call<Login>, t: Throwable) {
                    //웹 통신에 실패했을 때 실행
                    val dialog = AlertDialog.Builder(this@Log)
                    dialog.setTitle("실패!")
                    dialog.setMessage("통신에 실패했습니다.")
                    dialog.show()
                }

            })
        }
        setset.setOnClickListener {
            val intent = Intent(applicationContext, Setting::class.java)
            startActivity(intent)
        }
        sign.setOnClickListener {
            val intent = Intent(applicationContext, Sign::class.java)
            startActivity(intent)
        }
    }
}

//Retrofit 만들기
//1. retrofit 객체 (안드로이드에서 서비스를 호출하면 서버와 통신하고 응답값을 받아와 OUTPUT으로 내보냄)
//2. 서비스(Interface) (INPUT/OUTPUT)