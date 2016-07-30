package uk.co.optimisticpanda.regression;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

public class InvocationListener implements Closeable {

	private final JsonUtils utils;
	private final SerializedSubject<String,String> subject;

	private InvocationListener(final String... fieldsToIgnore) {
		utils = new JsonUtils(fieldsToIgnore);
		subject = new SerializedSubject<>(PublishSubject.create());
	}

	public static InvocationListener create() {
		return new InvocationListener();
	}
	
	public static InvocationListener ignoringAnyFieldsNamed(final String... fieldsToIgnore) {
		return new InvocationListener(fieldsToIgnore);
	}
	
	@RuntimeType
	public Object intercept(@Origin Method method, @AllArguments Object[] arguments) throws Exception {
		Map<String, Object> payload = new LinkedHashMap<>();
		List<String> types = stream(method.getParameterTypes()).map(Class::getName).collect(toList());
		payload.put("name", format("%s(%s)", method.getName(), join(",", types)));
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
