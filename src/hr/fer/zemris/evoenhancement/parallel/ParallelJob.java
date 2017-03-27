package hr.fer.zemris.evoenhancement.parallel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

import evofmj.algorithm.RegressionEFM;
import evofmj.evaluation.java.EFMScaledData;
import hr.fer.zemris.evoenhancement.es.Individual;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.core.AbstractInstance;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class ParallelJob implements Callable<Individual> {

	private int jobIndex;

	private RegressionEFM rEFM;

	private String dataPath;

	// execution time in minutes
	private double execTime;

	public ParallelJob(int index, String dataPath, double execTime) {
		this.jobIndex = index;
		this.dataPath = dataPath;
		this.execTime = execTime;
	}

	@Override
	public Individual call() throws Exception {

		try {

			// construct features
			this.parseRegEFMTrain();

			// take transformed data according to features selection
			
			//numeric values for attributes
			ArrayList<Attribute> attributes = new ArrayList<>();
			for (int attribute = 0; attribute < rEFM.maxFinalFeatures; attribute++) {
				attributes.add(new Attribute("attribute" + attribute));
			}
			
			//nominal values for classes
			ArrayList<String> classes = new ArrayList<>();
			double minClass = rEFM.dataMatrix.target_min;
			double maxClass = rEFM.dataMatrix.target_max;
			for (double clazz = (int)minClass; clazz < maxClass; clazz++) {
				classes.add(Double.toString(clazz));
			}
			attributes.add(new Attribute("class", classes));
			
			
			//declare feature vector
			Instances data = new Instances("data", attributes, 0);
			data.setClassIndex(data.numAttributes() - 1);
			

			//fill the data
			double[] classified = rEFM.dataMatrix.getTargetValues();
			for (int index = 0; index < rEFM.dataMatrix.getNumberOfFitnessCases(); index++) {
				AbstractInstance instance = new DenseInstance(data.numAttributes());
				instance.setDataset(data);
				
				int att = 0;
				for (; att < data.numAttributes()-1; att++) {
					instance.setValue(att, rEFM.dataMatrix.getRow(index)[att]);
				}
				instance.setValue(att, Double.toString(classified[att]));
				data.add(instance);
			}
			
			Instances train = data.trainCV(10, jobIndex);
			Instances tst = data.testCV(10, jobIndex);


			Classifier classifier = new NaiveBayes();
			classifier.buildClassifier(train);
			
			Evaluation test = new Evaluation(tst);
			StringBuffer buffer = new StringBuffer();
			PlainText plainText = new PlainText();
			plainText.setBuffer(buffer);
			test.crossValidateModel(classifier, tst, 10, new Random(1), plainText);
			
			System.out.println(test.toSummaryString());
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		// classify transformed data
		double accuracy = new Random().nextDouble();

		// return parallel result
		return new Individual(rEFM, accuracy);
	}

	/**
	 * parse arguments to train a EFM model
	 * 
	 * @param args
	 * @throws IOException
	 * @throws Exception
	 */
	private void parseRegEFMTrain() throws IOException, Exception {
		String dataPath;
		double numMinutes;
		dataPath = this.dataPath;
		numMinutes = this.execTime;

		EFMScaledData data = new EFMScaledData(0, 0, dataPath);
		int numberOfOriginalFeatures = data.getNumberOfOriginalFeatures();
		int numArchiveFeatures = 0;
		int numNewFeatures = 0;
		int maxFeatureSize = 5;

		if (numMinutes != 0) {
			numberOfOriginalFeatures = data.getNumberOfOriginalFeatures();
			numArchiveFeatures = 3 * numberOfOriginalFeatures;
			numNewFeatures = numberOfOriginalFeatures;
			maxFeatureSize = 5;
			int numberOfFinalFeatures = numberOfOriginalFeatures + numArchiveFeatures;
			// int numberOfFinalFeatures = numberOfOriginalFeatures ;
			rEFM = new RegressionEFM(dataPath, numArchiveFeatures, numNewFeatures, maxFeatureSize,
					numberOfFinalFeatures);
		} else {
			rEFM = new RegressionEFM(dataPath, numArchiveFeatures, numNewFeatures, maxFeatureSize,
					numberOfOriginalFeatures);
		}

		rEFM.runEFM(numMinutes * 60);
	}

	public int getJobIndex() {
		return jobIndex;
	}

}
