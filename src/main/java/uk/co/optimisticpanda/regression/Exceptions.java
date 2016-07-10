package uk.co.optimisticpanda.regression;

import com.google.common.base.Throwables;

public class Exceptions {

	public static void propagateAnyError(VoidExceptionHandler handler) {
		try {
			handler.accept();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public static <T> T propagateAnyError(ExceptionHandler<T> handler) {
		try {
			return handler.accept();
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	@FunctionalInterface
	public interface VoidExceptionHandler {
		void accept() throws Exception;
	}

	@FunctionalInterface
	public interface ExceptionHandler<T> {
		T accept() throws Exception;
	}
}
