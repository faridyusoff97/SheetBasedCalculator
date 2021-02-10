package com.mmu.fyp.sheetbasedcalculator.calculator;

// import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
// import androidx.core.view.ViewCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;


import com.mmu.fyp.sheetbasedcalculator.R;
import com.mmu.fyp.sheetbasedcalculator.util.ActivityUtils;

public class MainActivity extends AppCompatActivity {

    private CalculatorPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalculatorFragment calculatorFragment =
                (CalculatorFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (calculatorFragment == null) {
            // Create the fragment
            calculatorFragment = CalculatorFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), calculatorFragment, R.id.contentFrame);
        }

        // Create the presenter
        presenter = new CalculatorPresenter();

        View current = getCurrentFocus();
        if (current != null) current.clearFocus();


    }

}

