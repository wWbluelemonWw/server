package com.smart.server
import android.widget.EditText
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.PUT

interface LoginService {

    @FormUrlEncoded                //서버에서 정상적인 값을 읽기 위한 인코딩
    @POST("/app_login/")           //이게 어떤 통신인지 설명해줘야 함 ("루트 url 다음에 있는 url 입력)
    fun requestLogin(
        //인풋을 정의하는 곳
        @Field("LS") LS:Char,
        @Field("userid") userid:String,     //INPUT 만들기 주의! 이름 userid가 서버에서 POST로 받는 이름과 같아야함
        @Field("userpw") userpw:String,
    ) : Call<Login> //아웃풋을 정의하는 곳 <OUTPUT>
}

interface MapService{

    @FormUrlEncoded                //서버에서 정상적인 값을 읽기 위한 인코딩
    @POST("/app_map/")           //이게 어떤 통신인지 설명해줘야 함 ("루트 url 다음에 있는 url 입력)
    fun requestLogin(
        //인풋을 정의하는 곳
        @Field("lat") lat:String,     //INPUT 만들기 주의! 이름 userid가 서버에서 POST로 받는 이름과 같아야함
        @Field("lon") lon:Double,
        @Field("person") person: String,
        @Field("roomCode") roomCode:String,
        @Field("myCode") myCode:String
    ) : Call<Mapping> //아웃풋을 정의하는 곳 <OUTPUT>
}

interface LoopService{

    @FormUrlEncoded                //서버에서 정상적인 값을 읽기 위한 인코딩
    @POST("/app_loop/")           //이게 어떤 통신인지 설명해줘야 함 ("루트 url 다음에 있는 url 입력)
    fun requestLogin(
        //인풋을 정의하는 곳
        @Field("loop") loop:Int,     //INPUT 만들기 주의! 이름 userid가 서버에서 POST로 받는 이름과 같아야함
        @Field("roomCode") roomCode:String,
        @Field("myCode") myCode:String
    ) : Call<Looping> //아웃풋을 정의하는 곳 <OUTPUT>
}

interface MakeRoomService{

    @FormUrlEncoded                //서버에서 정상적인 값을 읽기 위한 인코딩
    @POST("/app_make/")           //이게 어떤 통신인지 설명해줘야 함 ("루트 url 다음에 있는 url 입력)
    fun requestLogin(
        //인풋을 정의하는 곳
        @Field("title") title: String,
        @Field("subtitle") subtitle: String,
        @Field("person") person: String,
        @Field("region") region: String,
        @Field("roomCode") roomCode:String,

        ) : Call<Rooming> //아웃풋을 정의하는 곳 <OUTPUT>
}

interface RoomService{

    @FormUrlEncoded                //서버에서 정상적인 값을 읽기 위한 인코딩
    @POST("/app_room/")           //이게 어떤 통신인지 설명해줘야 함 ("루트 url 다음에 있는 url 입력)
    fun requestLogin(
        //인풋을 정의하는 곳
        @Field("no") no: String

        ) : Call<MRoom> //아웃풋을 정의하는 곳 <OUTPUT>
}

interface Getservice{

    @FormUrlEncoded                //서버에서 정상적인 값을 읽기 위한 인코딩
    @POST("/app_get/")           //이게 어떤 통신인지 설명해줘야 함 ("루트 url 다음에 있는 url 입력)
    fun requestLogin(
        //인풋을 정의하는 곳
        @Field("roomCode") roomCode:String,
        @Field("myCode") myCode:String,
        @Field("person") person: String,

    ) : Call<Get> //아웃풋을 정의하는 곳 <OUTPUT>
}