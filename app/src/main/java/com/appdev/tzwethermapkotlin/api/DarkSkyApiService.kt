package com.appdev.tzwethermapkotlin.api

import com.appdev.tzwethermapkotlin.App
import com.appdev.tzwethermapkotlin.model.DarkSkyModel
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


const val BASE_URL = "https://api.darksky.net/"

interface DarkSkyApiService {

  companion object {
    fun create(): DarkSkyApiService {

      val cacheSize = 10 * 1024 * 1024 // 10 MB

      val cache = Cache(App.appContext!!.cacheDir, cacheSize.toLong())

      val okHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .build()

      val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

      return retrofit.create(DarkSkyApiService::class.java)
    }


  }



  @GET("forecast/{key}/{latitude},{longitude}")
  fun forecast(
    @Path("key") key: String,
    @Path("latitude") latitude: Double,
    @Path("longitude") longitude: Double,
    @Query("units") units: String,
    @Query("lang") language: String,
    @Query("exclude") exclude: String
  ): Call<DarkSkyModel.DarkSky>

}