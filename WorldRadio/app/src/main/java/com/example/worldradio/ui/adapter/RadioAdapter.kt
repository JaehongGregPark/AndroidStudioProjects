package com.example.worldradio.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
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
 * 방송국 목록 표시
 * Play 버튼 클릭 전달
 * 현재 재생중 표시
 */
class RadioAdapter(
    private val context: Context,
    private val onPlayClick: (RadioStation) -> Unit
) : RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    private var stations: List<RadioStation> = emptyList()

    // 현재 재생중 URL
    private var playingUrl: String? = null

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

        // 로고 이미지 로딩
        Glide.with(context)
            .load(station.favicon)
            .placeholder(R.drawable.ic_radio)
            .error(R.drawable.ic_radio)
            .into(holder.binding.ivLogo)

        // ⭐ 현재 재생 표시
        if (station.url == playingUrl) {
            holder.binding.tvPlaying.visibility = View.VISIBLE
        } else {
            holder.binding.tvPlaying.visibility = View.GONE
        }

        // Play 버튼 클릭
        holder.binding.btnPlay.setOnClickListener {

            playingUrl = station.url
            notifyDataSetChanged()

            onPlayClick(station)
        }
    }

    override fun getItemCount(): Int = stations.size

    fun submitList(list: List<RadioStation>) {

        stations = list
        notifyDataSetChanged()
    }

    // 외부에서 재생 상태 변경
    fun setPlaying(url: String) {

        playingUrl = url
        notifyDataSetChanged()
    }
}