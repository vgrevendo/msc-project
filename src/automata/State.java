package automata;

/**
 * An implementation of a general state.
 * It is not sure this is necessary (may affect performance).
 * 
 * @author vincent
 *
 */
public class State {
	public final boolean isFinal;
	public final String name;
	
	public State(String name) {
		this.name = name;
		this.isFinal = false;
	}
	
	public State(String name, boolean isFinal) {
		this.name = name;
		this.isFinal = isFinal;
	}
	
	
}
