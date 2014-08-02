package references.hasnexttrue;

public class HasNextMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitor implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
	protected Object clone() {
		try {
			HasNextMonitor ret = (HasNextMonitor) super.clone();
			return ret;
		}
		catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}

	int Prop_1_state;
	static final int Prop_1_transition_hasnexttrue[] = {1, 1, 3, 3};;
	static final int Prop_1_transition_hasnextfalse[] = {0, 0, 3, 3};;
	static final int Prop_1_transition_next[] = {2, 0, 3, 3};;

	boolean Prop_1_Category_violation = false;

	HasNextMonitor() {
		Prop_1_state = 0;

	}

	public int getState() {
		return Prop_1_state;
	}

	final boolean Prop_1_event_hasnexttrue(boolean b) {
		{
			if (!(b)) {
				return false;
			}
			{
			}
		}
		RVM_lastevent = 0;

		Prop_1_state = Prop_1_transition_hasnexttrue[Prop_1_state];
		Prop_1_Category_violation = Prop_1_state == 2;
		return true;
	}

	final boolean Prop_1_event_hasnextfalse(boolean b) {
		{
			if (!(!b)) {
				return false;
			}
			{
			}
		}
		RVM_lastevent = 1;

		Prop_1_state = Prop_1_transition_hasnextfalse[Prop_1_state];
		Prop_1_Category_violation = Prop_1_state == 2;
		return true;
	}

	final boolean Prop_1_event_next() {
		RVM_lastevent = 2;

		Prop_1_state = Prop_1_transition_next[Prop_1_state];
		Prop_1_Category_violation = Prop_1_state == 2;
		return true;
	}

	final void Prop_1_handler_violation (){
		{
			System.out.println("ltl violated!");
		}

	}

	final void reset() {
		RVM_lastevent = -1;
		Prop_1_state = 0;
		Prop_1_Category_violation = false;
	}

	// RVMRef_i was suppressed to reduce memory overhead

	//alive_parameters_0 = [Iterator i]
	boolean alive_parameters_0 = true;

	protected final void terminateInternal(int idnum) {
		switch(idnum){
			case 0:
			alive_parameters_0 = false;
			break;
		}
		switch(RVM_lastevent) {
			case -1:
			return;
			case 0:
			//hasnexttrue
			//alive_i
			if(!(alive_parameters_0)){
				RVM_terminated = true;
				return;
			}
			break;

			case 1:
			//hasnextfalse
			//alive_i
			if(!(alive_parameters_0)){
				RVM_terminated = true;
				return;
			}
			break;

			case 2:
			//next
			//alive_i
			if(!(alive_parameters_0)){
				RVM_terminated = true;
				return;
			}
			break;

		}
		return;
	}

	public static int getNumberOfEvents() {
		return 3;
	}

	public static int getNumberOfStates() {
		return 4;
	}

}