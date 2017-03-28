package hr.fer.zemris.evoenhancement.util;

import java.util.ArrayList;

import evofmj.algorithm.RegressionEFM;
import weka.core.AbstractInstance;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class DataProducer {
	
	private RegressionEFM rEFM;
	
	public DataProducer(RegressionEFM rEFM) {
		this.rEFM = rEFM;
	}

	public Instances produceData() {

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
		
		return data;
	}

}
