package com.nabiilawidya.tehteksi.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nabiilawidya.tehteksi.data.History
import com.nabiilawidya.tehteksi.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    initialList: List<History>,
    private val onItemClick: (History) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val list = initialList.toMutableList()

    inner class HistoryViewHolder(val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = list[position]
        with(holder.binding) {
            tvHistoriPenyakit.text = item.label

            val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault())
            val dateText = item.timestamp?.toDate()?.let { sdf.format(it) } ?: "No Date"
            tvTimestamp.text = dateText

            Glide.with(root.context)
                .load(item.imageUrl)
                .into(imgHistory)

            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    fun updateData(newList: List<History>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount() = list.size
}
