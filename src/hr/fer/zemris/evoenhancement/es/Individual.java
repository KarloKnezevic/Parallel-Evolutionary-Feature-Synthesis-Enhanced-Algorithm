package hr.fer.zemris.evoenhancement.es;

import evofmj.algorithm.RegressionEFM;

public class Individual {
	
	private RegressionEFM regressionEFM;
	
	private double accuracy;
	
	public Individual(RegressionEFM regressionEFM, double accuracy) {
		this.regressionEFM = regressionEFM;
		this.accuracy = accuracy;
	}

	public RegressionEFM getRegressionEFM() {
		return regressionEFM;
	}

	public double getAccuracy() {
		return accuracy;
	}
	
	@Override
	public String toString() {
		return Double.toString(accuracy);
	}
	
	
}
