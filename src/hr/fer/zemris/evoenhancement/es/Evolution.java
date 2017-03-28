package hr.fer.zemris.evoenhancement.es;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import hr.fer.zemris.evoenhancement.parallel.ParallelEvaluation;

public class Evolution {

	public static void mutate(List<Individual> population) {
		Random r = new Random();
		int limit = population.get(0).getRegressionEFM().dataIndexes.size();

		for (int index = 1; index < population.size(); index++) {
			Individual individual = population.get(index);

			int indexNewFeature = r.nextInt(limit);
			int indexParent1 = r.nextInt(limit);
			int indexParent2 = r.nextInt(limit);

			if (r.nextBoolean()) {
				individual.getRegressionEFM().binaryRecombination(indexNewFeature, indexParent1, indexParent2);
			} else {
				individual.getRegressionEFM().unaryRecombination(indexNewFeature, indexParent1);
			}
		}

	}

	public static void crossover(List<Individual> population) {
		// do nothing
	}

	public static void parallelEvaluation(List<Individual> population, ExecutorService pool) {
		List<Future<Void>> barrier = new ArrayList<>();
		for (int index = 1; index < population.size(); index++) {
			ParallelEvaluation parallelJob = new ParallelEvaluation(population.get(index));
			barrier.add(pool.submit(parallelJob));
		}

		for (Future<Void> result : barrier) {
			try {
				result.get();
			} catch (InterruptedException | ExecutionException e) {
			}
		}
	}

	public static Individual selection(List<Individual> population) {
		return null;
	}

}
