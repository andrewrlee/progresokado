package uk.co.optimisticpanda.regression;

import java.util.List;

public class TestService {
	
	public String createPerson(String surname, int age) {
		return "Sam " + surname + " is " + age;
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
