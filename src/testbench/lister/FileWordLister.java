package testbench.lister;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FileWordLister extends TestLister<List<Integer>> {
	//DEBUG
	private static final int INTERESTING_ID = 72;
	private static final int FIRST_ID = 6;
	private static final boolean DEBUG = false; 
	private static final boolean DEBUG_FILTER = true;
	private static final int LIMIT_TR_NUMBER = 700;
	private final Map<Integer, Integer> counts  =new HashMap<Integer, Integer>();
	
	private final double testStep;
	private double testPercentage;
	private final List<Integer> translation;
	
	public FileWordLister(double step, double percentage, String filename) throws FileNotFoundException {
		Scanner sc = new Scanner(new File(filename));
		this.testStep = step;
		
		//Load trace file
		if(DEBUG && DEBUG_FILTER)
			System.out.println("[FWL-DEBUG] Filtering trace for ID " + INTERESTING_ID);
		else if(DEBUG) 
			System.out.println("[FWL-DEBUG] Counting occurrences...");
		else
			System.out.print("[FWL] Loading trace file....");
		sc.nextLine();
		sc.nextLine();
		Scanner intScanner = new Scanner(sc.nextLine());
		
		//Handle percentage/size limit case
		int sizeLimit = Integer.MAX_VALUE;
		if(percentage > 1.1) {
			sizeLimit = (int) percentage;
			
			testPercentage = -1.0D;
		} else {
			testPercentage = percentage;
		}
		
		translation = new ArrayList<>(3_000_000);
		boolean recording = false;
		int examined = 0;
		while(intScanner.hasNextInt() && translation.size() < sizeLimit) {
			String l = sc.nextLine();
			if(l.startsWith("-"))
				break;
			int i = Integer.parseInt(l);
			
			if(DEBUG) {
				if(i == INTERESTING_ID) {
					recording = true;
				} else if(i >= FIRST_ID) {
					recording = false;
				}
				if(DEBUG_FILTER && recording)
					translation.add(i);
				else if(!DEBUG_FILTER)
					translation.add(i);
				if(!counts.containsKey(i))
					counts.put(i, 0);
				counts.put(i, counts.get(i)+1);
				examined++;
				if(examined >= LIMIT_TR_NUMBER)
					break;
			} else			
				translation.add(i);
		}
		
		if(testPercentage < 0.0D) { //A size limit was specified
			if(sizeLimit > translation.size()) {
				System.out.println("(w) Size limit to big! Adjusting to 100%: " + translation.size());
				testPercentage = 1.0D;
			} else
				this.testPercentage = ((double)sizeLimit)/((double)translation.size());
		}		
		
		if(DEBUG) {
			System.out.println("Small trace display (< 200):");
			if(translation.size() < 200) {
				System.out.println(translation.toString());
			} else
				System.out.println(translation.subList(0, 200).toString());
			
			System.out.println("Counts:");
			System.out.println(counts.get(1));
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
