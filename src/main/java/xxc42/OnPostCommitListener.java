package xxc42;

import java.util.Map.Entry;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.commitlog.CommitLogListener;
import org.apache.cayenne.commitlog.model.AttributeChange;
import org.apache.cayenne.commitlog.model.ChangeMap;
import org.apache.cayenne.commitlog.model.ObjectChange;
import org.apache.cayenne.commitlog.model.ToManyRelationshipChange;
import org.apache.cayenne.commitlog.model.ToOneRelationshipChange;

import xxc42.data.Company;
import xxc42.data.Division;
import xxc42.data.Person;

/**
 * Listens for updates to objects and performs afterUpdateAction() with the names of updated attributes and relationships.
 */

public class OnPostCommitListener implements CommitLogListener {

	@Override
	public void onPostCommit( ObjectContext originatingContext, ChangeMap changes ) {

		System.out.println( "==========> " );
		System.out.println( "==========> Invoking AfterUpdateListener.onPostCommit()" );
		System.out.println( "==========> " );

		for( ObjectChange objectChange : changes.getUniqueChanges() ) {
			System.out.println( " -> CHANGE PROCESSED. Type is: " + objectChange.getType() );
			System.out.println( " -> Object: " + objectChange.getPreCommitId() + " : " + objectChange.getPostCommitId() );

			System.out.println( " -> Attribute changes: " );
			for( Entry<String, ? extends AttributeChange> entry : objectChange.getAttributeChanges().entrySet() ) {
				System.out.println( entry.getKey() + " : " + entry.getValue().getOldValue() + " : " + entry.getValue().getNewValue() );
			}

			System.out.println( " -> To-one relationship changes: " );

			for( Entry<String, ? extends ToOneRelationshipChange> entry : objectChange.getToOneRelationshipChanges().entrySet() ) {
				System.out.println( entry.getKey() + " : " + entry.getValue().getOldValue() + " : " + entry.getValue().getNewValue() );
			}

			System.out.println( " -> To-many relationship changes: " );

			for( Entry<String, ? extends ToManyRelationshipChange> entry : objectChange.getToManyRelationshipChanges().entrySet() ) {
				System.out.println( entry.getKey() + " : " + entry.getValue().getAdded() + " : " + entry.getValue().getRemoved() );
			}

			final Object changedObject = Cayenne.objectForPK( originatingContext, objectChange.getPostCommitId() );

			if( changedObject instanceof Person person ) {
				final Company company = originatingContext.newObject( Company.class );
				company.getObjectContext().commitChanges();
				System.out.println();
				System.out.println( " ------------------------------------------" );
				System.out.println( " -> STARTING DB WORK IN AFTERUPDATELISTENER" );
				Division d = originatingContext.newObject( Division.class );
				d.setName( "Enn ein deildin" );
				d.getObjectContext().commitChanges();
				d.setCompany( originatingContext.newObject( Company.class ) );
				d.getObjectContext().commitChanges();
				System.out.println( " -> ENDING DB WORK IN AFTERUPDATELISTENER" );
				System.out.println();
			}

			//			final ObjectContext newContext = DB.runtime().newContext();
			//			final Person p = (Person)Cayenne.objectForPK( newContext, objectChange.getPostCommitId() );
			//			p.setName( UUID.randomUUID().toString() );
			//			newContext.commitChanges();
		}
	}
}