package com.example.worldradio.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.worldradio.R
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.databinding.ItemRadioBinding

class RadioAdapter(
    private val context: Context,
    private val onItemClick: (RadioStation) -> Unit
) : RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    private var stations: List<RadioStation> = emptyList()

    // 현재 재생 중 URL
    private var currentlyPlayingUrl: String? = null

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

        // Glide로 로고 이미지 로딩
        Glide.with(context)
            .load(station.favicon)
            .placeholder(R.drawable.logo_bg)
            .into(holder.binding.ivLogo)

        // 현재 재생 강조 효과
        if (station.url == currentlyPlayingUrl) {

            holder.binding.root.strokeWidth = 4
            holder.binding.root.strokeColor =
                ContextCompat.getColor(context, R.color.purple_500)

        } else {
            holder.binding.root.strokeWidth = 0
        }

        // 클릭 시 애니메이션
        holder.binding.root.setOnClickListener {

            it.animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .duration = 100
                }

            onItemClick(station)
        }
    }

    override fun getItemCount(): Int = stations.size

    fun submitList(list: List<RadioStation>) {
        stations = list
        notifyDataSetChanged()
    }

    fun setCurrentlyPlaying(url: String?) {
        currentlyPlayingUrl = url
        notifyDataSetChanged()
    }
}