package com.mesh.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.text.LinkAnnotation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MeshDeviceAdapter(
   // private val onClick: (MeshDevice) -> Unit
) :
    ListAdapter<MeshDevice, MeshDeviceAdapter.ViewHolder>(Diff()) {
    private lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val device = getItem(position)
        holder.view.findViewById<TextView>(android.R.id.text1).text =
            device.name ?: context.getString(R.string.device_name)
        holder.view.findViewById<TextView>(android.R.id.text2).text =
            "${device.address}  RSSI:${device.rssi}"
//        holder.view.setOnClickListener {
//            onClick(device)
//        }
    }

    class ViewHolder(
        val view: View
    ) : RecyclerView.ViewHolder(view)

    class Diff : DiffUtil.ItemCallback<MeshDevice>() {
        override fun areItemsTheSame(
            oldItem: MeshDevice,
            newItem: MeshDevice
        ) =
            oldItem.address == newItem.address


        override fun areContentsTheSame(
            oldItem: MeshDevice,
            newItem: MeshDevice
        ) = oldItem == newItem

    }

}