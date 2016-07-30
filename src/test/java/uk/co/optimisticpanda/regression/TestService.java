package uk.co.optimisticpanda.regression;

import static java.lang.String.join;

import java.util.List;

public class TestService {
	
	public String createPerson(String surname, int age) {
		return "Sam " + surname + " is " + age;
	}
	
	public String createPerson(String surname) {
		return "Sam " + surname;
	}
	
	public String createPerson(int age, String... names) {
		return join(" ", names) + " is " + age;
	}
	
	public String getTheThingsName() {
		return "The Thing";
	}
	
	public void takeTheThing(Thing thing) {
	}
	
	public static class Thing {
		private final int age;
		private final List<String> victims;
		private final Person lastVictim;
		public Thing(int age, List<String> victims, Person lastVictim) {
			this.age = age;
			this.victims = victims;
			this.lastVictim = lastVictim;
		}

		public static class Person {
			private final String name; 
			private final int hps;
			public Person(String name, int hps) {
				this.name = name;
				this.hps = hps;
			} 
		}
	}
}
