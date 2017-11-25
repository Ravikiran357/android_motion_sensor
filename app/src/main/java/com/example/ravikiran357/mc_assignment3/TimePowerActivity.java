package com.example.ravikiran357.mc_assignment3;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;

// References: https://github.com/appsthatmatter/GraphView
// http://www.android-graphview.org/points-graph/

public class TimePowerActivity extends AppCompatActivity {

    private long getMaxXVal(String val) {
        long lval = Long.parseLong(val);
        return (lval - (lval % 10)) + 10;
    }

    private long getMaxYVal(String val) {
        long lval = Long.parseLong(val);
        return (lval - (lval % 100)) + 100;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_power);

        Bundle bundle = getIntent().getExtras();
        String trainJSON = bundle.getString("trainUsed");
        DataPoint[] trainUsed = new Gson().fromJson(trainJSON, DataPoint[].class);
        String testJSON = bundle.getString("testUsed");
        DataPoint[] testUsed = new Gson().fromJson(testJSON, DataPoint[].class);
        long maxTX = getMaxXVal(bundle.getString("mTX"));
        long maxTY = getMaxYVal(bundle.getString("mTY"));
        long maxTeX = getMaxXVal(bundle.getString("mTeX"));
        long maxTeY = getMaxYVal(bundle.getString("mTeY"));
        long maxX = Math.max(maxTeX, maxTX);
        long maxY = Math.max(maxTeY, maxTY);

        // Plotting the training time and power used
        GraphView trainingGraph = findViewById(R.id.trainingGraph);
        PointsGraphSeries<DataPoint> trainPoints = new PointsGraphSeries<>(trainUsed);
        trainingGraph.getViewport().setMinX(0.0);
        trainingGraph.getViewport().setMaxX(maxX);
        trainingGraph.getViewport().setMinY(0.0);
        trainingGraph.getViewport().setMaxY(maxY);
        trainingGraph.getViewport().setYAxisBoundsManual(true);
        trainingGraph.getViewport().setXAxisBoundsManual(true);
        trainingGraph.addSeries(trainPoints);
        trainPoints.setShape(PointsGraphSeries.Shape.POINT);
        trainPoints.setColor(Color.BLUE);

        //graph for server stats:
        GraphView testingGraph = findViewById(R.id.testingGraph);
        PointsGraphSeries<DataPoint> testPoints = new PointsGraphSeries<>(testUsed);
        testingGraph.getViewport().setMinX(0.0);
        testingGraph.getViewport().setMaxX(maxX);
        testingGraph.getViewport().setMinY(0.0);
        testingGraph.getViewport().setMaxY(maxY);
        testingGraph.getViewport().setYAxisBoundsManual(true);
        testingGraph.getViewport().setXAxisBoundsManual(true);
        testingGraph.addSeries(testPoints);
        trainPoints.setShape(PointsGraphSeries.Shape.POINT);
        testPoints.setColor(Color.GREEN);
    }
}
