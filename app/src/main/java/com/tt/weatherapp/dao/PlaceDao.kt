package com.tt.weatherapp.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tt.weatherapp.model.PlaceEntity

@Dao
interface PlaceDao {

    @Query("SELECT p.* FROM place p ORDER BY p.dateSearch DESC")
    fun getAllPlaceHistory(): LiveData<List<PlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(placeEntity: PlaceEntity)

    // Get 10 items per-query
    @Query("SELECT p.* FROM place p ORDER BY p.dateSearch DESC LIMIT :limit OFFSET :offset")
    fun getAllPlaceHistoryPagingPagedAdapter(
        limit: Int,
        offset: Int
    ): DataSource.Factory<Int, PlaceEntity>

    @Query("SELECT p.* FROM place p ORDER BY p.dateSearch DESC LIMIT :limit OFFSET :offset")
    suspend fun getAllPlaceHistoryPaging(limit: Int, offset: Int): List<PlaceEntity>
}