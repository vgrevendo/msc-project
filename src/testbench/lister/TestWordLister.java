package testbench.lister;

import java.util.List;

import testbench.programs.translator.Translator;

public class TestWordLister extends TestLister<List<Integer>> {
	private final double testStep;
	private final double testPercentage;
	private final List<Integer> translation;	
	
	public TestWordLister(double step, double percentage, Translator translator) {
		this.testStep = step;
		this.testPercentage = percentage;
		translation = translator.translate();
		
		System.out.println("Translation size: " + translation.size());
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
