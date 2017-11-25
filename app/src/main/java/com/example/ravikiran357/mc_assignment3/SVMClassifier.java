package com.example.ravikiran357.mc_assignment3;

import android.content.Context;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import java.io.File;
import java.io.IOException;

import libsvm.LibSVM;

// References: https://github.com/AbeelLab/javaml , http://www.csie.ntu.edu.tw/~cjlin/libsvm/ ,
// https://source.android.com/devices/tech/power/device, https://sourceforge.net/p/java-ml/java-ml-code/
// ci/30237db0cb0457fe2629004ac91ac4cd2768d8ea/tree/src/tutorials/classification/TutorialLibSVM.java#l13

class SVMClassifier {

    float currentAccuracy = 0;
    private int KFOLD = 5;
    private String filename;

    void trainAndTest(String fileParam) {
        filename = fileParam;
        try {
            Dataset dataset = FileHandler.loadDataset(new File(filename), 150, ",");
            Dataset[] datasets = getKPartitions(dataset);
            float accuracy = 0.0f;
            Dataset trainDataset;
            for (int i = 0; i < KFOLD; i++) {
                trainDataset = getTrainingData(datasets, i);
                Classifier svmClassifier = new LibSVM();
                // Training here
                svmClassifier.buildClassifier(trainDataset);
                // Testing with K-fold for accuracy
                accuracy += getAccuracy(svmClassifier, datasets[i]);
            }
            currentAccuracy = accuracy / KFOLD;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    DataPoint [] trainAndTest(BatteryManager mBatteryManager, String fileParam, long trainStartBattery,
                              long testStartBattery, long trainStartTime, long testStartTime) {
        filename = fileParam;
        DataPoint [] dataTimePower = new DataPoint[2];
        DataPoint trainTimePower = null;
        DataPoint testTimePower = null;
        long trainTime = 0;
        long trainBattery = 0;
        long afterTrainBattery = 0;
        long testTime = 0;
        long testBattery = 0;
        try {
            Dataset dataset = FileHandler.loadDataset(new File(filename), 150, ",");
            Dataset[] datasets = getKPartitions(dataset);
            float accuracy = 0.0f;
            Dataset trainDataset;
            for (int i = 0; i < KFOLD; i++) {
                trainDataset = getTrainingData(datasets, i);
                Classifier svmClassifier = new LibSVM();
                // Training here
                svmClassifier.buildClassifier(trainDataset);
                trainTime += System.currentTimeMillis() - trainStartTime;
                // the remaining battery capacity in microampere-hours
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    trainBattery += Math.abs(trainStartBattery - mBatteryManager.getLongProperty(
                            BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER));

                // Testing with K-fold for accuracy
                accuracy += getAccuracy(svmClassifier, datasets[i]);
                testTime += System.currentTimeMillis() - testStartTime;
                // the test battery levels need to be reduced by a factor of 2 as the training
                // levels are included
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    testBattery += Math.abs(testStartBattery - mBatteryManager.getLongProperty(
                            BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)) / 2;
            }
            trainTimePower = new DataPoint(trainTime / 1000, trainBattery / KFOLD);
            testTimePower = new DataPoint(testTime / 1000,  testBattery / KFOLD);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        dataTimePower[0] = trainTimePower;
        dataTimePower[1] = testTimePower;
        return dataTimePower;
    }

    private Dataset getTrainingData(Dataset [] datasets, int index) {
        Dataset trainData = null;
        try {
            trainData = FileHandler.loadDataset(new File(filename), 150, ",");
            trainData.clear();
            for (int i = 0; i < KFOLD; i++) {
                if (i != index)
                    trainData.addAll(datasets[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trainData;
    }

    private Dataset[] getKPartitions(Dataset dataset) {
        Dataset[] datasetArray = new Dataset[KFOLD];
        try {
            datasetArray[0] = FileHandler.loadDataset(new File(filename), 150,
                    ",");
            for (int i = 1; i < KFOLD; i++) {
                datasetArray[i-1].clear();
                datasetArray[i] = FileHandler.loadDataset(new File(filename), 150,
                        ",");
            }
            datasetArray[KFOLD-1].clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int count = 0;
        for(Instance instance: dataset) {
            datasetArray[count % KFOLD].add(instance);
            count++;
        }
        return datasetArray;
    }

    private float getAccuracy(Classifier svmClassifier, Dataset testDataset)   {
        float correct = 0, incorrect = 0;
        for (Instance instance : testDataset) {
            Object predictedClassValue = svmClassifier.classify(instance);
            Object realClassValue = instance.classValue();
            if (predictedClassValue.toString().equals(realClassValue.toString()))
                correct++;
            else
                incorrect++;
        }
        return (correct / (correct + incorrect)) * 100;
    }
}
