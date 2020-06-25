package com.appdev.tzwethermapkotlin.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.appdev.tzwethermapkotlin.BuildConfig
import com.appdev.tzwethermapkotlin.api.DarkSkyApiService
import com.appdev.tzwethermapkotlin.model.DarkSkyModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

const val DEFAULT_LOCATION_NAME = "Москва"
@JvmField
val LOCATION = Location("default").apply {
  latitude = 55.751244
  longitude = 37.618423
}

class MainActivityViewModel(application: Application) : AndroidViewModel(application),
  OnSuccessListener<Location>, OnFailureListener,Callback<GeocodingResponse>
  {
  val darkSkyApiResponseLiveData = MutableLiveData<DarkSkyModel.DarkSky>()
  val locationNameLiveData = MutableLiveData<String>()

  private val forecastAdi by lazy { DarkSkyApiService.create() }

  private var userLocation: Location = LOCATION

  fun fetchForecastAtLocation(latitude: Double, longitude: Double) {
    forecastAdi.forecast(
      BuildConfig.DARKSKY_KEY,
      latitude,
      longitude,
      "uk",
      "ru",
      arrayListOf("minutely", "hourly", "alerts", "flags").toString())
      .enqueue(object : Callback<DarkSkyModel.DarkSky> {
        override fun onResponse(call: Call<DarkSkyModel.DarkSky>, response: Response<DarkSkyModel.DarkSky>) {
          if (!response.isSuccessful || response.body() == null) {
            // TODO display error message to the user
            return
          }
          darkSkyApiResponseLiveData.value = response.body()

          getLocationName(latitude,longitude)
        }

        override fun onFailure(call: Call<DarkSkyModel.DarkSky>, throwable: Throwable) {
        }
      })
  }

  override fun onSuccess(location: Location?) {
    if (location == null) {
      locationNameLiveData.value = DEFAULT_LOCATION_NAME
    } else {
      userLocation = location
    }
    fetchForecastAtLocation(userLocation.latitude, userLocation.longitude)

  }

  override fun onFailure(exception: Exception) {
  }

  private fun getLocationName(lat:Double,lon:Double) {
    MapboxGeocoding.builder()
      .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
      .query(Point.fromLngLat(lon,lat))
      .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
      .build().enqueueCall(this)
  }

  override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
    if (response.isSuccessful && response.body()!!.features().isNotEmpty()) {
      locationNameLiveData.value = response.body()?.features()!![0].text()
    }
  }

  override fun onFailure(call: Call<GeocodingResponse>, throwable: Throwable) {
  }

}