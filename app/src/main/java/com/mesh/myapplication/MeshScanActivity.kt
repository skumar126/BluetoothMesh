package com.mesh.myapplication

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mesh.myapplication.adapter.DeviceAdapter
import com.mesh.myapplication.databinding.ActivityMeshScanBinding

class MeshScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMeshScanBinding

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val scanResults = mutableListOf<ScanResult>()
    private lateinit var recyclerViewAdapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeshScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        applyEdgeToEdge()

        val networkName = intent.getStringExtra(EXTRA_NETWORK_NAME)
        if (!networkName.isNullOrBlank()) {
            binding.toolbar.title = networkName
            binding.title.text = networkName
        }

        val bluetoothManager =
            getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        checkAnsRequestPermission()

        recyclerViewAdapter = DeviceAdapter(scanResults) { clickedItem ->
            Log.d("RV_CLICK", "Clicked: ${clickedItem.device.address}")
            Toast.makeText(this, "Clicked: ${clickedItem.device.address}", Toast.LENGTH_SHORT).show()
            val isUnprovisioned = clickedItem.scanRecord?.serviceUuids?.any {
                it.uuid == Constants.MESH_PROVISIONING_UUID
            } == true
            if (isUnprovisioned) {
                Toast.makeText(this, "Clicked: Provision", Toast.LENGTH_SHORT).show()
            }
        }
        binding.deviceList.layoutManager = LinearLayoutManager(this)
        binding.deviceList.adapter = recyclerViewAdapter
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun checkAnsRequestPermission() {
        if (!hasPermission()) {
            permissionLauncher.launch(requiredPermission())
        } else {
            ensureBluetoothEnabled()
        }
    }

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startTimedScan() {
        startBluetooth()
        handler.postDelayed({ stopBleScan() }, 10_000)
    }

    private fun ensureBluetoothEnabled() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            Toast.makeText(
                this,
                getString(R.string.bluetooth_not_supported),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (adapter.isEnabled) {
            startTimedScan()
        } else {
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startBluetooth() {
        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            Toast.makeText(
                this,
                getString(R.string.bluetooth_required),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        bluetoothLeScanner = scanner

        val meshProvisioningFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(Constants.MESH_PROVISIONING_UUID))
            .build()

        val meshProxyFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(Constants.MESH_PROXY_UUID))
            .build()
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(
            listOf(meshProvisioningFilter, meshProxyFilter), scanSettings, scanCallback
        )

        Log.d("BLE", "Scanning started")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun stopBleScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
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
            ensureBluetoothEnabled()
        } else {
            Toast.makeText(
                this,
                "Permission required for Bluetooth",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (bluetoothAdapter?.isEnabled == true) {
            startTimedScan()
        } else {
            Toast.makeText(
                this,
                getString(R.string.bluetooth_required),
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
            if (!scanResults.contains(result)) {
                scanResults.add(result)
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
                if (!scanResults.contains(result)) {
                    scanResults.add(result)
                    recyclerViewAdapter.notifyItemInserted(scanResults.size - 1)
                }
            }
        }
    }

    companion object {
        const val EXTRA_NETWORK_NAME = "extra_network_name"
    }
}
