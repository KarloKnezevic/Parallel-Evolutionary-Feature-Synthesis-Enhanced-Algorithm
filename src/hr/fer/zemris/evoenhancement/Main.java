package hr.fer.zemris.evoenhancement;

import java.io.IOException;

import hr.fer.zemris.evoenhancement.util.Logger;

public class Main {

	public static void main(String[] args) {
		
		if (args.length != 1) {
			System.err.println("Path to parameter file not given");
			System.exit(1);
		}
		
		String parameters = args[0];
		
		try {
			Logger.initEvoWrapper(parameters).start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}
