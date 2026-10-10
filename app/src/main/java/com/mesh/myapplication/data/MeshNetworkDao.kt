package com.mesh.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeshNetworkDao {

    @Query("SELECT * FROM mesh_networks ORDER BY modified_at DESC")
    fun observeAll(): Flow<List<MeshNetwork>>

    @Query("SELECT * FROM mesh_networks ORDER BY modified_at DESC")
    suspend fun getAll(): List<MeshNetwork>

    @Query("SELECT * FROM mesh_networks WHERE id = :id")
    suspend fun getById(id: Long): MeshNetwork?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mesh: MeshNetwork): Long

    @Update
    suspend fun update(mesh: MeshNetwork)

    @Query(
        """
        UPDATE mesh_networks
        SET name = :name, modified_at = :modifiedAt
        WHERE id = :id
        """
    )
    suspend fun updateName(id: Long, name: String, modifiedAt: Long)

    @Delete
    suspend fun delete(mesh: MeshNetwork)

    @Query("DELETE FROM mesh_networks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
