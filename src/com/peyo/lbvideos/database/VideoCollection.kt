package com.peyo.lbvideos.database

import androidx.room.*

@Entity
data class VideoCollection(
        @PrimaryKey val category: String
)

@Dao
interface VideoCollectionDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg item: VideoCollection)

    @Query("SELECT * FROM VideoCollection")
    fun findAll(): List<VideoCollection>
}