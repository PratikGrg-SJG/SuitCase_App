package com.pratikgurung.suitcase;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import models.DestinationModel;

public class DestinationAdaptor extends RecyclerView.Adapter<DestinationAdaptor.ViewHolder> {
    private List<DestinationModel> destinations;

    public DestinationAdaptor(List<DestinationModel> destinations) {
        this.destinations = destinations;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DestinationModel destination = destinations.get(position);
        holder.destinationNameTextView.setText(destination.getDestinationName());
        holder.noteTextView.setText(destination.getNotes());
        holder.selectedDateTextView.setText(destination.getSelectedDate());
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }
}
