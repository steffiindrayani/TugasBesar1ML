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
import java.util.*;

/**
 *
 * @author raudi
 */
public class MyC45 extends AbstractClassifier {
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
    
    public Instances handleAttributeContinuesValue(Instances instances, int attributeIdx) {
        Instances dataset = new Instances(instances);
        double gain = countEntropy(dataset);
        dataset.sort(attributeIdx);
	int currentClassIdx = dataset.instance(0).classIndex();
        int maxGainIdx = -1;
        double maxGain = 0;
	for(int i=0;i<dataset.numInstances();i++) {
            if (dataset.instance(i).classIndex() != currentClassIdx) {
                double tempGain = gain;
                Instances leftDataset = new Instances(dataset, 0, i-1);
                Instances rightDataset = new Instances(dataset, i-1, dataset.numInstances());
                tempGain -= (double) rightDataset.numInstances() / (double) dataset.numInstances() * countEntropy(leftDataset);
                tempGain -= (double) rightDataset.numInstances() / (double) dataset.numInstances() * countEntropy(leftDataset);
                if (maxGain < tempGain) {
                    maxGain = tempGain;
                    maxGainIdx = i-1;
                }
                currentClassIdx = dataset.instance(i).classIndex();
            }
	}
        double threshold = (dataset.instance(maxGainIdx).value(attributeIdx) + dataset.instance(maxGainIdx+1).value(attributeIdx))/2;
        for(int i=0;i<dataset.numInstances();i++) {
            if (dataset.instance(i).value(attributeIdx) < threshold) {
                dataset.instance(i).setValue(attributeIdx, "less than "+threshold);
            } else {
                dataset.instance(i).setValue(attributeIdx, "more than or equal to "+threshold);
            }
            
        }
        return dataset;
    }

    public MyTree buildTree(Instances dataset, ArrayList<Integer> attributes) {
        if (dataset.numClasses() == 1) {
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
                listOfValue.add((double) i);
                listOfString.add(dataset.attribute(bestAttribute).value(i));
                MyTree child = buildTree(split[i], attributes);
                listOfChild.add(child);
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
            System.out.println(attributes.get(i) + " " + countInformationGain(dataset, attributes.get(i)));
        }
        System.out.println("best attribute = " + bestAttribute);
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
        System.out.println(dataset.numInstances());
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
           return "yes";
        } else {
            return dataset.get(instance[bestClass]).stringValue(dataset.classIndex());
        }
    }
}