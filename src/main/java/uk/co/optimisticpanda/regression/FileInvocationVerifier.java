package uk.co.optimisticpanda.regression;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.co.optimisticpanda.regression.Exceptions.propagateAnyError;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.SoftAssertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

public class FileInvocationVerifier implements Observer<String>, Closeable {

	private static final Logger L = LoggerFactory.getLogger(FileInvocationVerifier.class);
	private final Multiset<String> lines;
	private final Set<String> unexpected = new LinkedHashSet<>();
	private Subscription subscription;

	private FileInvocationVerifier(File tape, Observable<String> observable) {
		this.lines = load(tape);
		this.subscription = observable.subscribe(this);
	}

	static Multiset<String> load(File tape) {
		List<String> lines = propagateAnyError(() -> Files.readAllLines(tape.toPath(), UTF_8));
		return LinkedHashMultiset.create(lines);
	}

	public static FileInvocationVerifier subscribedTo(File tape, Observable<String> observable) {
		return new FileInvocationVerifier(tape, observable);
	}

	@Override
	public void onCompleted() {
	}

	@Override
	public void onError(Throwable e) {
		L.error("An error occured: {}", e.getMessage(), e);
	}

	@Override
	public void onNext(String line) {
       if (lines.contains(line)) {
    	   lines.remove(line);
       } else {
    	   unexpected.add(line);
       }
	}

	public Multiset<String> getRemainingInvocations() {
		return lines;
	}
	
	public Set<String> getUnexpectedInvocations() {
		return unexpected;
	}
	
	public void verifyInvocationsMatchRecorded() {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(lines).as("Remaining invocations").isEmpty();
		softly.assertThat(unexpected).as("Unexpected invocations").isEmpty();
		softly.assertAll();
	}
	
	
	
	@Override
	public void close() throws IOException {
		subscription.unsubscribe();
	}
}
