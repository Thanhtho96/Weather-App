package com.tt.weatherapp.model

import androidx.room.Entity

@Entity(tableName = "place", primaryKeys = ["title", "address", "latitude", "longitude"])
class PlaceEntity(
    val title: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val dateSearch: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val placeEntity: PlaceEntity =
            other as PlaceEntity
        return title == placeEntity.title &&
                address == placeEntity.address &&
                latitude == placeEntity.latitude &&
                longitude == placeEntity.longitude
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + dateSearch.hashCode()
        return result
    }
}
