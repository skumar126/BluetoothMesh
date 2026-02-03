package com.mesh.myapplication.adapter

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.RecyclerView
import com.mesh.myapplication.R

class DeviceAdapter(
    private val devices: MutableList<BluetoothDevice>,
   /* private val clickListener: (BluetoothDevice) -> Unit*/
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
        val devices = devices[position]
        holder.deviceName.text = devices.name ?: "Unknown Device"
        holder.deviceAddress.text = devices.address
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById<TextView>(R.id.deviceName)
        val deviceAddress: TextView = itemView.findViewById(R.id.deviceAddress)
    }

    fun updateDevices(newDevices: List<BluetoothDevice>) {
        devices.clear()
        devices.addAll(newDevices)
        notifyDataSetChanged() // Notify RecyclerView to refresh
    }
}