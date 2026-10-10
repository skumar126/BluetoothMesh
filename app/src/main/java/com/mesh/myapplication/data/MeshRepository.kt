package com.mesh.myapplication.data

import kotlinx.coroutines.flow.Flow

class MeshRepository(private val dao: MeshNetworkDao) {

    fun observeMeshes(): Flow<List<MeshNetwork>> = dao.observeAll()

    suspend fun getMeshes(): List<MeshNetwork> = dao.getAll()

    suspend fun getMesh(id: Long): MeshNetwork? = dao.getById(id)

    suspend fun createMesh(name: String): Long {
        val now = System.currentTimeMillis()
        return dao.insert(
            MeshNetwork(
                name = name.trim(),
                createdAt = now,
                modifiedAt = now
            )
        )
    }

    suspend fun renameMesh(id: Long, name: String) {
        dao.updateName(id, name.trim(), System.currentTimeMillis())
    }

    suspend fun deleteMesh(id: Long) {
        dao.deleteById(id)
    }
}
