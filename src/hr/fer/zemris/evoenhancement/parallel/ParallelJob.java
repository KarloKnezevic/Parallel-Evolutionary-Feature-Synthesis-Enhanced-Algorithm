package hr.fer.zemris.evoenhancement.parallel;

import java.io.IOException;
import java.util.concurrent.Callable;

import evofmj.algorithm.RegressionEFM;
import evofmj.evaluation.java.EFMScaledData;
import hr.fer.zemris.evoenhancement.Main;
import hr.fer.zemris.evoenhancement.es.Individual;
import hr.fer.zemris.evoenhancement.util.DataMiner;
import hr.fer.zemris.evoenhancement.util.DataProducer;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
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

			this.parseRegEFMTrain();

			/**
			 * DATA TRANSFORMATION AND LOADING
			 */

			Instances data = new DataProducer(rEFM).produceData();

			/**
			 * CLASSIFIER BUILDING
			 */

			Main.print("Building classifier");

			DataMiner dataMiner = new DataMiner();

			Classifier classifier = dataMiner.createClassifier(data);

			/**
			 * CLASSIFICATION
			 */

			Main.print("Testing");

			Evaluation evaluation = dataMiner.makeEvaluation(classifier, data);

			/**
			 * RETURN
			 */

			return new Individual(rEFM, evaluation, classifier);

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
