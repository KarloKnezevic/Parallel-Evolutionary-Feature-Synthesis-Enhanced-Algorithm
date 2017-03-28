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
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.J48;
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
			
			/**
			 * FEATURE SELECTION AND MODEL BUILDING
			 */

			// construct features
			this.parseRegEFMTrain();

			/**
			 * DATA TRANSFORMATION AND LOADING
			 */

			// numeric values for attributes
			ArrayList<Attribute> attributes = new ArrayList<>();
			for (int attribute = 0; attribute < rEFM.maxFinalFeatures; attribute++) {
				attributes.add(new Attribute("attribute" + attribute));
			}

			// nominal values for classes
			ArrayList<String> classes = new ArrayList<>();
			double minClass = rEFM.dataMatrix.target_min;
			double maxClass = rEFM.dataMatrix.target_max;
			for (double clazz = (int) minClass; clazz <= maxClass; clazz++) {
				classes.add(Double.toString(clazz));
			}
			attributes.add(new Attribute("class", classes));

			// set classes and class index
			Instances data = new Instances("data", attributes, 0);
			data.setClassIndex(data.numAttributes() - 1);

			// fill the data
			double[] classified = rEFM.dataMatrix.getTargetValues();
			for (int index = 0; index < rEFM.dataMatrix.getNumberOfFitnessCases(); index++) {
				AbstractInstance instance = new DenseInstance(data.numAttributes());
				instance.setDataset(data);

				double[] trace = rEFM.dataMatrix.getRow(index);
				int att = 0;
				for (Integer idx : rEFM.dataIndexes) {
					instance.setValue(att, trace[idx]);
					att++;
				}

				instance.setValue(att, Double.toString(classified[index]));
				data.add(instance);
			}
			
			/**
			 * CLASSIFIER BUILDING
			 */

			Classifier classifier = new J48();
			// training data
			classifier.buildClassifier(data);
			
			/**
			 * CLASSIFICATION
			 */

			// training data
			Evaluation test = new Evaluation(data);
			StringBuffer buffer = new StringBuffer();
			PlainText plainText = new PlainText();
			plainText.setBuffer(buffer);
			test.crossValidateModel(classifier, data, 10, new Random(1), plainText);
			
			double accuracy = test.pctCorrect();
			
			/**
			 * RETUNRN
			 */
			
			return new Individual(rEFM, accuracy);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// return parallel result
		return null;
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
		int numberOfFinalFeatures = numberOfOriginalFeatures;

		if (numMinutes != 0) {
			numberOfOriginalFeatures = data.getNumberOfOriginalFeatures();
			numArchiveFeatures = 3 * numberOfOriginalFeatures;
			numNewFeatures = numberOfOriginalFeatures;
			maxFeatureSize = 5;
			numberOfFinalFeatures = numberOfOriginalFeatures + numArchiveFeatures;
			// int numberOfFinalFeatures = numberOfOriginalFeatures ;
			rEFM = new RegressionEFM(dataPath, numArchiveFeatures, numNewFeatures, maxFeatureSize,
					numberOfFinalFeatures);
		}

		rEFM = new RegressionEFM(dataPath, numArchiveFeatures, numNewFeatures, maxFeatureSize, numberOfFinalFeatures);

		rEFM.runEFM(numMinutes * 60);
	}

	public int getJobIndex() {
		return jobIndex;
	}

}
