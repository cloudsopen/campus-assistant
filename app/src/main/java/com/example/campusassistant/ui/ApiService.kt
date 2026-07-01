package com.example.campusassistant

import retrofit2.http.GET

interface ApiService {
    // 因为一言的网址直接在根目录，所以这里写 "/"
    // suspend 代表这是一个挂起函数，会自动在后台线程执行，绝不卡顿 UI
    @GET("/")
    suspend fun getDailyQuote(): HitokotoResponse
}