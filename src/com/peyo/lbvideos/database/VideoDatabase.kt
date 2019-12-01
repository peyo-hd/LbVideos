package com.peyo.lbvideos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VideoMetadata::class, VideoCollection::class], version =1)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun metadata(): VideoMetadataDAO
    abstract fun collections(): VideoCollectionDAO

    companion object {
        @Volatile private var INSTANCE: VideoDatabase? = null
        fun getInstance(context: Context): VideoDatabase = INSTANCE ?: synchronized(this) {
            Room.inMemoryDatabaseBuilder(context.applicationContext, VideoDatabase::class.java)
                    .build().also {INSTANCE = it}
        }
    }
}