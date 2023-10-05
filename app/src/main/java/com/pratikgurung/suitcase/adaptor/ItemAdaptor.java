package com.pratikgurung.suitcase.adaptor;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pratikgurung.suitcase.R;
import com.pratikgurung.suitcase.models.ItemModel;
import java.util.List;

public class ItemAdaptor extends RecyclerView.Adapter<ItemAdaptor.ItemViewHolder> {

    private List<ItemModel> itemList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    // Interface to handle item clicks
    public interface OnItemClickListener {
        void onItemClick(ItemModel item);
    }

    public ItemAdaptor(List<ItemModel> itemList, Context context, OnItemClickListener onItemClickListener) {
        this.itemList = itemList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
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

        // Set a click listener for the item
        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item);
            }
        });


        // Update the text appearance based on purchase status
        if (item.isPurchased()) {
            // If purchased, set the text to strike-through and gray color
            holder.itemNameTextView.setPaintFlags(holder.itemNameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemNameTextView.setTextColor(ContextCompat.getColor(context, R.color.md_theme_dark_outline)); // Set your gray color resource
        } else {
            // If not purchased, remove strike-through and set the default color
            holder.itemNameTextView.setPaintFlags(holder.itemNameTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemNameTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black)); // Set your default text color
        }

        holder.itemCheckBox.setOnClickListener(view -> {
            holder.itemCheckBox.setChecked(item.isPurchased());

            // Handle checkbox click
            if(item.isPurchased()){
                showUndoDialog(item, position, holder);
            }else{

                showConfirmationDialog(item, position, holder);
            }

        });



    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }



    private void showConfirmationDialog(ItemModel item, int position, ItemViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Mark item as purchased?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Set the purchased status to true
            item.setPurchased(true);
            // Notify the adapter that the item has changed
            notifyItemChanged(position);
            updateDatabase(item, position, item.getItemDocumentId());
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnDismissListener(dialog -> {
            // Do something on dialog dismiss if needed
        });

        alertDialog.show();
    }

    private void showUndoDialog(ItemModel item, int position, ItemViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Undo item purchase?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Set the purchased status to true
            item.setPurchased(false);
            // Notify the adapter that the item has changed
            notifyItemChanged(position);
            updateDatabase(item, position, item.getItemDocumentId());
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnDismissListener(dialog -> {
            // Do something on dialog dismiss if needed
        });

        alertDialog.show();
    }


    private void updateDatabase(ItemModel item, int position, String documentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference itemsCollection = firestore.collection("items");

        itemsCollection.document(documentId)
                .update("purchased", item.isPurchased())
                .addOnSuccessListener(aVoid -> {
                    // Check if the position is valid before updating the list
                    if (position >= 0 && position < itemList.size()) {
                        // Update the item in the local list
                        itemList.get(position).setPurchased(item.isPurchased());
                        Log.d("ItemAdaptor", "Document successfully updated!");
                    } else {
                        Log.e("ItemAdaptor", "Invalid position: " + position);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("ItemAdaptor", "Error updating document", e);
                    // Handle the error
                });
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