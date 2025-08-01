package com.nabiilawidya.tehteksi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nabiilawidya.tehteksi.data.SolusiItem
import com.nabiilawidya.tehteksi.databinding.ItemSolusiBinding

class SolusiAdapter(private val items: List<SolusiItem>) :
    RecyclerView.Adapter<SolusiAdapter.SolusiViewHolder>() {

    inner class SolusiViewHolder(val binding: ItemSolusiBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SolusiViewHolder {
        val binding = ItemSolusiBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SolusiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SolusiViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvJudul.text = item.judul
        holder.binding.tvDetail.text = item.detail
    }

    override fun getItemCount(): Int = items.size
}
