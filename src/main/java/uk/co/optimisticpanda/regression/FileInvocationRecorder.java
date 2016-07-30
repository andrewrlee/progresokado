package uk.co.optimisticpanda.regression;

import static uk.co.optimisticpanda.regression.Exceptions.propagateAnyError;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

public class FileInvocationRecorder implements Observer<String>, Closeable {

	private static final Logger L = LoggerFactory.getLogger(FileInvocationRecorder.class);
	private final BufferedWriter writer;
	private final Subscription subscription;

	private FileInvocationRecorder(File tape, Observable<String> observable) {
		propagateAnyError(() ->  new FileOutputStream(tape).getChannel().truncate(0).close());
		this.subscription = observable.subscribe(this);
		this.writer = propagateAnyError(() -> new BufferedWriter(new FileWriter(tape)));
	}
	
	public static FileInvocationRecorder subscribedTo(File tape, Observable<String> observable) {
		return new FileInvocationRecorder(tape, observable);
	}
	
	@Override
	public void onCompleted() {
		close();
	}

	@Override
	public void onError(Throwable e) {
		L.error("An error occured: {}", e.getMessage(), e);
	}

	@Override
	public void onNext(String line) {
		propagateAnyError(() -> writer.write(line + "\n"));
	}

	@Override
	public void close() {
		subscription.unsubscribe();
		propagateAnyError(() -> writer.close());
	}
}
