package xxc42;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.annotation.PreUpdate;
import org.apache.cayenne.commitlog.CommitLogListener;
import org.apache.cayenne.commitlog.CommitLogModule;
import org.apache.cayenne.commitlog.model.ChangeMap;
import org.apache.cayenne.configuration.server.ServerRuntime;

import com.google.gson.GsonBuilder;

import xxc42.data.Company;
import xxc42.data.Division;

public class Main {

	public static void main( String[] args ) {

		//
		// Create the runtime and insert some test data
		//

		ServerRuntime runtime = ServerRuntime
				.builder()
				.addConfig( "cayenne/cayenne-project.xml" )
				.addModule( CommitLogModule.extend().addListener( MyCommitLogListener.class ).excludeFromTransaction().module() ) // Note that the CommitLogListener is .excludeFromTransaction(). If it isn't, everything works seemingly fine.
				.build();

		runtime.getDataDomain().addListener( new MyPreUpdateListener() );

		log( "main() - Inserting test data" );
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

		@PreUpdate({ Company.class })
		public void preUpdate( Company company ) {
			log( "@PreUpdate performs a simple attribute change without accessing a relationship. This works fine, totally great ChangeMap coming in" );
			company.setAddress( "SomeOtherAddress" );
		}

		@PreUpdate({ Division.class })
		public void preUpdate( Division division ) {
			log( "@PreUpdate changes an attribute on a related object. This results in CommitLogListener receiving an empty ChangeMap" );
			division.getCompany().setAddress( "SomeAddress" );
		}
	}

	static void log( String message ) {
		System.out.println( "===================" );
		System.out.println( message );
		System.out.println( "===================" );
	}
}