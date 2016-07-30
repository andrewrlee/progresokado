package uk.co.optimisticpanda.regression;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import uk.co.optimisticpanda.regression.TestService.Thing;
import uk.co.optimisticpanda.regression.TestService.Thing.Person;

import com.google.common.collect.Multiset;

public class FileInvocationRecorderTest {

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
	public void checkRecord() throws IOException {

		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
		
			service.createPerson("aa\na", 22);
			service.getTheThingsName();
			service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
			
		}
		assertThat(tape).hasContent(
			"{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aa\\na\",22]}\n" +
			"{\"name\":\"getTheThingsName()\",\"arguments\":[]}\n" + 
			"{\"name\":\"takeTheThing(uk.co.optimisticpanda.regression.TestService$Thing)\",\"arguments\":[{\"age\":1200,\"lastVictim\":{\"name\":\"Ralf\"},\"victims\":[\"bob\",\"cheryll\"]}]}\n"
		);
	}	

	@Test
	public void canRecordOverOldTapes() throws IOException {
		
		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("aa\na", 22);
			service.getTheThingsName();
			service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
		}
		
		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("bob", 123);
		}

		assertThat(tape).hasContent(
			"{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"bob\",123]}\n");
	}	
	
	@Test
	public void openTapeAndReadContents() {
		
		try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
			service.createPerson("aaa", 22);
			service.getTheThingsName();
			service.getTheThingsName();
			service.getTheThingsName();
			service.createPerson("aaa", 21);
			service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
		}			
		
		Multiset<String> contents = FileInvocationVerifier.load(tape);
		assertThat(contents.count("{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aaa\",22]}")).isEqualTo(1);
		assertThat(contents.count("{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aaa\",21]}")).isEqualTo(1);
		assertThat(contents.count("{\"name\":\"getTheThingsName()\",\"arguments\":[]}")).isEqualTo(3);
		assertThat(contents.count("{\"name\":\"takeTheThing(uk.co.optimisticpanda.regression.TestService$Thing)\",\"arguments\":[{\"age\":1200,\"lastVictim\":{\"name\":\"Ralf\"},\"victims\":[\"bob\",\"cheryll\"]}]}")).isEqualTo(1);
	}
}
