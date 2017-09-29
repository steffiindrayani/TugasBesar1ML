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
import java.util.Scanner;
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
    
    
    public static void main(String[] args) throws IOException, Exception {
        // TODO code application logic here
        TugasBesar1ML newClassifier = new TugasBesar1ML();
        Instances trainingData = newClassifier.loadData();
        trainingData = newClassifier.removeAttribute(trainingData);
        //trainingData = newClassifier.resample(trainingData);      
        //System.out.println(trainingData);
        //newClassifier.chooseClassifier();
        MyId3 id3 = new MyId3();
        System.out.println(id3.mostCommonClassValue(trainingData));
    }
    
}
