package hr.fer.zemris.evoenhancement.util;

import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.J48;
import weka.core.Instances;

public class DataMiner {

	public Classifier createClassifier(Instances data) {
		Classifier classifier = new J48();

		try {
			classifier.buildClassifier(data);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return classifier;
	}

	public Evaluation makeEvaluation(Classifier classifier, Instances data) throws Exception {
		Evaluation evaluation = new Evaluation(data);
		StringBuffer buffer = new StringBuffer();
		PlainText plainText = new PlainText();
		plainText.setBuffer(buffer);
		evaluation.crossValidateModel(classifier, data, 10, new Random(1), plainText);

		return evaluation;
	}

}
