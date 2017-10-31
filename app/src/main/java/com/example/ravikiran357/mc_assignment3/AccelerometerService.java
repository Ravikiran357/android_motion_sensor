package com.example.ravikiran357.mc_assignment3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;


public class AccelerometerService extends Service implements SensorEventListener {
    ArrayList<Float> valueList;
    int count = 0;
    public static int timeDelay = 50;
    public long lastSaved;
    final static String INTENT_ACCELEROMETER_ACTION = "ACCELEROMETER_DATA";

    @Override
    public void onCreate() {
        try {
            SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
            lastSaved = System.currentTimeMillis();
            valueList = new ArrayList<>();
        } catch (Exception e) {
            Log.d("accelerometerService", e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (System.currentTimeMillis() - lastSaved > timeDelay) {
                lastSaved = System.currentTimeMillis();
                getAccelerometer(sensorEvent);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        valueList.add(values[0]);
        valueList.add(values[1]);
        valueList.add(values[2]);
        count += 1;

        if(count == 50) {//original it should be 50
            count = 0;
            Intent intent = new Intent();
            intent.setAction(INTENT_ACCELEROMETER_ACTION);
            intent.putExtra("value_list", valueList);
            sendBroadcast(intent);
            valueList.clear();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}