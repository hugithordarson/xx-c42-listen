package xxc42;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.commitlog.CommitLogListener;
import org.apache.cayenne.commitlog.CommitLogModule;
import org.apache.cayenne.commitlog.model.ChangeMap;
import org.apache.cayenne.commitlog.model.ObjectChange;
import org.apache.cayenne.commitlog.model.ObjectChangeType;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;
import org.apache.cayenne.query.ObjectSelect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import xxc42.data.Division;

public class Main {

	/**
	 * Keeps track of changes logged by our CommitLogListener
	 */
	public static final List<ChangeMap> loggedChanges = Collections.synchronizedList( new ArrayList<>() );

	public static void main( String[] args ) throws InterruptedException {
		ServerRuntime runtime = createRuntime();
		ServerRuntime serverRuntime = runtime;

		ObjectContext oc = serverRuntime.newContext();

		// Touch the DB to separate out SQL logging for schema generation.
		log( "Generating schema" );
		ObjectSelect.query( Division.class ).select( oc );

		// Create the Division object we're going to be working on
		Division division = oc.newObject( Division.class );
		division.setName( UUID.randomUUID().toString() );
		oc.commitChanges();

		ExecutorService executor = Executors.newFixedThreadPool( 12 );

		ObjectContext parentOC = runtime.newContext();

		final int numberOfChangesToMake = 1000;

		for( int i = numberOfChangesToMake; i > 0; i-- ) {
			executor.submit( () -> {
				// Creating a child editing context. If this is a 'root' OC
				ObjectContext threadLocalChildOC = runtime.newContext( parentOC );

				Division localDivision = threadLocalChildOC.localObject( division );
				localDivision.setName( UUID.randomUUID().toString() );
				threadLocalChildOC.commitChanges();
			} );
		}

		parentOC.commitChanges();

		executor.awaitTermination( 5, TimeUnit.SECONDS );

		log( "The number of logged updates is %s. Expected %s ".formatted( loggedChanges.size(), numberOfChangesToMake ) );
	}

	public static void log( final String message ) {
		System.out.println( "============================" );
		System.out.println( message );
		System.out.println( "============================" );
	}

	public static ServerRuntime createRuntime() {
		final ServerRuntimeBuilder builder = ServerRuntime.builder();

		builder.addConfig( "cayenne/cayenne-project.xml" );

		builder.addModule( b -> b.bind( DataSourceFactory.class ).toInstance( nodeDescriptor -> {
			final HikariConfig config = new HikariConfig();
			config.setDriverClassName( "org.h2.Driver" );
			config.setJdbcUrl( "jdbc:h2:mem:testdb" );
			return new HikariDataSource( config );
		} ) );

		//		builder.addModule( CommitLogModule.extend().addListener( OnPostCommitListener.class ).module() );
		builder.addModule( CommitLogModule.extend().addListener( OnPostCommitListener.class ).excludeFromTransaction().module() );

		return builder.build();
	}

	public static class OnPostCommitListener implements CommitLogListener {
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
}