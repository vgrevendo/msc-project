package testbench;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import references.MembershipAlgorithms;
import testbench.lister.FileWordLister;
import testbench.lister.LargeFileLister;
import testbench.lister.TestLister;
import testbench.lister.TestWordLister;
import testbench.programs.translator.SafeIterTranslator;
import testbench.programs.translator.StrictHasNextTranslator;
import testbench.programs.translator.TRFTranslator;
import testbench.programs.translator.Translator;
import testbench.sat.PigeonHoleGenerator;
import testbench.tests.AsymptoticEmptinessTest;
import testbench.tests.AsymptoticMembershipTest;
import testbench.tests.ListMembershipTest;
import algorithms.Emptiness;
import algorithms.Membership;
import algorithms.emptiness.EMPDecisionAlgorithm;
import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.Automaton;
import automata.OptimisedRA;
import automata.RegisterAutomaton;
import automata.gen.AutomatonGenerator;
import automata.gen.BuildException;
import automata.gen.HighLevelPropertyGenerator;
import automata.gen.NondetSATGenerator;
import automata.gen.RootBranchGenerator;
import automata.gen.SpecificationSynthGenerator;
import automata.gen.StrictDCGenerator;
import automata.greedy.GreedyRA;
import automata.hra.HRAutomaton;

public class Testbench {
	public final static int TEST_LENGTH = 1500;
	public final static String HNP_TEST_TRACE_PATH = "gen/trace4.tr";

	public final static boolean DEBUG = true;
	public final static boolean COLLECT_STATS = false;

	/**
	 * A stub for parameter parsing has been implemented here, to make launches
	 * from the command line easier.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		System.out.println("Arguments: " + Arrays.toString(args));
		double p = 0.0D;

		try {

			switch (args[0]) {
			case "FMA2SAT":
				fma2satGen(); break;
			case "pigeon-test":
				pigeonHoleTest(args); break;
			case "pigeon-gen":
				pigeonHoleGenTest(); break;
			case "sattest":
				satTest(args); break;
			case "satgen":
				satGenTest(); break;
			case "mbs": mbsTests(args); break;
			case "deterministic-check":
				deterministicTest(); break;
			case "synth":
				synthesisTest(); break;
			case "synth2":
				hlpTest(); break;
			case "translation":
				translationTest(); break;
			case "safeIter":
				p = Double.parseDouble(args[2]);
				if (args[1].equals("LATEST"))
					safeIterTest(findLatestFilename("gen/trace", "tr"), p);
				else
					safeIterTest(args[1], p);
				break;
			case "hasNextProperty-STRICT":
				p = Double.parseDouble(args[2]);
				if (args[1].equals("LATEST"))
					strictHasNextPropertyTest(
							findLatestFilename("gen/trace", "tr"), p);
				else
					strictHasNextPropertyTest(args[1], p);
				break;
			case "greedy":
				if (args.length != 5) {
					System.out
							.println("Unexpected number of arguments on command line.");
					return;
				}

				greedyAutoTest(args[1], args[2], args[3], args[4]);
				break;
			case "greedyPT":
				if (args.length != 4) {
					System.out
							.println("Unexpected number of arguments on command line.");
					return;
				}

				greedyPTTest(args[1], args[2], args[3]);
				break;
			case "sdc":
				if(args.length != 2) {
					System.out.println("Expecting n");
					return;
				}
				gdcTest(args[1]);
				break;	
			case "sdcTest":
				if(args.length != 2) {
					System.out.println("Expecting n");
					return;
				}
				gdcAsymptoticTest(args[1]);
				break;
			case "auto":
			default:
				if (args.length != 4) {
					System.out
							.println("Unexpected number of arguments on command line.");
					return;
				}

				fullyAutomaticPropertyTest(args[1], args[2], args[3]);
			}
		} catch (Exception e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
	}
	private static void fma2satGen() {
		
	}
	private static void pigeonHoleTest(String[] args) throws FileNotFoundException, BuildException, ParseException {
		final int N = Integer.parseInt(args[1]);
		final int STEP = Integer.parseInt(args[2]);
		
		//Make testlister
		TestLister<RegisterAutomaton> rag = new TestLister<RegisterAutomaton>() {
			@Override
			protected RegisterAutomaton nextResource() {
				PigeonHoleGenerator phg;
				try {
					phg = new PigeonHoleGenerator(STEP*(index+1));
					String cnffile = phg.generate();
					System.out.println("CNF output to " + cnffile);
					AutomatonGenerator ag = new NondetSATGenerator(cnffile);
					String rafile = ag.generate();
					RegisterAutomaton ra = new RegisterAutomaton(rafile);
					return ra;
				} catch (FileNotFoundException | BuildException | ParseException e) {
					e.printStackTrace();
					System.exit(0);
				}

				return null;
			}

			@Override
			public int size() {
				return N/STEP;
			}
		};
		
		//Pick algorithms
		EMPDecisionAlgorithm[] algs = {
				Emptiness.generativeDFSCheck,
		};

		Test t = new AsymptoticEmptinessTest(rag, algs);
		t.test();
		ResultsContainer.getContainer().flush();
	}
	private static void pigeonHoleGenTest() throws FileNotFoundException {
		PigeonHoleGenerator phg = new PigeonHoleGenerator(1);
		String filename = phg.generate();
		System.out.println("PHG to " + filename);
	}
	private static void satTest(String[] args) throws FileNotFoundException, BuildException, ParseException {
		AutomatonGenerator ag = new NondetSATGenerator(args[1]);
		String filename = ag.generate();
		RegisterAutomaton a = new RegisterAutomaton(filename);
		a.displayInfo();
		
		//Do emptiness check
		if(Emptiness.generativeBFGSCheck.decide(a)) {
			System.out.println("Emptiness is success");
		} else {
			System.out.println("Fail");
		}
	}
	private static void gdcAsymptoticTest(String string) {
		final int n = Integer.parseInt(string);
		final int iterations = 10;
		final int step = n/iterations;
		
		//Make word lister
		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			private final int NUMBER = 23;
			
			@Override
			public int size() {
				return iterations;
			}
			
			@Override
			protected List<Integer> nextResource() {
				final int limit = 2*(index+1)*step+2;
				List<Integer> word = new ArrayList<>(limit);
				//Make word of size 2n+2 of all the same numbers
				for(int i = 0; i < limit; i++)
					word.add(NUMBER);
				return word;
			}
		};
		
		//Make automaton generator
		TestLister<RegisterAutomaton> tag = new TestLister<RegisterAutomaton>() {

			@Override
			protected RegisterAutomaton nextResource() {
				AutomatonGenerator ag = new StrictDCGenerator((index+1)*step);
				String filename;
				try {
					filename = ag.generate();
					RegisterAutomaton ra = new RegisterAutomaton(filename);
					return ra;
				} catch (FileNotFoundException | BuildException | ParseException e) {
					e.printStackTrace();
					System.exit(-1);
				}
				
				return null;
			}

			@Override
			public int size() {
				return iterations;
			}
		};
		
		//List algorithms
		MBSDecisionAlgorithm[] algs = new MBSDecisionAlgorithm[] {
//			Membership.ldftsCheck,
			Membership.bflgsCheck,
		};
		
		//Create and run test
		AsymptoticMembershipTest test = new AsymptoticMembershipTest(tag, twg, algs);
		test.test();
		
		//Flush output
		ResultsContainer.getContainer().flush();
	}
	private static void gdcTest(String string) throws FileNotFoundException, BuildException, ParseException {
		AutomatonGenerator ag = new StrictDCGenerator(Integer.parseInt(string));
		String filename = ag.generate();
		RegisterAutomaton ra = new RegisterAutomaton(filename);
		ra.displayInfo();
	}
	private static void hlpTest() throws FileNotFoundException, BuildException {
		HighLevelPropertyGenerator hlpg = new HighLevelPropertyGenerator("res/unique_servlet_output.hlp");
		hlpg.generate();
	}
	public static void mbsTests(String[] args) throws FileNotFoundException, ParseException {
		//Build resources
		Map<String, MBSDecisionAlgorithm> algorithms = new HashMap<>();
		algorithms.put("LDFTS", Membership.ldftsCheck);
		algorithms.put("BFLGS", Membership.bflgsCheck);
		algorithms.put("OBFLGS", Membership.optiBflgsCheck);
		algorithms.put("FBFLGS", Membership.forgetfulBflgsCheck);
		algorithms.put("GBFLGS", Membership.greedyCheck);
		algorithms.put("BFS", Membership.bfsCheck);
		algorithms.put("HNP-REF", MembershipAlgorithms.hasNextTrueRef);
		
		//Parse arguments
		String chosenAlgorithm = args[1];
		String automaton = args[2];
		String tracePath = args[3];
		String difficulty = args[4];
		String outputPath = args[5];
		
		//Build parameters
		MBSDecisionAlgorithm[] chosenAlgorithms = {
				algorithms.get(chosenAlgorithm)
		};
		
		Automaton a = null;
		
		switch(chosenAlgorithm) {
		case "GBFLGS":
			GreedyRA gra = new GreedyRA(automaton);
			gra.displayInfo();
			a = gra;
			break;
		case "":
			OptimisedRA ora = new OptimisedRA(automaton);
			ora.displayInfo();
			a = ora;
			break;
		default:
			RegisterAutomaton ra = new RegisterAutomaton(automaton);
			ra.displayInfo();
			a = ra;
		}
		
		TestLister<List<Integer>> twg = null;
		if(difficulty.trim().equals("all")) {
			twg = new LargeFileLister(tracePath);
		} else {
			String[] dTokens = difficulty.split("/");
			final double tracePercentage = Double.parseDouble(dTokens[0]);
			final double traceStep = Double.parseDouble(dTokens[1]);
			twg = new FileWordLister(traceStep, tracePercentage, tracePath);
		}
		

		Test lmt = new ListMembershipTest(a, chosenAlgorithms, twg, outputPath);
		lmt.test();

		ResultsContainer.getContainer().flush();
	}
	/**
	 * Tests for membership on sequences of (automaton, word)
	 * 
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static void mbsAsymptTests() throws FileNotFoundException,
			ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				Membership.ldftsCheck, Membership.bflgsCheck,
				Membership.bestFirstCheck, Membership.aStarCheck };

		final int numWords = 100;

		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			@Override
			public int size() {
				return TEST_LENGTH - 7;
			}

			@Override
			protected List<Integer> nextResource() {
				List<Integer> l = new ArrayList<>(index + 5);
				for (int i = 0; i < index + 5; i++) {
					l.add(0);
				}
				return l;
			}
		};

		TestLister<RegisterAutomaton> rag = new TestLister<RegisterAutomaton>() {
			@Override
			protected RegisterAutomaton nextResource() {
				RootBranchGenerator rbg = new RootBranchGenerator(index + 5);
				String filename;
				try {
					filename = rbg.generate();
					return new HRAutomaton(filename, index + 10);
				} catch (ParseException | FileNotFoundException
						| BuildException e) {
					e.printStackTrace();
				}

				return null;

			}

			@Override
			public int size() {
				return numWords;
			}
		};

		Test amt = new AsymptoticMembershipTest(rag, twg, algorithms);
		amt.test();

		ResultsContainer.getContainer().flush();
	}
	public static void generatorTests() throws FileNotFoundException,
			ParseException, BuildException {
		System.out.println("This is GENERATOR TESTBENCH");

		AutomatonGenerator ag = new RootBranchGenerator(5);
		String filename = ag.generate();

		RegisterAutomaton ra = new RegisterAutomaton(filename);
		ra.displayInfo();
	}
	/**
	 * Testing the hasNext property with the newly created automata
	 * example6.fma, while translating the input trace intelligently
	 * 
	 * @throws ParseException
	 * @throws FileNotFoundException
	 */
	public static void strictHasNextPropertyTest(final String tracePath,
			final double tracePercentage) throws FileNotFoundException,
			ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
		// Membership.ldftsCheck,
		// Membership.bflgsCheck,
		Membership.optiBflgsCheck,
		// Membership.forgetfulBflgsCheck,
		// Membership.bestFirstCheck,
		// Membership.aStarCheck
		};

		RegisterAutomaton ra = new HRAutomaton("res/example6.fma", 6000);
		ra.displayInfo();

		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			private final static double ANALYSIS_STEP = 0.1D;
			private List<Integer> translation;

			@Override
			public int size() {
				return (int) (1.0 / ANALYSIS_STEP);
			}

			@Override
			protected List<Integer> nextResource() {
				if (translation == null) {
					try {
						Translator translator = new StrictHasNextTranslator(
								tracePath);
						translation = translator.translate();
						System.out.println("Translation size: "
								+ translation.size());
					} catch (FileNotFoundException e) {
						System.out.println("Could not load word from trace...");
						e.printStackTrace();
						System.exit(-1);
					}
				}

				return translation.subList(
						0,
						Math.min((int) (translation.size() * (index + 1)
								* ANALYSIS_STEP * tracePercentage),
								translation.size()));
			}
		};

		Test lmt = new ListMembershipTest(ra, algorithms, twg);
		lmt.test();

		ResultsContainer.getContainer().flush();
	}
	/**
	 * Just a test to see what the translation gives
	 * 
	 * @throws Exception
	 */
	public static void translationTest() throws Exception {
		int errors = 0;
		int line = 1;
		int tsize = 0;
		try {
			List<Integer> translation = (new SafeIterTranslator("gen/trace0.tr"))
					.translate();
			List<Integer> translation2 = (new TRFTranslator("gen/trace0.tr",
					"res/safe_iter.trf")).translate();
			Iterator<Integer> it = translation.iterator();
			Iterator<Integer> it2 = translation2.iterator();
			System.out.println("Real trace size is "
					+ (tsize = translation.size()));

			while (true) {
				if (!(it.hasNext() && it2.hasNext())) {
					if (it.hasNext()) {
						System.err.println("IT2 is shorter than IT1!");
					}
					if (it2.hasNext()) {
						System.err.println("IT1 is shorter than IT2!");
					}
					break;
				}

				int i1, i2;
				if ((i1 = it.next()) != (i2 = it2.next())) {
					errors++;
					if (errors < 10)
						System.err.println("A mistake occurred (line=" + line
								+ "): " + i1 + " vs " + i2);
				}

				line++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (errors >= 10) {
			System.out.println("... and " + (errors - 10)
					+ " more errors occurred");
		}
		System.out.println((line - 1) + "/" + tsize
				+ " symbols were analysed, " + (line - errors - 1)
				+ " were agreed upon.");
		System.out.println("DONE");
	}
	private static String findLatestFilename(String root, String extension) {
		int id = 0;

		while ((new File(root + id + "." + extension)).exists()) {
			id++;
		}

		return root + (id - 1) + "." + extension;
	}
	private static void synthesisTest() throws FileNotFoundException,
			BuildException {
		AutomatonGenerator propRAGen = new SpecificationSynthGenerator(
				"res/has_next.mra");
		propRAGen.generate();
	}
	private static void safeIterTest(final String tracePath,
			final double tracePercentage) throws FileNotFoundException,
			ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
		// Membership.ldftsCheck,
		// Membership.bflgsCheck,
		// Membership.optiBflgsCheck,
		Membership.forgetfulBflgsCheck,
		// Membership.bestFirstCheck,
		// Membership.aStarCheck
		};

		RegisterAutomaton ra = new RegisterAutomaton("res/gen3.fma");
		ra.displayInfo();

		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			private final static double ANALYSIS_STEP = 0.1D;
			private List<Integer> translation;

			@Override
			public int size() {
				return (int) (1.0 / ANALYSIS_STEP);
			}

			@Override
			protected List<Integer> nextResource() {
				if (translation == null) {
					try {
						Translator translator = new SafeIterTranslator(
								tracePath);
						translation = translator.translate();
						System.out.println("Translation size: "
								+ translation.size());
					} catch (FileNotFoundException e) {
						System.out.println("Could not load word from trace...");
						e.printStackTrace();
						System.exit(-1);
					}
				}

				return translation.subList(
						0,
						Math.min((int) (translation.size() * (index + 1)
								* ANALYSIS_STEP * tracePercentage),
								translation.size()));
			}
		};

		Test lmt = new ListMembershipTest(ra, algorithms, twg);
		lmt.test();

		ResultsContainer.getContainer().flush();
	}
	/**
	 * A tool for automatic and quick testing. A trace should be provided, as
	 * well as a translation rules file, and an MRA macro automaton.
	 * 
	 * @param traceCommand
	 *            - can be "LATEST" or the path to a trace
	 * @param translationPath
	 *            - the path to the TRF translations file
	 * @param propertyCommand
	 *            - can be "SYNTH" or the path to a bas FMA automaton file
	 */
	public static void fullyAutomaticPropertyTest(String traceCommand,
												  String propertyCommand, String testCommand) {
		try {
			// Collect resource paths
			// Property command
			String automatonPath = "";

			if (propertyCommand.contains("SYNTH:")) {
				AutomatonGenerator synthesiser = new SpecificationSynthGenerator(
						propertyCommand.split(":")[1]);
				automatonPath = synthesiser.generate();
			} else
				automatonPath = propertyCommand;

			// Translation path is OK
			// Trace command
			String tracePath = "";
			if (traceCommand.equals("LATEST")) {
				tracePath = findLatestFilename("gen/trace", "tr");
			} else
				tracePath = traceCommand;

			// Setup resources
			RegisterAutomaton automaton = new RegisterAutomaton(automatonPath);
			automaton.displayInfo();

			MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
			// Membership.ldftsCheck,
			// Membership.bflgsCheck,
			// Membership.optiBflgsCheck,
			// Membership.forgetfulBflgsCheck,
			// Membership.bestFirstCheck,
			// Membership.aStarCheck,
			// Membership.greedyCheck,
			MembershipAlgorithms.hasNextSimpleRef,
			};

			double testStep = 0.1D;
			double tracePercentage = 1.0D;

			if (testCommand.contains(":")) {
				String[] tokens = testCommand.split(":");
				tracePercentage = Double.parseDouble(tokens[0]);
				testStep = Double.parseDouble(tokens[1]);
			}

			TestLister<List<Integer>> twg = null;
			if (DEBUG)
				twg = new TestLister<List<Integer>>() {

					@Override
					protected List<Integer> nextResource() {
						List<Integer> l = new ArrayList<>();
						l.add(512);
						l.add(1);
						l.add(0);

						l.add(512);
						l.add(2);
						l.add(5);

						l.add(512);
						l.add(3);
						l.add(0);
						return l;
					}

					@Override
					public int size() {
						return 1;
					}
				};

			else
				twg = new FileWordLister(testStep, tracePercentage, tracePath);

			// Make test
			Test lmt = new ListMembershipTest(automaton, algorithms, twg);
			lmt.test();

			ResultsContainer.getContainer().flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Test finished with success");

	}
	/**
	 * Same as above but with the greedy infrastructure
	 * @param traceCommand
	 * @param translationPath
	 * @param propertyCommand
	 * @param testCommand
	 */
	public static void greedyAutoTest(String traceCommand,
			String translationPath, String propertyCommand, String testCommand) {
		try {
			// Collect resource paths
			// Property command
			String automatonPath = "";

			if (propertyCommand.contains("SYNTH:")) {
				AutomatonGenerator synthesiser = new SpecificationSynthGenerator(
						propertyCommand.split(":")[1]);
				automatonPath = synthesiser.generate();
			} else
				automatonPath = propertyCommand;

			// Translation path is OK
			// Trace command
			String tracePath = "";
			if (traceCommand.equals("LATEST")) {
				tracePath = findLatestFilename("gen/trace", "tr");
			} else
				tracePath = traceCommand;

			// Setup resources
			GreedyRA automaton = new GreedyRA(automatonPath);
			automaton.displayInfo();

			MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
			// Membership.ldftsCheck,
			// Membership.bflgsCheck,
			// Membership.optiBflgsCheck,
			// Membership.forgetfulBflgsCheck,
			// Membership.bestFirstCheck,
			// Membership.aStarCheck,
			Membership.greedyCheck,
			// MembershipAlgorithms.hasNextJavaMOPReference,
			};

			double testStep = 0.1D;
			double tracePercentage = 0.5D;

			if (testCommand.contains(":")) {
				String[] tokens = testCommand.split(":");
				tracePercentage = Double.parseDouble(tokens[0]);
				testStep = Double.parseDouble(tokens[1]);
			}

			Translator translator = new TRFTranslator(tracePath,
					translationPath);
			TestLister<List<Integer>> twg = null;
			if (DEBUG)
				twg = new TestLister<List<Integer>>() {

					@Override
					protected List<Integer> nextResource() {
						List<Integer> l = new ArrayList<>();
						l.add(512);
						l.add(1);
						l.add(0);

						l.add(512);
						l.add(3);
						l.add(15);

						l.add(512);
						l.add(2);
						l.add(5);
						return l;
					}

					@Override
					public int size() {
						return 1;
					}
				};

			else
				twg = new TestWordLister(testStep, tracePercentage, translator);

			// Make test
			Test lmt = new ListMembershipTest(automaton, algorithms, twg);
			lmt.test();

			ResultsContainer.getContainer().flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Test finished with success");

	}
	public static void greedyPTTest(String traceCommand,
									String propertyCommand, String testCommand) {
		try {
			// Collect resource paths
			// Property command
			String automatonPath = "";

			if (propertyCommand.contains("SYNTH:")) {
				AutomatonGenerator synthesiser = new SpecificationSynthGenerator(
						propertyCommand.split(":")[1]);
				automatonPath = synthesiser.generate();
			} else
				automatonPath = propertyCommand;

			// Trace command
			String tracePath = "";
			if (traceCommand.equals("LATEST")) {
				tracePath = findLatestFilename("gen/trace", "tr");
			} else
				tracePath = traceCommand;

			// Setup resources
			GreedyRA automaton = new GreedyRA(automatonPath);
			automaton.displayInfo();

			MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
			// Membership.ldftsCheck,
			// Membership.bflgsCheck,
			// Membership.optiBflgsCheck,
			// Membership.forgetfulBflgsCheck,
			// Membership.bestFirstCheck,
			// Membership.aStarCheck,
			Membership.greedyCheck,
			// MembershipAlgorithms.hasNextJavaMOPReference,
			};

			double testStep = 0.1D;
			double tracePercentage = 1.0D;

			if (testCommand.contains(":")) {
				String[] tokens = testCommand.split(":");
				tracePercentage = Double.parseDouble(tokens[0]);
				testStep = Double.parseDouble(tokens[1]);
			}

			TestLister<List<Integer>> twg = null;
			if (DEBUG)
				twg = new TestLister<List<Integer>>() {

					@Override
					protected List<Integer> nextResource() {
						List<Integer> l = new ArrayList<>();
						l.add(512);
						l.add(1);
						l.add(0);

						l.add(512);
						l.add(3);
						l.add(15);

						l.add(512);
						l.add(2);
						l.add(5);
						return l;
					}

					@Override
					public int size() {
						return 1;
					}
				};

			else
				twg = new FileWordLister(testStep, tracePercentage, tracePath);

			// Make test
			Test lmt = new ListMembershipTest(automaton, algorithms, twg);
			lmt.test();

			ResultsContainer.getContainer().flush();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Test finished with success");

	}
	public static void deterministicTest() throws FileNotFoundException, ParseException {
		//Test example2 which is deterministic
		final int TOTAL_SIZE = 50_000_000;
		
		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			private List<Integer> word = new ArrayList<>();
			private final Integer[] LETTERS = {1,3,2};
			private boolean init = false;

			@Override
			protected List<Integer> nextResource() {
				int currentSize = (TOTAL_SIZE/size())*(index+1);
				
				if(!init) {
					init = true;
					word.add(LETTERS[0]);
					word.add(LETTERS[2]);
				}
				
				//Generate next word
				
				for(int i = word.size()-2; i < currentSize-2; i++) {
					word.add(LETTERS[i%3]);
				}
				
				return word;
			}

			@Override
			public int size() {
				return 10;
			}
		};
		
		RegisterAutomaton ra = new RegisterAutomaton("res/example2.fma");
		
		MBSDecisionAlgorithm[] algorithms = {
				Membership.deterministicCheck,
		};
		
		ListMembershipTest lmt = new ListMembershipTest(ra, algorithms, twg);
		lmt.test();
		ResultsContainer.getContainer().flush();
	}
	private static void satGenTest() throws FileNotFoundException, BuildException, ParseException {
		AutomatonGenerator ag = new NondetSATGenerator("res/sat1.cnf");
		String filename = ag.generate();
		RegisterAutomaton a = new RegisterAutomaton(filename);
		a.displayInfo();
	}
}
