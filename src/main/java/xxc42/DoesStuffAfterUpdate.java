package xxc42;

import java.util.Set;

import org.apache.cayenne.Persistent;

public interface DoesStuffAfterUpdate extends Persistent {

	public default void afterUpdateAction( Set<String> changedKeys ) {
		System.out.println( "=====> %s ===== Changed keys".formatted( getObjectId() ) );
		changedKeys.forEach( System.out::println );
		System.out.println( "========" );
		System.out.println();
	}
}
