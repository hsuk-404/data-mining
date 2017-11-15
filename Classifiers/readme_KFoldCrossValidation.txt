KFoldCrossValidation.java

Objective:
	Once a classifier is created, it is important to evaluate how well the classifier is doing. Classifiers must be evaluated using data not used in the training process in order to avoid overly optimistic performance assessments. When large numbers of samples are available, it is possible to simply reserve some of those samples for evaluation, and not use those samples in the training process. However, when the number of available samples is small this approach may not be practical. Methods such as k-fold cross validation and leave one out solve this problem by using each available sample multiple times. A series of classifiers is created using different subsets of the samples, and each classifier is evaluated using those samples not used to train it. The performance assessment is made based on statistics compiled over multiple training / evaluation runs.
	Partition the input data set into K folds using stratified sampling. Build a naïve Bayesian classifier. The program should train one classifier for each fold (K classifiers all together). The Nth classifier is trained using all but the Nth fold in the data set, and then evaluated using only the Nth fold. Performance statistics are gathered over the whole run, so each fold (and hence each pattern) is evaluated exactly once. Your program should produce a text file with the classifications produced by the naïve Bayesian classifier and the correct classifications.

Compilation:
	javac KFoldCrossValidation.java 

Execution:
	java KFoldCrossValidation -i <input_file_location> -K <kfolds> -c <class_name>
		K => Number of folds
		c => Class attribute is the target class for building the classifier
		i => Input arff file
	Example:
		java KFoldCrossValidation -K 10 -i test_dataset.arff -c class

Output Files:
	output_KFoldClassfication<input_file_name>.arff		=> New dataset file with the predicted class labels for each input pattern vector
	output_KFoldConfusion<input_file_name>.arff			=> Confusion matrix for the k-fold cross validation process and an overall accuracy score.
 
Test File:
	test_dataset.arff is renamed from bcwdisc.arff which is obtained from the University of California Irvine (UCI) machine learning repository (http://www.ics.uci.edu/~mlearn/MLRepository.html)