package testbench.lister;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileWordLister extends TestLister<List<Integer>> {
	private final double testStep;
	private final double testPercentage;
	private final List<Integer> translation;
	
	public FileWordLister(double step, double percentage, String filename) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(filename));
		this.testStep = step;
		
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
		
		if(percentage > 1.1) {
			int sizeLimit = (int) percentage;
			
			if(sizeLimit > translation.size()) {
				System.out.println("(w) Size limit to big! Adjusting to 100%: " + translation.size());
				testPercentage = 1.0D;
			} else
				this.testPercentage = ((double)sizeLimit)/((double)translation.size());
		} else {
			testPercentage = percentage;
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
		return (int) (1.0/testStep);
	}

}
