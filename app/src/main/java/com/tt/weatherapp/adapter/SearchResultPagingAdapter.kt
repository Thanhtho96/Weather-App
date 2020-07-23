package com.tt.weatherapp.adapter

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tt.weatherapp.R
import com.tt.weatherapp.model.PlaceEntity
import com.tt.weatherapp.ui.MainFragment
import com.tt.weatherapp.viewmodel.WeatherViewModel
import kotlinx.android.synthetic.main.item_search_result.view.*

class SearchResultPagingAdapter(
    context: Context,
    private val sharedPref: SharedPreferences,
    private val weatherViewModel: WeatherViewModel,
    private val mainFragment: MainFragment
) :
    PagedListAdapter<PlaceEntity, SearchResultPagingAdapter.ViewHolder>(DIFF_CALLBACK) {

    private val layoutInflater = LayoutInflater.from(context)

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bindTo(placeEntity: PlaceEntity?) {
            itemView.text_view_search_title.text = placeEntity?.title
            itemView.text_view_search_address.text = placeEntity?.address
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val placeEntity: PlaceEntity? = getItem(position)
        holder.bindTo(placeEntity)

        holder.itemView.setOnClickListener {
            sharedPref.edit().apply {
                putString("address", placeEntity?.address)
            }.apply()

            weatherViewModel.setLocationLatLon(
                placeEntity?.latitude!!,
                placeEntity.longitude
            )

            mainFragment.showHomeFragment()
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<PlaceEntity>() {
            // PlaceEntity details may have changed if reloaded from the database,
            // but ID is fixed.
            override fun areItemsTheSame(
                oldPlaceEntity: PlaceEntity,
                newPlaceEntity: PlaceEntity
            ) = oldPlaceEntity.title == newPlaceEntity.title &&
                    oldPlaceEntity.address == newPlaceEntity.address &&
                    oldPlaceEntity.latitude == newPlaceEntity.latitude &&
                    oldPlaceEntity.longitude == newPlaceEntity.longitude

            override fun areContentsTheSame(
                oldPlaceEntity: PlaceEntity,
                newPlaceEntity: PlaceEntity
            ) = oldPlaceEntity == newPlaceEntity
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.item_search_result, parent, false))
    }
}