/********************************************************************************
@FileName
   	ArffParser.java  
@Description
    Implementation of ArffParser class. ArffParser takes the input file and parses
    the content of the files on respective array list of data
@Updated 04/02/2016
@Author Kush Chandra Shrestha
********************************************************************************/
package classes;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ArffParser {
	private List<String> attributes = new ArrayList<String>();
	private HashMap<String, List<String>> hm_attributeClasses = new HashMap<String, List<String>>();
	private List<List<String>> data = new ArrayList<List<String>>();
	private String relation, file_name, class_name, kFolds, percentage;
	private String[] args;
	
	// Constructor
	public ArffParser(String[] args){
		this.args = args;
	}
	/********************************************************************************
	@Description
	    Method Name: getInputData
	        => Processes the input file and return data
	    Parameters: String args => Command Line arguments 
	********************************************************************************/
	public List<List<String>> getInputData(){
		this.processInputFile(this.args);
		if(!this.data.get(0).get(this.data.get(0).size() - 1).equals(this.class_name)){
			// Move the column to the last if the class attribute is different attribute than that lies in the last
			boolean classFound = false;
			for(int j = 0; j < this.data.get(0).size(); j++){
				if(this.data.get(0).get(j).equals(this.class_name)){
					int last_item = this.data.get(0).size() - 1;
					for(int i = 0; i < this.data.size(); i++){
						String temp = this.data.get(i).get(last_item);
						this.data.get(i).set(last_item, this.data.get(i).get(j));
						this.data.get(i).set(j, temp);
					}
					classFound = true;
					break;
				}
			}
			if(!classFound){
				System.out.println("Class attribute defined is not found in the given dataset.");
	    		System.out.println("Syntax : ");
	    		System.out.println("\t<program_name>  -i <input_file_location> -K <kfolds> -c <class_name>");
	    		System.out.println("Please try again.");
	    		System.exit(0);
			}
		}
		return this.data;
	}
	
	/********************************************************************************
	@Description
	    Method Name: getRelation
	        => Returns the attribute relation
	    Parameters: N/A 
	********************************************************************************/
	public String getRelation(){
		return this.relation;
	}
	
	/********************************************************************************
	@Description
	    Method Name: getFileName
	        => Returns the attribute file name
	    Parameters: N/A 
	********************************************************************************/
	public String getFileName(){
		return this.file_name;
	}
	
	/********************************************************************************
	@Description
	    Method Name: getKFolds
	        => Returns the attribute kFolds
	    Parameters: N/A 
	********************************************************************************/
	public String getKFolds(){
		return this.kFolds;
	}
	
	/********************************************************************************
	@Description
	    Method Name: getPercentage
	        => Returns the attribute percentage
	    Parameters: N/A 
	********************************************************************************/
	public String getPercentage(){
		return this.percentage;
	}

	/********************************************************************************
	@Description
	    Method Name: getClass
	        => Returns the attribute class
	    Parameters: N/A 
	********************************************************************************/
	public String getClassName(){
		return this.class_name;
	}
	
	/********************************************************************************
	@Description
	    Method Name: getAttributes
	        => Returns all the attributes
	    Parameters: N/A 
	********************************************************************************/
	public List<String> getAttributes(){
		return this.attributes;
	}
	
	/********************************************************************************
	@Description
	    Method Name: getAttributeClasses
	        => Returns all the attribute classes
	    Parameters: N/A 
	********************************************************************************/
	public HashMap<String, List<String>> getAttributeClasses(){
		return this.hm_attributeClasses;
	}
	
	/********************************************************************************
	@Description
	    Method Name: getData
	        => Returns all the data
	    Parameters: N/A 
	********************************************************************************/	
	public List<List<String>> getData(){
		return this.data;
	}
	
	/********************************************************************************
	@Description
	    Method Name: processInputFile
	        => Process the given input file and parses it on array lists
	    Parameters: 
	    	String[] args => command line arguments
	********************************************************************************/
    private void processInputFile(String[] args){
   	// System.out.println(Arrays.toString(args));
    	if(args.length != 6 || Arrays.asList(args).indexOf("-c") == -1 ||  Arrays.asList(args).indexOf("-i") == -1 ||  (Arrays.asList(args).indexOf("-K") == -1 && Arrays.asList(args).indexOf("-P") == -1)){
    		System.out.println("Wrong number of arguments.");
    		System.out.println("Syntax convention : ");
    		System.out.println("\t<program_name>  -i <input_file_location> -P <percentage> -K <kfolds> -c <class_name>");
    		System.out.println("Please try again.");
    		System.exit(0);
    	}
    	for(int i = 0; i < args.length;i++){
    		switch(args[i]){
    			case "-i":
    				this.file_name = args[i + 1];
//    				this.file_name = "data/test.arff";
    				break;
    			case "-c":
    				this.class_name = args[i + 1];
    				break;
    			case "-K":
    				this.kFolds = args[i + 1];
    				break;
				case "-P":
    				this.percentage = args[i + 1];
    				break;
    		}
    	}
    	File file = new File(this.file_name);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.indexOf("@relation") > -1){
                	this.relation = line.split(" ")[1];
                }else if(line.indexOf("@attribute") > -1){
                	this.attributes.add(line.split(" ")[1]);
                	String attrClass_line = line.split("\\{", 2)[1].trim();
                	List<String> attribute_classes = new ArrayList<String>();
                	String tempSplitted;
                	while(attrClass_line.indexOf("}") > -1){
                		if(attrClass_line.indexOf(",") > -1){
                			tempSplitted = attrClass_line.split(",", 2)[0].trim();
                    		attrClass_line = attrClass_line.split(",", 2)[1].trim();
                		}else{
                			tempSplitted = attrClass_line.split("}", 2)[0].trim();
                			attrClass_line = "";
                		}
                		attribute_classes.add(tempSplitted);
                	}
                	hm_attributeClasses.put(line.split(" ")[1], attribute_classes);
                }else if(line.indexOf("@data") > -1){
                	this.data.add(attributes);
                	while (scanner.hasNextLine()) {
                		String[] splitted_line = scanner.nextLine().trim().split(" ", 2);
                		List<String> temp_data = new ArrayList<String>();
                        for(int i = 0; i < this.attributes.size(); i++){
                        	if(i > 0){
                        		splitted_line = splitted_line[1].trim().split(" ", 2);
                        	}	
                        	temp_data.add(splitted_line[0]);
                        }
                        this.data.add(temp_data);
                	}   
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
