/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugasbesar1ml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import weka.classifiers.Evaluation;
import weka.core.DenseInstance;
import weka.core.Attribute;
import weka.core.Debug.Random;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author User
 */
public class TugasBesar1ML {

    
    /**
     * @return 
     * @throws java.io.IOException
     */
    
     public Instances loadData() throws IOException {
        System.out.print("Input File Name: ");
        
        //Read dataset file
        Scanner scanner = new Scanner(System.in);
        String file = scanner.nextLine();

        //Check if file CSV or Arff
        while (!file.endsWith(".csv") && !file.endsWith(".arff")) {
            System.out.println("File must be a CSV or Arff");
            System.out.print("Input File Name: ");
            //Read dataset file
            scanner = new Scanner(System.in);
            file = scanner.nextLine();
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            System.out.println("File Not Found");
        }
        
        //Assign to Instances
        Instances dataset = new Instances(reader);
        if (reader != null) {
           reader.close();
        }
        dataset.setClassIndex(dataset.numAttributes() - 1);
        return dataset;
    }
     
    public Instances removeAttribute (Instances dataset) throws Exception {
        //Read attribute indices
        System.out.print("Input attribute indices to be removed: ");
        Scanner scanner = new Scanner(System.in);
        String attributeIndices = scanner.nextLine();
        
        //Remove attributes
        Remove remove = new Remove();
        remove.setAttributeIndices(attributeIndices);
        remove.setInvertSelection(false);
        remove.setInputFormat(dataset);
        dataset = Filter.useFilter(dataset, remove);
        return dataset;
    }

    public Instances resample(Instances dataset) throws Exception {
        Resample resampleFilter = new Resample();
	resampleFilter.setNoReplacement(false);
	resampleFilter.setBiasToUniformClass(1.0); // Uniform distribution of class
	resampleFilter.setSampleSizePercent(100.0);
	resampleFilter.setInputFormat(dataset);
	Instances filteredDataset = Filter.useFilter(dataset,resampleFilter);
        return filteredDataset;
    }
    
    public void chooseClassifier() {
        System.out.println("Classifier Options: ");
        System.out.println("1. ID3");
        System.out.println("2. C4.5");
        System.out.print("Choose a classifier: ");
        Scanner scanner = new Scanner(System.in);
        String classifierOption = scanner.nextLine();        
    }
    
    public static Instances makeInstance(Instances dataset){
        System.out.println("Input new instance ");
        Scanner scanner = new Scanner(System.in);
        String value = scanner.nextLine();
        String[] valueAttribute = value.split(" ");
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < dataset.numAttributes(); i++) {
            attributes.add(dataset.attribute(i));
        } 
        Instances testData = new Instances("testData", attributes, 0);
        testData.setClassIndex(testData.numAttributes() - 1);
        Instance inst = new DenseInstance(testData.numAttributes());
        for (int i = 0; i < valueAttribute.length; i++) {
            inst.setValue(i, valueAttribute[i]);
        }
        testData.add(inst);
        return testData;
    }
    
    public static void main(String[] args) throws IOException, Exception {
        //Data Initialisation
        TugasBesar1ML newClassifier = new TugasBesar1ML();
        Instances data = newClassifier.loadData();
        data = newClassifier.removeAttribute(data);
        //newClassifier.chooseClassifier();
        Instances newData = new Instances(data);
        newData.randomize(new Random());
        
        Instances trainingData = newData;
        //Make training or test data
        //int folds = 5;
        //Instances trainingData = newData.trainCV(folds, 0);
        //Instances testData = newData.testCV(folds, 0);
        
        //Build Classifier
        MyId3 id3 = new MyId3();
        id3.buildClassifier(trainingData);
        
        id3.getModel().printTree("", false);
        Evaluation eval = new Evaluation(trainingData);
        //eval.crossValidateModel(id3, trainingData, 10, new Random());
        eval.evaluateModel(id3, trainingData);
        System.out.println(eval.toSummaryString());
    }
    
}
