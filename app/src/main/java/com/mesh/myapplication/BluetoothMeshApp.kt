package com.mesh.myapplication

import android.app.Application
import com.mesh.myapplication.data.MeshDatabase
import com.mesh.myapplication.data.MeshRepository

class BluetoothMeshApp : Application() {

    val meshRepository: MeshRepository by lazy {
        MeshRepository(MeshDatabase.getInstance(this).meshNetworkDao())
    }
}
