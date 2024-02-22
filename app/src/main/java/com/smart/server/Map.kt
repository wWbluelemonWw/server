package com.smart.server

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skt.Tmap.TMapData
import com.skt.Tmap.TMapMarkerItem
import com.skt.Tmap.TMapPoint
import com.skt.Tmap.TMapPolyLine
import com.skt.Tmap.TMapView
import com.skt.Tmap.TMapView.OnClickListenerCallback
import com.skt.Tmap.TMapView.VISIBLE
import com.skt.Tmap.poi_item.TMapPOIItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt


class Map : Activity() {
    private var mMapView: TMapView? = null
    private var mContext: Context? = null
    private val REQUEST_LOCATION_PERMISSION = 1
    private var point_count = 0
    private lateinit var locationManager: LocationManager
    private val markerPoints = mutableListOf<TMapPoint>()
    private var resultPoints = mutableListOf<TMapPoint>()
    private lateinit var mapContainer: ConstraintLayout
    private lateinit var address_layout: ConstraintLayout
    private lateinit var check_layout: ConstraintLayout
    private var re_data_check = "체크"
    private var my_real_markerPoints = mutableListOf<TMapPoint>()
    private var my_Points = mutableListOf<TMapPoint>()
    private var TOT = 0

    private var result_polyline: TMapPolyLine? = null
    private var result_polyline_route: TMapPolyLine? = null
    private var result_polyline_distance: TMapPolyLine? = null
    private lateinit var address_view: RecyclerView
    private lateinit var profileList: ArrayList<Addressfiles>
    private lateinit var adapter: AddressAdapter
    companion object {
        private const val mApiKey = "NaawXpPLLm3oGRXVIUuuO5rV68T7Ad6yaSUFDjoZ" // SKT
    }

    fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val region = intent.getStringExtra("region") ?: ""
        val city = intent.getStringExtra("city") ?: ""
        val person = intent.getStringExtra("person") ?: ""
        val person_member: TextView = findViewById(R.id.person_member)
        val set_txt: TextView = findViewById(R.id.set_txt)
        set_txt.setText("지역: $region $city")
        person_member.setText(person)

        address_layout = findViewById(R.id.address_layout)
        check_layout = findViewById(R.id.check_layout)
        address_view = findViewById(R.id.address_view)
        val research_button: Button = findViewById(R.id.research_button)
        val address: EditText = findViewById(R.id.address)

        initializeMapView()
        waitGuest()

        profileList = ArrayList()
        adapter = AddressAdapter(profileList, this)
        for (i in 1..profileList.size) {
            profileList.add(Addressfiles(null, null, null))
        }
        address_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        address_view.setHasFixedSize(true)
        address_view.adapter = adapter

        mContext = this
        val research: Button = findViewById(R.id.research)
        val re_set: Button = findViewById(R.id.re_set)

        val tmapdata = TMapData()

        research.setOnClickListener {
            if(address_layout.visibility == VISIBLE){
                address_layout.visibility = View.GONE
            }else{
                address_layout.visibility = VISIBLE
            }
        }

        re_set.setOnClickListener {
            Log.d("초기화", "Reset button clicked.")
            // Perform reset actions...
            initializeMapView() // Ensure this re-adds the map view and reconfigures listeners
        }

        research_button.setOnClickListener {
            val addressText = address.text?.toString() ?: ""

            try {
                tmapdata.findAllPOI(addressText, 50, object : TMapData.FindAllPOIListenerCallback {
                    override fun onFindAllPOI(p0: ArrayList<TMapPOIItem>?) {
                        p0?.let { poiItem ->
                            profileList.clear()
                            for (i in 0 until poiItem.size) {
                                val item = poiItem[i]
                                Log.d("지명 1",item.poiName)
                                Log.d("지명 2",item.poiAddress.replace("null", ""))
                                Log.d("지명 3", item.getPOIPoint().toString())
                                profileList.add(Addressfiles(item.poiName, item.poiAddress.replace("null", ""), item.getPOIPoint()))
                            }
                            adapter.notifyDataSetChanged()
                        }

                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//         (장소API) 통합 검색 함수

        setupMapListeners(mMapView!!)

    }

    private val mDelayHandler: Handler by lazy {
        Handler()
    }

    fun waitGuest() {
        mDelayHandler.postDelayed(::showGuest, 5000) // 5초 후에 showGuest 함수를 실행한다.
    }

    private fun showGuest() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
        // 실제 반복하는 코드를 여기에 적는다
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
            .build()

        val LoopService =
            retrofit.create(LoopService::class.java)        //retrofit 객체를 만든 다음 create를 통해 서비스를 올려주면 loginService가 앞에서 정의한 INPUT OUTPUT을 가지고 서버를 호출할 수 있는 서비스 인터페이스가 된다.
        val roomCode = intent.getStringExtra("roomCode")
        val myCode: String = intent.getStringExtra("myCode") ?: ""
        val member: TextView = findViewById(R.id.member)
        val OK_member: TextView = findViewById(R.id.OK_member)

        if (roomCode != null) {
            LoopService.requestLogin(0, roomCode, myCode).enqueue(object :
                Callback<Looping> {     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                override fun onResponse(
                    call: Call<Looping>,
                    response: Response<Looping>
                ) {     //응답값을 response.body로 받아옴
                    //웹 통신에 성공했을 때 실행. 응답값을 받아옴.
                    val count = response.body()     //count, OK_count
                    member.text = count!!.count.toString()
                    OK_member.text = count.OK_count.toString()
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
        val roomCode = intent.getStringExtra("roomCode") ?: ""
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
        DDelayHandler.removeCallbacksAndMessages(null)
        point_count = 0
    }

    fun cancelHandler() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
        // 실제 반복하는 코드를 여기에 적는다
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val LoopService = retrofit.create(LoopService::class.java)
        val roomCode = intent.getStringExtra("roomCode") ?: ""
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

    fun cancelGet() {
        mDelayHandler.removeCallbacksAndMessages(null)
    }

    fun getCode() {
        waitGet()
    }

    private val DDelayHandler: Handler by lazy {
        Handler()
    }

    fun waitGet() {
        DDelayHandler.postDelayed(::showGet, 5000) // 5초 후에 showGuest 함수를 실행한다.
    }

    fun showGet() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
        val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
        // 실제 반복하는 코드를 여기에 적는다
        val retrofit = Retrofit.Builder()
            .baseUrl("http://$IPnum:8000")        //(서버주소)
            .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
            .build()

        val GetService = retrofit.create(Getservice::class.java)        //retrofit 객체를 만든 다음 create를 통해 서비스를 올려주면 loginService가 앞에서 정의한 INPUT OUTPUT을 가지고 서버를 호출할 수 있는 서비스 인터페이스가 된다.
        val AcceptService = retrofit.create(Acceptservice::class.java)
        val person = intent.getStringExtra("person") ?: ""
        val roomCode = intent.getStringExtra("roomCode") ?: ""
        val myCode: String = intent.getStringExtra("myCode") ?: ""
        val member: TextView = findViewById(R.id.member)
        val OK_member: TextView = findViewById(R.id.OK_member)
        val reject_button: Button = findViewById(R.id.reject_button)
        val accept_button: Button = findViewById(R.id.accept_button)

        GetService.requestLogin(roomCode, myCode, person).enqueue(object :
            Callback<Get> {     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
            override fun onResponse(
                call: Call<Get>,
                response: Response<Get>
            ) {     //응답값을 response.body로 받아옴
                val get = response.body()       //wait, data, count, OK_count
                val re_data = get?.data.toString()
                member.text = get!!.count.toString()
                OK_member.text = get.OK_count.toString()

                if (get.wait != "1" && re_data_check != re_data) {
                    mMapView?.let { mapView ->
                        Log.d("안녕하세요", re_data)
                        re_data_check = re_data
                        Str_to_Tmap(re_data)

                        calculateRoute(resultPoints)

                        placeMultipleMarkers(0, mapView, resultPoints)

                        check_layout.visibility = VISIBLE
                        accept_button.setOnClickListener {
                            AcceptService.requestLogin(1).enqueue(object : Callback<Accept> {     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                                override fun onResponse(
                                    call: Call<Accept>,
                                    response: Response<Accept>
                                ) {
                                    Toast.makeText(this@Map, "모든 인원이 모였습니다. 버스출발~~!!", Toast.LENGTH_SHORT).show()

                                }
                                override fun onFailure(call: Call<Accept>, t: Throwable) {

                                }
                            })
                            check_layout.visibility = View.GONE
                        }
                        reject_button.setOnClickListener {
                            AcceptService.requestLogin(0).enqueue(object : Callback<Accept> {     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                                override fun onResponse(
                                    call: Call<Accept>,
                                    response: Response<Accept>
                                ) {

                                }
                                override fun onFailure(call: Call<Accept>, t: Throwable) {

                                }
                            })
                            check_layout.visibility = View.GONE
                            finish()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<Get>, t: Throwable) {
            }

        })
        waitGet() // 코드 실행뒤에 계속해서 반복하도록 작업한다.
    }


    fun initializeMapView() {
        point_count = 0
        // Ensure mapContainer is properly initialized
        mapContainer = findViewById(R.id.mapview_layout)

        // Remove the existing TMapView if it exists
        mMapView?.let { mapContainer.removeView(it) }
        mMapView = TMapView(this)

        mMapView!!.setSKTMapApiKey(mApiKey)
        mMapView!!.zoomLevel = 15
//        initializeMapView()

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

        val curLat: Double = locCurrent?.latitude ?: 35.2315
        val curLon: Double = locCurrent?.longitude ?: 129.0845

        mMapView!!.setCenterPoint(curLon, curLat)

        mapContainer.addView(mMapView)
        Log.d("지도생성","지도")

        mMapView?.removeAllMarkerItem()
        mMapView?.removeTMapPolyLine("Route")

        markerPoints.clear()

        // Setup listeners or any other initial configuration for the TMapView
        setupMapListeners(mMapView!!)

    }


    private fun setupMapListeners(mMapView: TMapView) {
        // 클릭 이벤트 설정
        val send: Button = findViewById(R.id.send)
        val point_save: Button = findViewById(R.id.point_save)
        val start: EditText = findViewById(R.id.start)
        val end: EditText = findViewById(R.id.end)
        mMapView.setOnClickListenerCallBack(object : OnClickListenerCallback {
            override fun onPressEvent(
                p0: ArrayList<TMapMarkerItem?>?,
                p1: ArrayList<TMapPOIItem?>?,
                p2: TMapPoint?,
                p3: PointF?
            ): Boolean {
                try {
                    val sharedPreferences: SharedPreferences = getSharedPreferences("pref", 0)
                    val IPnum = sharedPreferences.getString("IP_num", "0") ?: "0"
                    val lat: Double = p2?.latitude ?: 35.2315
                    val lon: Double = p2?.longitude ?: 129.0845
                    Log.d("MyApp", "선택한 위치의 주소는 " + lat + "\n" + lon)

                    fun addMarker(latitude: Double, longitude: Double) {
                        val markerItem = TMapMarkerItem()
                        val tMapPoint = TMapPoint(latitude, longitude)

                        markerItem.setPosition(0.5f, 1.0f)
                        markerItem.tMapPoint = tMapPoint
                        markerItem.name = "마커"
                        mMapView.addMarkerItem("markerItem", markerItem)
                    }
                    addMarker(lat, lon)

                    p2?.let { point ->
                        point_save.setOnClickListener(View.OnClickListener {
                            if (markerPoints.size < 2) {
                                markerPoints.add(point)
                                if (markerPoints.size == 1) {
                                    reverseGeocodeLocation(
                                        mMapView,
                                        markerPoints[0]
                                    ) { reverse_marker ->
                                        runOnUiThread {
                                            start.setText(reverse_marker)
                                        }
                                    }
                                } else {
                                    reverseGeocodeLocation(
                                        mMapView,
                                        markerPoints[1]
                                    ) { reverse_marker ->
                                        runOnUiThread {
                                            end.setText(reverse_marker)
                                        }
                                    }
                                }
                            } else {
                                if (point_count == 1) {
                                    markerPoints[1] = point
                                    reverseGeocodeLocation(
                                        mMapView,
                                        markerPoints[1]
                                    ) { reverse_marker ->
                                        runOnUiThread {
                                            end.setText(reverse_marker)
                                        }
                                    }
                                } else {
                                    markerPoints[0] = point
                                    reverseGeocodeLocation(
                                        mMapView,
                                        markerPoints[0]
                                    ) { reverse_marker ->
                                        runOnUiThread {
                                            start.setText(reverse_marker)
                                        }
                                    }
                                }
                            }

                            placeMultipleMarkers(0, mMapView, markerPoints)

                            if (point_count > 0) {
                                point_count = 0
                            } else {
                                point_count += 1
                            }
                            calculateRoute(markerPoints)

                        })
                    }

                    var retrofit = Retrofit.Builder()
                        .baseUrl("http://$IPnum:8000")        //(서버주소)
                        .addConverterFactory(GsonConverterFactory.create())     //응답값 JSON 데이터를 객체로 변환
                        .build()

                    var MapService =
                        retrofit.create(MapService::class.java)        //retrofit 객체를 만든 다음 create를 통해 서비스를 올려주면 loginService가 앞에서 정의한 INPUT OUTPUT을 가지고 서버를 호출할 수 있는 서비스 인터페이스가 된다.
                    val person = intent.getStringExtra("person") ?: ""
                    val roomCode = intent.getStringExtra("roomCode") ?: ""
                    val myCode: String = intent.getStringExtra("myCode") ?: ""

                    send.setOnClickListener {

                        MapService.requestLogin(markerPoints.toString(), person, roomCode, myCode)
                            .enqueue(object :
                                Callback<Mapping> {     //Retrofit을 사용해 서버로 요청을 보내고 응답을 처리. (서버에 textId/textPw를 보내고, enqueue로 응답 처리 콜백 정의)
                                override fun onResponse(
                                    call: Call<Mapping>,
                                    response: Response<Mapping>
                                ) {     //응답값을 response.body로 받아옴
                                    //웹 통신에 성공했을 때 실행. 응답값을 받아옴.
                                    var map = response.body()     //markerPoints
                                    Str_to_Tmap2(map!!.markerPoints)
                                    my_real_markerPoints = my_Points
                                    cancelGet()
                                    getCode()
                                }

                                override fun onFailure(call: Call<Mapping>, t: Throwable) {
                                    cancelGet()
                                    getCode()
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


    fun placeMultipleMarkers(ok: Int, mMapView: TMapView, locations: List<TMapPoint>) {
        if (ok == 0) {
            mMapView.removeAllMarkerItem()
        }

        locations.forEachIndexed { index, location ->
            val originalIcon = when {
                index == 0 -> BitmapFactory.decodeResource(resources, R.drawable.start)
                index == locations.lastIndex -> BitmapFactory.decodeResource(
                    resources,
                    R.drawable.end
                )

                index != 0 && index != locations.lastIndex && markerPoints.contains(location) -> BitmapFactory.decodeResource(
                    resources,
                    R.drawable.mypin
                )

                else -> BitmapFactory.decodeResource(resources, R.drawable.pin)
            }

            val resizedIcon = when {
                index == 0 -> resizeBitmap(originalIcon, 70, 70) // start 아이콘 크기 조정
                index == locations.lastIndex -> resizeBitmap(originalIcon, 70, 70) // end 아이콘 크기 조정
                else -> resizeBitmap(originalIcon, 35, 50) // pin 아이콘 크기 조정
            }
            val marker = TMapMarkerItem().apply {
                tMapPoint = location
                icon = resizedIcon // Set the resized icon
                setPosition(
                    0.5f,
                    1.0f
                ) // Adjusts the anchor point to the middle-bottom of the icon
                name = "Marker $index" // Optional: Set a name for each marker

                reverseGeocodeLocation(mMapView, tMapPoint) { reverse_marker ->
                    setCalloutSubTitle("위치: $reverse_marker")
                }
                setCalloutTitle("목적지 순서: $index")

                setCanShowCallout(true)
                setAutoCalloutVisible(true)
            }
            mMapView.addMarkerItem("Marker $index", marker)
        }
    }

    private fun calculateRoute(markerPoints: List<TMapPoint>) {
        TOT = 0
        val price: TextView = findViewById(R.id.price)
        val distance: TextView = findViewById(R.id.distance)
        val time: TextView = findViewById(R.id.time)
        if (markerPoints.size >= 2) { // Ensure there are at least two markers for a route
            val tMapData = TMapData()
            val startPoint = markerPoints.first()
            val endPoint = markerPoints.last()

            // If there are more than 2 markers, use the intermediate ones as waypoints
            val waypoints = if (markerPoints.size > 2) markerPoints.subList(
                1,
                markerPoints.size - 1
            ) else listOf()

            // Logging the input points
            Log.d(
                "RouteCalculation",
                "Starting point: ${startPoint.latitude}, ${startPoint.longitude}"
            )
            Log.d(
                "RouteCalculation",
                "Destination point: ${endPoint.latitude}, ${endPoint.longitude}"
            )
            waypoints.forEachIndexed { index, waypoint ->
                Log.d(
                    "RouteCalculation",
                    "Waypoint $index: ${waypoint.latitude}, ${waypoint.longitude}"
                )
            }

            tMapData.findPathDataWithType(
                TMapData.TMapPathType.CAR_PATH,
                startPoint,
                endPoint,
                ArrayList(waypoints),
                0
            ) { polyLine ->
                if (polyLine != null) {
                    mMapView!!.addTMapPolyLine("Route", polyLine)
                    Log.d("로그용1",polyLine.distance.toString())
                }
            }

            for (i in 0 until markerPoints.size - 1) {
                tMapData.findPathDataWithType(
                    TMapData.TMapPathType.CAR_PATH,
                    markerPoints[i],
                    markerPoints[i + 1],
                    ArrayList(waypoints),
                    0
                ) { polyLine ->
                    if (polyLine != null) {
                        Log.d("로그용2", polyLine.distance.toString())
                        TOT = TOT + polyLine.distance.roundToInt()


                    }
                }
                Log.d("정류장 총 거리",TOT.toString())
                val Price_num = (TOT / 1000 * 500)
                val Distance_num = (TOT / 1000)
                val Time_num = TOT

                price.setText("$Price_num 원")
                distance.setText("$Distance_num m")
                time.setText("$Time_num 분")

            }

        }
    }


    fun Str_to_Tmap(str: String) {
        resultPoints = mutableListOf()
        val points_String = str
        val pattern = "\\(([^,]+),\\s([^)]+)\\)".toRegex()
        val pointMatches = pattern.findAll(points_String)

        for (match in pointMatches) {
            val latitude = match.groupValues[1].toDouble()
            val longitude = match.groupValues[2].toDouble()
            val point = TMapPoint(latitude, longitude)
            resultPoints.add(point)
        }
    }

    fun Str_to_Tmap2(str: String) {
        my_Points = mutableListOf()
        val points_String = str
        val pattern = "\\(([^,]+),\\s([^)]+)\\)".toRegex()
        val pointMatches = pattern.findAll(points_String)

        for (match in pointMatches) {
            val latitude = match.groupValues[1].toDouble()
            val longitude = match.groupValues[2].toDouble()
            val point = TMapPoint(latitude, longitude)
            my_Points.add(point)
        }
    }

    fun reverseGeocodeLocation(
        mMapView: TMapView,
        location: TMapPoint,
        callback: (String) -> Unit
    ) {
        val tMapData = TMapData()
        tMapData.reverseGeocoding(location.latitude, location.longitude, "A03") { addressInfo ->
            val fullAddress = addressInfo.strFullAddress
            callback(fullAddress)
        }
    }

    fun Map_center(lat:Double,lon:Double){
        mMapView!!.setCenterPoint(lat, lon)
    }

}
