package testbench.programs.tss;

/**
 * A Resource that is Dirty, aka it can contaminate other Clean resources,
 * but can also be sanitized. Just useful for its init method
 * @author vincent
 *
 */
public interface DirtyResource extends Resource {
}
