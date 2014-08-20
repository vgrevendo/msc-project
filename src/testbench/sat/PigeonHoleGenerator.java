package testbench.sat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PigeonHoleGenerator {
	public static final String PATH_ROOT = "res/satgen";
	
	private final String filename;
	private final PrintWriter pw;
	private final int n;
	
	public PigeonHoleGenerator(int n) throws FileNotFoundException {
		filename = chooseFileName();
		pw = new PrintWriter(new File(filename));
		this.n = n;
	}
	
	public String generate() {
		pw.write("p cnf " + (n*(n+1)) + " 50000");
		
		//There are n >= 1 holes and n+1 pigeons
		//Each pigeon needs to be in one pigeon hole
		for(int i = 0; i < n+1; i++) {
			List<Integer> holeDisjunctions = new ArrayList<>(n+1);
			for(int j = 0; j < n; j++) {
				holeDisjunctions.add(p(i,j));
			}
			addCNF(holeDisjunctions);
		}
		
		//Two distinct pigeons cannot be in the same hole
		for(int i = 0; i < n+1; i++) {
			for(int k = i+1; k < n+1; k++) {
				for(int j = 0; j < n; j++) {
					List<Integer> holeDisjunctions = new ArrayList<>(2);
					holeDisjunctions.add(-p(i,j));
					holeDisjunctions.add(-p(k,j));
					addCNF(holeDisjunctions);
				}
			}
		}
		
		pw.close();
		return filename;
	}
	
	private int p(int i, int j) {
		return i*n+j+1;
	}
	
	private void addCNF(List<Integer> disjunctions) {
		pw.print("\n");
		for(Integer d: disjunctions) {
			pw.print(d);
			pw.print(" ");
		}
		pw.print("0");
	}
	
	private String chooseFileName() {
		int id = 0;
		
		while((new File(PATH_ROOT + id + ".fma")).exists()) {
			id ++;
		}
		
		return PATH_ROOT + id + ".fma";
	}
}
