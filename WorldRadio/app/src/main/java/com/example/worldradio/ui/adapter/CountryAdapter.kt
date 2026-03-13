package com.example.worldradio.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.worldradio.data.model.Country
import com.example.worldradio.databinding.ItemCountryBinding

class CountryAdapter(
    private val countries: List<Country>,
    private val onClick: (Country) -> Unit
) : RecyclerView.Adapter<CountryAdapter.CountryViewHolder>() {

    inner class CountryViewHolder(
        val binding: ItemCountryBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryViewHolder {

        val binding = ItemCountryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return CountryViewHolder(binding)
    }

    override fun getItemCount(): Int = countries.size

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {

        val country = countries[position]

        holder.binding.tvCountryName.text = country.name

        holder.binding.imgFlag.setImageResource(country.flagRes)

        holder.binding.root.setOnClickListener {
            onClick(country)
        }
    }
}