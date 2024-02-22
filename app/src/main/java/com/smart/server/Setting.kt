package com.smart.server

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton

class Setting : AppCompatActivity() {
    lateinit var IP_num: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        IP_num = findViewById(R.id.IP_num)

        val save: ImageButton = findViewById(R.id.save)
        save.setOnClickListener {
            saveData()
            finish()
        }
//        TODO: 저장된 데이터를 로드
        loadData()  //edit text 저장되어있던 값을 setText
    }

    private fun loadData() {        //함수는 ()로 감싸있는 형태
        val pref = getSharedPreferences("pref", 0)
        IP_num.setText(pref.getString("IP_num","125.134.184.179"))
//         1번째 인자에서는 저장할 당시의 키 값을 적어줌, 2번째는 키 값에 데이터가 존재하지 않을 경우 대체 값을 적어줌.
    }

    private fun saveData() {        //클래스 내부에서 접근할 때는 private 명시, 아니면 public등 사용가능
        val pref = getSharedPreferences("pref", 0)
        val edit = pref.edit() //수정모드
        edit.putString("IP_num", IP_num.text.toString())
        // 1번째 인자에는 키 값을, 2번째 인자에는 실제 담아둘 값 name이라는 이름으로 et_hello에 적은 내용을 저장하겠다.
        edit.apply() //값을 저장 완료
    }

    override fun onDestroy() {  //해당 액티비티가 종료되는 시점이 다가올 때 호출(종료되는 시점에 행위)
        super.onDestroy()

        saveData()  //edit text 값을 저장
    }

}