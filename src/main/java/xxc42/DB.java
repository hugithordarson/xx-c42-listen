package xxc42;

import javax.sql.DataSource;

import org.apache.cayenne.commitlog.CommitLogModule;
import org.apache.cayenne.configuration.DataNodeDescriptor;
import org.apache.cayenne.configuration.server.DataSourceFactory;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.configuration.server.ServerRuntimeBuilder;

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

		builder.addModule( CommitLogModule.extend().addListener( OnPostCommitListener.class ).excludeFromTransaction().module() );

		return builder.build();
	}
}