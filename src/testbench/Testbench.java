package testbench;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import references.MembershipAlgorithms;
import testbench.lister.TestLister;
import testbench.lister.TestWordLister;
import testbench.programs.translator.SafeIterTranslator;
import testbench.programs.translator.StrictHasNextTranslator;
import testbench.programs.translator.TRFTranslator;
import testbench.programs.translator.Translator;
import testbench.tests.AsymptoticMembershipTest;
import testbench.tests.ListMembershipTest;
import algorithms.Membership;
import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.gen.AutomatonGenerator;
import automata.gen.BuildException;
import automata.gen.RootBranchGenerator;
import automata.gen.SpecificationSynthGenerator;
import automata.greedy.GreedyRA;
import automata.hra.HRAutomaton;

public class Testbench {
	public final static int TEST_LENGTH = 1500;
	public final static String HNP_TEST_TRACE_PATH = "gen/trace4.tr";

	public final static boolean DEBUG = false;

	/**
	 * A stub for parameter parsing has been implemented here, to make launches
	 * from the command line easier.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		double p = 0.0D;

		try {

			switch (args[0]) {
			case "synth":
				synthesisTest();
				break;
			case "translation":
				translationTest();
				break;
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
			case "auto":
			default:
				if (args.length != 5) {
					System.out
							.println("Unexpected number of arguments on command line.");
					return;
				}

				fullyAutomaticPropertyTest(args[1], args[2], args[3], args[4]);
			}
		} catch (Exception e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
	}

	public static void mbsTests() throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				// Membership.ldftsCheck,
				Membership.bflgsCheck, Membership.bestFirstCheck };

		RegisterAutomaton ra = new HRAutomaton("res/example3.fma", TEST_LENGTH);
		ra.displayInfo();

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

		Test lmt = new ListMembershipTest(ra, algorithms, twg);
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
			MembershipAlgorithms.hasNextJavaMOPReference,
			};

			double testStep = 0.1D;
			double tracePercentage = 1.0D;

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
}
