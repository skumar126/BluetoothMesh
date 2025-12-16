package com.mesh.myapplication

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mesh.myapplication.databinding.FragmentMeshProvisioningBinding
import java.util.UUID
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * A simple [Fragment] subclass.
 * Use the [MeshProvisioningFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MeshProvisioningFragment : Fragment() {

    private lateinit var scanner: BluetoothLeScanner
    private val adapter = MeshDeviceAdapter()
    private val devices = mutableMapOf<String, MeshDevice>()
    private lateinit var binding: FragmentMeshProvisioningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMeshProvisioningBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndRequestBluetoothPermission()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MeshProvisioningFragment.adapter
        }

        binding.btnScan.setOnClickListener {
            startMeshScanning()
        }
    }

    private fun checkAndRequestBluetoothPermission() {
        if (hasBluetoothPermission()) {
            Toast.makeText(requireContext(), "Start Scan", Toast.LENGTH_SHORT).show()
        } else {
            permissionLauncher.launch(bluetoothPermissions())
        }
    }

    private fun hasBluetoothPermission(): Boolean =
        bluetoothPermissions().all {
            ContextCompat.checkSelfPermission(
                requireContext(), it
            ) == PackageManager.PERMISSION_GRANTED
        }

    private fun bluetoothPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        }else{
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {

        }else {
            showPermissionDeniedDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scanner.stopScan(scanCallBack)
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Bluetooth Permission Required")
            .setMessage("Bluetooth permission is required to scan mesh devices.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().packageName, null)
                )
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun startMeshScanning() {
        devices.clear()
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(MESH_PROVISIONING_UUID))
            .build()

        val setting = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(
            listOf(filter),
            setting, scanCallBack
        )
    }


    private val scanCallBack = object : ScanCallback() {
        override fun onScanResult(type: Int, result: ScanResult) {
            val device = result.device
            val meshDevice = MeshDevice(
                device.name,
                device.address,
                result.rssi
            )
            Log.d("MESH","Scan device: ${device.name}")
            devices[device.address] = meshDevice
            adapter.submitList(devices.values.toList())
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("MESH", "Scan failed: $errorCode")
        }
    }

    companion object {
        private val MESH_PROVISIONING_UUID =
            UUID.fromString("00001827-0000-1000-8000-00805F9B34FB")

        @JvmStatic
        fun newInstance() =
            MeshProvisioningFragment().apply {
            }
    }
}