package com.ss.smartdairy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnAddFarmer = view.findViewById(R.id.btnAddFarmer);
        btnAddFarmer.setOnClickListener(v -> {
            // Launch AddFarmersActivity
            Intent intent = new Intent(getActivity(), AddFarmersActivity.class);
            startActivity(intent);
        });

        Button btnMilkCollection = view.findViewById(R.id.btnMilkCollection);
        btnMilkCollection.setOnClickListener(v -> {
            // Launch MilkCollectionActivity
            Intent intent = new Intent(getActivity(), MilkCollectionActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
