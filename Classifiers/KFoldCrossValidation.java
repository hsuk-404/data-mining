/********************************************************************************
@FileName
    KFoldCrossValidation.java  
@Description
     A program to perform k-fold cross validation.
@Updated 04/02/2016
@Author Kush Chandra Shrestha
********************************************************************************/

import classes.ArffParser;
import classes.KFoldNaiveBayes;
public class KFoldCrossValidation {
	public static void main(String[] args) {
		ArffParser arff = new ArffParser(args); 
		new KFoldNaiveBayes(arff);
    }
}