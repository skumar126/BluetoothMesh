package com.mesh.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mesh.myapplication.R
import com.mesh.myapplication.data.MeshNetwork
import com.mesh.myapplication.databinding.ItemMeshNetworkBinding
import java.text.DateFormat
import java.util.Date

class MeshNetworkAdapter(
    private val onItemClick: (MeshNetwork) -> Unit
) : ListAdapter<MeshNetwork, MeshNetworkAdapter.ViewHolder>(Diff()) {

    private val dateFormat: DateFormat =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeshNetworkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMeshNetworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(network: MeshNetwork) {
            val context = binding.root.context
            binding.networkName.text = network.name
            binding.createdDate.text = context.getString(
                R.string.created_date_format,
                dateFormat.format(Date(network.createdAt))
            )
            binding.modifiedDate.text = context.getString(
                R.string.modified_date_format,
                dateFormat.format(Date(network.modifiedAt))
            )
        }
    }

    class Diff : DiffUtil.ItemCallback<MeshNetwork>() {
        override fun areItemsTheSame(oldItem: MeshNetwork, newItem: MeshNetwork) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MeshNetwork, newItem: MeshNetwork) =
            oldItem == newItem
    }
}
