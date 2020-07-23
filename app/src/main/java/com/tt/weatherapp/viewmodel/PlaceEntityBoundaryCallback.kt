package com.tt.weatherapp.viewmodel

import android.util.Log
import androidx.paging.PagedList
import com.tt.weatherapp.model.PlaceEntity

class PlaceEntityBoundaryCallback(placeViewModel: PlaceViewModel) :
    PagedList.BoundaryCallback<PlaceEntity>() {

    override fun onItemAtEndLoaded(itemAtEnd: PlaceEntity) {
        super.onItemAtEndLoaded(itemAtEnd)
        Log.d("TAG", "onItemAtEndLoaded: ${itemAtEnd.address}")
    }

}
