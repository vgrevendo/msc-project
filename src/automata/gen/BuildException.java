package automata.gen;

public class BuildException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5125022618396254647L;

	public BuildException() {
	}

	public BuildException(String message) {
		super(message);
	}

	public BuildException(Throwable cause) {
		super(cause);
	}

	public BuildException(String message, Throwable cause) {
		super(message, cause);
	}

	public BuildException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
