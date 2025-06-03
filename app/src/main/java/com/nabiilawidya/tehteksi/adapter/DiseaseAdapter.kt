package com.nabiilawidya.tehteksi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nabiilawidya.tehteksi.data.Disease
import com.nabiilawidya.tehteksi.databinding.ItemPenyakitBinding

class DiseaseAdapter(
    private val list: List<Disease>,
    private val onItemClick: (Disease) -> Unit
) : RecyclerView.Adapter<DiseaseAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPenyakitBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(list[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPenyakitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvNamaPenyakit.text = item.nama
        holder.binding.tvDeskripsi.text = item.deskripsi
        Glide.with(holder.itemView)
            .load(item.gambar_url)
            .into(holder.binding.imgPenyakit)
    }

    override fun getItemCount(): Int = list.size
}
