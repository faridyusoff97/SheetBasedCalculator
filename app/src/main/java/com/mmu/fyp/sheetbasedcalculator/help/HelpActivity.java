package com.mmu.fyp.sheetbasedcalculator.help;

// import android.support.v7.app.ActionBar;
import androidx.appcompat.app.ActionBar;
// import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.webkit.WebView;
import android.widget.TextView;

import com.mmu.fyp.sheetbasedcalculator.R;

public class HelpActivity extends AppCompatActivity {

    private WebView wvHelpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        wvHelpText = (WebView) findViewById(R.id.wvHelpText);
        String helpString = getString(R.string.help_activity_full_text);
        wvHelpText.loadData(helpString, "text/html", "UTF-8");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
