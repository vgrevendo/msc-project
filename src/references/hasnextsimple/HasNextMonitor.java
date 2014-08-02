package references.hasnextsimple;

class HasNextMonitor extends com.runtimeverification.rvmonitor.java.rt.tablebase.AbstractMonitor 
					 implements Cloneable, com.runtimeverification.rvmonitor.java.rt.RVMObject {
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
	static final int Prop_1_transition_hasnext[] = {1, 1, 1, 3};;
	static final int Prop_1_transition_next[] = {2, 0, 2, 3};;

	boolean Prop_1_Category_match = false;

	HasNextMonitor() {
		Prop_1_state = 0;

	}

	public int getState() {
		return Prop_1_state;
	}

	final boolean Prop_1_event_hasnext() {
		RVM_lastevent = 0;

		Prop_1_state = Prop_1_transition_hasnext[Prop_1_state];
		Prop_1_Category_match = Prop_1_state == 2;
		return true;
	}

	final boolean Prop_1_event_next() {
		RVM_lastevent = 1;

		Prop_1_state = Prop_1_transition_next[Prop_1_state];
		Prop_1_Category_match = Prop_1_state == 2;
		return true;
	}

	final void Prop_1_handler_match (){
		{
			System.out.println("next called without hasNext!");
		}

	}

	final void reset() {
		RVM_lastevent = -1;
		Prop_1_state = 0;
		Prop_1_Category_match = false;
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
			//hasnext
			//alive_i
			if(!(alive_parameters_0)){
				RVM_terminated = true;
				return;
			}
			break;

			case 1:
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
		return 2;
	}

	public static int getNumberOfStates() {
		return 4;
	}

}