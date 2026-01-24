package com.example.myradioapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// RecyclerView.Adapter를 상속받습니다.
public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationViewHolder> {
    private List<RadioStation> stationList;
    private OnItemClickListener listener; // 클릭 이벤트 처리를 위한 인터페이스

    // 1. 클릭 리스너 인터페이스 정의 (Callback)
    public interface OnItemClickListener {
        void onItemClick(RadioStation station);
    }

    // 생성자: 데이터와 리스너를 받습니다.
    public StationAdapter(List<RadioStation> list, OnItemClickListener listener) {
        this.stationList = list;
        this.listener = listener;
    }

    // 2. 화면(XML)을 생성해서 홀더에 담는 과정
    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_station, parent, false);
        return new StationViewHolder(view);
    }

    // 3. 생성된 홀더에 실제 데이터를 연결(Binding)하는 과정
    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        RadioStation station = stationList.get(position);
        holder.tvName.setText(station.getName());

        // 아이템 클릭 시 동작 설정
        holder.itemView.setOnClickListener(v -> listener.onItemClick(station));
    }

    // 데이터 개수 반환
    @Override
    public int getItemCount() {
        return stationList.size();
    }

    // 4. 뷰 홀더: 화면 요소(TextView 등)를 잡아두는 역할
    static class StationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public StationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_station_name);
        }
    }
}
