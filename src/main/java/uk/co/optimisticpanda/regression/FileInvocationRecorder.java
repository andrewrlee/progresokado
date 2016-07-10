package uk.co.optimisticpanda.regression;

import static uk.co.optimisticpanda.regression.Exceptions.propagateAnyError;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.Multiset;

import rx.Observer;
public class FileInvocationRecorder implements Observer<String>, Closeable {

	private static final Logger L = LoggerFactory.getLogger(FileInvocationRecorder.class);
	private final BufferedWriter writer;
	private final File tape;

	private FileInvocationRecorder(File tape) {
		this.tape = tape;
		this.writer = propagateAnyError(() -> new BufferedWriter(new FileWriter(tape)));
	}

	public static Multiset<String> load(File tape) {
		List<String> lines = propagateAnyError(() -> Files.readAllLines(tape.toPath(), Charsets.UTF_8));
		return LinkedHashMultiset.create(lines);
	}
	
	public static FileInvocationRecorder create(File tape) {
		propagateAnyError(() ->  new FileOutputStream(tape).getChannel().truncate(0).close());
		return new FileInvocationRecorder(tape);
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
		propagateAnyError(() -> writer.close());
	}
}
