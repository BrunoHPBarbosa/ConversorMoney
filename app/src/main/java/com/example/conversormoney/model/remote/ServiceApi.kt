package com.example.conversormoney.model.remote

import com.example.conversormoney.model.ConversorResponse
import com.example.conversormoney.model.Country
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface  ServiceApi {

    @GET("convert")
    suspend fun getConversor(
        @Query("access_key") accessKey: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: Double,


    ): Response<ConversorResponse>

        @GET("all")
        suspend fun getCountries(): Response<List<Country>>

}
