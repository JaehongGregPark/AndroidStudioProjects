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
 * 방송국 목록 표시
 * Play 버튼 클릭 전달
 */
class RadioAdapter(
    private val context: Context,
    private val onPlayClick: (RadioStation) -> Unit
) : RecyclerView.Adapter<RadioAdapter.RadioViewHolder>() {

    private var stations: List<RadioStation> = emptyList()

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

        // 로고 이미지
        Glide.with(context)
            .load(station.favicon)
            .placeholder(R.drawable.ic_radio)   // 로딩 중
            .error(R.drawable.ic_radio)         // 로고 없을 때
            .into(holder.binding.ivLogo)

        // Play 버튼 클릭
        holder.binding.btnPlay.setOnClickListener {

            onPlayClick(station)
        }
    }

    override fun getItemCount(): Int = stations.size

    fun submitList(list: List<RadioStation>) {

        stations = list

        notifyDataSetChanged()
    }
}