package com.example.ravikiran357.mc_assignment3;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Arrays;

public class WebviewActivity extends AppCompatActivity {

    SQLiteDatabase db;
    WebView webview;
    CheckBox checkRun, checkWalk, checkEat;
    private String[][] walkingArray, runningArray, eatingArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        float accuracy = 0.0f;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            accuracy = bundle.getFloat("currentAccuracy");
        }
        TextView accuracyTextView = findViewById(R.id.accuracyPageTextView);
        accuracyTextView.setText("Accuracy = " + accuracy + " %");

        webview = findViewById(R.id.webview);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDefaultTextEncodingName("utf-8");
        webview.setWebChromeClient(new WebChromeClient());
        webview.loadUrl("file:///android_asset/html/scatter_plot.html");
        loadGraph();
        checkRun = findViewById(R.id.checkBox1);
        checkWalk = findViewById(R.id.checkBox2);
        checkEat = findViewById(R.id.checkBox3);
        checkRun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callJS();
            }
        });
        checkWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callJS();
            }
        });
        checkEat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callJS();
            }
        });
    }

    private void loadGraph() {
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                callJS();
                TextView messageText = findViewById(R.id.messageText);
                messageText.setVisibility(View.GONE);
            }
        });
    }

    private void callJS() {
        getDataFromDatabase();
        boolean run = checkRun.isChecked();
        boolean walk = checkWalk.isChecked();
        boolean eat = checkEat.isChecked();
        String runningtext = (run ? Arrays.deepToString(runningArray) : "''");
        String walkingtext = (walk ? Arrays.deepToString(walkingArray) : "''");
        String eatingtext = (eat ? Arrays.deepToString(eatingArray) : "''");
        webview.loadUrl("javascript:showGraph(" + runningtext + ", " + walkingtext + ", " +
                eatingtext + ")");
    }

    private void getDataFromDatabase() {
        int DATA_LEN = 1000;
        walkingArray = new String[DATA_LEN][3];
        runningArray = new String[DATA_LEN][3];
        eatingArray = new String[DATA_LEN][3];
        int run = 0, walk = 0, eat = 0;
        db = SQLiteDatabase.openOrCreateDatabase(DBManager.DATABASE_LOCATION, null);
        db.beginTransaction();
        String query = "SELECT * FROM training;";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            db.setTransactionSuccessful(); //commit your changes
        } catch (Exception e) {
            Log.d("exp", e.getMessage());
        }
        try {
            if (cursor.moveToFirst()) {
                do {
                    String label = cursor.getString(1);
                    for (int col_index = 0; col_index < 50; col_index++) {
                        int res_col = (col_index * 3) + 2;
                        switch (label) {
                            case "running":
                                runningArray[run][0] = cursor.getString(res_col);
                                runningArray[run][1] = cursor.getString(res_col + 1);
                                runningArray[run++][2] = cursor.getString(res_col + 2);
                                break;
                            case "walking":
                                walkingArray[walk][0] = cursor.getString(res_col);
                                walkingArray[walk][1] = cursor.getString(res_col + 1);
                                walkingArray[walk++][2] = cursor.getString(res_col + 2);
                                break;
                            case "eating":
                                eatingArray[eat][0] = cursor.getString(res_col);
                                eatingArray[eat][1] = cursor.getString(res_col + 1);
                                eatingArray[eat++][2] = cursor.getString(res_col + 2);
                                break;
                        }
                    }
                } while (cursor.moveToNext());
            }
            // check eat, walk, run values - should be 1000
        } catch (Exception e) {
            Log.d("WebviewActivity",e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
            db.endTransaction();
        }
    }
}
