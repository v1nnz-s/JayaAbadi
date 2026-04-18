package com.example.jayaabadi.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: MidtransApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // ganti jika pakai HP atau server online
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MidtransApi::class.java)
    }
}