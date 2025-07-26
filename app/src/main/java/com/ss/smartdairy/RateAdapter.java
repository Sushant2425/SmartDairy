package com.ss.smartdairy;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RateAdapter extends RecyclerView.Adapter<RateAdapter.RateViewHolder> {

    private final List<RateModel> rateList;
    private final boolean isEditable;

    public RateAdapter(List<RateModel> rateList, boolean isEditable) {
        this.rateList = rateList;
        this.isEditable = isEditable;
    }

    public List<RateModel> getUpdatedRates() {
        return rateList;
    }

    @NonNull
    @Override
    public RateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rate_row, parent, false);
        return new RateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RateViewHolder holder, int position) {
        RateModel model = rateList.get(position);

        holder.textFat.setText("FAT " + model.getFat());

        holder.editRate.setTag(null); // Remove previous watcher tag
        holder.editRate.setText(String.valueOf(model.getRate()));

        // 🔐 Enable or Disable Editing based on today's date
        holder.editRate.setEnabled(isEditable);

        holder.editRate.setTag(model);
        holder.editRate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                RateModel tagModel = (RateModel) holder.editRate.getTag();
                if (tagModel != null) {
                    try {
                        double rate = Double.parseDouble(s.toString());
                        tagModel.setRate(rate);
                    } catch (NumberFormatException e) {
                        tagModel.setRate(0);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return rateList.size();
    }

    static class RateViewHolder extends RecyclerView.ViewHolder {
        TextView textFat;
        EditText editRate;

        RateViewHolder(@NonNull View itemView) {
            super(itemView);
            textFat = itemView.findViewById(R.id.textFat);
            editRate = itemView.findViewById(R.id.editRate);
        }
    }
}
