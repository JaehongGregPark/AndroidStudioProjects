package com.example.worldradio.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.databinding.ItemRadioBinding

class RadioAdapter(
    private val onItemClick: (RadioStation) -> Unit
) : RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    // 내부 리스트
    private var stations: List<RadioStation> = emptyList()

    inner class RadioViewHolder(
        val binding: ItemRadioBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewHolder {
        val binding = ItemRadioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RadioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RadioViewHolder, position: Int) {

        val station = stations[position]

        holder.binding.tvName.text = station.name
        holder.binding.tvCountry.text = station.country

        holder.itemView.setOnClickListener {
            onItemClick(station)
        }
    }

    override fun getItemCount(): Int = stations.size

    // 🔥 MainActivity에서 호출하는 함수
    fun submitList(newList: List<RadioStation>) {
        stations = newList
        notifyDataSetChanged()
    }
}