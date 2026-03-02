package com.example.worldradio.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.worldradio.R
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.databinding.ItemRadioBinding

class RadioAdapter(
    private val onItemClick: (RadioStation) -> Unit
) : RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    private var stations: List<RadioStation> = emptyList()
    private val favorites = mutableSetOf<String>() // ❤️ 즐겨찾기 저장

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

        // 🖼 로고 로딩
        Glide.with(holder.itemView.context)
            .load(station.favicon)   // API에 favicon 필드 있어야 함
            .placeholder(R.drawable.logo_bg)
            .into(holder.binding.ivLogo)

        // ❤️ 즐겨찾기 상태 표시
        val isFavorite = favorites.contains(station.url)

        holder.binding.ivFavorite.setImageResource(
            if (isFavorite) R.drawable.ic_favorite
            else R.drawable.ic_favorite_border
        )

        // ❤️ 즐겨찾기 클릭
        holder.binding.ivFavorite.setOnClickListener {

            if (isFavorite) {
                favorites.remove(station.url)
            } else {
                favorites.add(station.url)
            }

            notifyItemChanged(position)
        }

        // 🔘 카드 클릭 애니메이션 포함
        holder.binding.root.setOnClickListener {
            holder.binding.root.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
                .setDuration(100)
                .withEndAction {
                    holder.binding.root.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .duration = 100
                }

            onItemClick(station)
        }
    }

    override fun getItemCount(): Int = stations.size

    fun submitList(newList: List<RadioStation>) {
        stations = newList
        notifyDataSetChanged()
    }
}