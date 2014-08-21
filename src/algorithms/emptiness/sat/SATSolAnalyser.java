package algorithms.emptiness.sat;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class SATSolAnalyser {
	public static void analyseOutput(String mapFilename, String solutionFilename) throws FileNotFoundException {
		//Read the hashmap
		Scanner mapScanner = new Scanner(new File(mapFilename));
		ArrayList<String> map = new ArrayList<>();
		map.add("STOP");
		
		while(mapScanner.hasNextLine()) {
			String tokens[] = mapScanner.nextLine().split("->");
			map.add(tokens[1]);
		}
		
		mapScanner.close();
		
		//Read the solution
		Scanner solutionScanner = new Scanner(new File(solutionFilename));
		String[] valuationTokens = null;
		
		while(solutionScanner.hasNextLine()) {
			String line = solutionScanner.nextLine();
			
			switch(line.substring(0,1)) {
			case "s":
				if(!line.split(" ")[1].equals("SATISFIABLE")) {
					System.out.println("Formula was not satisfiable! : " + line);
					solutionScanner.close();
					System.exit(0);
				} else
					System.out.println("Formula was satisfiable.");
				break;
			case "v":
				valuationTokens = line.substring(2).split(" ");
				System.out.println(valuationTokens.length + " values found for valuation! True atoms:");
				break;
			}
		}
		
		for(String valuation : valuationTokens) {
			int val = Integer.parseInt(valuation);
			if(val > 0) {
				System.out.println(map.get(val));
			}
		}
	}
	public static void translateFormula(String mapFilename, String formulaFilename) throws FileNotFoundException {
		//Read the hashmap
		Scanner mapScanner = new Scanner(new File(mapFilename));
		ArrayList<String> map = new ArrayList<>();
		map.add("STOP");
		
		while(mapScanner.hasNextLine()) {
			String tokens[] = mapScanner.nextLine().split("->");
			map.add(tokens[1]);
		}
		
		mapScanner.close();
		
		//Read the solution
		Scanner formulaScanner = new Scanner(new File(formulaFilename));
		System.out.println(formulaScanner.nextLine());
		
		while(formulaScanner.hasNextLine()) {
			String line = formulaScanner.nextLine();
			String[] tokens = line.split(" ");
			
			for(int i = 0; i < tokens.length-1; i++) {
				int t = Integer.parseInt(tokens[i]);
				System.out.print((t < 0 ? "not " : "") + map.get(Math.abs(t)) + ", ");
			}
			
			System.out.println();
		}
		
		formulaScanner.close();
	}
}
