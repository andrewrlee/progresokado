package uk.co.optimisticpanda.regression;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import rx.Observable;
import rx.subjects.PublishSubject;

public class InvocationListener implements Closeable {

	private final JsonUtils utils;
	private final PublishSubject<String> subject;

	private InvocationListener(final String... fieldsToIgnore) {
		utils = new JsonUtils(fieldsToIgnore);
		subject = PublishSubject.create();
	}

	public static InvocationListener create() {
		return new InvocationListener();
	}
	
	public static InvocationListener ignoring(final String... fieldsToIgnore) {
		return new InvocationListener(fieldsToIgnore);
	}
	
	@RuntimeType
	// TODO: generate unique name to cope with overloading
	public Object intercept(@Origin Method method, @AllArguments Object[] arguments) throws Exception {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("name", method.getName());
		payload.put("arguments", arguments);
		subject.onNext(utils.toJson(payload));
		return null;
	}

	public Observable<String> asObservable() {
		return subject;
	}

	@Override
	public void close() {
		subject.onCompleted();
	}
}
