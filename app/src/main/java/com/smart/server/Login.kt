package com.smart.server

//아웃풋을 만든다.(서버에서 통신을 호출했을 때 받아오는 응답)
data class Login(
    var code : String      //주의! 변수명이 JsonResponse의 Key값과 같아야함
    )

data class Mapping(
    var lat : String,
    var lon : String
)

data class Looping(
    var count : Int
)

data class Rooming(
    var less : Int
)

data class MRoom(
    var data : Array<MRoomData>
)
data class MRoomData(
    var title: String,
    var subtitle: String,
    var person: String,
    var region: String,
    var roomCode: String
)