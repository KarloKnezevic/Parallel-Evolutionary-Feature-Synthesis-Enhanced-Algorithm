package hr.fer.zemris.evoenhancement.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import hr.fer.zemris.evoenhancement.EvoWrapper;

public class Logger {

	private static Writer writer;

	public static EvoWrapper initEvoWrapper(String fileName) throws IOException {

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new BufferedInputStream(new FileInputStream(fileName)), "UTF-8"));

		int runs = 0, generations = 0, populationsize = 0;
		double threadexect = 0;
		String dataPath = null;

		String line;
		while ((line = reader.readLine()) != null) {

			line = line.trim();

			// empty line
			if (line.isEmpty()) {
				continue;
			}

			// comment
			if (line.charAt(0) == '#') {
				continue;
			}

			String[] parameters = line.split("=");
			if (parameters.length != 2) {
				System.err.println("Input file error!");
				System.exit(1);
			}

			String key = parameters[0];
			String value = parameters[1];

			try {
				if (key.equals("runs")) {
					runs = Integer.parseInt(value);
				} else if (key.equals("generations")) {
					generations = Integer.parseInt(value);
				} else if (key.equals("population")) {
					populationsize = Integer.parseInt(value);
				} else if (key.equals("data")) {
					dataPath = parameters[1];
				} else if (key.equals("threadexect")) {
					threadexect = Double.parseDouble(value);
				} else {
					System.err.println("Not supported parameter.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		reader.close();

		return new EvoWrapper(runs, generations, populationsize, dataPath, threadexect);
	}

	public static void log(String message) {

		if (null == writer) {
			try {
				writer = new BufferedWriter(new OutputStreamWriter(
						new BufferedOutputStream(new FileOutputStream("report.txt")), "UTF-8"));
			} catch (UnsupportedEncodingException | FileNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}

		try {
			writer.write(message);
			writer.write(System.getProperty("line.separator"));
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(String message, int level) {

		if (0 == level) {
			log("--------------------------------------------");
			log(message);
		} else if (1 == level) {
			log("#####");
			log(message);
		} else {
			log(message);
		}

	}

	public static void closeLogger() {
		if (null != writer) {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void print(String message) {
		System.out.println("> " + message);
	}

}
