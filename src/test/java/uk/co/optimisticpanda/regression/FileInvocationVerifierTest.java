
package uk.co.optimisticpanda.regression;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.co.optimisticpanda.regression.TestService.Thing;
import uk.co.optimisticpanda.regression.TestService.Thing.Person;

public class FileInvocationVerifierTest {

	@Rule public TemporaryFolder folder = new TemporaryFolder();
	private TestService service;
	private InvocationListener listener;
	private File tape;

	@Before
	public void setup() throws IOException {
		this.listener = InvocationListener.ignoringAnyFieldsNamed("hps");
		this.service = ProxyFactory.createProxy(TestService.class, listener);
		this.tape = folder.newFile("tape.json");
	}

	@Test
	public void checkAllInvocationsMatch() throws IOException {

		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("aa\na", 22);
			service.getTheThingsName();
			service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
		}

		try (FileInvocationVerifier verifier = FileInvocationVerifier.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("aa\na", 22);
			service.getTheThingsName();
			service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
		
			assertThat(verifier.getRemainingInvocations()).isEmpty();
			assertThat(verifier.getUnexpectedInvocations()).isEmpty();
		}
	}	

	@Test
	public void checkExtraInvocationsDetected() throws IOException {

		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
			service.getTheThingsName();
			service.getTheThingsName();
		}

		try (FileInvocationVerifier verifier = FileInvocationVerifier.subscribedTo(tape, listener.asObservable())) {
			service.getTheThingsName();
		
			assertThat(verifier.getRemainingInvocations()).containsOnly(
					"{\"name\":\"getTheThingsName()\",\"arguments\":[]}");
			assertThat(verifier.getUnexpectedInvocations()).isEmpty();
		}
	}	

	@Test
	public void checkMissingInvocationsDetected() throws IOException {

		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
			service.getTheThingsName();
		}

		try (FileInvocationVerifier verifier = FileInvocationVerifier.subscribedTo(tape, listener.asObservable())) {
			service.getTheThingsName();
			service.getTheThingsName();
		
			assertThat(verifier.getRemainingInvocations()).isEmpty();
			assertThat(verifier.getUnexpectedInvocations()).containsOnly(
					"{\"name\":\"getTheThingsName()\",\"arguments\":[]}");
		}
	}	

	@Test
	public void checkInvocationsDontMatchWithDifferentArgs() throws IOException {

		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("aaa", 22);
		}

		try (FileInvocationVerifier verifier = FileInvocationVerifier.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("aaa", 23);
		
			assertThat(verifier.getRemainingInvocations()).containsOnly(
					"{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aaa\",22]}");
			assertThat(verifier.getUnexpectedInvocations()).containsOnly(
					"{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aaa\",23]}");
		}
	}	
	
	@Test
	public void checkAssertAllInvocationsMatch() throws IOException {
		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("aaa", 22);
			service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
		}

		try (FileInvocationVerifier verifier = FileInvocationVerifier.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("aaa", 23);
			service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
			
			assertThatThrownBy(() -> verifier.verifyInvocationsMatchRecorded())
				.hasMessage("\n" + 
						"The following 2 assertions failed:\n" + 
						"1) [Remaining invocations] \n" + 
						"Expecting empty but was:<[\"{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aaa\",22]}\"]>\n" + 
						"2) [Unexpected invocations] \n" + 
						"Expecting empty but was:<[\"{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aaa\",23]}\"]>\n");
		}
	}
}
