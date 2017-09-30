/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugasbesar1ml;

import java.util.ArrayList;

/**
 *
 * @author Vitra Chandra
 */
public class MyTree {
    String attribute;
    int numberOfValue;
    ArrayList<Double> listOfValue;
    ArrayList<String> listOfStringValue;
    ArrayList<MyTree> listOfChild;

    public MyTree() {
    }
    
    public MyTree(String attribute) {
        this.attribute = attribute;
        this.listOfValue = new ArrayList<>();
        this.listOfStringValue = new ArrayList<>();
        this.listOfChild = new ArrayList<>();
    }

    public void setNumberOfValue(int numberOfValue) {
        this.numberOfValue = numberOfValue;
    }

    public void setListOfValue(ArrayList<Double> listOfValue) {
        this.listOfValue = listOfValue;
    }

    public void setListOfStringValue(ArrayList<String> listOfStringValue) {
        this.listOfStringValue = listOfStringValue;
    }

    public void setListOfChild(ArrayList<MyTree> listOfChild) {
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

    public ArrayList<MyTree> getListOfChild() {
        return listOfChild;
    }
    
    public void addChild(MyTree child) {
        this.getListOfChild().add(child);
    }
    
    public int getValueIndex(double value) {
        return this.getListOfValue().indexOf(value);
    }
    
    public boolean isLeaf() {
        return this.getListOfChild().isEmpty();
    }
    
    public MyTree getChildFromValue(double value) {
        int index = this.getValueIndex(value);
        MyTree child = this.getListOfChild().get(index);
        return child;
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
