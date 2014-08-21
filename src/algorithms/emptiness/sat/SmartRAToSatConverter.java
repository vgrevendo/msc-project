package algorithms.emptiness.sat;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import testbench.Tools;
import automata.RegisterAutomaton;
import automata.State;
import automata.gen.BuildException;

/**
 * This class is a support class for reducing the nonemptiness problem to a SAT instance
 * using the STRIPS translation procedure.
 * @author vincent
 *
 */
public class SmartRAToSatConverter {
	public static final String PATH_ROOT = "gen/ra-gen";
	public static final int DEF_N = 10+30;
	
	private final PrintWriter pw;
	private final PrintWriter mapPw;
	private final RegisterAutomaton ra;
	private final String filename;
	private final String mapFilename;
	
	private final List<List<Integer>> clauses = new LinkedList<>();
	private final Map<String, Integer> litterals = new HashMap<String, Integer>();
	private List<Integer> clause = new LinkedList<>();
	
	private int n = 0;
	
	public SmartRAToSatConverter(RegisterAutomaton ra) throws FileNotFoundException {
		this.ra = ra;
		this.pw = new PrintWriter(filename = Tools.chooseFileName(PATH_ROOT, "cnf"));
		this.mapPw = new PrintWriter(mapFilename = Tools.chooseFileName("gen/map-sat", "map"));
	}
	
	/**
	 * Generates the CNF file at the returned location.
	 * @return
	 * @throws BuildException 
	 */
	public String generate() throws BuildException {
		//Compute N
		int N = computeN();
		
		System.out.println("[STRIPS] This is IntelliSTRIPS, commencing transformation -------");
		System.out.println("[STRIPS] Estimated N: " + N);
		
		//Generate STRIPS formula given N
		System.out.println("[STRIPS] Generating litterals");
		addLitterals(N);
		System.out.println("[STRIPS] Computing phi INIT");
		computePhiInit(N);
		System.out.println("[STRIPS] Computing phi GOAL");
		computePhiGoal(N);
		System.out.print("[STRIPS] Computing phi SUCC");
		computePhiSucc(N);
		System.out.println("[STRIPS] Computing phi PREC");
		computePhiPrec(N);
		System.out.print("[STRIPS] Computing phi EXCL");
		computePhiExcl(N);
		
		//Write to CNF file
		System.out.println("[STRIPS] Writing CNF file");
		writeCNF();
		
		System.out.println("[STRIPS] Litterals: " + n);
		System.out.println("[STRIPS] Clauses: " + clauses.size());
		System.out.println("[STRIPS] File written to " + filename);
		System.out.println("[STRIPS] Map written to " + mapFilename);
		System.out.println("[STRIPS] Done --------------------------------------------------");
		
		return filename;
	}
	
	private int computeN() {
		if(DEF_N > 0)
			return DEF_N;
		return ra.getInitialRegisters().length * ra.getStates().length;
	}
	
	//Litterals
	private void addLitterals(int N) throws BuildException {
		State[] states = ra.getStates();
		int[] registers = ra.getInitialRegisters();
		Map<State, Map<Integer, List<State>>> mu = ra.getTransitions();
		
		//Temporal ground atoms
		// Defines the current configuration
		for(int t = 0; t <= N; t++) {
			//Add initial locations
			for(State q: states) {
				addLitteral("at-" + t + "-" + q.name);
			}
			
			//Add register statuses
			for(int r = 0; r < registers.length; r++) {
				addLitteral("regset-" + t + "-" + r);
			}
		}
		
		//Temporal actions
		// There are only N of them, not N+1
		for(int t = 0; t < N; t++) {
			for(Entry<State, Map<Integer, List<State>>> ts : mu.entrySet()) {
				State qi = ts.getKey();
				Integer rho = ra.getAssignmentRegister(qi);
				for(Entry<Integer, List<State>> trans : ts.getValue().entrySet()) {
					int r = trans.getKey();
					for(State qj : trans.getValue()) {
						if(qi == qj) {
							if(rho != null && rho == r)
								//Move same rho t (qi,r)
								addLitteral("movesamerho-" + t + "-" + qi.name + "-" + r);
							else
								//Move same t (qi,r)
								addLitteral("movesame-" + t + "-" + qi.name + "-" + r);
						} else {
							if(rho != null && rho == r)
								//Move rho t (qi,r,qj)
								addLitteral("moverho-" + t + "-" + qi.name + "-" + r + "-" + qj.name);
							else
								//Move t (qi,r,qj)
								addLitteral("move-" + t + "-" + qi.name + "-" + r + "-" + qj.name);
						}
					}
				}
			}
		}
	}
	
	//Phi values
	private void computePhiInit(int N) throws BuildException {
		int[] registers = ra.getInitialRegisters();
		
		//CLAUSES
		//The current state is q0
		State initialState = ra.getInitialState();
		addToClause("at-0-" + initialState.name, true);
		commitClause();
		
		//The current state is not the others
		for(State q:ra.getStates()) {
			if(q == initialState)
				continue;
			
			addToClause("at-0-" + q.name, false);
			commitClause();
		}
		
		//Registers that are set in the automaton are set
		for(int r = 0; r < registers.length; r++) {
			addToClause("regset-0-" + r, registers[r] >= 0);
			commitClause();
		}
	}
	private void computePhiGoal(int N) throws BuildException {
		State[] states = ra.getStates();
		
		//Whether a configuration is a goal or not depends on the 
		// state solely
		for(State q: states) {
			if(q.isFinal) {
				addToClause("at-" + N + "-" + q.name, true);
			}
		}
		commitClause();
	}
	private void computePhiSucc(int N) throws BuildException {
		State[] states = ra.getStates();
		int[] registers = ra.getInitialRegisters();
		Map<State, Map<Integer, List<State>>> mu = ra.getTransitions();
		
		//Build destination-based mu (dbmu: dest-reg-source)
		Map<State, Map<Integer, List<State>>> dbmu = new HashMap<>();
		
		for(Entry<State, Map<Integer, List<State>>> ts : mu.entrySet()) {
			for(Entry<Integer, List<State>> trans : ts.getValue().entrySet()) {
				for(State destination : trans.getValue()) {
					if(!dbmu.containsKey(destination)) {
						dbmu.put(destination, new HashMap<Integer, List<State>>());
					}
					if(!dbmu.get(destination).containsKey(trans.getKey())) {
						dbmu.get(destination).put(trans.getKey(), new ArrayList<State>());
					}
					dbmu.get(destination).get(trans.getKey()).add(ts.getKey());
				}
			}
		}
		
		//Build register-based mu (rbmu: reg-source-dest)
		Map<Integer, Map<State, List<State>>> rbmu = new HashMap<>();
		
		for(Entry<State, Map<Integer, List<State>>> ts : mu.entrySet()) {
			for(Entry<Integer, List<State>> trans : ts.getValue().entrySet()) {
				for(State destination : trans.getValue()) {
					if(!rbmu.containsKey(trans.getKey())) {
						rbmu.put(trans.getKey(), new HashMap<State, List<State>>());
					}
					if(!rbmu.get(trans.getKey()).containsKey(ts.getKey())) {
						rbmu.get(trans.getKey()).put(ts.getKey(), new ArrayList<State>());
					}
					rbmu.get(trans.getKey()).get(ts.getKey()).add(destination);
				}
			}
		}
		
		//Fill in action constraints
		for(int t = 0; t < N; t++) {
			//Ground atoms: AT
			for(State q: states) {
				String groundAtomT = "at-" + t + "-" + q.name;
				String groundAtomTP = "at-" + (t+1) + "-" + q.name;
				List<String> posActionClause = new LinkedList<>();
				List<String> negActionClause = new LinkedList<>();
				
				//Actions causing F: all possible moves with destination q
				if(dbmu.containsKey(q))
					for(Entry<Integer, List<State>> ts : dbmu.get(q).entrySet()) {
						int r = ts.getKey();
						for(State qp : ts.getValue()) {
							Integer rho = ra.getAssignmentRegister(qp);
							if(q == qp) {
								//Same clauses
								if(rho != null && rho == r)
									posActionClause.add("movesamerho-" + t + "-" + q.name + "-" + r);
								else
									posActionClause.add("movesame-" + t + "-" + q.name + "-" + r);
							} else {
								//Regular clauses
								if(rho != null && rho == r)
									posActionClause.add("moverho-" + t + "-" + qp.name + "-" + r + "-" + q.name);
								else
									posActionClause.add("move-" + t + "-" + qp.name + "-" + r + "-" + q.name);
							}
						}
					}
				
				//Actions causing not F: all possible moves with source q
				Integer rho = ra.getAssignmentRegister(q);
				if(mu.containsKey(q))
					for(Entry<Integer, List<State>> ts : mu.get(q).entrySet()) {
						int r = ts.getKey();
						for(State qp : ts.getValue()) {
							if(q == qp) {
								continue;
							} else {
								//Regular clauses
								if(rho != null && rho == r)
									negActionClause.add("moverho-" + t + "-" + q.name + "-" + r + "-" + qp.name);
								else
									negActionClause.add("move-" + t + "-" + q.name + "-" + r + "-" + qp.name);
							}
						}
					}
				
				addSuccClause(groundAtomTP, posActionClause, groundAtomT, negActionClause);
			}
			
			//Ground atoms: REGSET
			for(int r = 0; r < registers.length; r++) {
				String groundAtomT = "regset-" + t + "-" + r;
				String groundAtomTP = "regset-" + (t+1) + "-" + r;
				List<String> posActionClause = new LinkedList<>();
				List<String> negActionClause = new LinkedList<>();
				
				//Actions causing F: move rho and move same rho for register r
				for(Entry<State, List<State>> trans : rbmu.get(r).entrySet()) {
					State q = trans.getKey();
					Integer rho = ra.getAssignmentRegister(q);
					
					for(State qp : trans.getValue()) {
						if(rho == null || rho != r)
							continue;
						if(qp == q)
							posActionClause.add("movesamerho-" + t + "-" + q.name + "-" + r);
						else
							posActionClause.add("moverho-" + t + "-" + q.name + "-" + r + "-" + qp.name);
					}
					
				}
				
				//Actions causing not F: none
				
				addSuccClause(groundAtomTP, posActionClause, groundAtomT, negActionClause);
			}
			
			System.out.print(" : " + t + "/" + N);
		}
		
		System.out.println();
	}
	private void computePhiPrec(int N) throws BuildException {
		Map<State, Map<Integer, List<State>>> mu = ra.getTransitions();
		
		for(int t = 0; t < N; t++) {
			for(Entry<State, Map<Integer, List<State>>> ts : mu.entrySet()) {
				State q = ts.getKey();
				Integer rho = ra.getAssignmentRegister(q);
				
				for(Entry<Integer, List<State>> trans : ts.getValue().entrySet()) {
					int r = trans.getKey();
					for(State qp : trans.getValue()) {
						String actionAtom = null;
						List<String> precAtoms = null;
						if(q == qp) {
							if(rho != null && rho == r) {
								actionAtom = "movesamerho-" + t + "-" + q.name + "-" + r;
								precAtoms = new LinkedList<String>();
								precAtoms.add("at-" + t + "-" + q.name);
							} else {
								//Move same
								actionAtom = "movesame-" + t + "-" + q.name + "-" + r;
								precAtoms = new LinkedList<String>();
								precAtoms.add("regset-" + t + "-" + r);
								precAtoms.add("at-" + t + "-" + q.name);
							}
						} else {
							if(rho != null && rho == r) {
								//Move rho action
								actionAtom = "moverho-" + t + "-" + q.name + "-" + r + "-" + qp.name;
								precAtoms = new LinkedList<String>();
								precAtoms.add("at-" + t + "-" + q.name);
								precAtoms.add("-at-" + t + "-" + qp.name);
							} else {
								//Move action
								actionAtom = "move-" + t + "-" + q.name + "-" + r + "-" + qp.name;
								precAtoms = new LinkedList<String>();
								precAtoms.add("regset-" + t + "-" + r);
								precAtoms.add("at-" + t + "-" + q.name);
								precAtoms.add("-at-" + t + "-" + qp.name);
							}
						}
						addPrecClause(actionAtom, precAtoms);
					}
				}
			}
		}
	}
	private void computePhiExcl(int N) throws BuildException {
		Map<State, Map<Integer, List<State>>> mu = ra.getTransitions();
		
		for(int t = 0; t < N; t++) {
			
			//Generate all actions
			Set<String> actions = new HashSet<>();
			for(Entry<State, Map<Integer, List<State>>> ts : mu.entrySet()) {
				State q = ts.getKey();
				Integer rho = ra.getAssignmentRegister(q);
				for(Entry<Integer, List<State>> trans : ts.getValue().entrySet()) {
					int r = trans.getKey();
					for(State qp : trans.getValue()) {
						if(q == qp) {
							if(rho != null && rho == r) {
								//Move same rho
								actions.add("movesamerho-" + t + "-" + q.name + "-" + r);
							} else {
								//Move same
								actions.add("movesame-" + t + "-" + q.name + "-" + r);
							}
						} else {
							if(rho != null && rho == r) {
								//Move rho action
								actions.add("moverho-" + t + "-" + q.name + "-" + r + "-" + qp.name);
							} else {
								//Move action
								actions.add("move-" + t + "-" + q.name + "-" + r + "-" + qp.name);
							}
						}
					}
				}
			}
			
			//Generate all possibilities
			for(String action1 : actions) {
				for(String action2 : actions) {
					if(action1 == action2)
						continue;
					
					
					addExclClause(action1, action2);
				}
			}
			System.out.print(" : " + t + "/" + N);
		}
		System.out.println();
	}
	
	private void writeCNF() {
		pw.print("p cnf " + n + " " + clauses.size());
		for(List<Integer> clause : clauses) {
			pw.println();
			for(Integer i : clause) {
				pw.print(i.toString() + " ");
			}
			pw.print(0);
		}
		
		pw.close();
		mapPw.close();
	}
	
	//Low-level tools
	private void addLitteral(String name) throws BuildException {
		if(litterals.containsKey(name))
			throw new BuildException("Litteral " + name + " added twice!");
		
		litterals.put(name, ++n);
		mapPw.println(n + "->" + name);
	}
	private void addToClause(String litteral, boolean truth) throws BuildException {
		if(!litterals.containsKey(litteral))
			throw new BuildException("Litteral " + litteral + " unknown!");
		
		clause.add((truth ? 1 : -1) * litterals.get(litteral));
	}
	private void commitClause() {
		if(clause.isEmpty())
			return;
		
		clauses.add(clause);
		clause = new LinkedList<>();
	}
	
	/**
	 * <p>
	 * Given the mapping:
	 * <ul>
	 * 	<li>A: F T+1</li>
	 * 	<li>B1 or B2: ACF T</li>
	 *  <li>C: F T</li>
	 *  <li>D1 or D2: ACNF T</li> 
	 * </ul>
	 * 
	 * then this function adds several clauses in the generalised form of
	 * </p>
	 * 
	 * <p>
	 * ((NOT A) OR  B1 OR B2 OR  C) AND  ((NOT A) OR  B OR  D OR  E OR  F) AND  (A OR  (NOT B)) AND  (A OR  (NOT C) OR  (NOT D)) AND  (A OR  (NOT C) OR  (NOT F)) AND  (A OR  (NOT E))
	 * wrong - see dissertation!
	 * </p>
	 * @param groundAtomTP
	 * @param posActionClause
	 * @param groundAtomT
	 * @param negActionClause
	 * @throws BuildException 
	 */
	private void addSuccClause(String groundAtomTP, List<String> posActionClause,
							   String groundAtomT,  List<String> negActionClause) throws BuildException {
		//CLAUSE 1: ((NOT A) OR  B OR  C OR  E)
		addToClause(groundAtomTP, false);
		addToClause(groundAtomT, true);
		for(String acft: posActionClause) {
			addToClause(acft, true);
		}
		commitClause();
		
		//CLAUSE 2: ((NOT A) OR  B OR  D OR  E OR  F)
		for(String acnft: negActionClause) {
			addToClause(groundAtomTP, false);
			for(String acft: posActionClause) {
				addToClause(acft, true);
			}
			addToClause(acnft, false);
			commitClause();
		}
		
		//CLAUSE 3: (A OR  (NOT B)) AND (A OR  (NOT E))
		for(String acft: posActionClause) {
			addToClause(groundAtomTP, true);
			addToClause(acft, false);
			commitClause();
		}
		
		//CLAUSE 4: (A OR  (NOT C) OR  (NOT D)) AND  (A OR  (NOT C) OR  (NOT F))
		addToClause(groundAtomTP, true);
		addToClause(groundAtomT, false);
		for(String acnft: negActionClause) {
			addToClause(acnft, true);
		}
		commitClause();
	}
	private void addPrecClause(String actionAtom, List<String> precAtoms) throws BuildException {
		for(String prec : precAtoms) {
			addToClause(actionAtom, false);
			if(prec.startsWith("-")) {
				addToClause(prec.substring(1), false);
			} else {
				addToClause(prec, true);
			}
			
			commitClause();
		}
	}
	private void addExclClause(String action1, String action2) throws BuildException {
		addToClause(action1, false);
		addToClause(action2, false);
		commitClause();
	}
}
