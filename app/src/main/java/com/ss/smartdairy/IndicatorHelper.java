package com.ss.smartdairy;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class IndicatorHelper {
    private LinearLayout indicatorContainer;
    private Context context;
    private int currentPosition = 0;

    public IndicatorHelper(LinearLayout container, Context context) {
        this.indicatorContainer = container;
        this.context = context;
    }

    public void setupIndicators(int count) {
        indicatorContainer.removeAllViews();

        for (int i = 0; i < count; i++) {
            View dot = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (8 * context.getResources().getDisplayMetrics().density),
                    (int) (8 * context.getResources().getDisplayMetrics().density)
            );
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);

            dot.setBackgroundResource(i == 0 ?
                    R.drawable.indicator_dot_selected :
                    R.drawable.indicator_dot_unselected);

            indicatorContainer.addView(dot);
        }
    }

    public void updateIndicator(int position) {
        if (position == currentPosition) return;

        // Update previous indicator
        if (currentPosition < indicatorContainer.getChildCount()) {
            indicatorContainer.getChildAt(currentPosition)
                    .setBackgroundResource(R.drawable.indicator_dot_unselected);
        }

        // Update current indicator
        if (position < indicatorContainer.getChildCount()) {
            indicatorContainer.getChildAt(position)
                    .setBackgroundResource(R.drawable.indicator_dot_selected);
        }

        currentPosition = position;
    }
}

