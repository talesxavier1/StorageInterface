package br.com.tx.storageInterface;

import org.springframework.context.ConfigurableApplicationContext;

public class SpringContext {

	private static ConfigurableApplicationContext context;

	public static void setSpringContext(ConfigurableApplicationContext context_) {
		context = context_;

	}

	public static ConfigurableApplicationContext getSpringContext() {
		return context;
	}
	
}
