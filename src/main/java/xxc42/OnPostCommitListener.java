package xxc42;

import java.util.HashSet;
import java.util.Set;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.commitlog.CommitLogListener;
import org.apache.cayenne.commitlog.model.ChangeMap;
import org.apache.cayenne.commitlog.model.ObjectChange;
import org.apache.cayenne.commitlog.model.ObjectChangeType;

/**
 * Listens for updates to objects and performs afterUpdateAction() with the names of updated attributes and relationships.
 */

public class OnPostCommitListener implements CommitLogListener {

	@Override
	public void onPostCommit( ObjectContext originatingContext, ChangeMap changes ) {

		for( ObjectChange objectChange : changes.getUniqueChanges() ) {
			if( objectChange.getType() == ObjectChangeType.UPDATE ) {
				final DoesStuffAfterUpdate changedObject = (DoesStuffAfterUpdate)Cayenne.objectForPK( DB.runtime().newContext(), objectChange.getPostCommitId() );

				final Set<String> changedKeys = new HashSet<>();
				changedKeys.addAll( objectChange.getAttributeChanges().keySet() );
				changedKeys.addAll( objectChange.getToManyRelationshipChanges().keySet() );
				changedKeys.addAll( objectChange.getToOneRelationshipChanges().keySet() );

				changedObject.afterUpdateAction( changedKeys );
			}
		}
	}
}