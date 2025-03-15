package xxc42;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistentObject;
import org.apache.cayenne.annotation.PreUpdate;
import org.apache.cayenne.commitlog.CommitLogListener;
import org.apache.cayenne.commitlog.CommitLogModule;
import org.apache.cayenne.commitlog.model.ChangeMap;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;

import com.google.gson.GsonBuilder;

import xxc42.data.Company;
import xxc42.data.Division;

public class Main {

	public static void main( String[] args ) throws InterruptedException {

		//
		// Create the runtime
		//

		ServerRuntime runtime = ServerRuntime
				.builder()
				.addConfig( "cayenne/cayenne-project.xml" )
				.addModule( CommitLogModule.extend().addListener( MyCommitLogListener.class ).excludeFromTransaction().module() )
				.build();

		runtime.getDataDomain().addListener( new MyPreUpdateListener() );

		log( "main() - Touching the DB just to generate the schema" );
		ObjectSelect
				.query( Division.class )
				.select( runtime.newContext() );

		log( "main() - Inserting data for testing on" );
		ObjectContext oc = runtime.newContext();
		Company company = oc.newObject( Company.class );
		Division division = oc.newObject( Division.class );
		division.setCompany( company );
		oc.commitChanges();

		//
		// OK, runtime, DB and test data in place, do some actual testing
		//

		log( "main() - Updating Company. @PreUpdate will change an attribute on the Company as well" );
		company.setName( "New company name" );
		oc.commitChanges();

		log( "main() - Updating Division. @PreUpdate will change an attribute on the related Company" );
		division.setName( "New division name" );
		oc.commitChanges();
	}

	public static class MyCommitLogListener implements CommitLogListener {

		@Override
		public void onPostCommit( ObjectContext originatingContext, ChangeMap changeMap ) {
			log( "onPostCommit() received ChangeMap: " + new GsonBuilder().setPrettyPrinting().create().toJson( changeMap ) );
		}
	}

	public static class MyPreUpdateListener {

		@PreUpdate({ PersistentObject.class })
		public void preUpdate( PersistentObject object ) {
			if( object instanceof Company company ) {
				log( "@PreUpdate performs a simple attribute change without accessing a relationship. This works fine, totally great ChangeMap coming in" );
				company.setAddress( "SomeOtherAddress" );
			}

			if( object instanceof Division division ) {
				log( "@PreUpdate changes an attribute on a related object. This results in OnPostCommitListener receiving an empty ChangeMap" );
				division.getCompany().setAddress( "SomeAddress" );
			}
		}
	}

	static void log( String message ) {
		System.out.println( "===================" );
		System.out.println( message );
		System.out.println( "===================" );
	}
}