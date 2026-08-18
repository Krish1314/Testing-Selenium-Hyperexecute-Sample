# HyperExecute Solutions Engineer — Technical Assignment

**Submitted by:** Krish Patel  
**LambdaTest Username:** krishp1809  
**Date:** August 2026

---

## Table of Contents
- [Task 1: Fix the Broken YAML](#task-1-fix-the-broken-yaml)
- [Task 2: Environment Variables](#task-2-environment-variables)
- [Task 3: Force a Failure and Configure Retries](#task-3-force-a-failure-and-configure-retries)
- [Task 4: Linux/Unix Basics](#task-4-linuxunix-basics)

---

## Task 1: Fix the Broken YAML

### Original Broken YAML
```yaml
---
version: 0.1
runson: win

autosplit: true
conCurrency: 1

env: TOKEN: anvdegtod-asdaasda0asda-asda

pre:
  - mvn dependency:resolve

testDiscovery:
  type: raw
  mode: dynamic
  command: grep 'test name' xml/testng_win.xml | awk '{print$2}' | sed 's/name=//g' | sed 's/\x3e//g'

testRunnerCommand: mvn test `-Dplatname=win `-Dmaven.repo.local=./.m2 dependency:resolve `-DselectedTests=$test

 retryOnFailure: true
maxRetries: 1

jobLabel: [selenium-testng, win, v1, autosplit]
```

### Bugs Found and Fixes

| # | Bug | Why It Was Breaking | Fix |
|---|-----|---------------------|-----|
| 1 | `version: 0.1` — Unquoted version | YAML parses `0.1` as a floating-point number (0.1). HyperExecute expects the version as a string `"0.1"`. This can cause type mismatch errors during config parsing. | Changed to `version: "0.1"` |
| 2 | `conCurrency: 1` — Wrong casing | HyperExecute expects `concurrency` (all lowercase). `conCurrency` (camelCase) is not a recognized key and gets silently ignored, meaning no concurrency is configured. | Changed to `concurrency: 1` |
| 3 | `env: TOKEN: anvdegtod...` — Invalid YAML structure | The `env` key must be a YAML mapping (block format), not an inline colon-separated value. `env: TOKEN: value` is invalid YAML — it makes `TOKEN: value` the string value of `env`, not a key-value pair. | Changed to proper block mapping with `env:` on its own line and `TOKEN:` indented below |
| 4 | Backticks (`` ` ``) in `testRunnerCommand` | The backticks (`` `-Dplatname ``) are PowerShell escape characters, not valid in standard shell/YAML contexts. They cause command parsing errors when the command is executed on the HyperExecute VM. | Removed all backticks from the command |
| 5 | ` retryOnFailure: true` — Leading space | There is an extra leading space before `retryOnFailure`. In YAML, indentation is significant — this leading space makes it appear as a continuation of the previous block (`testRunnerCommand`) rather than a new top-level key. This causes a YAML parsing error. | Removed the leading space so `retryOnFailure` starts at column 0 |
| 6 | Missing `runtime` block | The YAML doesn't specify the Java runtime version. Without this, HyperExecute may use a default runtime that might not match the project's requirements (Java 11). | Added `runtime:` block with `language: java` and `version: "11"` |

### Corrected YAML
See [`hyperexecute.yaml`](hyperexecute.yaml) for the full corrected configuration.

```yaml
---
version: "0.1"
runson: win

autosplit: true
concurrency: 1

env:
  TOKEN: anvdegtod-asdaasda0asda-asda
  ENVIRONMENT: staging

runtime:
  language: java
  version: "11"

cacheKey: '{{ checksum "pom.xml" }}'
cacheDirectories:
  - .m2

pre:
  - echo "ENVIRONMENT variable value is $env:ENVIRONMENT"
  - mvn -Dmaven.repo.local=./.m2 dependency:resolve

testDiscovery:
  type: raw
  mode: dynamic
  command: grep 'test name' xml/testng_win.xml | awk '{print$2}' | sed 's/name=//g' | sed 's/\x3e//g'

testRunnerCommand: mvn test -Dplatname=win -Dmaven.repo.local=./.m2 dependency:resolve -DselectedTests=$test

retryOnFailure: true
maxRetries: 2

jobLabel: [selenium-testng, win, autosplit, assignment]
```

### Evidence
- **Job Link on HyperExecute:** https://hyperexecute.lambdatest.com/hyperexecute/task?jobId=65355c20-a403-4214-8a8e-e7c24c18b764
- **Job ID:** `65355c20-a403-4214-8a8e-e7c24c18b764`
- **Execution Evidence:** 
  - `✔ [1] "Test_EnvVariable"` (Passed)
  - `x [1] "Test_IntentionalFailure"` + `{retry 1}` + `{retry 2}` (Verified 2x Retries)

---

## Task 2: Environment Variables

### 1. Environment Variable in YAML
The `ENVIRONMENT: staging` variable is defined in the `env` block of `hyperexecute.yaml`:

```yaml
env:
  TOKEN: anvdegtod-asdaasda0asda-asda
  ENVIRONMENT: staging
```

### 2. Printing in Pre-Steps
The value is echoed during pre-steps (before tests run):

```yaml
pre:
  - echo "ENVIRONMENT variable value is $env:ENVIRONMENT"
  - mvn -Dmaven.repo.local=./.m2 dependency:resolve
```

### 3. Accessing from Test Code
Created [`EnvVariableTest.java`](src/test/java/EnvVariableTest.java) that reads the environment variable using `System.getenv()`:

```java
import org.testng.Assert;
import org.testng.annotations.Test;

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
```

### Evidence
> Log output screenshots showing the value printed in both pre-steps and during test execution will be added here.

---

## Task 3: Force a Failure and Configure Retries

### 1. Intentionally Failing Test
Created [`IntentionalFailureTest.java`](src/test/java/IntentionalFailureTest.java) with an explicit `Assert.fail()`:

```java
import org.testng.Assert;
import org.testng.annotations.Test;

public class IntentionalFailureTest {
    @Test(description = "This test intentionally fails to demonstrate retry mechanism")
    public void testIntentionalFailure() {
        System.out.println("========================================");
        System.out.println("Running IntentionalFailureTest...");
        System.out.println("This test will ALWAYS fail intentionally.");
        System.out.println("========================================");

        Assert.fail("INTENTIONAL FAILURE: This test fails on purpose to demonstrate HyperExecute retry mechanism.");
    }
}
```

### 2. YAML Retry Configuration
The retry mechanism is configured in the YAML:

```yaml
retryOnFailure: true
maxRetries: 2
```

This tells HyperExecute to automatically retry any failed test up to 2 additional times.

### Evidence
> Dashboard screenshots showing the retry execution will be added here.

---

## Task 4: Linux/Unix Basics

### Sample Log File (`sample.log`)
```
2024-01-15 10:23:45 INFO Test TestLogin started
2024-01-15 10:23:50 FAIL Test TestLogin assertion failed
2024-01-15 10:24:01 INFO Test TestSignup started
2024-01-15 10:24:05 ERROR Test TestSignup connection timeout
2024-01-15 10:24:15 INFO Test TestDashboard started
2024-01-15 10:24:20 PASS Test TestDashboard completed
2024-01-15 10:25:00 FAIL Test TestCheckout validation error
2024-01-15 10:25:10 INFO Environment staging ready
2024-01-15 10:25:30 ERROR Test TestPayment gateway unreachable
2024-01-15 10:25:45 PASS Test TestProfile completed successfully
```

### Command 1: `grep` — Find all lines containing FAIL or ERROR

```bash
grep -E 'FAIL|ERROR' sample.log
```

**Explanation:** Uses extended regex (`-E`) to match any line containing either "FAIL" or "ERROR" — filters the log down to only failure/error entries.

**Output:**
```
2024-01-15 10:23:50 FAIL Test TestLogin assertion failed
2024-01-15 10:24:05 ERROR Test TestSignup connection timeout
2024-01-15 10:25:00 FAIL Test TestCheckout validation error
2024-01-15 10:25:30 ERROR Test TestPayment gateway unreachable
```

### Command 2: `awk` — Extract the second column

```bash
awk '{print $2}' sample.log
```

**Explanation:** Prints the 2nd whitespace-delimited field from each line, which in this log file is the timestamp (time portion).

**Output:**
```
10:23:45
10:23:50
10:24:01
10:24:05
10:24:15
10:24:20
10:25:00
10:25:10
10:25:30
10:25:45
```

### Command 3: `sed` — Find-and-replace

```bash
sed 's/staging/production/g' sample.log
```

**Explanation:** Substitutes all occurrences of "staging" with "production" globally (`g` flag) across the file. The `s/old/new/g` syntax is sed's substitution command.

**Output:**
```
2024-01-15 10:23:45 INFO Test TestLogin started
2024-01-15 10:23:50 FAIL Test TestLogin assertion failed
2024-01-15 10:24:01 INFO Test TestSignup started
2024-01-15 10:24:05 ERROR Test TestSignup connection timeout
2024-01-15 10:24:15 INFO Test TestDashboard started
2024-01-15 10:24:20 PASS Test TestDashboard completed
2024-01-15 10:25:00 FAIL Test TestCheckout validation error
2024-01-15 10:25:10 INFO Environment production ready    ← changed
2024-01-15 10:25:30 ERROR Test TestPayment gateway unreachable
2024-01-15 10:25:45 PASS Test TestProfile completed successfully
```

### Command 4: Pipe — Filter failures, then extract test names

```bash
grep -E 'FAIL|ERROR' sample.log | awk '{print $5}'
```

**Explanation:** First `grep` filters for lines containing FAIL or ERROR, then pipes (`|`) the results to `awk` which extracts the 5th field (the test name) from each matching line.

**Output:**
```
TestLogin
TestSignup
TestCheckout
TestPayment
```

---

## Files Modified/Created

| File | Purpose |
|------|---------|
| `hyperexecute.yaml` | Corrected YAML (Task 1) + env vars (Task 2) + retries (Task 3) |
| `src/test/java/EnvVariableTest.java` | Task 2: Reads ENVIRONMENT env variable |
| `src/test/java/IntentionalFailureTest.java` | Task 3: Always fails to demonstrate retries |
| `xml/testng_win.xml` | Updated to include new test classes |
| `sample.log` | Task 4: Sample log file for grep/awk/sed demo |
| `README.md` | This submission document |
