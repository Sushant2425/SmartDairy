package com.ss.smartdairy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FarmersAdapter extends RecyclerView.Adapter<FarmersAdapter.FarmerViewHolder> {

    private List<Farmer> farmersList;
    private OnFarmerClickListener listener;

    public interface OnFarmerClickListener {
        void onEditClick(Farmer farmer);
        void onCallClick(Farmer farmer);
    }

    public FarmersAdapter(List<Farmer> farmersList, OnFarmerClickListener listener) {
        this.farmersList = farmersList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FarmerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_farmer, parent, false);
        return new FarmerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FarmerViewHolder holder, int position) {
        Farmer farmer = farmersList.get(position);
        holder.bind(farmer, listener);
    }

    @Override
    public int getItemCount() {
        return farmersList.size();
    }

    static class FarmerViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvFarmerName, tvFarmerMobile, tvFarmerAddress;
        private TextView tvAnimals, tvMilkCodes;
        private ImageButton btnEdit, btnCall;

        public FarmerViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardFarmer);
            tvFarmerName = itemView.findViewById(R.id.tvFarmerName);
            tvFarmerMobile = itemView.findViewById(R.id.tvFarmerMobile);
            tvFarmerAddress = itemView.findViewById(R.id.tvFarmerAddress);
            tvAnimals = itemView.findViewById(R.id.tvAnimals);
            tvMilkCodes = itemView.findViewById(R.id.tvMilkCodes);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnCall = itemView.findViewById(R.id.btnCall);
        }

        public void bind(Farmer farmer, OnFarmerClickListener listener) {
            tvFarmerName.setText(farmer.getName());
            tvFarmerMobile.setText(farmer.getMobile());
            tvFarmerAddress.setText(farmer.getAddress().isEmpty() ? "Address not provided" : farmer.getAddress());
            tvAnimals.setText(farmer.getAnimalsText());
            tvMilkCodes.setText(farmer.getMilkCodesText());

            // Set click listeners
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(farmer);
                }
            });

            btnCall.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCallClick(farmer);
                }
            });

            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(farmer);
                }
            });
        }
    }
}
