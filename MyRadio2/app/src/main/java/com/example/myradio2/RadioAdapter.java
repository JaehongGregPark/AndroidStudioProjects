package com.example.myradio2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RadioAdapter extends RecyclerView.Adapter<RadioAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(RadioStation station);
    }

    private List<RadioStation> radioList;
    private OnItemClickListener listener;

    public RadioAdapter(List<RadioStation> radioList, OnItemClickListener listener) {
        this.radioList = radioList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_radio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RadioStation station = radioList.get(position);
        holder.bind(station, listener);
    }

    @Override
    public int getItemCount() {
        return radioList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtCountry;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtCountry = itemView.findViewById(R.id.txtCountry);
        }

        public void bind(final RadioStation station, final OnItemClickListener listener) {
            txtName.setText(station.getName());
            txtCountry.setText(station.getCountry());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(station);
                }
            });
        }
    }
}