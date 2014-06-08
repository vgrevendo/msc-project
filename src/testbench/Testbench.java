package testbench;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import testbench.lister.TestLister;
import testbench.programs.translator.NaiveHasNextTranslator;
import testbench.programs.translator.SafeIterTranslator;
import testbench.programs.translator.StrictHasNextTranslator;
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
import automata.hra.HRAutomaton;

public class Testbench {
	public final static int TEST_LENGTH = 1500;
	public final static String HNP_TEST_TRACE_PATH = "gen/trace4.tr";
	
	/**
	 * A stub for parameter parsing has been implemented here,
	 * to make launches from the command line easier.
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		double p = 0.0D;
		
		try {
			
			switch(args[0]) {
			case "synth":
				synthesisTest();
				break;
			case "translation":
				translationTest();
				break;
			case "safeIter":
				p = Double.parseDouble(args[2]);
				if(args[1].equals("LATEST"))
					safeIterTest(findLatestFilename("gen/trace", "tr"), p);
				else
					safeIterTest(args[1], p);
				break;
			case "hasNextProperty-STRICT":
			default:
				p = Double.parseDouble(args[2]);
				if(args[1].equals("LATEST"))
					strictHasNextPropertyTest(findLatestFilename("gen/trace", "tr"), p);
				else
					strictHasNextPropertyTest(args[1], p);
			}
		} catch (FileNotFoundException | ParseException | BuildException e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
	}
	
	public static void mbsTests() throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				//Membership.ldftsCheck,
				Membership.bflgsCheck,
				Membership.bestFirstCheck};

		RegisterAutomaton ra = new HRAutomaton("res/example3.fma", TEST_LENGTH);
		ra.displayInfo();
		
		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			@Override
			public int size() {
				return TEST_LENGTH-7;
			}
			
			@Override
			protected List<Integer> nextResource() {
				List<Integer> l = new ArrayList<>(index+5);
				for(int i = 0; i < index + 5; i++) {
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
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static void mbsAsymptTests() throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				Membership.ldftsCheck,
				Membership.bflgsCheck,
				Membership.bestFirstCheck,
				Membership.aStarCheck};
		
		final int numWords = 100;

		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			@Override
			public int size() {
				return TEST_LENGTH-7;
			}
			
			@Override
			protected List<Integer> nextResource() {
				List<Integer> l = new ArrayList<>(index+5);
				for(int i = 0; i < index + 5; i++) {
					l.add(0);
				}
				return l;
			}
		};
		
		TestLister<RegisterAutomaton> rag = new TestLister<RegisterAutomaton>() {
			@Override
			protected RegisterAutomaton nextResource() {
				RootBranchGenerator rbg = new RootBranchGenerator(index+5);
				String filename;
				try {
					filename = rbg.generate();
					return new HRAutomaton(filename, index+10);
				} catch (ParseException | FileNotFoundException | BuildException e) {
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
	
	public static void generatorTests() throws FileNotFoundException, ParseException, BuildException {
		System.out.println("This is GENERATOR TESTBENCH");
		
		AutomatonGenerator ag = new RootBranchGenerator(5);
		String filename = ag.generate();
		
		RegisterAutomaton ra = new RegisterAutomaton(filename);
		ra.displayInfo();
	}

	/**
	 * Testing the hasNext property with the newly created automata example6.fma
	 * @throws ParseException 
	 * @throws FileNotFoundException 
	 */
	public static void hasNextPropertyTest() throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				//Membership.ldftsCheck,
				Membership.bflgsCheck,
				//Membership.bestFirstCheck,
				//Membership.aStarCheck
				};

		RegisterAutomaton ra = new HRAutomaton("res/example6.fma", 6000);
		ra.displayInfo();
		
		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			private final static double TRANSLATION_PERCENTAGE = 0.4D;
			private final static double ANALYSIS_STEP = 0.05D;
			private List<Integer> translation;
			
			@Override
			public int size() {
				return (int) (1.0/ANALYSIS_STEP);
			}
			
			@Override
			protected List<Integer> nextResource() {
				if(translation == null) {
					try {
						Translator translator = new NaiveHasNextTranslator(HNP_TEST_TRACE_PATH);
						translation = translator.translate();
						System.out.println("Translation size: " + translation.size());
					} catch (FileNotFoundException e) {
						System.out.println("Could not load word from trace...");
						e.printStackTrace();
						System.exit(-1);
					}
				}
				
				return translation.subList(0, Math.min((int)(translation.size()*(index+1)*ANALYSIS_STEP*TRANSLATION_PERCENTAGE), translation.size()));
			}
		};
		
		Test lmt = new ListMembershipTest(ra, algorithms, twg);
		lmt.test();
		
		ResultsContainer.getContainer().flush();
	}
	
	/**
	 * Testing the hasNext property with the newly created automata example6.fma,
	 * while translating the input trace intelligently
	 * @throws ParseException 
	 * @throws FileNotFoundException 
	 */
	public static void strictHasNextPropertyTest(final String tracePath, final double tracePercentage) throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				//Membership.ldftsCheck,
				//Membership.bflgsCheck,
				Membership.optiBflgsCheck,
				//Membership.forgetfulBflgsCheck,
				//Membership.bestFirstCheck,
				//Membership.aStarCheck
				};

		RegisterAutomaton ra = new HRAutomaton("res/example6.fma", 6000);
		ra.displayInfo();
		
		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			private final static double ANALYSIS_STEP = 0.1D;
			private List<Integer> translation;
			
			@Override
			public int size() {
				return (int) (1.0/ANALYSIS_STEP);
			}
			
			@Override
			protected List<Integer> nextResource() {
				if(translation == null) {
					try {
						Translator translator = new StrictHasNextTranslator(tracePath);
						translation = translator.translate();
						System.out.println("Translation size: " + translation.size());
					} catch (FileNotFoundException e) {
						System.out.println("Could not load word from trace...");
						e.printStackTrace();
						System.exit(-1);
					}
				}
				
				return translation.subList(0, Math.min((int)(translation.size()*(index+1)*ANALYSIS_STEP*tracePercentage), translation.size()));
			}
		};
		
		Test lmt = new ListMembershipTest(ra, algorithms, twg);
		lmt.test();
		
		ResultsContainer.getContainer().flush();
	}

	/**
	 * Just a test to see what the translation gives
	 */
	public static void translationTest() {
		try {
			List<Integer> translation = (new SafeIterTranslator("gen/trace0.tr")).translate();
			Iterator<Integer> it = translation.iterator();
			while(it.hasNext()) {
				int i = it.next();
				int j = it.next();
				int k = it.next();
				
				if(i == 132)
					System.out.println(i +" - " + j + " - " + k);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("DONE");
	}

	private static String findLatestFilename(String root, String extension) {
		int id = 0;
		
		while((new File(root + id + "." + extension)).exists()) {
			id ++;
		}
		
		return root + (id-1) + "." + extension;
	}

	private static void synthesisTest() throws FileNotFoundException, BuildException {
		AutomatonGenerator propRAGen = new SpecificationSynthGenerator("res/safe_iter.mra");
		propRAGen.generate();
	}

	private static void safeIterTest(final String tracePath, final double tracePercentage) throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				//Membership.ldftsCheck,
				//Membership.bflgsCheck,
				//Membership.optiBflgsCheck,
				Membership.forgetfulBflgsCheck,
				//Membership.bestFirstCheck,
				//Membership.aStarCheck
				};

		RegisterAutomaton ra = new RegisterAutomaton("res/gen3.fma");
		ra.displayInfo();
		
		TestLister<List<Integer>> twg = new TestLister<List<Integer>>() {
			private final static double ANALYSIS_STEP = 0.1D;
			private List<Integer> translation;
			
			@Override
			public int size() {
				return (int) (1.0/ANALYSIS_STEP);
			}
			
			@Override
			protected List<Integer> nextResource() {
				if(translation == null) {
					try {
						Translator translator = new SafeIterTranslator(tracePath);
						translation = translator.translate();
						System.out.println("Translation size: " + translation.size());
					} catch (FileNotFoundException e) {
						System.out.println("Could not load word from trace...");
						e.printStackTrace();
						System.exit(-1);
					}
				}
				
				return translation.subList(0, Math.min((int)(translation.size()*(index+1)*ANALYSIS_STEP*tracePercentage), translation.size()));
			}
		};
		
		Test lmt = new ListMembershipTest(ra, algorithms, twg);
		lmt.test();
		
		ResultsContainer.getContainer().flush();
	}
}
