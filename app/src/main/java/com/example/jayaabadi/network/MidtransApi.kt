package com.example.jayaabadi.network

import com.example.jayaabadi.data.payment.MidtransRequest
import com.example.jayaabadi.data.payment.MidtransResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MidtransApi {
    @GET("/")
    suspend fun checkConnection(): Response<ResponseBody>

    @POST("/createTransaction")
    suspend fun createTransaction(@Body request: MidtransRequest): Response<MidtransResponse>
}
