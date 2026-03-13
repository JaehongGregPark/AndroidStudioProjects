package com.example.worldradio.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.worldradio.R
import com.example.worldradio.data.model.RadioStation
import com.example.worldradio.databinding.ItemRadioBinding

/**
 * RecyclerView Adapter
 *
 * 역할
 * 1️⃣ 방송국 목록 표시
 * 2️⃣ Play 버튼 클릭 전달
 * 3️⃣ 현재 재생중 표시
 * 4️⃣ 즐겨찾기 버튼 처리
 */
class RadioAdapter(
    private val context: Context,

    // ▶ 라디오 재생 클릭
    private val onPlayClick: (RadioStation) -> Unit,

    // ⭐ 즐겨찾기 버튼 클릭
    private val onFavoriteClick: (RadioStation) -> Unit

) : RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    // 방송국 목록
    private var stations: List<RadioStation> = emptyList()

    // 현재 재생중 URL
    private var playingUrl: String? = null

    // 즐겨찾기 URL 목록
    private var favoriteUrls: Set<String> = emptySet()

    inner class RadioViewHolder(
        val binding: ItemRadioBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RadioViewHolder {

        val binding = ItemRadioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return RadioViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: RadioViewHolder,
        position: Int
    ) {

        val station = stations[position]

        // 방송국 이름
        holder.binding.tvName.text = station.name

        // 방송국 로고
        Glide.with(context)
            .load(station.favicon)
            .placeholder(R.drawable.ic_radio)
            .error(R.drawable.ic_radio)
            .into(holder.binding.ivLogo)

        /**
         * ⭐ 현재 재생중 표시
         */
        if (station.urlResolved == playingUrl) {
            holder.binding.tvPlaying.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.tvPlaying.visibility = android.view.View.GONE
        }

        /**
         * ⭐ 즐겨찾기 상태 표시
         */
        if (favoriteUrls.contains(station.urlResolved)) {

            holder.binding.btnFavorite.setImageResource(
                R.drawable.ic_favorite
            )

        } else {

            holder.binding.btnFavorite.setImageResource(
                R.drawable.ic_favorite_border
            )
        }

        /**
         * ▶ Play 버튼 클릭
         */
        holder.binding.btnPlay.setOnClickListener {

            playingUrl = station.urlResolved

            notifyDataSetChanged()

            onPlayClick(station)
        }

        /**
         * ⭐ 즐겨찾기 버튼 클릭
         */
        holder.binding.btnFavorite.setOnClickListener {

            onFavoriteClick(station)
        }
    }

    override fun getItemCount(): Int = stations.size

    /**
     * 리스트 업데이트
     */
    fun submitList(list: List<RadioStation>) {

        stations = list

        notifyDataSetChanged()
    }

    /**
     * 외부에서 재생 상태 변경
     */
    fun setPlaying(url: String) {

        playingUrl = url

        notifyDataSetChanged()
    }

    /**
     * 즐겨찾기 목록 업데이트
     */
    fun setFavorites(urls: Set<String>) {

        favoriteUrls = urls

        notifyDataSetChanged()
    }
}