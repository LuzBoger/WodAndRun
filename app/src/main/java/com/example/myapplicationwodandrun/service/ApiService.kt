package com.example.myapplicationwodandrun.service

import com.example.myapplicationwodandrun.wodData
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("wod.json")
    suspend fun getWod():Response<MutableList<wodData>>
}