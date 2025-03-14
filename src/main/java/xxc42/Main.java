package xxc42;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.commitlog.CommitLogListener;
import org.apache.cayenne.commitlog.CommitLogModule;
import org.apache.cayenne.commitlog.model.ChangeMap;
import org.apache.cayenne.commitlog.model.ObjectChange;
import org.apache.cayenne.commitlog.model.ObjectChangeType;
import org.apache.cayenne.configuration.Constants;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerModule;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.MapBuilder;
import org.apache.cayenne.query.ObjectSelect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import xxc42.data.Change;
import xxc42.data.Division;

public class Main {

	/**
	 * Keeps track of changes logged by our CommitLogListener
	 */
	public static final List<Set<String>> loggedChanges = Collections.synchronizedList( new ArrayList<>() );
	private static ServerRuntime _runtime;

	public static void main( String[] args ) throws InterruptedException {
		ServerRuntime runtime = runtime();

		// Touch the DB to separate out SQL logging for schema generation.
		log( "Generating schema" );
		ObjectSelect.query( Division.class ).select( runtime.newContext() );

		// Create the Division object we're going to be working on
		Division division = runtime.newContext().newObject( Division.class );
		division.setName( UUID.randomUUID().toString() );
		division.getObjectContext().commitChanges();

		// Using a high level of concurrency. A concurrency level of '1' won't show any problems, but as the level is raised, more commits get lost.
		ExecutorService executor = Executors.newFixedThreadPool( 12 );

		final int numberOfChangesToMake = 1000;

		for( int i = numberOfChangesToMake; i > 0; i-- ) {
			executor.submit( () -> {
				ObjectContext threadLocalChildOC1 = runtime.newContext();
				ObjectContext threadLocalChildOC2 = runtime.newContext();

				Division localDivision1 = threadLocalChildOC1.localObject( division );
				Division localDivision2 = threadLocalChildOC2.localObject( division );

				localDivision1.setName( UUID.randomUUID().toString() );
				localDivision2.setName( UUID.randomUUID().toString() );

				threadLocalChildOC2.commitChanges();
				threadLocalChildOC1.commitChanges();
			} );
		}

		executor.awaitTermination( 5, TimeUnit.SECONDS );

		log( "The number of logged updates is %s. Expected %s ".formatted( loggedChanges.size(), numberOfChangesToMake ) );

		List<Change> changes = ObjectSelect
				.query( Change.class )
				.select( runtime.newContext() );

		System.out.println( "Logged changes are: " + changes.size() );

		//		for( Change change : changes ) {
		//			System.out.println( change.getChangedAttributes() );
		//		}
	}

	public static ServerRuntime runtime() {
		if( _runtime == null ) {
			_runtime = createRuntime();
		}

		return _runtime;
	}

	public static void log( final String message ) {
		System.out.println( "============================" );
		System.out.println( message );
		System.out.println( "============================" );
	}

	public static ServerRuntime createRuntime() {
		final ServerRuntimeBuilder builder = ServerRuntime.builder();

		builder.addConfig( "cayenne/cayenne-project.xml" );

		// Using Hikari only because it's used by the project where the problem manifested itself.
		builder.addModule( b -> b.bind( DataSourceFactory.class ).toInstance( nodeDescriptor -> {
			final HikariConfig config = new HikariConfig();
			config.setMaximumPoolSize( 8 );
			config.setDriverClassName( "org.h2.Driver" );
			config.setJdbcUrl( "jdbc:h2:mem:testdb" );
			return new HikariDataSource( config );
		} ) );

		builder.addModule( CommitLogModule.extend().addListener( OnPostCommitListener.class ).excludeFromTransaction().module() );

		org.apache.cayenne.di.Module cachePropertiesModule = new org.apache.cayenne.di.Module() {
			@Override
			public void configure( Binder binder ) {
				MapBuilder<String> props = binder.bindMap( String.class, Constants.PROPERTIES_MAP );
				// https://cayenne.apache.org/docs/4.0/cayenne-guide/performance-tuning.html#turning-off-synchronization-of-objectcontexts
				props.put( Constants.SERVER_CONTEXTS_SYNC_PROPERTY, "false" );
				ServerModule.setSnapshotCacheSize( binder, 2097152 );
			}
		};
		builder.addModule( cachePropertiesModule );

		return builder.build();
	}

	public static class OnPostCommitListener implements CommitLogListener {

		private static final ExecutorService service = Executors.newFixedThreadPool( 12 );

		@Override
		public void onPostCommit( ObjectContext originatingContext, ChangeMap changeMap ) {

			service.submit( () -> {
				for( ObjectChange objectChange : changeMap.getUniqueChanges() ) {
					// We're only keeping track of updates for the test
					if( objectChange.getType() == ObjectChangeType.UPDATE ) {
						final ObjectContext oc = runtime().newContext();

						DataObject changedObject = (DataObject)Cayenne.objectForPK( oc, objectChange.getPostCommitId() );

						Set<String> changedKeys = new HashSet<>();
						changedKeys.addAll( objectChange.getAttributeChanges().keySet() );
						changedKeys.addAll( objectChange.getToManyRelationshipChanges().keySet() );
						changedKeys.addAll( objectChange.getToOneRelationshipChanges().keySet() );

						loggedChanges.add( changedKeys );
						final Change change = oc.newObject( Change.class );
						change.setChangedAttributes( changedKeys.toString() );
						change.getObjectContext().commitChanges();
					}
				}
			} );
		}
	}
}