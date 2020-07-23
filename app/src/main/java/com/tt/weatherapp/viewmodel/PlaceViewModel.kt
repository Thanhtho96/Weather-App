package com.tt.weatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.tt.weatherapp.database.PlaceRoomDatabase
import com.tt.weatherapp.model.PlaceEntity
import com.tt.weatherapp.repository.PlaceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val placeRepository: PlaceRepository
    var placeListPagedList: LiveData<PagedList<PlaceEntity>> = MutableLiveData()
    var listPlacePaging: MutableLiveData<List<PlaceEntity>> = MutableLiveData()
    var listPlaceHistoryLiveData: LiveData<List<PlaceEntity>> = MutableLiveData()
    var placeAdded: MutableLiveData<PlaceEntity> = MutableLiveData()
    private val pagedListConfig = PagedList.Config.Builder().setPageSize(10).build()
    private val placeEntityBoundaryCallback: PlaceEntityBoundaryCallback

    init {
        val placeRoomDatabase =
            PlaceRoomDatabase.getDatabase(
                application
            )
        placeRepository = PlaceRepository(placeRoomDatabase.placeDao())
        placeEntityBoundaryCallback = PlaceEntityBoundaryCallback(this)
    }

    fun getPlacePagingPagedAdapter(limit: Int, offset: Int) {
        placeListPagedList = LivePagedListBuilder(
            placeRepository.getAllPlaceHistoryPagingPagedAdapter(limit, offset),
            pagedListConfig
        ).setBoundaryCallback(placeEntityBoundaryCallback).build()
    }

    fun getPlacePaging(limit: Int, offset: Int) {
        viewModelScope.launch {
            delay(3000)
            listPlacePaging.value = placeRepository.getAllPlaceHistoryPaging(limit, offset)
        }
    }

    fun getAllPlaceHistory() {
        listPlaceHistoryLiveData = placeRepository.getAllPlaceHistory()
    }

    fun insertPlace(placeEntity: PlaceEntity) {
        viewModelScope.launch {
            placeRepository.insertPlace(placeEntity)
            placeAdded.value = placeEntity
        }
    }
}