package xxc42;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;

import xxc42.data.Division;
import xxc42.data.Person;

public class Main {

	public static void main( String[] args ) throws InterruptedException {
		ServerRuntime serverRuntime = DB.runtime();

		ObjectContext oc = serverRuntime.newContext();

		// Touch the DB to separate out SQL logging for schema generation.
		log( "Generating schema" );
		ObjectSelect.query( Person.class ).select( oc );

		// Create the Division object we're going to be working on
		Division division = oc.newObject( Division.class );
		division.setName( UUID.randomUUID().toString() );
		oc.commitChanges();

		ExecutorService executor = Executors.newFixedThreadPool( 2 );

		ObjectContext parentOC = DB.runtime().newContext();

		for( int i = 1000; i > 0; i-- ) {
			executor.submit( () -> {
				ObjectContext threadLocalOC = DB.runtime().newContext( parentOC );
				Division localDivision = threadLocalOC.localObject( division );
				localDivision.setName( UUID.randomUUID().toString() );
				threadLocalOC.commitChanges();
			} );
		}

		parentOC.commitChanges();

		executor.awaitTermination( 5, TimeUnit.SECONDS );

		log( "The number of logged updates is: " + OnPostCommitListener.loggedChanges.size() );
	}

	public static void log( final String message ) {
		System.out.println( "============================" );
		System.out.println( message );
		System.out.println( "============================" );
	}
}