package com.pratikgurung.suitcase.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pratikgurung.suitcase.R;
import com.pratikgurung.suitcase.models.ItemModel;

import java.util.List;

public class ItemAdaptor extends RecyclerView.Adapter<ItemAdaptor.ItemViewHolder> {

    private List<ItemModel> itemList;

    public ItemAdaptor(List<ItemModel> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel item = itemList.get(position);
        holder.itemNameTextView.setText(item.getItemName());
        holder.itemCheckBox.setChecked(item.isPurchased());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox itemCheckBox;
        TextView itemNameTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemCheckBox = itemView.findViewById(R.id.itemCheckBox);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
        }
    }
}
