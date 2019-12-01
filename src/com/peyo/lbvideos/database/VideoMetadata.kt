package com.peyo.lbvideos.database

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Entity
@Parcelize
data class VideoMetadata(
    @PrimaryKey val title: String,
    val category: String,
    val description: String,
    val card: String,
    val background: String,
    val studio:String,
    val source : String
) : Parcelable

@Dao
interface VideoMetadataDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg metadata: VideoMetadata)

    @Query("SELECT * FROM VideoMetadata WHERE category = :category")
    fun findByCategory(category: String): List<VideoMetadata>
}