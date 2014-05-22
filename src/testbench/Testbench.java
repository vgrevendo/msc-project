package testbench;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Scanner;

import testbench.lister.TestLister;
import testbench.programs.translator.NaiveHasNextTranslator;
import testbench.programs.translator.StrictHasNextTranslator;
import testbench.programs.translator.Translator;
import testbench.tests.AsymptoticMembershipTest;
import testbench.tests.ListMembershipTest;
import algorithms.Membership;
import algorithms.membership.MBSDecisionAlgorithm;
import algorithms.tools.ResultsContainer;
import automata.RegisterAutomaton;
import automata.gen.AutomatonGenerator;
import automata.gen.RootBranchGenerator;
import automata.hra.HRAutomaton;

public class Testbench {
	public final static int TEST_LENGTH = 1500;
	public final static String HNP_TEST_TRACE_PATH = "gen/trace3.tr";
	
	public static void main(String[] args) {
		System.out.println("This is testbench, running...");
		
		try {
			hasNextPropertyTest();
			//strictHasNextPropertyTest();
		} catch (FileNotFoundException | ParseException e) {
			System.out.println("An error occurred:");
			e.printStackTrace();
		}
		
//		translationTest();
	}
	
	public static void mbsTests() throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				//Membership.ldftsCheck,
				Membership.bflgsCheck,
				Membership.bestFirstCheck};

		RegisterAutomaton ra = new HRAutomaton("res/example3.fma", TEST_LENGTH);
		ra.displayInfo();
		
		TestLister<int[]> twg = new TestLister<int[]>() {
			@Override
			public int size() {
				return TEST_LENGTH-7;
			}
			
			@Override
			protected int[] nextResource() {
				return new int[index+5];
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

		TestLister<int[]> twg = new TestLister<int[]>() {
			@Override
			public int size() {
				return numWords;
			}
			
			@Override
			protected int[] nextResource() {
				return new int[index+5];
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
				} catch (ParseException | FileNotFoundException e) {
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
	
	public static void generatorTests() throws FileNotFoundException, ParseException {
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
		
		TestLister<int[]> twg = new TestLister<int[]>() {
			private int[] translation;
			
			@Override
			public int size() {
				return 10;
			}
			
			@Override
			protected int[] nextResource() {
				if(translation == null) {
					try {
						Translator translator = new NaiveHasNextTranslator(HNP_TEST_TRACE_PATH);
						translation = translator.translate();
					} catch (FileNotFoundException e) {
						System.out.println("Could not load word from trace...");
						e.printStackTrace();
						translation = new int[100000];
					}
				}
				
				return Arrays.copyOfRange(translation, 0, translation.length*(index+1)/20);
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
	public static void strictHasNextPropertyTest() throws FileNotFoundException, ParseException {
		MBSDecisionAlgorithm[] algorithms = new MBSDecisionAlgorithm[] {
				//Membership.ldftsCheck,
				Membership.bflgsCheck,
				//Membership.bestFirstCheck,
				//Membership.aStarCheck
				};

		RegisterAutomaton ra = new HRAutomaton("res/example6.fma", 6000);
		ra.displayInfo();
		
		TestLister<int[]> twg = new TestLister<int[]>() {
			private int[] translation;
			
			@Override
			public int size() {
				return 10;
			}
			
			@Override
			protected int[] nextResource() {
				if(translation == null) {
					try {
						Translator translator = new StrictHasNextTranslator(HNP_TEST_TRACE_PATH);
						translation = translator.translate();
					} catch (FileNotFoundException e) {
						System.out.println("Could not load word from trace...");
						e.printStackTrace();
						translation = new int[100000];
					}
				}
				
				System.out.println("Translation yields word of size " + translation.length*(index+1)/size());
				
				return Arrays.copyOfRange(translation, 0, translation.length*(index+1)/20);
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
			Scanner sc = new Scanner(new File(HNP_TEST_TRACE_PATH));
			int[] word = (new NaiveHasNextTranslator(HNP_TEST_TRACE_PATH)).translate();
			PrintWriter pw = new PrintWriter("gen/translationtest.tr");
			
			int i = 0;
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if(line.contains("--")) {
					continue;
				}
				
				pw.println(word[2*i] + " " + word[2*i+1] + " " + line);
				i++;
			}
			
			pw.close();
			sc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		System.out.println("DONE");
	}
}
