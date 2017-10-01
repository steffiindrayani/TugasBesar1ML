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
import weka.core.Attribute;
import weka.core.Instance;
import weka.filters.unsupervised.instance.RemovePercentage;
//import org.apache.commons.math3.distribution.NormalDistribution;

/**
 *
 * @author raudi
 */
public class MyC45 extends AbstractClassifier {
    
    protected PruneableMyTree m_root;
    protected boolean isReducedError = true;
    protected ArrayList< LinkedHashMap<String,Double> > listOfRules = new ArrayList<>();
    protected ArrayList<Double> rulesError = new ArrayList<>();
    protected double confidence;
    
    public PruneableMyTree getModel() {
        return m_root;
    }
    
    public void pruneTree(Instances dataset, PruneableMyTree tree) {
        if (!tree.isLeaf()) {
            for(int i=0;i<tree.getListOfChild().size();i++) {
                pruneTree(dataset, tree.getListOfChild().get(i));
            }
            if (calculateErrorTree(tree) > calculateErrorLeaf(tree)) {
                tree.setListOfChild(null);
                tree.setListOfStringValue(null);
                tree.setListOfValue(null);
                tree.setNumberOfValue(0);
                int maks = 0;
                int maksIdx = -1;
                for(int i=0;i<tree.getListOfNumClass().size();i++) {
                    if (tree.getListOfNumClass().get(i) > maks) {
                        maks = tree.getListOfNumClass().get(i);
                        maksIdx = i;
                    }
                }
                tree.setAttribute(dataset.classAttribute().value(maksIdx));
            }
        }
    }
    
    public void prune(Instances dataset, PruneableMyTree tree) {
        if (isReducedError) {
            classifyInstancesForReducedPruning(dataset,tree);
            pruneTree(dataset, tree);
        } else {
            LinkedHashMap<String,Double> emptyRule = new LinkedHashMap<>();
            treeToRules(m_root,emptyRule);
            System.out.println("Sebelum Pruning");
            for(int i=0;i<listOfRules.size();i++) {
                System.out.println(listOfRules.get(i));
            }
            pruningRules(dataset);
            
            System.out.println("Sesudah Pruning");
        }
    }
    
    public double calculateErrorTree(PruneableMyTree tree) {
        double treeError = 0;
        if (tree.isLeaf()) {
            return calculateErrorLeaf(tree);
        } else {
            for(int i=0;i<m_root.getListOfChild().size();i++) {
                treeError += calculateErrorLeaf(m_root.getListOfChild().get(i));
            } 
        }
        return treeError;
    }
    
    public double calculateErrorLeaf(PruneableMyTree tree) {
        return 0;
    }
    public void calculateValidationSet(Instances validationSet) {
        
    }
    
    // 0 artinya salah nebak, 1 artinya bener nebak, 2 artinya ga bisa ditebak
    public String classifyInstanceForReducedPruning(Instance instance, PruneableMyTree tree) {
        int size = instance.numAttributes();
        String att = tree.getAttribute();
        String c = null;
        if (tree.isLeaf()) {
            c = tree.getAttribute();
            double cls = -1;
            for (int i = 0; i < instance.numClasses(); i++) {
                String cIns = instance.attribute(instance.classIndex()).value(i);
                if (c.equals(cIns)) {
                    cls = (double) i;
                }
            }
            tree.setListOfNumClass((int)cls,tree.getListOfNumClass().get((int)cls)+1);
            if (cls != instance.classValue()) {
                tree.setWrongClass(tree.getWrongClass()+1);
            } else {
                tree.setCorrectClass(tree.getCorrectClass()+1);
            }
        } else {
            int index = 0;
            for (int i = 0; i < size; i++) {
                if (instance.attribute(i).name().equals(att)) {
                    index = i;
                }
            }
            if (!Double.isNaN(instance.value(index))) {
                PruneableMyTree child = tree.getChildFromValue(instance.value(index));
                c = classifyInstanceForReducedPruning(instance, child);
                double cls = -1;
                for (int i = 0; i < instance.numClasses(); i++) {
                    String cIns = instance.attribute(instance.classIndex()).value(i);
                    if (c.equals(cIns)) {
                        cls = (double) i;
                    }
                }
                tree.setListOfNumClass((int)cls,tree.getListOfNumClass().get((int)cls)+1);
                if (cls != instance.classValue()) {
                    tree.setWrongClass(tree.getWrongClass()+1);
                } else {
                    tree.setCorrectClass(tree.getCorrectClass()+1);
                }
            }
        }
        return c;
        // gabakal masuk juga
    }
    
    public void classifyInstancesForReducedPruning(Instances instances, PruneableMyTree tree) {
        
        for(int i=0;i<instances.numInstances();i++) {
            classifyInstanceForReducedPruning(instances.instance(i),tree);
        }
        
    }
    
    public void treeToRules(PruneableMyTree tree, LinkedHashMap<String,Double> priorRule) {
        if (tree.isLeaf()) {
            priorRule.put("class",tree.getIdxClass());
            this.listOfRules.add(priorRule);
        } else if (m_root == tree) {
            for(int i=0;i<tree.getListOfValue().size();i++){
                LinkedHashMap<String,Double> rule = new LinkedHashMap<String,Double>();
                rule.put(tree.getAttribute(),(double)i);
                treeToRules(tree.getChildFromValue(tree.getValueIndex(i)),rule);
            }
        } else {
            for(int i=0;i<tree.getListOfValue().size();i++){
                LinkedHashMap<String,Double> rule = new LinkedHashMap<String,Double>(priorRule);
                rule.put(tree.getAttribute(),(double)i);
                System.out.println(tree.getAttribute()+" "+i);
                m_root.printTree("",false);
                treeToRules(tree.getListOfChild().get(tree.getValueIndex(i)),rule);
            }
        }
    }
    
    // 0 artinya salah nebak, 1 artinya bener nebak, 2 artinya ga bisa ditebak
    public int classifyInstanceForRulePruning(Instance instance, LinkedHashMap<String,Double> rule) {
        for(Map.Entry r:rule.entrySet()){
            if (r.getKey().equals("class")) {
                if ((double) r.getValue() == instance.classValue()) {
                    return 1;
                } else {
                    return 0;
                }
            }
            int index = -1;
            for (int i = 0; i < instance.numAttributes(); i++) {
                if (instance.attribute(i).name().equals(r.getKey())) {
                    index = i;
                }
            }
            if ((double)r.getValue() != instance.value(index)) {
                return 2;
            }   
        }
        // gabakal masuk juga
        return 0;
    }
    
    public int[] classifyInstancesForRulePruning(Instances instances, LinkedHashMap<String,Double> rule) {
        int[] result = new int[3];
        for(int i=0;i<3;i++) {
            result[i] = 0;
        }
        for(int i=0;i<instances.numInstances();i++) {
            result[classifyInstanceForRulePruning(instances.instance(i),rule)]++;
        }
        return result;
    }
    
    
    public void pruningRules(Instances instances) {
        for(int i=0;i<this.listOfRules.size();i++) {
            LinkedHashMap<String,Double> rule = new LinkedHashMap<String,Double>(listOfRules.get(i));
            int[] result = classifyInstancesForRulePruning(instances,rule);
            double priorError = calculateErrorRulePruning(result);
            double newError = priorError;
            String keyWillBeRemoved = "";
            for(;;) {
                double minimumError = 99999999;
                for(Map.Entry r:rule.entrySet()) {
                    LinkedHashMap<String,Double> tempRule = new LinkedHashMap<String,Double>(rule);
                    tempRule.remove(r.getKey());
                    int[] tempResult = classifyInstancesForRulePruning(instances,tempRule);
                    double error = calculateErrorRulePruning(tempResult);
                    if (minimumError>error) {
                        minimumError = error;
                        keyWillBeRemoved = (String) r.getKey();
                    }
                }
                newError = minimumError;
                if (priorError <= newError) {
                    break;
                } else {
                    priorError = newError;
                    rule.remove(keyWillBeRemoved);
                }
            }
            this.listOfRules.set(i, rule);
            this.rulesError.add(priorError);
        }
        for(int i=0;i<rulesError.size();i++) {
            double min = 99999999;
            int idx = -1;
            for(int j=i;j<rulesError.size();j++) {
                if (rulesError.get(j)<min) {
                    min = rulesError.get(j);
                    idx = j;
                }
            }
            Collections.swap(listOfRules, i, idx);
            Collections.swap(rulesError, i, idx);
        }
        System.out.println(listOfRules);
        System.out.println(rulesError);
        
    }
    
    public String classifyRule(Instance instance) {
        String c = "";
        for(int i=0;i<this.listOfRules.size();i++) {
            LinkedHashMap<String,Double> rule = new LinkedHashMap<String,Double>(listOfRules.get(i));
            for(Map.Entry r:rule.entrySet()){
                if (r.getKey().equals("class")) {
                    //System.out.println(r.getValue());
                    return instance.classAttribute().value((int)(double)r.getValue());
                }
                int index = -1;
                for (int j = 0; j < instance.numAttributes(); j++) {
                    if (instance.attribute(j).name().equals(r.getKey())) {
                        index = j;
                    }
                }
                if (index == -1) {
                    System.out.println("Error at" +r.getKey());
                
                }
                if ((double)r.getValue() != instance.value(index)) {
                    break;
                }   
            }
        }
        // harusnya ga masuk sini
        return c;
    }
    
    public double calculateErrorRulePruning(int[] result) {
        double alpha = 1.0 - confidence;
        //NormalDistribution distribution = new NormalDistribution(mean, standardDev);
        //double z = distribution.inverseCumulativeProbability(alpha);
        
        return (double)result[0]/(double)(result[0]+result[1]);
    }
    
    public void ruleToTree(PruneableMyTree tree) {
        for(int i=0;i<this.listOfRules.size();i++) {
            LinkedHashMap<String,Double> rule = this.listOfRules.get(i);
            for(Map.Entry r:rule.entrySet()){
                if (tree.getAttribute() == (String) r.getKey()) {
                    
                } else {
                    
                }
                PruneableMyTree newTree = new PruneableMyTree((String) r.getKey());
                ArrayList<Double> listOfValue = new ArrayList<>();
                ArrayList<PruneableMyTree> listOfChild = new ArrayList<>();
                listOfValue.add((Double) r.getValue());
                PruneableMyTree child = new PruneableMyTree("asd");
                listOfChild.add(child);
                System.out.println(r.getKey()+" "+r.getValue());  
            }
            
            
        }
    }
    
    // Edited
    public Instances handleAttributeContinuesValue(Instances instances, int attributeIdx) {
        Instances dataset = new Instances(instances);
        double gain = countEntropy(dataset);
        dataset.sort(attributeIdx);
	int currentClassValue = (int)dataset.instance(0).classValue();
        int maxGainIdx = -1;
        double maxGain = 0;
        ArrayList<Integer> candidateIdx = new ArrayList<>();
	for(int i=0;i<dataset.numInstances();i++) {
            if (dataset.instance(i).classValue() != currentClassValue) {
                candidateIdx.add(i-1);
                currentClassValue = (int)dataset.instance(i).classValue();
            }
	}
        System.out.println(candidateIdx.size());
        Collections.shuffle(candidateIdx);
        for (int i=0; i< Math.min(candidateIdx.size(),10); i++) {
            double tempGain = gain;
            Instances leftDataset = new Instances(dataset, 0, candidateIdx.get(i));
            Instances rightDataset = new Instances(dataset, candidateIdx.get(i)+1, dataset.numInstances()-(candidateIdx.get(i)+1));
            tempGain -= (double) rightDataset.numInstances() / (double) dataset.numInstances() * countEntropy(leftDataset);
            tempGain -= (double) rightDataset.numInstances() / (double) dataset.numInstances() * countEntropy(leftDataset);
            if (maxGain < tempGain) {
                maxGain = tempGain;
                maxGainIdx = candidateIdx.get(i);
            }
        }
        double threshold = (dataset.instance(maxGainIdx).value(attributeIdx) + dataset.instance(maxGainIdx+1).value(attributeIdx))/2;
        ArrayList<String> values = new ArrayList<>(); /* FastVector is now deprecated. Users can use any java.util.List */
        values.add("<"+threshold);              /* implementation now */
        values.add(">="+threshold);              /* implementation now */
        Instances newDataset = new Instances(dataset);
        newDataset.replaceAttributeAt(new Attribute(dataset.attribute(attributeIdx).name(), values), attributeIdx);
        for(int i=0;i<dataset.numInstances();i++) {
            if (dataset.instance(i).value(attributeIdx) < threshold) {
                newDataset.instance(i).setValue(attributeIdx, 0);
            } else {
                newDataset.instance(i).setValue(attributeIdx, 1);
            }
            
        }
        return newDataset;
    }

    @Override
    public void buildClassifier(Instances dataset) throws Exception {
        //Delete missing value
        dataset.deleteWithMissingClass();
        for(int i=0;i<dataset.numAttributes();i++) {
            if (dataset.attribute(i).isNumeric()) {
                dataset = handleAttributeContinuesValue(dataset,i);
            }
        }
        for(int i=0;i<dataset.numInstances();i++) {
            dataset.instance(i).setWeight(1);
        }
        ArrayList<Integer> attributeList = new ArrayList<>(); 
        for (int i = 0; i < dataset.numAttributes() - 1; i++) {
            attributeList.add(i);
        }
        if (isReducedError) {
            //splitData
            Random rand = new Random(1);   // create seeded number generator
            Instances randData = new Instances(dataset);   // create copy of original data
            System.out.println(randData.size());
            randData.randomize(rand);         // randomize data with number generator
            Instances train = randData.trainCV(2, 1);
            Instances validationSet = randData.testCV(2, 1);
            System.out.println(train);
            System.out.println(validationSet);
            
            m_root = buildTree(dataset, attributeList);
            prune(dataset);
            m_root.printTree(" ", false);
        } else {
            m_root = buildTree(dataset, attributeList);
            prune(dataset);
            for(int i=0;i<listOfRules.size();i++) {
                System.out.println(listOfRules.get(i));
            }
        }
        
       
    }

    public PruneableMyTree buildTree(Instances dataset, ArrayList<Integer> attributes) {
        if (isOneElement(dataset)) {
            String c = dataset.get(0).stringValue(dataset.classIndex());
            PruneableMyTree tree = new PruneableMyTree(c);
            tree.setIdxClass(dataset.get(0).value(dataset.classIndex()));
            return tree;
        } else if (attributes.isEmpty()) {
            String c = mostCommonClassValue(dataset);
            PruneableMyTree tree = new PruneableMyTree(c);
            int size = dataset.numClasses();
            for (int i = 0; i < size; i++) {
                String cIns = dataset.attribute(dataset.classIndex()).value(i);
                if (c.equals(cIns)) {
                    tree.setIdxClass(i);
                }
            }
            return tree;
        } else {
            
            //choose best attribute
            int bestAttribute = chooseBestAttribute(dataset, attributes);
            System.out.println(bestAttribute);
            PruneableMyTree tree = new PruneableMyTree(dataset.attribute(bestAttribute).name());
            ArrayList<Double> listOfValue = new ArrayList<>();
            ArrayList<String> listOfString = new ArrayList<>();
            ArrayList<PruneableMyTree> listOfChild = new ArrayList<>();
            ArrayList<Integer> listOfNumClass = new ArrayList<>();
            Instances[] split = seperateData(dataset, bestAttribute);
            attributes.remove(Integer.valueOf(bestAttribute));
            for (int i = 0; i < dataset.attribute(bestAttribute).numValues(); i++) {
                if (split[i].numInstances() == 0) {
                    String c = mostCommonClassValue(dataset);
                    PruneableMyTree child = new PruneableMyTree(c);
                    int size = dataset.numClasses();
                    for (int j = 0; j < size; j++) {
                        String cIns = dataset.attribute(dataset.classIndex()).value(j);
                        if (c.equals(cIns)) {
                            child.setIdxClass(i);
                        }
                    }
                    listOfValue.add((double) i);
                    listOfChild.add(child);
                } else {
                    PruneableMyTree child = buildTree(split[i], attributes);
                    listOfValue.add((double) i);
                    listOfString.add(dataset.attribute(bestAttribute).value(i));
                    listOfChild.add(child);
                }
            }
            for(int i=0;i<dataset.numClasses();i++) {
                listOfNumClass.add(0);
            }
            tree.setListOfNumClass(listOfNumClass);
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
            if (attributes.size()==16) {
                System.out.println(countGainRatio(dataset, attributes.get(i)));
                System.out.println(dataset.attribute(i).name());
            }
            if (countGainRatio(dataset, attributes.get(i)) > bestGainRatio) {
                bestAttribute = attributes.get(i);
                bestGainRatio = countGainRatio(dataset, attributes.get(i));
            }
        }
        System.out.println(attributes.size());
        return bestAttribute;
    }
    
    // Edited
    public double countGainRatio(Instances dataset, int attribute) {
        double informationGain = countInformationGain(dataset, attribute);
        System.out.println("IG "+informationGain);
        double splitInformation = countSplitInformation(dataset, attribute);
        System.out.println("SI "+splitInformation);
        return informationGain/splitInformation;
    }
    
    // Edited
    public double countSplitInformation(Instances dataset, int attribute) {
        double splitInformation = 0;
        Instances[] split = seperateData(dataset, attribute);
        for (int i = 0; i < dataset.attribute(attribute).numValues(); i++) {
            if (split[i].sumOfWeights() > 0) {
                splitInformation -= (double) split[i].sumOfWeights() / (double) dataset.sumOfWeights() * Utils.log2((double) split[i].sumOfWeights() / (double) dataset.sumOfWeights());
            }
        }
        return splitInformation;
    }
    
    public double countInformationGain(Instances dataset, int attribute) {
        double gain = countEntropy(dataset);
        Instances[] split = seperateData(dataset, attribute);
        for (int i = 0; i < dataset.attribute(attribute).numValues(); i++) {
            System.out.println("SPLIT "+i+" "+split[i].size());
            System.out.println("WEIGHT "+i+" "+split[i].sumOfWeights());
            
            if (split[i].sumOfWeights() > 0) {
                gain -= (double) split[i].sumOfWeights() / (double) dataset.sumOfWeights() * countEntropy(split[i]);
            }
        }
        return gain;
    }
    
    //Edited
    public double countEntropy(Instances dataset) {
        double entropy = 0;
        
        //Count each class value in dataset
        double[] countClass = new double[dataset.numClasses()];
        for (int i = 0; i < dataset.numInstances(); i++) {
            countClass[(int) dataset.get(i).classValue()] += dataset.get(i).weight();
        }
                
        //Count entropy
        for (int i = 0; i < dataset.numClasses(); i++) {
            if (countClass[i] > 0) {
                entropy -= countClass[i]/ (double) dataset.sumOfWeights() * Utils.log2(countClass[i]/ (double) dataset.sumOfWeights());
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
            if (Double.isNaN(dataset.get(i).value(attribute))) {
                split[size-1].add(dataset.get(i));
            } else {  
                split[(int) dataset.get(i).value(attribute)].add(dataset.get(i));
            }
        }
        double[] weight = new double[size-1];
        
        for (int i=0;i<weight.length;i++) {
            weight[i] = (double)split[i].numInstances()/(dataset.numInstances()-split[size-1].numInstances());
        }
        
//        if (!split[size-1].isEmpty()) {
//            System.out.println("Sebelum "+split[0].instance(0).weight()+"\n Attribute "+attribute+"\n"+split[0]);
//            
//        }
        for(int i=0;i<split.length-1;i++) {
            for(int j=0;j<split[size-1].numInstances();j++) {
                split[size-1].instance(j).setWeight(weight[i]);
                split[size-1].instance(j).setValue(attribute,dataset.attribute(attribute).value(i));
                split[i].add(split[size-1].instance(j));
            }
        }
//        if (!split[size-1].isEmpty()) {
//            System.out.println("Sesudah\n Attribute "+attribute+"\n"+split[0]);
//            
//        }
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
    
    public String classify(Instance inst, PruneableMyTree tree) {
        int size = inst.numAttributes();
        String att = tree.getAttribute();
        String c = null;
        if (tree.isLeaf()) {
            c = tree.getAttribute();
        } else {
            int index = 0;
            for (int i = 0; i < size; i++) {
                if (inst.attribute(i).name().equals(att)) {
                    index = i;
                }
            }
            if (!Double.isNaN(inst.value(index))) {
                PruneableMyTree child = tree.getChildFromValue(inst.value(index));
                c = classify(inst, child);
            }
        }
        return c;
    }
    
    @Override
    public double classifyInstance(Instance instance) {
        double cls = 0.0;
        String c = "";
        if (isReducedError) {
           c = classify(instance, this.m_root);
        } else {
           c = classifyRule(instance); 
        }
        int size = instance.numClasses();
        for (int i = 0; i < size; i++) {
            String cIns = instance.attribute(instance.classIndex()).value(i);
            if (c.equals(cIns)) {
                cls = (double) i;
            }
        }
        return cls;
    }
    
    public double classifyInstances(Instances dataset, PruneableMyTree tree) {
        int size = dataset.numInstances();
        int successCount = 0;
        for (int i = 0; i < size; i++) {
            String classifiedClass = classify(dataset.get(i), tree);
            String originalClass = dataset.get(i).stringValue(dataset.classIndex());
            if (originalClass.equals(classifiedClass)) {
                successCount++;
            }
        }
        double accuracy = (double) successCount / (double) size * 100;
        return accuracy;
    }
}
