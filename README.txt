Restory - Integration tests as API documentation
==============================================

## About

Restory generates HTML API documentation from your JUnit integration tests. This allows you to keep your documentation close to
the implementation, to automatically capture test data and ensures your sampled requests are always up to date.

## Maven dependency

Restory is added to your Maven POM with the following dependency:

  <dependency>
      <groupId>org.kantega.restory</groupId>
      <artifactId>restory</artifactId>
      <version>1.0-SNAPSHOT</version>
      <scope>test</scope>
  </dependency>

## Adding Restory to your tests

Create JUnit integration tests like this:

    /**
     * The API allows looking up user profiles based on a user name
     * @title User profiles
     */
    public class UserProfiles {

        @Rule
        public Restory restory = new Restory();

        private ClientBuilder clientBuilder;

        /**
         * Getting a user profile for a user is easy peasy!
         *
         * @title Getting the user profile
         */
        @Test
        public void getUserProfile() {
            clientBuilder.register(HttpAuthenticationFeature.basic("joe", "joe"));
            UserProfile profile = clientBuilder.build()
                    .target("http://localhost:" + getReststopPort() + "/userprofiles")
                    .path("OLANOR")
                    .request().get(UserProfile.class);
        }

    }

Then create a JUnit test suite like this, adding your set of suite classes:

    @RunWith(Suite.class)
    @Suite.SuiteClasses(UserProfiles.class)
    public class DocumentationTestSuite {

        @ClassRule
        public static SuiteRestory suiteRestory = new SuiteRestory();

    }


Now run your tests, and reports will be written to target/restory/DocumentationTestSuite.html