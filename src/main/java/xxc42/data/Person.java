package xxc42.data;

import java.util.Set;

import xxc42.DoesStuffAfterUpdate;
import xxc42.data.auto._Person;

public class Person extends _Person implements DoesStuffAfterUpdate {

	@Override
	public void afterUpdateAction( Set<String> changedKeys ) {
		DoesStuffAfterUpdate.super.afterUpdateAction( changedKeys );

		System.out.println( "Let's do some stuff here!" );
		setName( "Glorpidorpi" );
		getObjectContext().commitChanges();
		System.out.println( "did some stuff here!" );
	}
}