# progresokado


```java

InvocationListener listener = InvocationListener.ignoring("hps");
TestService service = ProxyFactory.createProxy(TestService.class, listener);

try (FileInvocationRecorder store = FileInvocationRecorder.create(tape)) {

  listener.asObservable().subscribe(store);
	
  service.createPerson("aa\na", 22);
  service.getTheThingsName();
  service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
}

assertThat(tape).hasContent(
  "{\"name\":\"createPerson\",\"arguments\":[\"aa\\na\",22]}\n" +
  "{\"name\":\"getTheThingsName\",\"arguments\":[]}\n" + 
  "{\"name\":\"takeTheThing\",\"arguments\":[{\"age\":1200,\"lastVictim\":{\"name\":\"Ralf\"},\"victims\":[\"bob\",\"cheryll\"]}]}\n");

```
