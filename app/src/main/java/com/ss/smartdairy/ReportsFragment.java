package com.ss.smartdairy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ReportsFragment extends Fragment {

    public ReportsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        // 🔗 Rate Chart Report
        TextView txtRateChart = view.findViewById(R.id.textRateChartReport);
        txtRateChart.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), RateChartReportActivity.class);
            startActivity(i);
        });

        // 🔗 Farmers Report
        TextView txtFarmersReport = view.findViewById(R.id.textFarmersReport);
        txtFarmersReport.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), FarmerReportActivity.class); // 🔄 You will create this Activity
            startActivity(i);
        });

        return view;
    }
}
