package testbench.programs.tss;

/**
 * Just a identification interface for a resource. Theoretical.
 * @author vincent
 *
 */
public interface Resource {
	public Resource combineWith(Resource other);
	public void sanitize();
	public void sink();
}
