package xxc42;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.commitlog.CommitLogListener;
import org.apache.cayenne.commitlog.model.ChangeMap;
import org.apache.cayenne.commitlog.model.ObjectChange;
import org.apache.cayenne.commitlog.model.ObjectChangeType;

/**
 * Listens for updates to objects and performs afterUpdateAction() with the names of updated attributes and relationships.
 */

public class OnPostCommitListener implements CommitLogListener {

	/**
	 * Keep track of all of our changes
	 */
	public static final List<ChangeMap> loggedChanges = Collections.synchronizedList( new ArrayList<>() );

	@Override
	public void onPostCommit( ObjectContext originatingContext, ChangeMap changeMap ) {

		for( ObjectChange objectChange : changeMap.getUniqueChanges() ) {
			// We're only keeping track of updates for the test
			if( objectChange.getType() == ObjectChangeType.UPDATE ) {
				loggedChanges.add( changeMap );
			}
		}
	}
}