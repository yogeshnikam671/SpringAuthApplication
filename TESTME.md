## Testing controller endpoints -

#### For testing controller endpoints @WebFluxTest annotation is used.
The @WebFluxTest annotation is recommended while testing controller endpoints as it does not
load the whole application context (Controllers, Services, Repositories, etc.). 
Instead it loads the configuration relevant to only webflux test which includes Controller, ControllerAdvice, JsonComponent, etc.

Annotating a class with this annotation gives us a bean named WebTestClient which can be used to hit the controller endpoints to test them.

Typically, @WebFluxTest is used in combination with @MockBean or @Import to create any collaborators required by the @Controller beans.

@MockBean annotation requires - org.mockito:mockito-inline test dependency to work.

Have a look at SecurityTestConfiguration and how we have imported it in our webFluxTest.
We are disabling csrf and spring security in the respective configuration.

Note - Configuration classes annotated with @TestConfiguration are excluded from component scanning, therefore we need to import it explicitly in every test where we want to @Autowire it

## Integration Testing -

#### For integration testing @SpringBootTest annotation is used.

@SpringBootTest annotation loads the actual spring application context so that we can test out the logic with the actual beans.
We can provide separate application yaml properties for integration tests by using @ActiveProfiles("test") and creating a file in test -> resources named application-test.yaml

@DirtiesContext is recommended to use as it reloads the whole application context on every new integration test run which ensures there are no corrupt beans.

If you are using the reactive mongo db, you will need to add the following dependency - testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")

Also, you will need to define the following property in application-test.yaml -
spring.mongodb.embedded.version = 3.5.5

If the above is not configured, the spring integration test will throw some unwanted errors.