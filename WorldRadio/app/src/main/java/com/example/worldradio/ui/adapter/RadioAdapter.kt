package com.example.worldradio.ui.adapter

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.worldradio.data.model.RadioStation

/**
 * RadioAdapter
 *
 * RecyclerView에 라디오 방송 목록을 표시하는 Adapter 클래스
 *
 * ListAdapter를 상속하여:
 *  - DiffUtil을 자동으로 사용
 *  - 리스트 변경 시 최소한의 UI 갱신만 수행 (성능 최적화)
 *
 * @param onClick 사용자가 항목을 클릭했을 때 실행될 콜백 함수
 */
class RadioAdapter(

    // 아이템 클릭 시 실행할 람다 함수
    // Activity에서 playRadio() 같은 함수를 전달받음
    private val onClick: (RadioStation) -> Unit

) : ListAdapter<RadioStation, RadioAdapter.RadioViewHolder>(DIFF_CALLBACK) {


    /**
     * DiffUtil
     *
     * 기존 리스트와 새 리스트를 비교해서
     * 무엇이 바뀌었는지 계산해주는 객체
     *
     * notifyDataSetChanged() 대신
     * 변경된 부분만 갱신하므로 성능이 매우 좋음
     */
    companion object {

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RadioStation>() {

            /**
             * 두 항목이 "같은 항목"인지 비교
             * (고유 ID 비교)
             */
            override fun areItemsTheSame(
                oldItem: RadioStation,
                newItem: RadioStation
            ): Boolean {
                return oldItem.stationuuid == newItem.stationuuid
            }

            /**
             * 내용이 동일한지 비교
             * (UI를 다시 그릴 필요가 있는지 판단)
             */
            override fun areContentsTheSame(
                oldItem: RadioStation,
                newItem: RadioStation
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


    /**
     * ViewHolder 클래스
     *
     * RecyclerView는 View를 재사용한다.
     * 이 ViewHolder가 각 아이템 View를 보관한다.
     */
    class RadioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // 기본 simple_list_item_1 레이아웃의 TextView
        val txtName: TextView =
            itemView.findViewById(R.id.text1)
    }


    /**
     * ViewHolder 생성
     *
     * 스크롤 시 새 아이템 View가 필요할 때 호출됨
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RadioViewHolder {

        // XML 레이아웃을 View 객체로 변환
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.simple_list_item_1, // 기본 제공 레이아웃
                parent,
                false
            )

        return RadioViewHolder(view)
    }


    /**
     * 데이터 바인딩
     *
     * 화면에 표시될 데이터를 View에 연결하는 부분
     */
    override fun onBindViewHolder(
        holder: RadioViewHolder,
        position: Int
    ) {

        // 현재 위치의 아이템 가져오기
        val station = getItem(position)

        // 방송 이름 + 국가 표시
        holder.txtName.text =
            "${station.name} (${station.country})"

        // 클릭 이벤트 처리
        holder.itemView.setOnClickListener {

            // 외부(Activity)에서 전달한 클릭 콜백 실행
            onClick(station)
        }
    }
}