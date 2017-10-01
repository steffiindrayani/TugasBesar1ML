/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugasbesar1ml;

import java.util.ArrayList;
import weka.core.Instances;

/**
 *
 * @author raudi
 */
public class PruneableMyTree {
    String attribute;
    boolean pruned;
    int numberOfValue;
    double idxClass;
    ArrayList<String> listOfStringValue;
    ArrayList<PruneableMyTree> listOfChild;
    int correctClass;
    int falseClass;
//    ArrayList<Double> listOfAttributeWeight;
//    ArrayList<Double> listOfClassWeight;
//    ArrayList<Double> listOfClassPerAttributeWeight;
//    Instances localDataset;
    ArrayList<Double> listOfValue;
   
    
    public PruneableMyTree(String attribute) {
        this.pruned = false;
        this.attribute = attribute;
        this.listOfStringValue = new ArrayList<>();
        this.listOfValue = new ArrayList<>();
        this.listOfChild = new ArrayList<>();
        this.idxClass = -1;
        this.correctClass = -1;
        this.falseClass = -1;
    }
    
    public void setIdxClass(double idx) {
        this.idxClass = idx;
    }
    
    public boolean isPruned() {
        return this.pruned;
    }
    
    public void setPruned() {
        this.pruned = true;
    }
    
    public void setUnpruned() {
        this.pruned = false;
    }

    public void setNumberOfValue(int numberOfValue) {
        this.numberOfValue = numberOfValue;
    }

    public void setListOfStringValue(ArrayList<String> listOfStringValue) {
        this.listOfStringValue = listOfStringValue;
    }
    
    public void setListOfValue(ArrayList<Double> listOfValue) {
        this.listOfValue = listOfValue;
    }

    public void setListOfChild(ArrayList<PruneableMyTree> listOfChild) {
        this.listOfChild = listOfChild;
    }

    public String getAttribute() {
        return attribute;
    }

    public int getNumberOfValue() {
        return numberOfValue;
    }

    public ArrayList<Double> getListOfValue() {
        return listOfValue;
    }
    
    public ArrayList<String> getListOfStringValue() {
        return listOfStringValue;
    }

    public ArrayList<PruneableMyTree> getListOfChild() {
        return listOfChild;
    }
    
    public void addChild(PruneableMyTree child) {
        this.getListOfChild().add(child);
    }
    
    public boolean isLeaf() {
        return this.getListOfChild().isEmpty();
    }
    
    public double getIdxClass() {
        return this.idxClass;
    }
    
    public String getValueString() {
        String value = new String();
        for (String val : this.getListOfStringValue()) {
            if (this.getListOfStringValue().lastIndexOf(val) == this.getListOfStringValue().size() - 1) {
                value += val;
            } else {
                value += (val + ", ");
            }
        }
        return value;
    }
    
    public int getValueIndex(double value) {
        return this.getListOfValue().indexOf(value);
    }
    
    public PruneableMyTree getChildFromValue(double value) {
        int index = this.getValueIndex(value);
        PruneableMyTree child = this.getListOfChild().get(index);
        return child;
    }
    
    public void printTree(String prefix, boolean isLeaf) {
        System.out.println(prefix + (isLeaf ? "└── " : "├── ") + this.getAttribute() + " [" + this.getValueString() + "]");
        for (int i = 0; i < this.getListOfChild().size() - 1; i++) {
            this.getListOfChild().get(i).printTree(prefix + (isLeaf ? "    " : "│   "), false);
        }
        if (this.getListOfChild().size() > 0) {
            this.getListOfChild().get(this.getListOfChild().size() - 1).printTree(prefix + (isLeaf ? "    " : "│   "), true);
        }
    }
}