package hr.fer.zemris.evoenhancement.es;

import evofmj.algorithm.RegressionEFM;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

public class Individual {
	
	private RegressionEFM regressionEFM;
	
	private Evaluation evaluation;
	
	private Classifier classifier;
	
	public Individual(RegressionEFM regressionEFM, Evaluation evaluation, Classifier classifier) {
		this.regressionEFM = regressionEFM;
		this.evaluation = evaluation;
		this.classifier = classifier;
	}

	public RegressionEFM getRegressionEFM() {
		return regressionEFM;
	}
	
	public Evaluation getEvaluation() {
		return evaluation;
	}
	
	public double getFitness() {
		return evaluation.pctCorrect();
	}

	public Classifier getClassifier() {
		return classifier;
	}

	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}

	@Override
	public String toString() {
		return Double.toString(this.getFitness());
	}
	
	
}
