package com.appdev.tzwethermapkotlin.ui

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.appdev.tzwethermapkotlin.R
import com.appdev.tzwethermapkotlin.databinding.ActivityMainBinding
import com.appdev.tzwethermapkotlin.utils.WeatherIcons
import com.appdev.tzwethermapkotlin.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1


    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    private var weatherIconMap: Map<String, Drawable>? = null

    private val adapter = DailyForecastAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        setUpMap()
        addObservers()

        weatherIconMap = WeatherIcons.map(this)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.fetchForecastAtLocation(55.751244, 37.618423)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(p0: TabLayout.Tab?) {}

            override fun onTabSelected(p0: TabLayout.Tab?) {

                when (tabLayout.selectedTabPosition) {
                    0 -> {
                        viewModel.fetchForecastAtLocation(55.751244, 37.618423)

                    }
                    1 -> {
                        viewModel.fetchForecastAtLocation(59.9342802, 30.3350986)
                    }
                }

            }


        })

        btn_map.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val newFragment = DialogMap.newTargetInstance()
            newFragment.show(ft, "dialog")
        }
    }

    private fun addObservers() {

        viewModel.darkSkyApiResponseLiveData.observe(this, Observer { darkSkyModel ->
            binding.currentCondition = darkSkyModel.currently

            adapter.setDayForecast(darkSkyModel.daily.data)

            if (darkSkyModel.currently.icon != null && weatherIconMap != null) {
                binding.currentConditionIcon = weatherIconMap!![darkSkyModel.currently.icon]
            }
        })

    }
}
