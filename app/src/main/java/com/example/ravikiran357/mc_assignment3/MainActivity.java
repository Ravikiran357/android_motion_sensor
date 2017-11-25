package com.example.ravikiran357.mc_assignment3;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jjoe64.graphview.series.DataPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    SQLiteDatabase db;
    private static final int PERMISSION_STORAGE = 1;
    long timeUsed, powerUsed;
    public String[][] walkingArray, runningArray, eatingArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, AccelerometerService.class);
        // Starts the accelerometer service
        this.startService(intent);
        // If Android version is Marshmellow or above, get the required permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissions();
        } else {
            setupModules();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void getPermissions() {
        int permissionCheck1 = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck1 == PackageManager.PERMISSION_GRANTED ) {
            setupModules();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupModules();
                } else {
                    Toast.makeText(this, "Please restart the app",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupModules() {
        Button buttonCollect;
        Button buttonTrain;
        Button timeAndPowerButton;
        Button resetData;

        buttonCollect = findViewById(R.id.data_gather);
        buttonCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Data collection")
                .setMessage("Any previously collected data will be lost. Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        Intent i = new Intent(MainActivity.this, DBManager.class);
                        startActivity(i);
                    }
                })
                .setNegativeButton("No", null)
                .show();
            }
        });
        buttonTrain = findViewById(R.id.train);
        timeAndPowerButton = findViewById(R.id.timeAndPowerButton);
        final TextView accuracyTextView = findViewById(R.id.accuracyTextView);
        if(DBManager.doesTableExist()) {
            buttonTrain.setEnabled(true);
            timeAndPowerButton.setEnabled(true);
            buttonTrain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // training and testing
                    SVMClassifier svm = new SVMClassifier();
                    LinkedList<String> Dataset = getDataFromDatabase();
                    writeToCSV(Dataset);
                    svm.trainAndTest(DBManager.TRAIN_DATA_LOCATION);
                    accuracyTextView.setText(String.valueOf(svm.currentAccuracy));
                    Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
                    intent.putExtra("currentAccuracy", svm.currentAccuracy);
                    startActivity(intent);
                }
            });
        } else {
            buttonTrain.setEnabled(false);
            timeAndPowerButton.setEnabled(false);
        }

        timeAndPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.TimePowerAsyncTask timePowerAsyncTask =
                        new MainActivity.TimePowerAsyncTask();
                timePowerAsyncTask.execute();
            }
        });
        resetData = findViewById(R.id.resetToOriginaldatabaseButton);
        resetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Using sample data")
                .setMessage("Any previously collected data will be lost. Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int choice) {
                        copyAssets();
                    }
                })
                .setNegativeButton("No", null)
                .show();
            }
        });
    }

    //referred from: http://stackoverflow.com/questions/4447477/how-to-copy-files-from-assets-folder-to-sdcard

    // useless function, just uses default values for the database.
    // instead disable the button for the first time.
    // Buttweight - may need this for one time, to get the sqlite file which is needed from sdcard.
    public void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("Main:copyAssets", "Failed to get asset file list.", e);
        }
        if (files != null) {
            for (String filename : files) {
                if (filename.contains("group11")) {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = assetManager.open(filename);
                        File folder = new File(DBManager.FILE_PATH);
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File outFile = new File(DBManager.FILE_PATH, filename);
                        out = new FileOutputStream(outFile);
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                    } catch (IOException e) {
                        Log.e("Main:copyAssets:loop", filename + " " + e.getMessage());
                    } finally {
                        try {
                            if (in != null)
                                in.close();
                            if (out != null)
                                out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private LinkedList<String> getDataFromDatabase() {
        walkingArray = new String[50][20];
        runningArray = new String[50][20];
        eatingArray = new String[50][20];
        db = SQLiteDatabase.openOrCreateDatabase(DBManager.DATABASE_LOCATION, null);
        db.beginTransaction();
        String query = "SELECT  * FROM " + DBManager.TABLE + ";";
        Cursor cursor = null;
        LinkedList<String> dataList = null;
        try {
            cursor = db.rawQuery(query, null);
            db.setTransactionSuccessful(); //commit your changes
            dataList = new LinkedList<String>();
            if (cursor.moveToFirst()) {
                do {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 2; j < 152; j++) {
                        stringBuilder.append(cursor.getString(j));
                        stringBuilder.append(",");
                    }
                    String labelTemp = cursor.getString(1);
                    switch (labelTemp) {
                        case "running":
                            stringBuilder.append("1\n"); //do not remove new line
                            break;
                        case "walking":
                            stringBuilder.append("2\n"); //do not remove new line
                            break;
                        default:
                            stringBuilder.append("3\n"); //do not remove new line
                            break;
                    }
                    dataList.add(stringBuilder.toString());
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("M:getDataFromDatabase", e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        db.endTransaction();
        return dataList;
    }

    public void writeToCSV(LinkedList<String> Dataset) {
        File file = new File(DBManager.TRAIN_DATA_LOCATION);
        try {
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bfWriter = new BufferedWriter(fileWriter);

            for (int i = 0; i < Dataset.size(); i++) {
                bfWriter.write(Dataset.get(i));
            }
            bfWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class TimePowerAsyncTask extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;
        int COUNT = 5;
        DataPoint [] trainUsed = new DataPoint[COUNT];
        DataPoint [] testUsed = new DataPoint[COUNT];
        long maxTrainX = 0;
        long maxTrainY = 0;
        long maxTestX = 0;
        long maxTestY = 0;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "Processing",
                    "Please wait");
        }

        @Override
        protected String doInBackground(String... strings) {
            LinkedList<String> Dataset = getDataFromDatabase();
            writeToCSV(Dataset);
            //start time and battery profiling here
            // referred from: https://source.android.com/devices/tech/power/device
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                try {
                    long trainStartTime = System.currentTimeMillis();
                    long testStartTime = System.currentTimeMillis();
                    BatteryManager mBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                    long trainStartBattery = mBatteryManager.getLongProperty(
                            BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                    long testStartBattery = mBatteryManager.getLongProperty(
                            BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);

                    for (int j = 0; j < COUNT; j++) {
                        //training and testing:
                        SVMClassifier svm = new SVMClassifier();
                        DataPoint[] trainAndTestPoints = svm.trainAndTest(mBatteryManager,
                                DBManager.TRAIN_DATA_LOCATION, trainStartBattery, testStartBattery,
                                trainStartTime, testStartTime);
                        trainUsed[j] = trainAndTestPoints[0];
                        testUsed[j] = trainAndTestPoints[1];
                        maxTrainX = Math.max(maxTrainX, (long) trainUsed[j].getX());
                        maxTrainY = Math.max(maxTrainY,(long) trainUsed[j].getY());
                        maxTestX = Math.max(maxTestX, (long) testUsed[j].getX());
                        maxTestY = Math.max(maxTestY, (long) testUsed[j].getY());
                    }
                } catch (Exception e) {
                    Log.d("TimePower", e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            Intent intent = new Intent(MainActivity.this, TimePowerActivity.class);
            intent.putExtra("trainUsed", new Gson().toJson(trainUsed));
            intent.putExtra("testUsed", new Gson().toJson(testUsed));
            intent.putExtra("mTX", new Gson().toJson(maxTrainX));
            intent.putExtra("mTY", new Gson().toJson(maxTrainY));
            intent.putExtra("mTeX", new Gson().toJson(maxTestX));
            intent.putExtra("mTeY", new Gson().toJson(maxTestY));
            startActivity(intent);
        }
    }
}

