package hr.fer.zemris.evoenhancement.parallel;

import java.util.concurrent.Callable;

import hr.fer.zemris.evoenhancement.es.Individual;
import hr.fer.zemris.evoenhancement.util.DataMiner;
import hr.fer.zemris.evoenhancement.util.DataProducer;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class ParallelEvaluation implements Callable<Void> {

	private Individual individual;

	public ParallelEvaluation(Individual individual) {
		this.individual = individual;
	}

	@Override
	public Void call() throws Exception {

		try {

			Instances data = new DataProducer(individual.getRegressionEFM()).produceData();

			DataMiner dataMiner = new DataMiner();
			Classifier classifier = dataMiner.createClassifier(data);
			Evaluation evaluation = dataMiner.makeEvaluation(classifier, data);

			individual.setEvaluation(evaluation);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
