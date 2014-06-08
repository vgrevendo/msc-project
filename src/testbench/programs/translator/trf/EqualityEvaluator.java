package testbench.programs.translator.trf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class EqualityEvaluator extends RuleEvaluator {
	private Map<String, Map<String, ReturnEvaluator>> equalityMap = new HashMap<>();

	@Override
	public void addRule(String ruleLine) throws Exception {
		parseRule(ruleLine);
		
		//Compile equality
		if(!equalityMap.containsKey(getConditionString())) {
			equalityMap.put(getConditionString(), new HashMap<String, ReturnEvaluator>());
		}
		equalityMap.get(getConditionString()).put(getMethodName(), getRe());
	}

	@Override
	public LinkedList<Integer> evaluate(int id, String cl, String method, String rv)
			throws Exception {
		Map<String, ReturnEvaluator> evaluatorMap = equalityMap.get(cl);
		if(evaluatorMap == null)
			return null;
		
		ReturnEvaluator evaluator = evaluatorMap.get(method);
		
		return filterEncode(id, evaluator == null ? null : evaluator.evaluate(rv));
	}

}
