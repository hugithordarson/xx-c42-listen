package xxc42;

import javax.sql.DataSource;

import org.apache.cayenne.commitlog.CommitLogModule;
import org.apache.cayenne.configuration.Constants;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerModule;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.MapBuilder;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DB {

	private static ServerRuntime _runtime;

	public static ServerRuntime runtime() {
		if( _runtime == null ) {
			_runtime = createRuntime();
		}

		return _runtime;
	}

	public static ServerRuntime createRuntime() {
		final ServerRuntimeBuilder builder = ServerRuntime.builder();

		builder.addConfig( "cayenne/cayenne-project.xml" );

		builder.addModule( b -> b.bind( DataSourceFactory.class ).toInstance( new DataSourceFactory() {
			@Override
			public DataSource getDataSource( DataNodeDescriptor nodeDescriptor ) throws Exception {
				final HikariConfig config = new HikariConfig();
				config.setMaximumPoolSize( 2 );
				config.setDriverClassName( "org.h2.Driver" );
				config.setJdbcUrl( "jdbc:h2:mem:testdb" );
				return new HikariDataSource( config );
			}
		} ) );

		//		builder.addModule( CommitLogModule.extend().addListener( OnPostCommitListener.class ).module() );
		builder.addModule( CommitLogModule.extend().addListener( OnPostCommitListener.class ).excludeFromTransaction().module() );

		org.apache.cayenne.di.Module cachePropertiesModule = new org.apache.cayenne.di.Module() {
			@Override
			public void configure( Binder binder ) {
				MapBuilder<String> props = binder.bindMap( String.class, Constants.PROPERTIES_MAP );
				// https://cayenne.apache.org/docs/4.0/cayenne-guide/performance-tuning.html#turning-off-synchronization-of-objectcontexts
				props.put( Constants.SERVER_CONTEXTS_SYNC_PROPERTY, "false" );
				ServerModule.setSnapshotCacheSize( binder, 1000000 );
			}
		};
		builder.addModule( cachePropertiesModule );

		return builder.build();
	}
}