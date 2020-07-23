package com.tt.weatherapp.repository

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.tt.weatherapp.dao.PlaceDao
import com.tt.weatherapp.model.PlaceEntity

class PlaceRepository(private val placeDao: PlaceDao) {

    fun getAllPlaceHistory(): LiveData<List<PlaceEntity>> {
        return placeDao.getAllPlaceHistory()
    }

    suspend fun insertPlace(placeEntity: PlaceEntity) {
        placeDao.insertPlace(placeEntity)
    }

    fun getAllPlaceHistoryPagingPagedAdapter(
        limit: Int,
        offset: Int
    ): DataSource.Factory<Int, PlaceEntity> {
        return placeDao.getAllPlaceHistoryPagingPagedAdapter(limit, offset)
    }

    suspend fun getAllPlaceHistoryPaging(limit: Int, offset: Int): List<PlaceEntity> {
        return placeDao.getAllPlaceHistoryPaging(limit, offset)
    }
}