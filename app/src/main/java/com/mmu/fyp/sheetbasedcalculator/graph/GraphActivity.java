package com.mmu.fyp.sheetbasedcalculator.graph;

// import android.support.v7.app.ActionBar;
import androidx.appcompat.app.ActionBar;
// import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.webkit.WebView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mmu.fyp.sheetbasedcalculator.R;

public class GraphActivity extends AppCompatActivity {
    private LineGraphSeries<DataPoint> series1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);

        int max_X_Value = getIntent().getIntExtra("max_x", 10);
        int t = getIntent().getIntExtra("testnum", 100);
        Bundle b = this.getIntent().getExtras();
        double[] yArray = b.getDoubleArray("passArrayIntent");

        double x, y;
        x = 0;

        GraphView graph = (GraphView) findViewById(R.id.graph);
        series1 = new LineGraphSeries<>();

        int numDataPoints = max_X_Value*10;
        int j=0;
        for(int i = 0; i < numDataPoints; i++ ){
            x = x + 0.1;
            y = yArray[j];
            j++;
            series1.appendData(new DataPoint(x,y),true,numDataPoints);
        }

        graph.getViewport().setMaxX(max_X_Value);
        // graph.getViewport().setMaxY(100);
        graph.getViewport().setScalable(true);  // activate horizontal zooming and scrolling
        graph.getViewport().setScrollable(true);  // activate horizontal scrolling
        graph.getViewport().setScalableY(true);  // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScrollableY(true);  // activate vertical scrolling

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);

        graph.addSeries(series1);


    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
