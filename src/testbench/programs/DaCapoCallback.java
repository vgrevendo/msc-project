package testbench.programs;

import org.dacapo.harness.Callback;
import org.dacapo.harness.CommandLineArgs;

public class DaCapoCallback extends Callback {

	public DaCapoCallback(CommandLineArgs args) {
		super(args);
	}

	@Override
	public void start(String benchmark) {
		//Trigger debugger
		main();
		super.start(benchmark);
	}

	@Override
	public void stop() {
		//Trigger debugger stop
		end();
		super.stop();
	}

	//Debugger triggers
	public void main() {
		System.out.println("Trace has now started!");
	}
	public void end() {
		System.out.println("Trace is ending...");
	}
}
