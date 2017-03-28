package hr.fer.zemris.evoenhancement.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Statistics {

	private static List<Double> best;

	public static void makeRunStatistics(List<Double> resultList) {

		Collections.sort(resultList);

		if (best == null) {
			best = new ArrayList<>();
		}

		double median = resultList.get((resultList.size() - 1) / 2);
		double mean = 0;
		for (Double d : resultList) {
			mean += d;
		}

		mean /= resultList.size();
		double bestIndividual = resultList.get(resultList.size() - 1);
		best.add(bestIndividual);

		Logger.log("Median: " + median + " Mean: " + mean + " Best: " + bestIndividual);
	}

	public static void makeAllStatistics() {

		Collections.sort(best);

		double median = best.get((best.size() - 1) / 2);
		double mean = 0;
		for (Double d : best) {
			mean += d;
		}

		mean /= best.size();
		double bestIndividual = best.get(best.size() - 1);

		Logger.log("Median: " + median + " Mean: " + mean + " Best: " + bestIndividual, 0);

	}

}
