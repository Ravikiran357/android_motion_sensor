package com.example.ravikiran357.mc_assignment3;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;


public class TimePowerActivity extends AppCompatActivity {
    int timeUsedMobile, powerUsedMobile;
    int timeUsedServer, powerUsedServer;
    int zero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_power);

        zero = 0;
        Bundle bundle = getIntent().getExtras();
        timeUsedMobile = (int) bundle.getLong("timeUsed");
        powerUsedMobile = (int) bundle.getLong("powerUsed");

        /*
         * referred from: https://github.com/appsthatmatter/GraphView
         * referred from: http://www.android-graphview.org/points-graph/
         *
         */

//        graph for mobile stats:
        GraphView graphMobile = findViewById(R.id.mobileGraph);
        PointsGraphSeries<DataPoint> pointsMobile = new PointsGraphSeries<>(new DataPoint[] {
                new DataPoint(timeUsedMobile,powerUsedMobile)
        });
        graphMobile.getViewport().setMinX(0.0);
        graphMobile.getViewport().setMaxX(2000.0);
        graphMobile.getViewport().setMinY(0.0);
        graphMobile.getViewport().setMaxY(1000.0);
        graphMobile.getViewport().setYAxisBoundsManual(true);
        graphMobile.getViewport().setXAxisBoundsManual(true);
        graphMobile.addSeries(pointsMobile);
        pointsMobile.setShape(PointsGraphSeries.Shape.POINT);
        pointsMobile.setColor(Color.RED);


        /*
         * we performed repeated experiments of SVM with k-fold cross validation with k=5 using the same dataset
         * on our laptop which we assume as fog server
         * On the laptop as fog server, we get:
         *      average time used = 328 milliseconds
         *      average power used = 790 microAmpere-Hour
         */
        timeUsedServer = 328;
        powerUsedServer = 790;

        //graph for server stats:
        GraphView graphServer = findViewById(R.id.serverGraph);
        PointsGraphSeries<DataPoint> pointsServer = new PointsGraphSeries<>(new DataPoint[] {
                new DataPoint(timeUsedServer, powerUsedServer)
        });
        graphServer.getViewport().setMinX(0.0);
        graphServer.getViewport().setMaxX(2000.0);
        graphServer.getViewport().setMinY(0.0);
        graphServer.getViewport().setMaxY(1000.0);
        graphServer.getViewport().setYAxisBoundsManual(true);
        graphServer.getViewport().setXAxisBoundsManual(true);
        graphServer.addSeries(pointsServer);
        pointsServer.setShape(PointsGraphSeries.Shape.POINT);
        pointsServer.setColor(Color.BLUE);
    }
}
