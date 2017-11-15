/********************************************************************************
@FileName
    DecisionTreeInduction.java  
@Description
    A program to perform supervised pattern classification using decision trees 
@Updated 02/11/2016
@Author Kush Chandra Shrestha
********************************************************************************/
import classes.*;
public class DecisionTreeInduction {
	public static void main(String[] args) {
		ArffParser arff = new ArffParser(args);  		//Gets the data parsed from the input file
		new DTDecisionTree(arff);								//Takes the parsed data and builds decision tree
    }
}