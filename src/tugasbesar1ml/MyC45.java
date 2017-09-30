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
    
    protected PruneableMyTree m_root;
    protected boolean isReducedError = false;
    
    public void prune() {
        if (isReducedError) {
            double threshold = tree.classifyInstances(validationSet);
            ArrayList<PruneableMyTree> trees = new ArrayList<>();
            buildListOfTrees(m_root, trees);
            double[] accuracySubTrees = new double[trees.size()];
            double maxAccuracy = 0;
            int maxIndexSubTrees = -1;
        }
    }
    
    public void buildListOfTrees(PruneableMyTree tree, ArrayList<PruneableMyTree> trees) {
        
    }
    // Edited
    public void pruneErrorReduced(MyTree tree, Instances validationSet) {
        double threshold = tree.classifyInstances(validationSet);
        for(;;) {
            ArrayList<MyTree> trees = new ArrayList<>();
            double[] accuracySubTrees = new double[trees.size()];
            double maxAccuracy = 0;
            int maxIndexSubTrees = -1;
            for(int i=0;i<trees.size();i++) {
                accuracySubTrees[i] = trees.get(i).classifyInstances(validationSet);
                if (accuracySubTrees[i] > maxAccuracy) {
                    maxAccuracy = accuracySubTrees[i];
                    maxIndexSubTrees = i;
                }
            }
            if (maxIndexSubTrees < threshold) {
                break;
            }
        }
        
    }
    
    public void pruneRuleBased(MyTree tree) {
        
    }
    
    // Edited
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
    
    public void handleMissingValue() {
        
    }
            
    // Edited
    public int chooseBestAttribute(Instances dataset, ArrayList<Integer> attributes) {
        int bestAttribute = -1;
        double bestGainRatio = -999;
        for (int i = 0; i < attributes.size(); i++) {
            if (countGainRatio(dataset, attributes.get(i)) > bestGainRatio) {
                bestAttribute = attributes.get(i);
                bestGainRatio = countInformationGain(dataset, attributes.get(i));
            }
        }
        return bestAttribute;
    }
    
    // Edited
    public double countGainRatio(Instances dataset, int attribute) {
        double informationGain = countInformationGain(dataset, attribute);
        double splitInformation = countSplitInformation(dataset, attribute);
        return informationGain/splitInformation;
    }
    
    // Edited
    public double countSplitInformation(Instances dataset, int attribute) {
        double splitInformation = 0;
        Instances[] split = seperateData(dataset, attribute);
        for (int i = 0; i < dataset.attribute(attribute).numValues(); i++) {
            if (split[i].numInstances() > 0) {
                splitInformation -= (double) split[i].numInstances() / (double) dataset.numInstances() * Utils.log2((double) split[i].numInstances() / (double) dataset.numInstances());
            }
        }
        return splitInformation;
    }
    
    public double countInformationGain(Instances dataset, int attribute) {
        double gain = countEntropy(dataset);
        Instances[] split = seperateData(dataset, attribute);
        double nMissingValue = split[split.length-1].numInstances();
        double[] fraction = new double[split.length-2];
        for (int i=0;i<fraction.length;i++) {
            fraction[i] = nMissingValue*split[i].numInstances()/(dataset.numInstances()-nMissingValue);
        }
        for (int i = 0; i < dataset.attribute(attribute).numValues(); i++) {
            if (split[i].numInstances() > 0) {
                gain -= (double) (split[i].numInstances()+fraction[i]) / (double) dataset.numInstances() * countEntropyC45(split[i],fraction[i],split[split.length-1]);
            }
        }
        return gain;
    }
    
    public double countEntropyC45(Instances dataset, double fraction, Instances missingValueDataset) {
        double entropy = 0;
        
        //Count each class value in dataset
        int[] countClass = new int[dataset.numClasses()];
        for (int i = 0; i < dataset.numInstances(); i++) {
            countClass[(int) dataset.get(i).classValue()]++;
        }
        
        int[] countClassMissingValue = new int[missingValueDataset.numClasses()];
        for (int i = 0; i < missingValueDataset.numInstances(); i++) {
            countClassMissingValue[(int) missingValueDataset.get(i).classValue()]++;
        }
                
        //Count entropy
        for (int i = 0; i < dataset.numClasses(); i++) {
            if (countClass[i] > 0) {
                entropy -= (countClass[i]+fraction*countClassMissingValue[i])/ (double) (dataset.numInstances()+missingValueDataset.numInstances()*fraction) * Utils.log2((countClass[i]+fraction*countClassMissingValue[i])/ (double) (dataset.numInstances()+missingValueDataset.numInstances()*fraction));
            }
        }
        return entropy;
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
    
    // Edited
    public Instances[] seperateData(Instances dataset, int attribute) {
        int size = dataset.attribute(attribute).numValues()+1;
        Instances[] split = new Instances[size];
        for (int i = 0; i < size; i++) {
            split[i] = new Instances(dataset, dataset.numInstances());
        }
        for (int i = 0; i < dataset.numInstances(); i++) {
            // Ragu
            if (dataset.get(i).value(attribute) == -1) {
                split[size-1].add(dataset.get(i));
            } else {  
                split[(int) dataset.get(i).value(attribute)].add(dataset.get(i));
            }
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
