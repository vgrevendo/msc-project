package testbench.programs.translator.trf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class RuleEvaluator {
	private enum OnObjectAction { NOTHING, ID, NUMBER };
	
	private String conditionType;
	private String conditionString;
	private String methodName;
	private int methodCode;
	private ReturnEvaluator re;
	
	//Contracts
	public abstract void addRule(String ruleLine) throws Exception;
	public abstract LinkedList<Integer> evaluate(int id, String cl, String method, String rv)
								throws Exception;
	
	//Tools
	protected void parseRule(String rule) {
		String[] tokens = rule.split(" ");
		
		//Condition
		String[] subTokens = tokens[0].split(":");
		conditionType = subTokens[0];
		conditionString = subTokens[1];
		
		//Method
		subTokens = tokens[1].split(":");
		methodName = subTokens[0];
		methodCode = Integer.parseInt(subTokens[1]);
		
		//Return rules: each token is a return rule
		re = new ReturnEvaluator(methodCode);
		for(int i = 2; i < tokens.length; i++) {
			subTokens = tokens[i].split(":");
			
			switch(subTokens.length) {
			case 1:
				re.setDefaultNumber(Integer.parseInt(subTokens[0]));
				break;
			case 2:
				if(subTokens[1].equals("ID"))
					re.addIDObjectAction();
				else
					re.addNumberObjectAction(Integer.parseInt(subTokens[1]));
				break;
			case 3:
				re.addEqualityNumber(subTokens[1], Integer.parseInt(subTokens[2]));
				break;
			}
		}
	}
	
	protected String getConditionType() {
		return conditionType;
	}
	protected String getConditionString() {
		return conditionString;
	}
	protected String getMethodName() {
		return methodName;
	}
	protected int getMethodCode() {
		return methodCode;
	}
	protected ReturnEvaluator getRe() {
		return re;
	}

	protected class ReturnEvaluator {
		private int defaultNumber = -1;
		private Map<String, Integer> equalities = new HashMap<String, Integer>();
		private OnObjectAction objectAction = OnObjectAction.NOTHING;
		private int onObjectNumber = -1;
		private final int methodCode;
		
		public ReturnEvaluator(int mCode) {
			methodCode = mCode;
		}
		
		public LinkedList<Integer> evaluate(String input) {
			if(defaultNumber >= 0)
				return encode(defaultNumber);
			
			if(input.contains("instance of"))
				switch(objectAction) {
				case ID:
					onObjectNumber = Integer.parseInt(input.replaceAll("[^0-9]", ""));
				case NUMBER:
					return encode(onObjectNumber);
				default:
					return encode(0);
				}
			
			Integer eqNumber = equalities.get(input);
			
			return encode(eqNumber == null ? 0 : eqNumber);
		}
		
		private LinkedList<Integer> encode(int rv) {
			LinkedList<Integer> list = new LinkedList<>();
			list.add(methodCode);
			list.add(rv);
			return list;
		}
		
		public void setDefaultNumber(int number) {
			defaultNumber = number;
		}
		
		public void addIDObjectAction() {
			objectAction = OnObjectAction.ID;
		}
		
		public void addNumberObjectAction(int number) {
			objectAction = OnObjectAction.NUMBER;
			onObjectNumber = number;
		}
		
		public void addEqualityNumber(String equals, int number) {
			equalities.put(equals, number);
		}
	}
	
	protected LinkedList<Integer> filterEncode(int id, LinkedList<Integer> toAttach) {
		if(toAttach == null)
			return null;
		toAttach.push(id);
		return toAttach;
	}
	
}
