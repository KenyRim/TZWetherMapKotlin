package com.appdev.tzwethermapkotlin.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import com.appdev.tzwethermapkotlin.BuildConfig
import com.appdev.tzwethermapkotlin.R
import com.appdev.tzwethermapkotlin.api.DarkSkyApiService
import com.appdev.tzwethermapkotlin.model.DarkSkyModel
import com.appdev.tzwethermapkotlin.utils.WeatherIcons
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import kotlinx.android.synthetic.main.map_fragment_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.roundToInt


class DialogMap : DialogFragment()  , OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    val darkSkyApiResponseLiveData = MutableLiveData<DarkSkyModel.DarkSky>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val forecastAdi by lazy { DarkSkyApiService.create() }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogFragmentTheme)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.map_fragment_layout, container, false)

        val mapFragment: SupportMapFragment? =
            parentFragmentManager.findFragmentById(R.id.map_layout) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)




        return view
    }

    private fun getLastKnownLocation() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    makeRequest(location.latitude, location.longitude)
                }

            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
            val fragment = activity
                ?.supportFragmentManager?.findFragmentById(
                    R.id.map_layout
                ) as SupportMapFragment?
            if (fragment != null) parentFragmentManager.beginTransaction().remove(fragment).commit()
        } catch (e: IllegalStateException) {

        }
    }

    companion object {

        fun newTargetInstance(): DialogMap {
            return DialogMap()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID


        getLastKnownLocation()

        btn_main_location.setOnClickListener { getLastKnownLocation() }

        mMap!!.setOnMapClickListener { latLon ->
            makeRequest(  latLon.latitude, latLon.longitude)
        }

    }

    private fun makeRequest(latitude: Double, longitude: Double){
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
                        return
                    }

                    darkSkyApiResponseLiveData.value = response.body()

                    val temp = darkSkyApiResponseLiveData.value!!.currently.temperature!!.roundToInt().toString()
                    tv_temp.text = getString(R.string.temperature,temp)
                    tv_summary.text = darkSkyApiResponseLiveData.value!!.currently.summary
                    val weatherIcons = WeatherIcons.map(requireContext())

                    iv_image.setImageDrawable(weatherIcons[darkSkyApiResponseLiveData.value!!.currently.icon])

                    markLocation(latitude, longitude,darkSkyApiResponseLiveData.value!!.timezone)


                }

                override fun onFailure(call: Call<DarkSkyModel.DarkSky>, throwable: Throwable) {
                }
            })


    }
    fun markLocation(lat:Double,lon:Double, timeZone: String){

        MapboxGeocoding.builder()
            .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
            .query(Point.fromLngLat(lon,lat))
            .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
            .languages(Locale.getDefault())
            .build().enqueueCall(object : Callback<GeocodingResponse> {
                override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {

                }

                override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {

                    if (response.isSuccessful && response.body()!!.features().isNotEmpty()) {

                        val locationName = response.body()?.features()!![0].placeName().toString()
                        addMark(lat,lon,locationName)

                    }else{
                        addMark(lat,lon,timeZone)
                    }
                }


            })


    }

    fun addMark(lat:Double,lon:Double,locationName:String){
        val latLng = LatLng(lat,lon)
        mMap!!.clear()
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 3f))
        mMap!!.addMarker(MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
            .title(locationName)).showInfoWindow()
    }

}