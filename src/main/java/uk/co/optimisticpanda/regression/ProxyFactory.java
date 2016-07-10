package uk.co.optimisticpanda.regression;

import static net.bytebuddy.implementation.MethodDelegation.to;
import static net.bytebuddy.matcher.ElementMatchers.any;

import org.objenesis.ObjenesisBase;
import org.objenesis.strategy.StdInstantiatorStrategy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

public enum ProxyFactory {
	;
	
	public static <T> T createProxy(Class<T> clazzToProxy, InvocationListener handler) {
		Class<? extends T> clazz = new ByteBuddy().<T>subclass(clazzToProxy).method(any())
				.intercept(to(handler)).make()
				.load(clazzToProxy.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER).getLoaded();
		return new ObjenesisBase(new StdInstantiatorStrategy()).newInstance(clazz);
	}
}