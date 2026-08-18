import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Task 3: Intentional Failure Test
 * This test is designed to always fail, demonstrating
 * HyperExecute's retryOnFailure mechanism.
 */
public class IntentionalFailureTest {

    @Test(description = "This test intentionally fails to demonstrate retry mechanism")
    public void testIntentionalFailure() {
        System.out.println("========================================");
        System.out.println("Running IntentionalFailureTest...");
        System.out.println("This test will ALWAYS fail intentionally.");
        System.out.println("========================================");

        // This assertion will always fail
        Assert.fail("INTENTIONAL FAILURE: This test fails on purpose to demonstrate HyperExecute retry mechanism.");
    }
}
