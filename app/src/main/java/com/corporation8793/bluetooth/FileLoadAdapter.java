package com.corporation8793.bluetooth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FileLoadAdapter extends RecyclerView.Adapter<FileLoadAdapter.ItemViewHolder> {

    // adapter에 들어갈 list 입니다.
    private List<String> listData;
    Context context;

    public interface OnItemClickEventListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickEventListener mItemClickListener;

    public void setOnItemClickListener(OnItemClickEventListener a_listener) {
        mItemClickListener = a_listener;
    }

    public FileLoadAdapter(Context context, List<String> arrayList){
        listData = arrayList;
        this.context = context;

        Log.e("file", listData.size()+"");
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_list_itemview, parent, false);
        view.getLayoutParams().height = 80;
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        // Item을 하나, 하나 보여주는(bind 되는) 함수입니다.
        Log.e("file list check",listData.get(position));
        holder.file_name_area.setText(listData.get(position));
    }

    @Override
    public int getItemCount() {
        // RecyclerView의 총 개수 입니다.
        return listData.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView file_name_area;

        ItemViewHolder(View itemView) {
            super(itemView);

            file_name_area = itemView.findViewById(R.id.file_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION) {
                        if(mItemClickListener != null) {
                            mItemClickListener.onItemClick(v, position);
                        }
                    }
                }
            });
        }
    }
}