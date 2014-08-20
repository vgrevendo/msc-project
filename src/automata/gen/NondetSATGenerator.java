package automata.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Takes as input a SAT description file and generates the corresponding automaton,
 * in its DET version as shown in fma_npc.
 * To solve the SAT problem, apply an emptiness algorithm.
 * @author vincent
 *
 */
public class NondetSATGenerator extends AutomatonGenerator {
	protected final Scanner sc;
	
	protected int n;
	protected int m;
	protected final List<List<Integer>> clauses = new ArrayList<>();

	public NondetSATGenerator(String filename) throws FileNotFoundException {
		super("ND-SAT-GEN");
		
		sc = new Scanner(new File(filename));
	}

	@Override
	protected void build() throws BuildException {
		//parse
		parseInputFile();
		
		//and build
		buildAutomaton();
	}
	
	protected void buildAutomaton() {
		//Build automaton according to description in fma_npc
		//STATES
		addState(0, 2*n, false);
		for(int i = 1; i <= 2*n; i++) {
			addState(i, i-1, false);
		}
		for(int i = 2*n + 1; i <= 2*n + m; i++) {
			addState(i, -1, false);
		}
		addState(2*n+m+1, -1, true);
		
		//REGSITERS
		for(int i = 0; i < 2*n+1; i++)
			addRegister(-1);
		setInitialState(0);
		
		//TRANSITIONS
		//Delta1
		addTransition(0, 1, 2*n+1-1);
		addTransition(0, n+1, 2*n+1-1);
		addTransition(n, 2*n+1, n-1);
		addTransition(2*n, 2*n+1, 2*n-1);
		
		//Delta2
		for(int i = 1; i <= n-1; i++) {
			addTransition(i, i+1, i-1);
			addTransition(i, i+n+1, i-1);
		}
		for(int i = n+1; i <= 2*n-1; i++) {
			addTransition(i, i+1, i-1);
			addTransition(i, i-n+1, i-1);
		}
		
		//Delta3
		for(int i = 1; i <= m; i++) {
			for(int j : clauses.get(i-1)) {
				addTransition(2*n+i, 2*n+i+1, j-1);
			}
		}
	}

	private void parseInputFile() throws BuildException {
		//First line declares n
		if(!sc.hasNextLine()) {
			throw new BuildException("Empty file");
		}
		
		String line = sc.nextLine();
		String[] tokens = line.split("\\s+");
		
		n = Integer.parseInt(tokens[2]);
		
		//Next lines declare m and clauses
		while(sc.hasNextLine()) {
			line = sc.nextLine();
			tokens = line.split("\\s+");
			
			m++;
			List<Integer> clause = new ArrayList<Integer>();
			for(String token : tokens) {
				int tv = Integer.parseInt(token);
				if(tv > 0) {
					clause.add(tv);
				} else if(tv == 0) {
					break;
				} else {
					clause.add(-tv+n);
				}
			}
			if(clause.isEmpty())
				throw new BuildException("Empty clause line!");
			clauses.add(clause);
		}
		
		//Close input file
		sc.close();
		
		//Signal results
		System.out.println("  (i)(Found n=" + n + " and m=" + m + ")");
	}

}
