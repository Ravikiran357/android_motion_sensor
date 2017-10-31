package com.example.ravikiran357.mc_assignment3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class DBManager extends AppCompatActivity {

    SQLiteDatabase db;
    public String TABLE = "training";
    public String label_activity = "eating";
    public static final String DATABASE_NAME = "group11";
    public static final String FILE_PATH = Environment.getExternalStorageDirectory() +
            File.separator + "Android/Data/CSE535_ASSIGNMENT3";
    public static final String DATABASE_LOCATION = FILE_PATH + File.separator + DATABASE_NAME;
    public static final String TRAIN_DATA_LOCATION = FILE_PATH + File.separator + "Train_data.csv";
    AccelerometerReceiver accelerometerReceiver;
    Button run;
    Button walk;
    Button eat;
    boolean running = true;
    boolean walking = true;
    boolean eating = true;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        try{
            File folder = new File(FILE_PATH);
            if (!folder.exists()) {
                folder.mkdir();
            }
            createDBTable(DATABASE_LOCATION, TABLE);
        } catch (SQLException e){
            Toast.makeText(DBManager.this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("exp",e.getMessage() );
        }
    }

    @Override
    public void onBackPressed() {
        if (db != null) {
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
        }
    }

    public void createDBTable(String DATABASE_LOCATION, String TABLE){
        db = SQLiteDatabase.openOrCreateDatabase(DATABASE_LOCATION, null);
        db.beginTransaction();
        db.execSQL("DROP TABLE " + TABLE);
        Toast.makeText(DBManager.this,
                "Press label_activity button to start calibrating Database",
                Toast.LENGTH_LONG).show();

        StringBuilder createQuery;
        createQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS " + TABLE + " ("
                + " created_at DATETIME DEFAULT CURRENT_TIMESTAMP, label, ");
        for(int i = 1; i <= 150; i++){
            createQuery.append(" val").append(Integer.toString(i)).append(" float");
            if(i != 150){
                createQuery.append(", ");
            }
        }
        createQuery.append(");");
        db.execSQL(createQuery.toString());

        accelerometerReceiver = new AccelerometerReceiver();
        Intent intent = new Intent(DBManager.this, AccelerometerService.class);
        startService(intent);

        run = findViewById(R.id.run);
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label_activity = "running";
                running = false;
                disableButtonsCollectData();
            }
        });

        walk = findViewById(R.id.walk);
        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label_activity = "walking";
                walking = false;
                disableButtonsCollectData();
            }
        });

        eat = findViewById(R.id.eat);
        eat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label_activity = "eating";
                eating = false;
                disableButtonsCollectData();
            }
        });
    }

    private class AccelerometerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //get list of 150 elements from accelerometer

            try {
                ArrayList<Float> valueList = (ArrayList<Float>)
                        intent.getSerializableExtra("value_list");
                String insertQuery;
                StringBuilder insertQueryBuilder1 = new StringBuilder("INSERT INTO " + TABLE +
                        " (label, ");
                for(int i = 1; i <= 150; i++){
                    insertQueryBuilder1.append(" val").append(Integer.toString(i));
                    if(i != 150){
                        insertQueryBuilder1.append(", ");
                    }
                }
                insertQuery = insertQueryBuilder1.toString();
                insertQuery += ") VALUES ('"+ label_activity +"', ";
                StringBuilder insertQueryBuilder = new StringBuilder(insertQuery);
                for (int i = 0; i <= 149; i++) {
                    insertQueryBuilder.append("'").append(valueList.get(i)).append("'");
                    if(i != 149){
                        insertQueryBuilder.append(", ");
                    }
                }
                insertQuery = insertQueryBuilder.toString();
                insertQuery += " );";
                db.execSQL(insertQuery);
                counter = counter + 1;
                if(counter == 20) {
                    counter = 0;
                    enableButtons();
                }
                Toast.makeText(DBManager.this, Integer.toString(counter),
                        Toast.LENGTH_SHORT).show();
            }
            catch (SQLiteException e) {
                Log.d("DBManager", e.getMessage());
            }
        }
    }

    private void disableButtonsCollectData() {
        run.setEnabled(false);
        walk.setEnabled(false);
        eat.setEnabled(false);
        Toast.makeText(DBManager.this, "Collecting data for " + label_activity,
                Toast.LENGTH_LONG).show();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AccelerometerService.INTENT_ACCELEROMETER_ACTION);
        registerReceiver(accelerometerReceiver, intentFilter);
    }

    private void enableButtons() {
        unregisterReceiver(accelerometerReceiver);
        if(!running && !walking && !eating){
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
            Intent i = new Intent(DBManager.this, MainActivity.class);
            startActivity(i);
        }
        run.setEnabled(running);
        walk.setEnabled(walking);
        eat.setEnabled(eating);
    }
}
