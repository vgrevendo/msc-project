package testbench.programs.agent;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class SaneWeakReference<T> extends WeakReference<T> {

	public SaneWeakReference(T referent) {
		super(referent);
	}

	public SaneWeakReference(T referent, ReferenceQueue<? super T> q) {
		super(referent, q);
	}

	/** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
    	if(get()==null)
    		return false;
    	
        if (obj instanceof Reference) {
            return get().equals(((Reference<?>) obj).get());
        } else {
            return get().equals(obj);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
    	if(get()==null)
    		return 0;
    	
        return get().hashCode();
    }
}
