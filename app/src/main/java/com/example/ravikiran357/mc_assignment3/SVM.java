package com.example.ravikiran357.mc_assignment3;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import java.io.File;
import java.io.IOException;

import libsvm.LibSVM;


//references: https://github.com/AbeelLab/javaml
//references: http://www.csie.ntu.edu.tw/~cjlin/libsvm/
//references: https://sourceforge.net/p/java-ml/java-ml-code/ci/30237db0cb0457fe2629004ac91ac4cd2768d8ea/tree/src/tutorials/classification/TutorialLibSVM.java#l13

class SVM {
    float currentAccuracy = 0;
    private int KFOLD = 5;
    private String filename;

    void trainAndTest(String filename1) {
        filename = filename1;
        try {
            Dataset dataset = FileHandler.loadDataset(new File(filename), 150, ",");
            Dataset[] datasetArray = divideDatasetInto5(dataset);
            Dataset dataset0 = datasetAdd(datasetArray[1], datasetArray[2], datasetArray[3],
                    datasetArray[4]);
            Dataset dataset1 = datasetAdd(datasetArray[0], datasetArray[2], datasetArray[3],
                    datasetArray[4]);
            Dataset dataset2 = datasetAdd(datasetArray[0], datasetArray[1], datasetArray[3],
                    datasetArray[4]);
            Dataset dataset3 = datasetAdd(datasetArray[0], datasetArray[1], datasetArray[2],
                    datasetArray[4]);
            Dataset dataset4 = datasetAdd(datasetArray[0], datasetArray[1], datasetArray[2],
                    datasetArray[3]);

            float[] accuracy = new float[KFOLD];

            //fold iteration 1:
            Classifier svmClassifier = new LibSVM();
            svmClassifier.buildClassifier(dataset0);
            accuracy[0] = getAccuracy(svmClassifier, datasetArray[0]);

            //fold iteration 2:
            svmClassifier = new LibSVM();
            svmClassifier.buildClassifier(dataset1);
            accuracy[1] = getAccuracy(svmClassifier, datasetArray[1]);

            //fold iteration 3:
            svmClassifier = new LibSVM();
            svmClassifier.buildClassifier(dataset2);
            accuracy[2] = getAccuracy(svmClassifier, datasetArray[2]);

            //fold iteration 4:
            svmClassifier = new LibSVM();
            svmClassifier.buildClassifier(dataset3);
            accuracy[3] = getAccuracy(svmClassifier, datasetArray[3]);

            //fold iteration 5:
            svmClassifier = new LibSVM();
            svmClassifier.buildClassifier(dataset4);
            accuracy[4] = getAccuracy(svmClassifier, datasetArray[4]);

            currentAccuracy = (accuracy[0]+accuracy[1]+accuracy[2]+accuracy[3]+accuracy[4]) / KFOLD;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Dataset[] divideDatasetInto5(Dataset dataset)    {
        int count = 0;
        Dataset[] datasetArray = new Dataset[KFOLD];
        for(int i=0 ; i < KFOLD ; i++) {
            try {
                datasetArray[i] = FileHandler.loadDataset(new File(filename), 150,
                        ",");
                datasetArray[i].clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for(Instance instance: dataset) {
            datasetArray[count % KFOLD].add(instance);
            count++;
        }
        return datasetArray;
    }

    private Dataset datasetAdd(Dataset datasetA, Dataset datasetB, Dataset datasetC, Dataset datasetD)   {
        Dataset temp = null;
        try {
            temp = FileHandler.loadDataset(new File(filename), 150, ",");
            temp.clear();
            temp.addAll(datasetA);
            temp.addAll(datasetB);
            temp.addAll(datasetC);
            temp.addAll(datasetD);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  temp;
    }

    private float getAccuracy(Classifier svmClassifier, Dataset testDataset)   {
        float correct = 0, wrong = 0;
        for (Instance instance : testDataset) {
            Object predictedClassValue = svmClassifier.classify(instance);
            Object realClassValue = instance.classValue();
            if (predictedClassValue.toString().equals(realClassValue.toString()))
                correct++;
            else
                wrong++;

        }
        return (100 * correct) / (correct + wrong);
    }
}
