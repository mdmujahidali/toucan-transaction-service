# AI usage disclosure

## Tool used

I used Codex (OpenAI) to help inspect the supplied Spring Boot project, run the test suite, and make the changes described below. I reviewed the resulting code and tests rather than treating the generated structure as a finished answer.

## What I reviewed and changed

I traced the request flow from `TransactionController`, through `TransactionService`, to `TransactionRepository`, and reviewed `GlobalExceptionHandler` alongside the DTO validation.

The lifecycle rule is deliberately small: new records start as `PENDING`; from there they may become `COMPLETED`, `FAILED`, or `CANCELLED`; all three are terminal. The service looks up the allowed next states in an `EnumMap` and rejects any missing transition with `InvalidStatusTransitionException`. The controller advice turns not-found errors into 404, business conflicts into 409, and validation or unreadable JSON into 400, all using the same error body.

I agreed with the terminal-state design because changing a completed, failed, or cancelled financial transaction would rewrite its history. A reversal should be represented by a separate transaction rather than moving the original back to `PENDING`.

I did not make an arbitrary validation or transition-rule change merely to put my name on the submission. Instead, I found and fixed a real concurrency gap in the duplicate-ID handling. Previously the service checked `existsById` and then saved. Two concurrent requests could both pass that check, after which the database's primary-key constraint would reject one with `DataIntegrityViolationException`; without a translation it could become a 500. `TransactionService` now translates that exception to `DuplicateTransactionException`, preserving the intended 409 response. I added `TransactionServiceTests` to exercise that database-rejection fallback.

## First run and verification

The first `./mvnw clean test` run did not fail: it completed with 8 tests, 0 failures, and 0 errors. On this Windows checkout I ran the equivalent `./mvnw.cmd clean test`. The issue above came from reading the service's check-then-save logic, not from a failing test. After the fix and the additional regression test, I reran the complete suite from a clean target directory.

The local Maven process also printed Spring's verbose condition-evaluation diagnostics and JVM/Mockito agent warnings. They are warnings, not test errors. The verbatim build and test-result portion of the final command output is below.

```text
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::staticFieldBase has been called by com.google.inject.internal.aop.HiddenClassDefiner
[INFO] Scanning for projects...
[INFO] ------------------< com.example:transaction-starter >-------------------
[INFO] Building transaction-starter 0.0.1-SNAPSHOT
[INFO] --- clean:3.4.1:clean (default-clean) @ transaction-starter ---
[INFO] --- resources:3.3.1:resources (default-resources) @ transaction-starter ---
[INFO] --- compiler:3.14.0:compile (default-compile) @ transaction-starter ---
[INFO] Compiling 17 source files with javac [debug parameters release 17] to target\classes
[INFO] --- compiler:3.14.0:testCompile (default-testCompile) @ transaction-starter ---
[INFO] Compiling 3 source files with javac [debug parameters release 17] to target\test-classes
[INFO] --- surefire:3.5.3:test (default-test) @ transaction-starter ---
[INFO] Running com.example.transactionstarter.service.TransactionServiceTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.921 s -- in com.example.transactionstarter.service.TransactionServiceTests
[INFO] Running com.example.transactionstarter.TransactionControllerTests
[INFO] Running com.example.transactionstarter.TransactionStarterApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.577 s -- in com.example.transactionstarter.TransactionStarterApplicationTests
[INFO] Results:
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.733 s
[INFO] Finished at: 2026-08-29T08:31:35+05:30
[INFO] ------------------------------------------------------------------------
```

## Follow-up

I did not receive the variant email referenced by the exercise. Before the interview, I would confirm that omission with the placement office or Toucan so I know whether a variant-specific rule set is expected.
