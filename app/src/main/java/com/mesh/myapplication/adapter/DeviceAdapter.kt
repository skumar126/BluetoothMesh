package com.mesh.myapplication.adapter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mesh.myapplication.MainActivity.Companion.MESH_PROVISIONING_UUID
import com.mesh.myapplication.R

class DeviceAdapter(
    private val devices: MutableList<ScanResult>,
    private val onItemClick: (ScanResult) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_item_layout, parent, false)
        return DeviceViewHolder(view)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(
        holder: DeviceViewHolder,
        position: Int
    ) {
        val scannedDevice = devices[position]
        holder.deviceName.text = scannedDevice.device.name ?: "Unknown Device"
        holder.deviceAddress.text = scannedDevice.device.address
        holder.deviceRssi.text = "RSSI :" + scannedDevice.rssi
        val serviceUUID = scannedDevice.scanRecord?.serviceUuids
        val isUnprovisioned = serviceUUID?.any {
            it.uuid == MESH_PROVISIONING_UUID
        } == true
        val status = if (isUnprovisioned) "UNPROVISIONED" else "PROVISIONED"
        val col = if (isUnprovisioned) R.color.green else R.color.red
        holder.devicesStatus.text = "Status: " + status
        holder.devicesStatus.setTextColor(ContextCompat.getColor(holder.itemView.context, col))

        holder.deviceParent.setOnClickListener {
            
        }

    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceParent: View = itemView.findViewById(R.id.deviceParent)
        val deviceName: TextView = itemView.findViewById<TextView>(R.id.deviceName)
        val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress)
        val deviceRssi: TextView = itemView.findViewById(R.id.deviceRssi)
        val devicesStatus: TextView = itemView.findViewById(R.id.deviceStatus)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(itemView[position])
                }
            }
        }
    }

    fun updateDevices(newDevices: List<ScanResult>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged() // Notify RecyclerView to refresh
    }
}