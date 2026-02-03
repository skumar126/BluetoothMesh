package com.mesh.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mesh.myapplication.adapter.DeviceAdapter
import com.mesh.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.ThreadContextElement
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val scanResults = mutableListOf<BluetoothDevice>()
    private lateinit var recyclerViewAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 1. Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 3. Setup AppBar / Toolbar
        setSupportActionBar(binding.toolbar)

        // 4. Apply window insets to toolbar and content
        applyEdgeToEdge()

        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        //Permission
        checkAnsRequestPermission()

        recyclerViewAdapter = DeviceAdapter(scanResults)
        binding.deviceList.layoutManager = LinearLayoutManager(this)
        binding.deviceList.adapter = recyclerViewAdapter
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun checkAnsRequestPermission() {
        if (!hasPermission()) {
            permissionLauncher.launch(requiredPermission())
        } else {
            startTimedScan()
        }
    }

    private val handler = android.os.Handler()

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startTimedScan() {
        startBluetooth()
        handler.postDelayed({ stopBleScan() }, 10_000)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startBluetooth() {
        val meshProvisioningFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(MESH_PROVISIONING_UUID))
            .build()

        val meshProxyFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(MESH_PROXY_UUID))
            .build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner.startScan(
            listOf(meshProvisioningFilter, meshProxyFilter), scanSettings, scanCallback
        )

        Log.d("BLE", "Scanning started")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun stopBleScan() {
        bluetoothLeScanner.stopScan(scanCallback)
        Log.d("BLE", "Scanning stopped")
    }


    private fun hasPermission(): Boolean {
        return requiredPermission().all {
            ContextCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun requiredPermission(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun applyEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, sysBars.top, 0, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { view, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(0, 0, 0, navBars.bottom)
            insets
        }
    }


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        val allGranted = permission.values.all { it }
        if (allGranted) {
            startTimedScan()
        } else {
            Toast.makeText(
                this,
                "Permission required for Bluetooth",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private val scanCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ScanCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val name = device.name ?: "Unknown"
            val rssi = result.rssi
            val record = result.scanRecord ?: return
            val bytes = record.bytes ?: return

            Log.d(
                "MESH",
                "Mesh device found: $name - ${device.address}, RSSI=${rssi}"
            )
            Log.d(
                "MESH",
                "Mesh device Info: record$record - byte:$bytes"
            )
            if (!scanResults.contains(device)) {
                scanResults.add(device)
                recyclerViewAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BLE", "Scan failed: $errorCode")
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            for (result in results) {
                val device = result.device
                if (!scanResults.contains(device)) {
                    scanResults.add(device)
                    recyclerViewAdapter.notifyItemInserted(scanResults.size - 1)
                }
            }
        }
    }


    fun containsMeshBeacon(scanData: ByteArray): Boolean {
        var index = 0
        while (index < scanData.size) {
            val length = scanData[index].toInt()
            if (length == 0) break

            val type = scanData[index + 1].toInt() and 0xFF
            if (type == 0x2B) {
                return true
            }
            index += length + 1
        }
        return false
    }


    companion object {

        val MESH_PROVISIONING_UUID =
            UUID.fromString("00001827-0000-1000-8000-00805F9B34FB")

        val MESH_PROXY_UUID =
            UUID.fromString("00001828-0000-1000-8000-00805F9B34FB")
    }
}

