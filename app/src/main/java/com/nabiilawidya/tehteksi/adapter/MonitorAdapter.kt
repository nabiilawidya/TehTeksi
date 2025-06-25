package com.nabiilawidya.tehteksi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nabiilawidya.tehteksi.data.Classification
import com.nabiilawidya.tehteksi.databinding.ItemMonitorBinding

class MonitorAdapter(
    private val list: List<Classification>,
    private val onItemClick: (Classification) -> Unit
) : RecyclerView.Adapter<MonitorAdapter.MonitorViewHolder>() {

    inner class MonitorViewHolder(val binding: ItemMonitorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonitorViewHolder {
        val binding = ItemMonitorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonitorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonitorViewHolder, position: Int) {
        val item = list[position]
        val b = holder.binding

        b.tvHistoriPenyakit.text = "${item.label} (${String.format("%.2f", item.confidence)}%)"
        b.tvUser.text = item.userName
        b.tvTimestamp.text = item.timestamp

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(b.imgHistory)

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

    }

    override fun getItemCount(): Int = list.size
}

