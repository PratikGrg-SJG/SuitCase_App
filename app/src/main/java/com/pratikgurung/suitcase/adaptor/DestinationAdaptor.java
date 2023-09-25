package com.pratikgurung.suitcase.adaptor;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.pratikgurung.suitcase.R;
import com.pratikgurung.suitcase.models.DestinationModel;

public class DestinationAdaptor extends RecyclerView.Adapter<DestinationAdaptor.ViewHolder> {
    private List<DestinationModel> destinations;
    private OnDestinationClickListener onDeleteClickListener;
    private OnDestinationClickListener onUpdateClickListener;
    private OnDestinationClickListener onItemClickListener;


    public interface OnDestinationClickListener {
        void onDeleteClick(int position);
        void onUpdateClick(int position);
        void onItemClick(DestinationModel destination);
    }



    public DestinationAdaptor(List<DestinationModel> destinations, OnDestinationClickListener onDeleteClickListener, OnDestinationClickListener onUpdateClickListener, OnDestinationClickListener onItemClickListener) {
        this.destinations = destinations;
        this.onDeleteClickListener = onDeleteClickListener;
        this.onUpdateClickListener = onUpdateClickListener;
        this.onItemClickListener = onItemClickListener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView destinationNameTextView;
        public TextView noteTextView;
        public TextView selectedDateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            destinationNameTextView = itemView.findViewById(R.id.destinationNameTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            selectedDateTextView = itemView.findViewById(R.id.selectedDateTextView);
        }
    }

    // Method to update the adapter with new data
    public void setData(List<DestinationModel> newData) {
        destinations.clear(); // Clear the existing data
        destinations.addAll(newData); // Add the new data
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.destination_card_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        DestinationModel destination = destinations.get(position);
        holder.destinationNameTextView.setText(destination.getDestinationName());
        holder.noteTextView.setText(destination.getNotes());
        holder.selectedDateTextView.setText(destination.getSelectedDate());

        // Set a long click listener to open the update dialog
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onUpdateClickListener != null) {
                    onUpdateClickListener.onUpdateClick(position);
                }
                return true;
            }
        });

        // Set a click listener to navigate to ItemListActivity for the specific destination
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(destination);
                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return destinations.size();
    }
}
