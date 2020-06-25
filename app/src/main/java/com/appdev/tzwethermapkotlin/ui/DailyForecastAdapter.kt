package com.appdev.tzwethermapkotlin.ui

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.appdev.tzwethermapkotlin.R
import com.appdev.tzwethermapkotlin.databinding.ItemDaycardBinding
import com.appdev.tzwethermapkotlin.model.DarkSkyModel
import com.appdev.tzwethermapkotlin.utils.WeatherIcons
import java.text.SimpleDateFormat
import java.util.*

class DailyForecastAdapter
  : RecyclerView.Adapter<DailyForecastAdapter.ForecastViewHolder>() {

  private var dayForecast: MutableList<DarkSkyModel.Data> = mutableListOf()
  private lateinit var weatherIcons: Map<String, Drawable>

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
    val layoutInflater = LayoutInflater.from(parent.context)
    val binding: ItemDaycardBinding
      = DataBindingUtil.inflate(layoutInflater, R.layout.item_daycard, parent, false)

    weatherIcons = WeatherIcons.map(parent.context)
    return ForecastViewHolder(binding)
  }

  override fun getItemCount(): Int {
    return dayForecast.size
  }

  override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {

    val day = if (position == 0) {
      "сегодня"
    } else {
      val date = Date(dayForecast[position].time * 1000)
      val dateFormatter = SimpleDateFormat("E, d MMMM", Locale.getDefault())
      dateFormatter.format(date)
    }
    holder.binding.dayOfWeek = day
    holder.binding.dailyForecast = dayForecast[position]
    holder.binding.dailyWeatherIcon = weatherIcons[dayForecast[position].icon]
    holder.binding.executePendingBindings()
  }

  fun setDayForecast(dayForecast: MutableList<DarkSkyModel.Data>?) {
    if (dayForecast == null) {
      return
    }
    this.dayForecast = dayForecast
    notifyDataSetChanged()
  }

  class ForecastViewHolder(val binding: ItemDaycardBinding) : RecyclerView.ViewHolder(binding.root)
}