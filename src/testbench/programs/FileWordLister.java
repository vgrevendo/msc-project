package testbench.programs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import testbench.lister.TestLister;

public class FileWordLister extends TestLister<List<Integer>> {
	private final double testStep;
	private final double testPercentage;
	private final List<Integer> translation;
	
	public FileWordLister(double step, double percentage, String filename) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(filename));
		this.testStep = step;
		this.testPercentage = percentage;
		
		//Load trace file
		System.out.print("[FWL] Loading trace file....");
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		Scanner intScanner = new Scanner(sc.nextLine());
		
		translation = new ArrayList<>(3_000_000);
		while(intScanner.hasNextInt()) {
			translation.add(intScanner.nextInt());
		}
		
		intScanner.close();
		sc.close();
		
		System.out.println(" - found " + translation.size() + " symbols");
	}

	@Override
	protected List<Integer> nextResource() {
		return translation.subList(0, Math.min((int)(translation.size()*(index+1)*this.testStep*this.testPercentage), 
                translation.size()));
	}

	@Override
	public int size() {
		return (int) (testPercentage/testStep);
	}

}
