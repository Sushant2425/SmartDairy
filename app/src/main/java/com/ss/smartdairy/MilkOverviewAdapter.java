package com.ss.smartdairy;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class MilkOverviewAdapter extends RecyclerView.Adapter<MilkOverviewAdapter.ViewHolder> {

    private static final String TAG = "MilkOverviewAdapter";
    private List<MilkSummaryModel> milkSummaries;



    public MilkOverviewAdapter(List<MilkSummaryModel> milkSummaries) {
        this.milkSummaries = milkSummaries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_milk_overview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MilkSummaryModel summary = milkSummaries.get(position);
        holder.bind(summary);
    }

    @Override
    public int getItemCount() {
        return milkSummaries.size();
    }

    public void updateData(List<MilkSummaryModel> newData) {
        this.milkSummaries = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate, tvCowMilk, tvBuffaloMilk, tvTotalMilk;
        private Spinner spinnerSession;
        private MilkSummaryModel currentSummary;
        private boolean isUpdatingSpinner = false;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCowMilk = itemView.findViewById(R.id.tvCowMilk);
            tvBuffaloMilk = itemView.findViewById(R.id.tvBuffaloMilk);
            tvTotalMilk = itemView.findViewById(R.id.tvTotalMilk);
            spinnerSession = itemView.findViewById(R.id.spinnerSession);

            setupSpinner();
        }

        private void setupSpinner() {
            // Create session options
            String[] sessions = {"All", "Morning", "Evening"};

            // Create adapter for spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    itemView.getContext(),
                    android.R.layout.simple_spinner_item,
                    sessions
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    textView.setTextSize(12f);
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView textView = (TextView) view;
                    textView.setTextSize(14f);
                    return view;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerSession.setAdapter(adapter);

            // Set up spinner listener
            spinnerSession.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!isUpdatingSpinner && currentSummary != null) {
                        updateDisplayForSession(position);
                        Log.d(TAG, "Session selected: " + position + " for date: " + currentSummary.getDate());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        }

        private void updateDisplayForSession(int sessionPosition) {
            if (currentSummary == null) {
                Log.w(TAG, "Current summary is null");
                return;
            }

            String cowMilk, buffaloMilk, totalMilk;

            Log.d(TAG, "Updating display for session position: " + sessionPosition);
            Log.d(TAG, "Available sessions: " + currentSummary.getAllSessionData().keySet().toString());

            switch (sessionPosition) {
                case 1: // Morning
                    MilkSummaryModel.SessionData morningData = currentSummary.getSessionData("morning");
                    if (morningData != null) {
                        cowMilk = String.format(Locale.getDefault(), "%.1f", morningData.getCowMilk());
                        buffaloMilk = String.format(Locale.getDefault(), "%.1f", morningData.getBuffaloMilk());
                        totalMilk = String.format(Locale.getDefault(), "%.1f", morningData.getTotalMilk());
                        Log.d(TAG, "Morning data found - Cow: " + cowMilk + ", Buffalo: " + buffaloMilk);
                    } else {
                        cowMilk = buffaloMilk = totalMilk = "0.0";
                        Log.d(TAG, "No morning data available");
                    }
                    break;

                case 2: // Evening
                    MilkSummaryModel.SessionData eveningData = currentSummary.getSessionData("evening");
                    if (eveningData != null) {
                        cowMilk = String.format(Locale.getDefault(), "%.1f", eveningData.getCowMilk());
                        buffaloMilk = String.format(Locale.getDefault(), "%.1f", eveningData.getBuffaloMilk());
                        totalMilk = String.format(Locale.getDefault(), "%.1f", eveningData.getTotalMilk());
                        Log.d(TAG, "Evening data found - Cow: " + cowMilk + ", Buffalo: " + buffaloMilk);
                    } else {
                        cowMilk = buffaloMilk = totalMilk = "0.0";
                        Log.d(TAG, "No evening data available");
                    }
                    break;

                case 0: // All
                default:
                    cowMilk = currentSummary.getCowMilk();
                    buffaloMilk = currentSummary.getBuffaloMilk();
                    totalMilk = currentSummary.getTotalMilk();
                    Log.d(TAG, "All data - Cow: " + cowMilk + ", Buffalo: " + buffaloMilk);
                    break;
            }

            // Update the display
            tvCowMilk.setText(cowMilk + " L");
            tvBuffaloMilk.setText(buffaloMilk + " L");
            tvTotalMilk.setText(totalMilk + " L");

            Log.d(TAG, "Display updated - Cow: " + cowMilk + "L, Buffalo: " + buffaloMilk + "L, Total: " + totalMilk + "L");
        }

        public void bind(MilkSummaryModel summary) {
            this.currentSummary = summary;
            isUpdatingSpinner = true; // Prevent listener from firing during setup

            tvDate.setText(summary.getDate());

            // Debug logging
            Log.d(TAG, "Binding data for: " + summary.getDate());
            Log.d(TAG, "Has data: " + summary.hasData());

            if (summary.hasData()) {
                Log.d(TAG, "Session data available:");
                for (String session : summary.getAllSessionData().keySet()) {
                    MilkSummaryModel.SessionData sessionData = summary.getSessionData(session);
                    if (sessionData != null) {
                        Log.d(TAG, "  " + session + ": Cow=" + sessionData.getCowMilk() +
                                ", Buffalo=" + sessionData.getBuffaloMilk() +
                                ", Total=" + sessionData.getTotalMilk());
                    }
                }
            } else {
                Log.d(TAG, "No session data available");
            }

            // Reset spinner to "All" and update display
            spinnerSession.setSelection(0, false);
            updateDisplayForSession(0);

            // Change appearance based on data availability
            if (!summary.hasData()) {
                itemView.setAlpha(0.6f);
                tvDate.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                spinnerSession.setEnabled(false);
            } else {
                itemView.setAlpha(1.0f);
                tvDate.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
                spinnerSession.setEnabled(true);
            }

            isUpdatingSpinner = false; // Re-enable listener
        }
    }
}
