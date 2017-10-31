package com.example.ravikiran357.mc_assignment3;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
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
        File databaseFile = this.getDatabasePath(DBManager.DATABASE_LOCATION);
        if(!databaseFile.exists()) {
            try {
                copyAssets();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void getPermissions() {
        int permissionCheck1 = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck2 = this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck1 == PackageManager.PERMISSION_GRANTED &&
                (permissionCheck2 == PackageManager.PERMISSION_GRANTED)) {
            setupModules();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_STORAGE);
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

    private void setupModules() {
        Button buttonTrain;
        Button timeAndPowerButton;
        Button buttonCalibrate;
        Button resetData;

        buttonTrain = findViewById(R.id.train);
        final TextView accuracyTextView = findViewById(R.id.accuracyTextView);
        buttonTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // training and testing
                SVM svm = new SVM();
                LinkedList<String> Dataset = getDataFromDatabase();
                writeToCSV(Dataset);
                svm.trainAndTest(DBManager.TRAIN_DATA_LOCATION);
                accuracyTextView.setText(String.valueOf(svm.currentAccuracy));
                Intent intent = new Intent(MainActivity.this, WebviewActivity.class);
                intent.putExtra("currentAccuracy", svm.currentAccuracy);
                startActivity(intent);
            }
        });

        timeAndPowerButton = findViewById(R.id.timeAndPowerButton);
        timeAndPowerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.TimePowerAsyncTask timePowerAsyncTask =
                        new MainActivity.TimePowerAsyncTask();
                timePowerAsyncTask.execute();
            }
        });


        buttonCalibrate = findViewById(R.id.data_gather);
        buttonCalibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DBManager.class);
                startActivity(i);
            }
        });

        resetData = findViewById(R.id.resetToOriginaldatabaseButton);
        resetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyAssets();
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
        String query = "SELECT  * FROM training;";
        Cursor cursor = null;
        LinkedList<String> Dataset = null;
        try {
            cursor = db.rawQuery(query, null);
            db.setTransactionSuccessful(); //commit your changes
            Dataset = new LinkedList<String>();
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
                    Dataset.add(stringBuilder.toString());
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("M:getDataFromDatabase", e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        db.endTransaction();
        return Dataset;
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

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainActivity.this, "Processing",
                    "Please wait");
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected String doInBackground(String... strings) {
            LinkedList<String> Dataset = getDataFromDatabase();
            writeToCSV(Dataset);

            //start time and battery profiling here
            // referred from: https://source.android.com/devices/tech/power/device
            BatteryManager mBatteryManager = (BatteryManager)
                    getSystemService(Context.BATTERY_SERVICE);
            long startBattery = 0;
            powerUsed = 0;
            try {
                for (int j = 0; j < 20; j++) {
                    // the remaining battery capacity in microampere-hours
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                        startBattery = mBatteryManager.getLongProperty(
                                BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                    long startTime = System.currentTimeMillis();
                    //training and testing:
                    SVM svm = new SVM();
                    svm.trainAndTest(DBManager.TRAIN_DATA_LOCATION);
                    //end time and battery profiling now
                    long endBattery = 0;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                        endBattery = mBatteryManager.getLongProperty(
                                BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                    long endTime = System.currentTimeMillis();

                    timeUsed = endTime - startTime;
                    powerUsed = endBattery - startBattery;
                    if (powerUsed < 0)
                        powerUsed = powerUsed * (-1); //taking absolute value
                    Log.d("time and battery used: ", (endTime - startTime) + " " + powerUsed);
                /*
                 * BATTERY_PROPERTY_CHARGE_COUNTER of BatteryManager does not work smoothly for all
                 * APIs also, it depends on the hardware interrupt of the battery change, thus its
                 * sampling frequency varies
                 *
                 * Hence, we carried out repeated experiments using BATTERY_PROPERTY_CHARGE_COUNTER
                 * of BatteryManager and averaged out a value of 113 microAmpere-Hour power
                 * consumption
                 */
                    if (powerUsed == 0)
                        powerUsed = 113;
                    if (powerUsed > 0)
                        break;
                }
            } catch (Exception e) {
                Log.d("TimePower", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            Intent intent = new Intent(MainActivity.this, TimePowerActivity.class);
            intent.putExtra("timeUsed", timeUsed);
            intent.putExtra("powerUsed", powerUsed);
            startActivity(intent);
        }
    }
}

