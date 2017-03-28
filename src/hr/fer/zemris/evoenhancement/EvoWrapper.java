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

public class EvoWrapper {

	private int generations;

	private int populationSize;

	private int threadNumber;

	private String dataPath;

	private double threadExecTime;

	public EvoWrapper(int generations, int populationSize, String dataPath, double threadExecTime) {
		this.generations = generations;
		this.threadNumber = Runtime.getRuntime().availableProcessors();
		this.populationSize = populationSize;
		this.dataPath = dataPath;
		this.threadExecTime = threadExecTime;
	}

	public void start() {

		Main.print("Wrapper execution started");

		// create executor pool
		ExecutorService pool = Executors.newFixedThreadPool(threadNumber);

		// population
		List<Individual> population = new ArrayList<>();

		// outer loop
		for (int generation = 0; generation < generations; generation++) {
			Main.print(generation + ". generation");

			/**
			 * PARALLEL PART
			 */

			Main.print("Feature synthesis and classification starting");

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

			Main.print("Building extended population");
			population.sort(new Comparator<Individual>() {

				@Override
				public int compare(Individual first, Individual second) {
					return (int) Math.signum(second.getFitness() - first.getFitness());
				}

			});

			Main.print("Killing bad solutions");

			int diff = population.size() - populationSize;
			for (int index = population.size() - 1; diff > 0; index--, diff--) {
				population.remove(index);
			}

			Main.print("Performing evolutionary operators");

			Evolution.crossover(population);
			Evolution.mutate(population);
			
			Evolution.parallelEvaluation(population, pool);
			
			//print population fitness
			for (Individual individual : population) {
				Main.print("Individual fitness: " + individual.getFitness());
			}
		}

		// shutdown
		pool.shutdown();

		// save best solution
		Individual bestIndividual = population.get(0);
		try {
			bestIndividual.getRegressionEFM().saveBestFeatureSet(true);
			bestIndividual.getRegressionEFM().saveBestModel(true);
		} catch (IOException e) {
		}

	}

}
