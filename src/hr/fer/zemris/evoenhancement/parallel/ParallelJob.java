package hr.fer.zemris.evoenhancement.parallel;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Callable;

import evofmj.algorithm.RegressionEFM;
import evofmj.evaluation.java.EFMScaledData;
import hr.fer.zemris.evoenhancement.es.Individual;

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
			
			
		} catch (Exception e) {
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
