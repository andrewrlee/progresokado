# progresokado

This library intercepts methods calls on an object and serializes them to a file. 
On later test runs, the file is loaded and calls to the object can then be matched against the previously recorded invocations.
This so that changes in the interactions between collaborators and the object under test can be detected.   

###Example:

```java
	
  @Rule public TemporaryFolder folder = new TemporaryFolder();
  private TestService service;
  private InvocationListener listener;
  private File tape;

  @Before
  public void setup() throws IOException {
    this.listener = InvocationListener.ignoringAnyFieldsNamed("hps");
    this.service = ProxyFactory.createProxy(TestService.class, listener);
    this.tape = folder.newFile("tape.json");
  }

  @Test
  public void checkAssertAllInvocationsMatch() throws IOException {

    try (FileInvocationRecorder recorder = FileInvocationRecorder.subscribedTo(tape, listener.asObservable())) {
      service.createPerson("aaa", 22);
      service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
      service.getTheThingsName();
    }

    try (FileInvocationVerifier verifier = FileInvocationVerifier.subscribedTo(tape, listener.asObservable())) {
      service.createPerson("aaa", 23);
      service.takeTheThing(new Thing(1200, asList("bob", "cheryll"), new Person("Ralf", -21)));
      
      assertThatThrownBy(() -> verifier.verifyInvocationsMatchRecorded())
        .hasMessage("\n" + 
            "The following 2 assertions failed:\n" + 
            "1) [Remaining invocations] \n" + 
            "Expecting empty but was:<[\"{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aaa\",22]}\", \"{\"name\":\"getTheThingsName()\",\"arguments\":[]}\"]>\n" + 
            "2) [Unexpected invocations] \n" + 
            "Expecting empty but was:<[\"{\"name\":\"createPerson(java.lang.String,int)\",\"arguments\":[\"aaa\",23]}\"]>\n");
    }
  }
  
```

###Limitations:

  * ~~Not Thread Safe~~
  * ~~Does not verify invocations~~
  * Creates dumb proxy, does not allow proxying calls to actual implementation 
  * Does not allow verifying order of invocations
  * Only serializes invocations to json
  * Only support simple string comparison on serialized invocations (does not support custom equality)
  * Does not include class info to serialized invocations (so for safety, each proxy requires recording to separate files) 
  * Has not been tested using one invocation store with multiple proxied services  
  * Only supports storing invocations in a file small enough to fit in memory when loaded (maybe nice to store in DB/mongo)
  * Some boiler plate when verifying calls  