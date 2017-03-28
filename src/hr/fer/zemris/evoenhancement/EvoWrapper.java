package hr.fer.zemris.evoenhancement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import hr.fer.zemris.evoenhancement.es.Evolution;
import hr.fer.zemris.evoenhancement.es.Individual;
import hr.fer.zemris.evoenhancement.parallel.ParallelJob;
import hr.fer.zemris.evoenhancement.util.Logger;
import hr.fer.zemris.evoenhancement.util.Statistics;

public class EvoWrapper {

	private int generations;

	private int runs;

	private int populationSize;

	private int threadNumber;

	private String dataPath;

	private double threadExecTime;

	public EvoWrapper(int runs, int generations, int populationSize, String dataPath, double threadExecTime) {
		this.generations = generations;
		this.runs = runs;
		this.threadNumber = Runtime.getRuntime().availableProcessors();
		this.populationSize = populationSize;
		this.dataPath = dataPath;
		this.threadExecTime = threadExecTime;
	}

	public void start() {

		Logger.print("Wrapper execution started");

		// create executor pool
		ExecutorService pool = Executors.newFixedThreadPool(threadNumber);

		for (int run = 0; run < runs; run++) {
			Logger.print(run + ". run");
			Logger.log("RUN " + run, 0);

			// population
			List<Individual> population = new ArrayList<>();

			// stat
			List<Double> bestFitness = new ArrayList<>();

			// outer loop
			for (int generation = 0; generation < generations; generation++) {
				Logger.print(generation + ". generation");

				/**
				 * PARALLEL PART
				 */

				Logger.print("Feature synthesis and classification starting");

				// create array of individuals
				List<Future<Individual>> barrier = new ArrayList<>();

				for (int job = 0; job < populationSize; job++) {
					ParallelJob parallelJob = new ParallelJob(job, dataPath, threadExecTime);
					barrier.add(pool.submit(parallelJob));
				}

				for (Future<Individual> individual : barrier) {
					try {
						population.add(individual.get());
					} catch (InterruptedException | ExecutionException e) {
					}
				}

				/**
				 * SERIAL PART
				 */

				Logger.print("Building extended population");
				population.sort(new Comparator<Individual>() {

					@Override
					public int compare(Individual first, Individual second) {
						return (int) Math.signum(second.getFitness() - first.getFitness());
					}

				});

				Logger.print("Killing bad solutions");

				int diff = population.size() - populationSize;
				for (int index = population.size() - 1; diff > 0; index--, diff--) {
					population.remove(index);
				}

				Logger.print("Performing evolutionary operators");

				Evolution.crossover(population);
				Evolution.mutate(population);

				Evolution.parallelEvaluation(population, pool);

				// print population fitness
				for (Individual individual : population) {
					Logger.print("Individual fitness: " + individual.getFitness());
				}

				Logger.log(generation + "] " + population.get(0).toString());

				bestFitness.add(population.get(0).getFitness());
			}

			Statistics.makeRunStatistics(bestFitness);

			try {
				population.get(0).getRegressionEFM().saveBestFeatureSet(true);
				population.get(0).getRegressionEFM().saveBestModel(true);
			} catch (IOException e) {
			}
		}

		Statistics.makeAllStatistics();

		// shutdown
		pool.shutdown();

		Logger.closeLogger();

	}
}
