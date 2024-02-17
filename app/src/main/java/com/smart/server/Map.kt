package com.smart.server

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PointF
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapView
import com.skt.Tmap.TMapView.OnClickListenerCallback
import com.skt.Tmap.poi_item.TMapPOIItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.os.Handler
import android.widget.TextView


class Map : Activity() {
    private var mMapView: TMapView? = null
    private var mContext: Context? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        waitGuest()

        mContext = this

        var address: EditText = findViewById(R.id.address)
        var result: EditText = findViewById(R.id.result)
        var research: Button = findViewById(R.id.research)
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
            getLocation()
        }

        val locCurrent = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        val curLat: Double = locCurrent?.latitude ?: 0.0
        val curLon: Double = locCurrent?.longitude ?: 0.0
        val send: Button = findViewById(R.id.send)

        // 지도 생성하기
        val mMainRelativeLayout = findViewById<View>(R.id.mapview_layout) as LinearLayout
        mMapView = TMapView(this)
        mMainRelativeLayout.addView(mMapView)
        mMapView!!.setSKTMapApiKey(mApiKey)
        mMapView!!.zoomLevel = 14
        mMapView!!.setCenterPoint(curLon, curLat)

        val tmapdata = TMapData()

        research.setOnClickListener{
            val addressText = address.text?.toString() ?: ""
            val get_result: MutableList<String> = mutableListOf()
            try {
                tmapdata.findAllPOI(addressText, 10, object : TMapData.FindAllPOIListenerCallback {
                    override fun onFindAllPOI(p0: ArrayList<TMapPOIItem>?) {
                        p0?.let { poiItem ->
                            for (i in 0 until poiItem.size) {
                                val item = poiItem[i]
                                Log.d("POI Name: ", "${item.poiName}, " + "Address: ${item.poiAddress.replace("null", "")}, " + "Point: ${item.poiPoint}")
                                get_result.add("${item.poiName}," +" ${item.poiAddress.replace("null", "")} \n\n ")
                            }
                            val resultText = get_result.joinToString(separator = "")
                            result.setText(resultText)
                        }

                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//         (장소API) 통합 검색 함수


//        // POI 상세검색 함수
//        try {
//            tmapdata.findPOIDetailInfo(poiId, new TMapData.FindAllPOIListenerCallback() {
//                @Override
//                public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
//                    mPoiItem = poiItem.get(0);
//                    setTextLevel(MESSAGE_STATE_POI_DETAIL);
//                }
//            });
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//    }
//}

        // 클릭 이벤트 설정
        mMapView!!.setOnClickListenerCallBack(object : OnClickListenerCallback {
            override fun onPressEvent(
                p0: ArrayList<TMapMarkerItem?>?,
                p1: ArrayList<TMapPOIItem?>?,
                p2: TMapPoint?,
                p3: PointF?
            ): Boolean {
                try {
                    val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
                    val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
                    var lat: Double = p2?.latitude ?: 0.0
                    var lon: Double = p2?.longitude ?: 0.0
                    Log.d("MyApp", "선택한 위치의 주소는 " + lat + "\n"+lon)

                    fun addMarker(latitude: Double, longitude: Double) {
                        val markerItem = TMapMarkerItem()
                        val tMapPoint = TMapPoint(latitude, longitude)

                        markerItem.setPosition(0.5f, 1.0f)
                        markerItem.tMapPoint = tMapPoint
                        markerItem.name = "마커"
                        mMapView?.addMarkerItem("markerItem", markerItem)
                    }
                    addMarker(lat, lon)
                    result.setText("위도: " + "$lat" + "\n경도: " + "$lon")

                    var retrofit = Retrofit.Builder()
                        .baseUrl("http://$IPnum:8000")        //(서버주소)
                        .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
                        .build()

                    var MapService = retrofit.create(MapService::class.java)        //retrofit 객체를 만든 다음 create를 통해 서비스를 올려주면 loginService가 앞에서 정의한 INPUT OUTPUT을 가지고 서버를 호출할 수 있는 서비스 인터페이스가 된다.

                    send.setOnClickListener{

                        MapService.requestLogin(lat, lon).enqueue(object : Callback<Mapping> {     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                            override fun onResponse(call: Call<Mapping>, response: Response<Mapping>) {     //응답값을 response.body로 받아옴
                                //웹 통신에 성공했을 때 실행. 응답값을 받아옴.
                                var map = response.body()     //lat, lon

                                val dialog = AlertDialog.Builder(this@Map)        //대괄호 안에 있어서 this@MainActivity 사용
                                dialog.setTitle("알림!")
                                dialog.setMessage("id =" + map?.lat + "\npw = " + map?.lon)
                                dialog.show()
                            }

                            override fun onFailure(call: Call<Mapping>, t: Throwable) {
                                //웹 통신에 실패했을 때 실행
                                val dialog = AlertDialog.Builder(this@Map)
                                dialog.setTitle("실패!")
                                dialog.setMessage("통신에 실패했습니다.")
                                dialog.show()
                            }

                        })
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            // onPressEvent 메서드 내용 작성
            return false // 또는 true, 이벤트 처리에 따라 적절히 반환
        }

            override fun onPressUpEvent(
                p0: ArrayList<TMapMarkerItem?>?,
                p1: ArrayList<TMapPOIItem?>?,
                p2: TMapPoint?,
                p3: PointF?
            ): Boolean {
                // onPressUpEvent 메서드 내용 작성
                return false // 또는 true, 이벤트 처리에 따라 적절히 반환
            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 승인된 경우 위치 정보를 가져올 수 있습니다.
                    getLocation()
                } else {
                    // 권한이 거부된 경우 사용자에게 권한의 중요성을 설명하거나 대체 작업을 수행할 수 있습니다.
                }
            }
        }
    }
    companion object {
        private const val mApiKey = "qdxPlTC4Sa1btX0D2LcTt4j3r8eTYsqS7QMhmNLj" // SKT
    }


    private val mDelayHandler: Handler by lazy {
        Handler()
    }

    private fun waitGuest(){
        mDelayHandler.postDelayed(::showGuest, 3000) // 3초 후에 showGuest 함수를 실행한다.
    }

    private fun showGuest(){
        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
        // 실제 반복하는 코드를 여기에 적는다
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
            .build()

        val LoopService = retrofit.create(LoopService::class.java)        //retrofit 객체를 만든 다음 create를 통해 서비스를 올려주면 loginService가 앞에서 정의한 INPUT OUTPUT을 가지고 서버를 호출할 수 있는 서비스 인터페이스가 된다.
        val roomCode = intent.getStringExtra("roomCode")
        val myCode: String = intent.getStringExtra("myCode") ?: ""
        val member: TextView = findViewById(R.id.member)

        if (roomCode != null) {
            LoopService.requestLogin(0, roomCode, myCode).enqueue(object :
                Callback<Looping> {     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                override fun onResponse(call: Call<Looping>, response: Response<Looping>) {     //응답값을 response.body로 받아옴
                    //웹 통신에 성공했을 때 실행. 응답값을 받아옴.
                    var count = response.body()     //count
                    member.text = count?.count.toString()
                }

                override fun onFailure(call: Call<Looping>, t: Throwable) {
                    //웹 통신에 실패했을 때 실행
                }

            })
        }
        waitGuest() // 코드 실행뒤에 계속해서 반복하도록 작업한다.
    }

    override fun onDestroy() {
        super.onDestroy()

        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
        // 실제 반복하는 코드를 여기에 적는다
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val LoopService = retrofit.create(LoopService::class.java)
        val roomCode = intent.getStringExtra("roomCode") ?:""
        val myCode: String = intent.getStringExtra("myCode") ?: ""

        LoopService.requestLogin(1000, roomCode, myCode).enqueue(object :
            Callback<Looping> {
            override fun onResponse(call: Call<Looping>, response: Response<Looping>) {

            }

            override fun onFailure(call: Call<Looping>, t: Throwable) {
                //웹 통신에 실패했을 때 실행
            }

        })
        mDelayHandler.removeCallbacksAndMessages(null)
    }
}