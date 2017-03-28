package hr.fer.zemris.evoenhancement;

public class Main {

	private static int GENERATIONS = 20;
	private static int POPULATION_SIZE = 5;
	private static String DATA_PATH = "data/1data.arff";
	private static double THREAD_EXEC_TIME = 0.5;

	public static void main(String[] args) {

		EvoWrapper evolutionaryWrapper = new EvoWrapper(GENERATIONS, POPULATION_SIZE, DATA_PATH,
				THREAD_EXEC_TIME);

		evolutionaryWrapper.start();

	}

	public static void print(String message) {
		System.out.println("> " + message);
	}

}
