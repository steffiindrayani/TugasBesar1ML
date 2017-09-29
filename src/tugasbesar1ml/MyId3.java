/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugasbesar1ml;

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
        
        //Check if there are missing value attributes
    }

    public void buildTree(Instances dataset, int[] attributes) {
        if (dataset.numClasses() == 1) {
            //specify one root only
        } else if (attributes.length == 0) {
            //mostcommonvalue
        } else {
            //choose best attribute
            int bestAttribute = chooseBestAttribute(dataset);
            
        }
    }
    
    public int chooseBestAttribute(Instances dataset) {
        int bestAttribute = 0;
        double bestGain = countInformationGain(dataset, 0);
        for (int i = 1; i < dataset.numAttributes() - 1; i++) {
            if (countInformationGain(dataset,i) > bestGain) {
                bestAttribute = i;
                bestGain = countInformationGain(dataset, i);
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
        Instances[] split = new Instances[dataset.get(attribute).numValues()];
        for (int i = 0; i < dataset.get(attribute).numValues(); i++) {
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
    
    public double mostCommonClassValue(Instances dataset) {
        int[] countClass = new int[dataset.numClasses()];
        for (int i = 0; i < dataset.numInstances(); i++) {
            countClass[(int) dataset.get(i).classValue()]++;
        }
        double bestClass = 0.0;
        int maxClass = countClass[0];
        for (int i = 1; i < dataset.numClasses(); i++) {
            if (countClass[i] > maxClass) {
                bestClass = (double) i;
                maxClass = countClass[i];
            }
        }
        return bestClass;
    }
}
