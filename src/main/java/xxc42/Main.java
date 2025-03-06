package xxc42;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.query.ObjectSelect;

import xxc42.data.Person;

public class Main {

	public static void main( String[] args ) {
		ServerRuntime serverRuntime = DB.runtime();

		ObjectContext oc = serverRuntime.newContext();

		// Touch the DB to separate out SQL logging for schema generation.
		log( "Generating schema" );
		ObjectSelect.query( Person.class ).select( oc );

		Person me = oc.newObject( Person.class );
		Person dad = oc.newObject( Person.class );

		log( "Commit 1 -- creating objects" );
		oc.commitChanges();

		me.setParent( dad );

		log( "Commit 2 -- added first reference" );
		oc.commitChanges();

		dad.setParent( me );

		log( "Commit 3 -- added second reference, creating a circle. If you're on Cayenne v4.2, here's where we fail" );
		oc.commitChanges();
	}

	private static void log( final String message ) {
		System.out.println( "============================" );
		System.out.println( message );
		System.out.println( "============================" );
	}
}