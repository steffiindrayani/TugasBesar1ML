/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugasbesar1ml;

import java.util.ArrayList;
import weka.classifiers.AbstractClassifier;
import weka.core.Instances;
import weka.core.Utils;
/**
 *
 * @author User
 */
public class MyId3 extends AbstractClassifier {
    @Override
    public void buildClassifier(Instances dataset) throws Exception {
        //Delete missing value
        dataset.deleteWithMissingClass();
        ArrayList<Integer> attributeList = new ArrayList<>(); 
        for (int i = 0; i < dataset.numAttributes() - 1; i++) {
            attributeList.add(i);
        }
        MyTree id3 = buildTree(dataset, attributeList);
        id3.printTree(" ", false);
    }

    public MyTree buildTree(Instances dataset, ArrayList<Integer> attributes) {
        if (isOneElement(dataset)) {
            String c = dataset.get(0).stringValue(dataset.classIndex());
            MyTree tree = new MyTree(c);
            return tree;
        } else if (attributes.isEmpty()) {
            String c = mostCommonClassValue(dataset);
            MyTree tree = new MyTree(c);
            return tree;
        } else {
            //choose best attribute
            int bestAttribute = chooseBestAttribute(dataset, attributes);
            MyTree tree = new MyTree(dataset.attribute(bestAttribute).name());
            ArrayList<Double> listOfValue = new ArrayList<>();
            ArrayList<String> listOfString = new ArrayList<>();
            ArrayList<MyTree> listOfChild = new ArrayList<>();
            Instances[] split = seperateData(dataset, bestAttribute);
            attributes.remove(Integer.valueOf(bestAttribute));
            for (int i = 0; i < dataset.attribute(bestAttribute).numValues(); i++) {
                if (split[i].numInstances() == 0) {
                    String c = mostCommonClassValue(dataset);
                    MyTree child = new MyTree(c);
                    listOfChild.add(child);
                } else {
                    MyTree child = buildTree(split[i], attributes);
                    listOfValue.add((double) i);
                    listOfString.add(dataset.attribute(bestAttribute).value(i));
                    listOfChild.add(child);
                }
            }
            tree.setListOfValue(listOfValue);
            tree.setListOfChild(listOfChild);
            tree.setListOfStringValue(listOfString);
            return tree;
        }
    }
    
    public int chooseBestAttribute(Instances dataset, ArrayList<Integer> attributes) {
        int bestAttribute = -1;
        double bestGain = -999;
        for (int i = 0; i < attributes.size(); i++) {
            if (countInformationGain(dataset, attributes.get(i)) > bestGain) {
                bestAttribute = attributes.get(i);
                bestGain = countInformationGain(dataset, attributes.get(i));
            }
        }
        return bestAttribute;
    }
    
    public double countInformationGain(Instances dataset, int attribute) {
        double gain = countEntropy(dataset);
        Instances[] split = seperateData(dataset, attribute);
        for (int i = 0; i < dataset.attribute(attribute).numValues(); i++) {
            if (split[i].numInstances() > 0) {
                gain -= (double) split[i].numInstances() / (double) dataset.numInstances() * countEntropy(split[i]);
            }
        }
        return gain;
    }
    
    public double countEntropy(Instances dataset) {
        double entropy = 0;
        
        //Count each class value in dataset
        int[] countClass = new int[dataset.numClasses()];
        for (int i = 0; i < dataset.numInstances(); i++) {
            countClass[(int) dataset.get(i).classValue()]++;
        }
                
        //Count entropy
        for (int i = 0; i < dataset.numClasses(); i++) {
            if (countClass[i] > 0) {
                entropy -= countClass[i]/ (double) dataset.numInstances() * Utils.log2(countClass[i] / (double) dataset.numInstances());
            }
        }
        return entropy;
    }
    
    public Instances[] seperateData(Instances dataset, int attribute) {
        int size = dataset.attribute(attribute).numValues();
        Instances[] split = new Instances[size];
        for (int i = 0; i < size; i++) {
            split[i] = new Instances(dataset, dataset.numInstances());
        }
        for (int i = 0; i < dataset.numInstances(); i++) {
            split[(int) dataset.get(i).value(attribute)].add(dataset.get(i));
        }
        for (Instances split1 : split) {
            split1.compactify();
        }
        return split;
    }
    
    public String mostCommonClassValue(Instances dataset) {
        int[] countClass = new int[dataset.numClasses()];
        int[] instance = new int[dataset.numClasses()];
        for (int i = 0; i < dataset.numInstances(); i++) {
            countClass[(int) dataset.get(i).classValue()]++;
            instance[(int) dataset.get(i).classValue()] = i;
        }
        int bestClass = -1;
        int maxClass = 0;
        for (int i = 0; i < countClass.length; i++) {
            if (countClass[i] > maxClass) {
                bestClass = i;
                maxClass = countClass[i];
            }
        }
        if (bestClass < 0) {
           return "ERROR";
        } else {
            return dataset.get(instance[bestClass]).stringValue(dataset.classIndex());
        }
    }
    
    public boolean isOneElement(Instances dataset) {
        switch (dataset.numInstances()) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                double currentClass = dataset.get(0).classValue();
                int i = 0;
                while (i < dataset.numInstances()) {
                    if (dataset.get(i).classValue() == currentClass) {
                        i++;
                    } else {
                        return false;
                    }
                }
                return true;
        } 
    }
}
