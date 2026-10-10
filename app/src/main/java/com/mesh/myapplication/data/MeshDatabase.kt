package com.mesh.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MeshNetwork::class],
    version = 1,
    exportSchema = false
)
abstract class MeshDatabase : RoomDatabase() {

    abstract fun meshNetworkDao(): MeshNetworkDao

    companion object {
        private const val DATABASE_NAME = "mesh_database"

        @Volatile
        private var instance: MeshDatabase? = null

        fun getInstance(context: Context): MeshDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MeshDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
