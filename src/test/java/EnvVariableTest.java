import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Task 2: Environment Variable Test
 * This test reads the ENVIRONMENT variable set in the HyperExecute YAML
 * and prints its value during test execution.
 */
public class EnvVariableTest {

    @Test(description = "Verify ENVIRONMENT variable is accessible from test code")
    public void testEnvironmentVariable() {
        String environment = System.getenv("ENVIRONMENT");
        System.out.println("========================================");
        System.out.println("ENVIRONMENT variable value: " + environment);
        System.out.println("========================================");

        Assert.assertNotNull(environment, "ENVIRONMENT variable should be set");
        Assert.assertEquals(environment, "staging", "ENVIRONMENT should be 'staging'");
        System.out.println("Environment variable test PASSED - value is: " + environment);
    }
}
