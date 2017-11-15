/********************************************************************************
@FileName
    DTDecisionTree.java  
@Description
    Implementation of DecisionTree class. DecisionTree takes the parsed
    data from arffParser object and builds a decision tree and writes it in files.
@Updated 03/08/2016
@Author Kush Chandra Shrestha
********************************************************************************/
package classes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DTDecisionTree {
	private HashMap<String, List<String>> hm_attributeClasses = new HashMap<String, List<String>>();
	private List<List<String>> data = new ArrayList<List<String>>();
	private List<List<String>> allData = new ArrayList<List<String>>();
	private String relation, class_name, input_file_name;
	private int percentage;
	private String filenamePrefix = "output_";

	/********************************************************************************
	@Description
	    Method Name: Constructor for DecisionTree
	    	=> Initializes all class attributes
	        => Processes the parsed data from arff parser 
	        => Creates the decision tree
	        => Predicts the training set
	    Parameters: ArffParser arff => that holds the parsed input file
	********************************************************************************/
	public DTDecisionTree(ArffParser arff){
		List<List<String>> data = arff.getInputData();
    	HashMap<String, List<String>> attribute_classes = arff.getAttributeClasses();
    	this.relation = arff.getRelation();
    	this.percentage = Integer.parseInt(arff.getPercentage());
    	this.class_name = arff.getClassName();
    	this.input_file_name = arff.getFileName().replace(".arff", "");
		this.data.add(data.get(0));
		this.data.addAll(data.subList(1, (this.percentage * data.size() / 100)));
		this.allData.addAll(data);
		this.hm_attributeClasses = attribute_classes;

		DTNode x = new DTNode();
		this.generateDecisionTree(this.data, x);
		File output_file = new File(this.filenamePrefix + "Training" + this.percentage + this.input_file_name + ".dt");
		try{
			FileWriter fWriter = new FileWriter(output_file);
	        PrintWriter pWriter = new PrintWriter (fWriter);
	        this.printTree(x, 0, pWriter); 	//Writes the decision tree on the file which is a recursive function
			pWriter.close();
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
			e.printStackTrace();
		}
		// Decision Tree and the full data set is given to predict the data
		this.predictTrainingSet(this.allData, x);		//Predicts the training set and write the new training set on the file
	}
	
	/********************************************************************************
	@Description
	    Method Name: generateDecisionTree
	    		=> Generates the decision tree for the given dataset
	    		=> Gets splitting attribute
	    		=> Recurse the same function with evey attribute values for the splitted
	    			attribute
	    Parameters: 
	    		List<List<String>> _data => matrix of data
	    		DTNode dt 				 => decision tree for recursion	
	********************************************************************************/
	public DTNode generateDecisionTree(List<List<String>> _data, DTNode dt){
		
		if(isEmptyTable(_data)){
			// checks if the data is empty
//			System.out.println("*************empty**************");
			dt.setIsLeaf(true);
			dt.setLabel(getMostFrequentClass(_data));
			return dt;
		}
		if(isHomogenousClass(_data)){
			// checks if all the classes are same for the given dataset
			dt.setIsLeaf(true);
			dt.setLabel(_data.get(1).get(_data.get(1).size() - 1));
			return dt;
		}
		if(isHomogenousRecords(_data)){
			// checks if all the records are same for the given dataset
			dt.setIsLeaf(true);
			dt.setLabel(getMostFrequentClass(_data));
			return dt;
		}
		// gets splitting attribute by calculating maximum gain
		String splitAttr = getMaximumGainAttribute(_data);
		
		dt.setSplitAttribute(splitAttr);
		// get attribute values for the splitted class
		List<String> attributeValues = this.hm_attributeClasses.get(splitAttr);
		
		for(int i = 0; i < attributeValues.size(); i++){
			List<List<String>> splittedTable = this.splitTable(_data, splitAttr, attributeValues.get(i));
			
			if(splittedTable.get(0).size() > 1){
				DTNode newDt = new DTNode();
				newDt.setLabel(attributeValues.get(i));
				newDt.setIsLeaf(false);
				dt.addChildrenValue(attributeValues.get(i));
				dt.addChildren(this.generateDecisionTree(splittedTable, newDt));
			}
		}
		return dt;
	}
	
	/********************************************************************************
	@Description
	    Method Name: printTree
	    		=> Writes the decision tree on file
	    Parameters: 
	    		DTNode x				=> Decision tree
	    		int depth			=> depth of the decision tree to maintain the alignment
	    		PrintWriter pWriter => writer object that appends the content on file
	********************************************************************************/
	public void printTree(DTNode x, int depth, PrintWriter pWriter){
		if(x != null){
			if(x.isLeaf){
				pWriter.print(" class = " + x.getLabel());
			}else if(!x.getChildren().isEmpty() && !x.isLeaf){
				for(int i = 0; i < x.getChildren().size(); i++){
					pWriter.print("\n");
					for(int j = 0; j < depth; j++){
						pWriter.print("|\t");
					}
					pWriter.print(x.getSplitAttribute() + " = ");
					pWriter.print(x.getChildrenValues().get(i));
					printTree(x.getChildren().get(i), depth + 1, pWriter);
				}
			}
		}
	}

	/********************************************************************************	
	@Description
	    Method Name: testDecisionTree
	    		=> Returns the prediction of particular record implementing the 
	    			decision tree
	    Parameters: 
	    		List<String> record 	=> A single record
	    		DTNode dt 				=> Given decision tree
	********************************************************************************/
	public String testDecisionTree(List<String> record, DTNode dt){
		String prediction = "";
		while(!dt.isLeaf && !dt.children.isEmpty()){
			String DTNodeAttribute = dt.splitAttribute;
			int ind = getAttributeIndex(DTNodeAttribute);
			String val = record.get(ind);
			int childIndex = returnIndexOfChild(dt.childrenValues, val);
			dt = dt.children.get(childIndex);
			prediction = dt.label;
			if(dt.isLeaf){
				break;
			}
		}
		return prediction;
	}


	/********************************************************************************
	@Description
	    Method Name: predictTrainingSet
	    		=> Predicts the given training set with given decision tree
	    Parameters: List<List<String>> data => Data set that has to be predicted
	    			DTNode x 					=> Decision Tree
	********************************************************************************/
	public void predictTrainingSet(List<List<String>> data, DTNode x){
		HashMap<String, List<Integer>> confusionMatrix = new HashMap<String, List<Integer>>();
		String class_string = this.data.get(0).get(this.data.get(0).size() - 1);
		for(int n = 0; n < this.hm_attributeClasses.get(class_string).size(); n++){
			List<Integer> temp = new ArrayList<Integer>();
			temp.add(0);
			temp.add(0);
			confusionMatrix.put(this.hm_attributeClasses.get(class_string).get(n), temp);
		}
		
		File output_file = new File(this.filenamePrefix + "Application" + this.percentage + this.input_file_name + ".arff");
		File output_file1 = new File(this.filenamePrefix + "Accuracy" + this.percentage + this.input_file_name + ".txt");
		try{
			FileWriter fWriter = new FileWriter(output_file);
			FileWriter fWriter1 = new FileWriter(output_file1);
	        PrintWriter pWriter = new PrintWriter (fWriter);
	        PrintWriter pWriter1 = new PrintWriter (fWriter1);
	        
	        pWriter.println("@relation " + this.relation);
	        pWriter.println();
	        for(int i = 0; i < this.hm_attributeClasses.size(); i++){
	        	pWriter.print("@attribute " + this.data.get(0).get(i) + " ");
	        	pWriter.print("{");
	        	for(int j = 0; j < this.hm_attributeClasses.get(this.data.get(0).get(i)).size(); j++){
	        		pWriter.print(this.hm_attributeClasses.get(this.data.get(0).get(i)).get(j));
	        		if(j != this.hm_attributeClasses.get(this.data.get(0).get(i)).size() - 1){
	        			pWriter.print(",");
	        		}
	        	}
	        	pWriter.println("}");
	        }
	        pWriter.println("@attribute dtClass real");
	        // for(int j = 0; j < this.hm_attributeClasses.get(this.data.get(0).get(this.data.get(0).size() - 1)).size(); j++){
	        // 	pWriter.print(this.hm_attributeClasses.get(this.data.get(0).get(this.data.get(0).size() - 1)).get(j));
        	// 	if(j != this.hm_attributeClasses.get(this.data.get(0).get(this.data.get(0).size() - 1)).size() - 1){
        	// 		pWriter.print(",");
        	// 	}
	        // }
	        // pWriter.println("}");
	        pWriter.println();
	        pWriter.println("@data");
			
			pWriter1.println("-----------------------------------------");
	        pWriter1.println("CONFUSION MATRIX");
	        pWriter1.println("-----------------------------------------");
	        
	        for(int n = 0; n < this.hm_attributeClasses.get(class_string).size(); n++){
	        	pWriter1.print("\t\t" + this.hm_attributeClasses.get(class_string).get(n));
	        }
	        pWriter1.println();
	        pWriter1.println("-----------------------------------------");

			int total_ = 0;
			int true_ = 0;
			int[][] confusion = new int[this.hm_attributeClasses.get(class_string).size()][this.hm_attributeClasses.get(class_string).size()];
				
	        for(int i = 1; i < data.size(); i++){
				for(int j = 0; j < data.get(i).size(); j++){
					pWriter.print(data.get(i).get(j) + " ");
				}
				String prediction = testDecisionTree(data.get(i), x);
				pWriter.println(prediction);
				for(int m = 0; m < this.hm_attributeClasses.get(class_string).size(); m++){
					for(int n = 0; n < this.hm_attributeClasses.get(class_string).size(); n++){
						if(this.hm_attributeClasses.get(class_string).get(m).equals(prediction) 
							&& this.hm_attributeClasses.get(class_string).get(n).equals(data.get(i).get(data.get(i).size() - 1))) 
						{
							confusion[m][n] += 1;
						}
					}
				}
			}
			// System.out.println(Arrays.deepToString(confusion));
			// System.out.println(confusionMatrix);
			pWriter.close();
			for(int m = 0; m < this.hm_attributeClasses.get(class_string).size(); m++){
				pWriter1.print("  " + this.hm_attributeClasses.get(class_string).get(m));
				for(int n = 0; n < this.hm_attributeClasses.get(class_string).size(); n++){
					if(m == n) true_ += confusion[m][n];
					total_ += confusion[m][n];
					pWriter1.print("\t\t" + confusion[m][n]);	
				}
				pWriter1.println();
			}
			pWriter1.println("-----------------------------------------");
			// pWriter1.println("True : " + true_ + " Total : " + total_);
			pWriter1.println();
			pWriter1.println("-----------------------------------------");
			pWriter1.println("Accuracy : " + (float)(100 * true_)/total_ + " %");
			pWriter1.println("-----------------------------------------");
			pWriter1.close();
		}catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e) {
			e.printStackTrace();
		}
	}

	/********************************************************************************	
	@Description
	    Method Name: getMaximumGainAttribute
	    		=> Returns the attribute with maximum gain for the given data set
	    Parameters: 
	    		List<List<String>> _data => Matrix to be analyzed
	********************************************************************************/	
	public String getMaximumGainAttribute(List<List<String>> _data){
		String highest_IG_attribute = null;
		double highest_IG = Double.NEGATIVE_INFINITY;
		
		double[] gini = new double[_data.get(0).size()];
		double[] information_gain = new double[_data.get(0).size()];
		
//		System.out.println("Total Data => " + (_data.size() - 1));
		for(int a = 0; a < _data.get(0).size() - 1; a++){
			gini[a] = 0;
			information_gain[a] = 0;
			String attr = _data.get(0).get(a);
			String class_attr = _data.get(0).get((_data.get(0).size() - 1));
			int[][] countAttrs = new int[this.hm_attributeClasses.get(attr).size()][this.hm_attributeClasses.get(class_attr).size()];
//			int[] countAttrs = new int[this.hm_attributeClasses.get(class_attr).size()];
			
			for(int i = 1; i < _data.size(); i++){
//				total rows loop
				String tempData = _data.get(i).get(a);
				String tempDataClass = _data.get(i).get(_data.get(0).size() - 1);
				for(int n = 0; n < this.hm_attributeClasses.get(attr).size(); n++){
//					total current attribute values
					if(this.hm_attributeClasses.get(attr).get(n).trim().equals(tempData.trim())){
						for(int cn = 0; cn < this.hm_attributeClasses.get(class_attr).size(); cn++){
							if(this.hm_attributeClasses.get(class_attr).get(cn).trim().equals(tempDataClass)){
								countAttrs[n][cn]++;
//								countAttrs[cn]++;
							}
						}
					}
				}
			}
			
//			System.out.println("\tAttribute:" + attr);
			int[] total = new int[this.hm_attributeClasses.get(attr).size()];
			double tempGini = 0;
			for(int n = 0; n < this.hm_attributeClasses.get(attr).size(); n++){
				tempGini = 1;
//				System.out.println(1);
//				System.out.println("\t" + this.hm_attributeClasses.get(attr).get(n) + ":");
				
				for(int cn = 0; cn < this.hm_attributeClasses.get(class_attr).size(); cn++){
					total[n] = 0;
					for(int ccn = 0; ccn < this.hm_attributeClasses.get(class_attr).size(); ccn++){
						total[n] += countAttrs[n][ccn];
					}
//					System.out.println("-" + countAttrs[n][cn] + "/" + (double)total[n]);
					if(total[n] > 0){
						tempGini -= Math.pow(((double)countAttrs[n][cn] / (double)total[n]), 2);
					}
//					System.out.println("\t\t" + this.hm_attributeClasses.get(class_attr).get(cn) + " => " + countAttrs[n][cn]);
				}
				tempGini *= ((double)total[n] / (double)(_data.size() - 1));
//				System.out.println("*" + total[n] + "/" + (_data.size() - 1));
				
				gini[a] += tempGini;
			}
//			System.out.println("Gini for " + attr + " => " + gini[a]);
			information_gain[a] = (getTableGini(_data)) - gini[a];
//			System.out.println("Information Gain " + attr + " => " + information_gain[a]);
//			if(gini[a] < lowest_gini){
//				lowest_gini = gini[a];
//				lowest_gini_attribute = attr;
//			}
			if(information_gain[a] > highest_IG){
				highest_IG = information_gain[a];
				highest_IG_attribute = attr;
			}
		}
//		return lowest_gini_attribute;
//		System.out.println(">>>>>>>> " + highest_IG_attribute + " => " + highest_IG);
		return highest_IG_attribute;
	}
	
	/********************************************************************************	
	@Description
	    Method Name: getTableGini
	    		=> Returns the gini for the given data set
	    Parameters: 
	    		List<List<String>> _data => Matrix to be analyzed
	********************************************************************************/	
	public double getTableGini(List<List<String>> _data){
		double tempGini = 0;
		
		String attr = _data.get(0).get((_data.get(0).size() - 1));
		int total = 0;
		int[] countAttrs = new int[this.hm_attributeClasses.get(attr).size()];
		for(int i = 1; i < _data.size(); i++){
			for(int n = 0; n < this.hm_attributeClasses.get(attr).size(); n++){
				if(this.hm_attributeClasses.get(attr).get(n).trim().equals(_data.get(i).get(_data.get(0).size() - 1))){
					countAttrs[n]++;
				}
			}
		}
		tempGini = 1;
//			System.out.println(1);
		for(int cn = 0; cn < this.hm_attributeClasses.get(attr).size(); cn++){
			total = 0;
			for(int ccn = 0; ccn < this.hm_attributeClasses.get(attr).size(); ccn++){
				total += countAttrs[ccn];
			}
//				System.out.println("-" + countAttrs[cn] + "/" + (double)total);
			if(total > 0){
				tempGini -= Math.pow(((double)countAttrs[cn] / (double)total), 2);
			}
		}
		return tempGini;
	}

	/********************************************************************************	
	@Description
	    Method Name: splitTable
	    		=> Split the table by splitting attribute
	    Parameters: 
	    		List<List<String>> _data => Matrix to be splitted
	    		String splitAttr 		 => Splitting attribute
	    		String attrValue 		 => Splitting attribute value
	********************************************************************************/
	public List<List<String>> splitTable(List<List<String>> _data, String splitAttr, String attrValue){
		List<List<String>> _temp = new ArrayList<List<String>>();
		int attribute_index = -1;
		for(int i = 0; i < _data.get(0).size(); i++){
			if(_data.get(0).get(i).equals(splitAttr)){
				attribute_index = i;
				break;
			}
		}
//		System.out.println(splitAttr + " => " + attribute_index);
		List<String> tempHeader = new ArrayList<String>();
		for(int i = 0; i < _data.get(0).size(); i++){
			if(!_data.get(0).get(i).equals(splitAttr)){
				tempHeader.add(_data.get(0).get(i));
			}
		}
		_temp.add(tempHeader);
		for(int i = 1; i < _data.size(); i++){
			if(_data.get(i).get(attribute_index).equals(attrValue)){
				List<String> tempRecord = new ArrayList<String>();
				for(int j = 0; j < _data.get(i).size(); j++){
					if(j != attribute_index){
						tempRecord.add(_data.get(i).get(j));
					}
				}
				_temp.add(tempRecord);
			}
		}
//		System.out.println(_temp);
		return _temp;
	}
	
	/********************************************************************************	
	@Description
	    Method Name: isHomogenousClass
	    		=> Checks if the given data has only one class
	    Parameters: 
	    		List<List<String>> _data => Matrix to be checked
	********************************************************************************/
	public boolean isHomogenousClass(List<List<String>> _data){
		String x = _data.get(1).get(_data.get(0).size() - 1);
		for(int i = 1; i < _data.size(); i++){
			if(!_data.get(i).get(_data.get(0).size() - 1).equals(x)){
				return false;
			}
		}
		return true;
	}
	
	/********************************************************************************	
	@Description
	    Method Name: isHomogenousRecords
	    		=> Checks if the given data has same records
	    Parameters: 
	    		List<List<String>> _data => Matrix to be checked
	********************************************************************************/
	public boolean isHomogenousRecords(List<List<String>> _data){
		for(int i = 2; i < _data.size(); i++){
			if(!_data.get(i).subList(0, _data.get(0).size() - 2).equals(_data.get(i - 1).subList(0, _data.get(0).size() - 2))){
				return false;
			}
		}
		return true;
	}
	
	/********************************************************************************	
	@Description
	    Method Name: getMostFrequentClass
	    		=> Returns the most frequent class for the given data set
	    Parameters: 
	    		List<List<String>> _data => Matrix to be analyzed
	********************************************************************************/
	public String getMostFrequentClass(List<List<String>> _data){
		String attr = _data.get(0).get((_data.get(0).size() - 1));
		int[] countAttrs = new int[this.hm_attributeClasses.get(attr).size()];
		for(int i = 1; i < _data.size(); i++){
			for(int n = 0; n < this.hm_attributeClasses.get(attr).size(); n++){
					if(this.hm_attributeClasses.get(attr).get(n).trim().equals(_data.get(i).get(_data.get(0).size() - 1))){
						countAttrs[n]++;
					}
			}
		}
		double max_frequency = Double.NEGATIVE_INFINITY;
		String max_frequent_class = "";
		for(int n = 0; n < countAttrs.length; n++){
//			System.out.println("Count: " + this.hm_attributeClasses.get(attr).get(n) + " => " + countAttrs[n]);
			if((double)countAttrs[n] > max_frequency){
				max_frequency = (double)countAttrs[n];
				max_frequent_class = this.hm_attributeClasses.get(attr).get(n);
			}
		}
		return max_frequent_class;
	}

	/********************************************************************************
	@Description
	    Method Name: returnIndexOfChild
	    		=> Returns the index of particular child from children values
	    Parameters: 
	    		List<String> childrenValues 	=> Children values
	    		String value 					=> Specific child value	
	********************************************************************************/
	public int returnIndexOfChild(List<String> childrenValues, String value){
		for(int i = 0; i < childrenValues.size(); i++){
			if(childrenValues.get(i).equals(value)){
				return i;
			}
		}
		return -1;
	}

	/********************************************************************************	
	@Description
	    Method Name: getAttributeIndex
	    		=> Returns the index of particular attribute from the data
	    Parameters: 
	    		String value 					=> Specific value	
	********************************************************************************/
	public int getAttributeIndex(String attribute){
		for(int i = 0; i < this.data.get(0).size(); i++){
			if(this.data.get(0).get(i).equals(attribute)){
				return i;
			}
		}
		return -1;
	}

	/********************************************************************************
	@Description
	    Method Name: getData
	        => Return data
	    Parameters: N/A 
	********************************************************************************/
	public List<List<String>> getData(){
		return this.data;
	}
	
	/********************************************************************************	
	@Description
	    Method Name: splitTable
	    		=> Split the table by splitting attribute
	    Parameters: 
	    		List<List<String>> _data => Matrix to be splitted
	    		String splitAttr 		 => Splitting attribute
	    		String attrValue 		 => Splitting attribute value
	********************************************************************************/
	public boolean isEmptyTable(List<List<String>> _data){
		if(_data.size() <= 1)
			return true;
		return false;
	}
}