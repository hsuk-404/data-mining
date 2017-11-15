/********************************************************************************
@FileName
   	KFoldNaiveBayes.java  
@Description
    Implementation of NaiveBayes class. Takes the input from ArffParser and runs
    the naive base that makes an output file showing accuracy and an new Arff file
    including the predicted class
@Updated 04/02/2016
@Author Kush Chandra Shrestha
********************************************************************************/
package classes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class KFoldNaiveBayes {
	private HashMap<String, List<String>> hm_attributeClasses = new HashMap<String, List<String>>();
	private List<List<String>> allData = new ArrayList<List<String>>();
	private String relation, class_name, input_file_name;
	private int kFolds;
	private String filenamePrefix = "output_";

	public KFoldNaiveBayes(ArffParser arff){
		List<List<String>> data = arff.getInputData();
    	HashMap<String, List<String>> attribute_classes = arff.getAttributeClasses();
    	this.relation = arff.getRelation();
    	this.kFolds = Integer.parseInt(arff.getKFolds());
    	
    	this.class_name = arff.getClassName();
    	this.input_file_name = arff.getFileName().replace(".arff", "");
		this.allData.addAll(data);
		this.hm_attributeClasses = attribute_classes;
		
		int each_fold_size = (this.allData.size() - 1)/this.kFolds;
		File output_file = new File(this.filenamePrefix + "KFoldClassification" + this.input_file_name + ".arff");
		File output_file1 = new File(this.filenamePrefix + "KFoldConfusion" + this.input_file_name + ".txt");
		try{
			FileWriter fWriter = new FileWriter(output_file);
			FileWriter fWriter1 = new FileWriter(output_file1);
	        PrintWriter pWriter = new PrintWriter (fWriter);
	        PrintWriter pWriter1 = new PrintWriter (fWriter1);
	    
	        pWriter.println("@relation " + this.relation);
	        pWriter.println();
	        for(int i = 0; i < this.hm_attributeClasses.size(); i++){
	        	pWriter.print("@attribute " + this.allData.get(0).get(i) + " ");
	        	pWriter.print("{");
	        	for(int j = 0; j < this.hm_attributeClasses.get(this.allData.get(0).get(i)).size(); j++){
	        		pWriter.print(this.hm_attributeClasses.get(this.allData.get(0).get(i)).get(j));
	        		if(j != this.hm_attributeClasses.get(this.allData.get(0).get(i)).size() - 1){
	        			pWriter.print(",");
	        		}
	        	}
	        	pWriter.println("}");
	        }
	        pWriter.println("@attribute bayesClass real");
	        pWriter.println();
	        pWriter.println("@data");
	        
			int[][] confusion = new int[this.hm_attributeClasses.get(this.class_name).size()][this.hm_attributeClasses.get(class_name).size()];
			if(this.kFolds > 1){
				for(int i = 0; i < this.kFolds; i++){
					int test_start_index = (i* each_fold_size + 1);
					int test_end_index = (i + 1) * each_fold_size;
					if(i == (this.kFolds - 1)) {
						test_end_index = this.allData.size() - 1;
					}
	//				System.out.println("Start => " + test_start_index + " End => " + test_end_index + " " + (test_start_index + each_fold_size - 1));
					int[][] temp_confusion = countX(test_start_index, test_end_index, false, pWriter);
					for(int m = 0; m < this.hm_attributeClasses.get(this.class_name).size(); m++){
						for(int n = 0; n < this.hm_attributeClasses.get(this.class_name).size(); n++){
							confusion[m][n] += temp_confusion[m][n];
						}
					}
				}
			}else{
				confusion = countX(1, this.allData.size(), true, pWriter);
			}
	//		System.out.println(Arrays.deepToString(confusion));
			
			int total_ = 0;
			int true_ = 0;
			pWriter1.println("-----------------------------------------");
			pWriter1.println("CONFUSION MATRIX");
			pWriter1.println("-----------------------------------------");
			for(int n = 0; n < this.hm_attributeClasses.get(this.class_name).size(); n++){
				pWriter1.print("\t\t" + this.hm_attributeClasses.get(this.class_name).get(n));
	        }
			pWriter1.println();
			pWriter1.println("-----------------------------------------");
	        
			for(int m = 0; m < this.hm_attributeClasses.get(this.class_name).size(); m++){
				pWriter1.print("  " + this.hm_attributeClasses.get(this.class_name).get(m));
				for(int n = 0; n < this.hm_attributeClasses.get(this.class_name).size(); n++){
					if(m == n) true_ += confusion[m][n];
					total_ += confusion[m][n];
					pWriter1.print("\t\t" + confusion[m][n]);	
				}
				pWriter1.println();
			}
			pWriter1.println();
			pWriter1.println("-----------------------------------------");
			pWriter1.println("Accuracy : " + (float)(100 * true_)/total_ + " %");
			pWriter1.println("-----------------------------------------");
			
			pWriter1.close();
			pWriter.close();
			
		}catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int[][] countX(int test_start_index, int test_end_index, boolean training_testing_same, PrintWriter pWriter){
		int[][] confusion = new int[this.hm_attributeClasses.get(this.class_name).size()][this.hm_attributeClasses.get(class_name).size()];
		int[][][] total = new int[this.allData.get(0).size()][5][this.hm_attributeClasses.get(this.class_name).size()];
		int[] class_total = new int[this.hm_attributeClasses.get(this.class_name).size()];
		for(int a = 1; a < this.allData.size(); a++){
			String cla_value = this.allData.get(a).get(this.allData.get(a).size() - 1);
			int cla_index = this.hm_attributeClasses.get(this.class_name).indexOf(cla_value);
			class_total[cla_index]++;
			for(int b = 0; b < this.allData.get(0).size(); b++){
				String attr_name = this.allData.get(0).get(b);
				if((!(a >= test_start_index && a <= test_end_index)) || training_testing_same){
					String cell_value = this.allData.get(a).get(b); 
					for(int j = 0; j < this.hm_attributeClasses.get(attr_name).size(); j++){
						if(this.hm_attributeClasses.get(attr_name).get(j).equals(cell_value)){
							for(int i = 0; i < this.hm_attributeClasses.get(this.class_name).size(); i++){
								String class_value = this.hm_attributeClasses.get(this.class_name).get(i);
								String data_class = this.allData.get(a).get(this.allData.get(a).size() - 1);
								if(class_value.equals(data_class)){
									total[b][j][i]++;
								}
							}
						}
					}
				}
			}
		}
		for(int a = 1; a < this.allData.size(); a++){
			if((a >= test_start_index && a <= test_end_index) || training_testing_same){
				double[] probabilities = new double[this.hm_attributeClasses.get(this.class_name).size()];
				for(int i = 0; i < this.hm_attributeClasses.get(this.class_name).size(); i++){
					probabilities[i] = 1;
					if(training_testing_same){
						probabilities[i] *= (double)class_total[i] / (double)(this.allData.size() - 1);
					}else{
						probabilities[i] *= (double)class_total[i] / (double)(this.allData.size() - 1 - (test_end_index - test_start_index));
					}
				}
				for(int b = 0; b < this.allData.get(0).size(); b++){
					pWriter.print(this.allData.get(a).get(b) + " ");
					if(b < this.allData.get(0).size() - 1){
						String cell_value = this.allData.get(a).get(b);
						String attr_title = this.allData.get(0).get(b);
						int cell_index = this.hm_attributeClasses.get(attr_title).indexOf(cell_value);
						for(int i = 0; i < this.hm_attributeClasses.get(this.class_name).size(); i++){
							int temp_total = 0;
							for(int n = 0; n < this.hm_attributeClasses.get(attr_title).size(); n++){
								temp_total += total[b][n][i];
							}
							probabilities[i] *= ((double)(total[b][cell_index][i] + 1)/ (double)(temp_total + 3));
						}
					}
				}
//				System.out.println(Arrays.toString(probabilities));
				double max_probability = Double.NEGATIVE_INFINITY;
				int max_likelihood_index = -1;
				for(int i = 0; i < this.hm_attributeClasses.get(this.class_name).size(); i++){
					if(probabilities[i] > max_probability){
						max_probability = probabilities[i];
						max_likelihood_index = i;
					}
				}
				String prediction = this.hm_attributeClasses.get(this.class_name).get(max_likelihood_index);
				pWriter.print(prediction);
				pWriter.println();
				for(int m = 0; m < this.hm_attributeClasses.get(this.class_name).size(); m++){
					for(int n = 0; n < this.hm_attributeClasses.get(this.class_name).size(); n++){
						if(this.hm_attributeClasses.get(this.class_name).get(m).equals(prediction) 
							&& this.hm_attributeClasses.get(this.class_name).get(n).equals(this.allData.get(a).get(this.allData.get(a).size() - 1))) 
						{
							confusion[m][n] += 1;
						}
					}
				}
			}
		}
//		System.out.println(Arrays.deepToString(confusion));
		return confusion;
	}
}