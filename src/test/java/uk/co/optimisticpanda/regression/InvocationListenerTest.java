
package uk.co.optimisticpanda.regression;

import static java.util.Arrays.asList;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import rx.observers.TestSubscriber;
import uk.co.optimisticpanda.regression.TestService.Thing;
import uk.co.optimisticpanda.regression.TestService.Thing.Person;

public class InvocationListenerTest {

	private TestService service;
	private InvocationListener listener;

	@Before
	public void setup() throws IOException {
		listener = InvocationListener.ignoring("hps");
		service = ProxyFactory.createProxy(TestService.class, listener);
	}

	@Test
	public void checkObservable() throws IOException {
		
		TestSubscriber<String> subscriber = new TestSubscriber<>();
		listener.asObservable().subscribe(subscriber);
		
		service.createPerson("aa\na", 22);
		service.getTheThingsName();
		service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));

		subscriber.assertNoErrors();
		subscriber.assertValues(
				"{\"name\":\"createPerson\",\"arguments\":[\"aa\\na\",22]}",
				"{\"name\":\"getTheThingsName\",\"arguments\":[]}",
				"{\"name\":\"takeTheThing\",\"arguments\":[{\"age\":1200,\"lastVictim\":{\"name\":\"Ralf\"},\"victims\":[\"bob\",\"cheryll\"]}]}");
	}	
}
