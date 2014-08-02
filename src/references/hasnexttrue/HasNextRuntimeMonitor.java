package references.hasnexttrue;

import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.runtimeverification.rvmonitor.java.rt.RuntimeOption;
import com.runtimeverification.rvmonitor.java.rt.ref.CachedWeakReference;
import com.runtimeverification.rvmonitor.java.rt.table.MapOfMonitor;
import com.runtimeverification.rvmonitor.java.rt.tablebase.TerminatedMonitorCleaner;

public class HasNextRuntimeMonitor implements com.runtimeverification.rvmonitor.java.rt.RVMObject {
	private static com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager HasNextMapManager;
	static {
		HasNextMapManager = new com.runtimeverification.rvmonitor.java.rt.map.RVMMapManager();
		HasNextMapManager.start();
	}

	// Declarations for the Lock
	static final ReentrantLock HasNext_RVMLock = new ReentrantLock();
	static final Condition HasNext_RVMLock_cond = HasNext_RVMLock.newCondition();

	private static boolean HasNext_activated = false;

	// Declarations for Indexing Trees
	private static Object HasNext_i_Map_cachekey_i;
	private static HasNextMonitor HasNext_i_Map_cachevalue;
	private static final MapOfMonitor<HasNextMonitor> HasNext_i_Map = new MapOfMonitor<HasNextMonitor>(0) ;

	public static int cleanUp() {
		int collected = 0;
		// indexing trees
		collected += HasNext_i_Map.cleanUpUnnecessaryMappings();
		return collected;
	}

	// Removing terminated monitors from partitioned sets
	static {
		TerminatedMonitorCleaner.start() ;
	}
	// Setting the behavior of the runtime library according to the compile-time option
	static {
		RuntimeOption.enableFineGrainedLock(false) ;
	}
	
	//CUSTOM monitoring
	private static int matches = 0;

	public static final void hasnexttrueEvent(Iterator i, boolean b) {
		HasNext_activated = true;
		while (!HasNext_RVMLock.tryLock()) {
			Thread.yield();
		}

		CachedWeakReference wr_i = null;
		MapOfMonitor<HasNextMonitor> matchedLastMap = null;
		HasNextMonitor matchedEntry = null;
		boolean cachehit = false;
		if ((i == HasNext_i_Map_cachekey_i) ) {
			matchedEntry = HasNext_i_Map_cachevalue;
			cachehit = true;
		}
		else {
			wr_i = new CachedWeakReference(i) ;
			{
				// FindOrCreateEntry
				MapOfMonitor<HasNextMonitor> itmdMap = HasNext_i_Map;
				matchedLastMap = itmdMap;
				HasNextMonitor node_i = HasNext_i_Map.getNodeEquivalent(wr_i) ;
				matchedEntry = node_i;
			}
		}
		// D(X) main:1
		if ((matchedEntry == null) ) {
			if ((wr_i == null) ) {
				wr_i = new CachedWeakReference(i) ;
			}
			// D(X) main:4
			HasNextMonitor created = new HasNextMonitor() ;
			matchedEntry = created;
			matchedLastMap.putNode(wr_i, created) ;
		}
		// D(X) main:8--9
		boolean cloned_monitor_condition_satisfied = true;
		final HasNextMonitor matchedEntryfinalMonitor = matchedEntry;
		matchedEntry.Prop_1_event_hasnexttrue(b);
		if(matchedEntryfinalMonitor.Prop_1_Category_violation) {
			matches++;
		}

		if ((cachehit == false) ) {
			HasNext_i_Map_cachekey_i = i;
			HasNext_i_Map_cachevalue = matchedEntry;
		}

		HasNext_RVMLock.unlock();
	}

	public static final void hasnextfalseEvent(Iterator i, boolean b) {
		HasNext_activated = true;
		while (!HasNext_RVMLock.tryLock()) {
			Thread.yield();
		}

		CachedWeakReference wr_i = null;
		MapOfMonitor<HasNextMonitor> matchedLastMap = null;
		HasNextMonitor matchedEntry = null;
		boolean cachehit = false;
		if ((i == HasNext_i_Map_cachekey_i) ) {
			matchedEntry = HasNext_i_Map_cachevalue;
			cachehit = true;
		}
		else {
			wr_i = new CachedWeakReference(i) ;
			{
				// FindOrCreateEntry
				MapOfMonitor<HasNextMonitor> itmdMap = HasNext_i_Map;
				matchedLastMap = itmdMap;
				HasNextMonitor node_i = HasNext_i_Map.getNodeEquivalent(wr_i) ;
				matchedEntry = node_i;
			}
		}
		// D(X) main:1
		if ((matchedEntry == null) ) {
			if ((wr_i == null) ) {
				wr_i = new CachedWeakReference(i) ;
			}
			// D(X) main:4
			HasNextMonitor created = new HasNextMonitor() ;
			matchedEntry = created;
			matchedLastMap.putNode(wr_i, created) ;
		}
		// D(X) main:8--9
		boolean cloned_monitor_condition_satisfied = true;
		final HasNextMonitor matchedEntryfinalMonitor = matchedEntry;
		matchedEntry.Prop_1_event_hasnextfalse(b);
		if(matchedEntryfinalMonitor.Prop_1_Category_violation) {
			matches++;
		}

		if ((cachehit == false) ) {
			HasNext_i_Map_cachekey_i = i;
			HasNext_i_Map_cachevalue = matchedEntry;
		}

		HasNext_RVMLock.unlock();
	}

	public static final void nextEvent(Iterator i) {
		HasNext_activated = true;
		while (!HasNext_RVMLock.tryLock()) {
			Thread.yield();
		}

		CachedWeakReference wr_i = null;
		MapOfMonitor<HasNextMonitor> matchedLastMap = null;
		HasNextMonitor matchedEntry = null;
		boolean cachehit = false;
		if ((i == HasNext_i_Map_cachekey_i) ) {
			matchedEntry = HasNext_i_Map_cachevalue;
			cachehit = true;
		}
		else {
			wr_i = new CachedWeakReference(i) ;
			{
				// FindOrCreateEntry
				MapOfMonitor<HasNextMonitor> itmdMap = HasNext_i_Map;
				matchedLastMap = itmdMap;
				HasNextMonitor node_i = HasNext_i_Map.getNodeEquivalent(wr_i) ;
				matchedEntry = node_i;
			}
		}
		// D(X) main:1
		if ((matchedEntry == null) ) {
			if ((wr_i == null) ) {
				wr_i = new CachedWeakReference(i) ;
			}
			// D(X) main:4
			HasNextMonitor created = new HasNextMonitor() ;
			matchedEntry = created;
			matchedLastMap.putNode(wr_i, created) ;
		}
		// D(X) main:8--9
		boolean cloned_monitor_condition_satisfied = true;
		final HasNextMonitor matchedEntryfinalMonitor = matchedEntry;
		matchedEntry.Prop_1_event_next();
		if(matchedEntryfinalMonitor.Prop_1_Category_violation) {
			matches++;
		}

		if ((cachehit == false) ) {
			HasNext_i_Map_cachekey_i = i;
			HasNext_i_Map_cachevalue = matchedEntry;
		}

		HasNext_RVMLock.unlock();
	}
	
	public static boolean isMember() {
		return matches > 0;
	}

}