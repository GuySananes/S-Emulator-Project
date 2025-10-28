# Server Tests

This directory contains integration tests for the S-Emulator server implementation.

## Test Files

### SessionIsolationTest.java
Tests session isolation between multiple users to verify that different sessions have independent engine states.

**Tests:**
- Different sessions get different Engine instances
- Engine state is properly isolated between sessions
- Concurrent session access is handled correctly
- Session cleanup works properly

**Requirements Covered:** 2.1, 2.2, 2.3, 2.4

### ExecutionWorkflowTest.java
Tests complete execution workflows including regular execution, debug execution, expand/collapse operations, and rerun functionality.

**Tests:**
- Regular execution flow end-to-end
- Debug execution flow with step/resume operations
- Expand/collapse operations
- Rerun functionality

**Requirements Covered:** 1.1, 1.2, 1.3, 1.4, 3.1, 3.2, 3.3, 3.4, 5.1, 5.2, 5.3, 5.4

### ErrorHandlingTest.java
Tests error handling and edge cases to verify proper exception handling and error responses.

**Tests:**
- Invalid program path handling
- Invalid XML file handling
- Execution without loaded program
- Invalid input handling
- Session expiration scenarios
- Concurrent modification handling

**Requirements Covered:** 6.1, 6.2, 6.3, 6.4, 6.5

## Running the Tests

From the project root directory, run:

```batch
.\run-tests.bat
```

This will:
1. Compile the test classes
2. Run SessionIsolationTest
3. Run ExecutionWorkflowTest
4. Run ErrorHandlingTest
5. Report results

## Test Results

All tests use the test program at `run/badic.xml` for execution testing.

Tests output:
- ✓ for passed tests
- ✗ for failed tests
- Detailed information about each test execution

## Requirements

- Java compiler (javac) in PATH
- Compiled server, engine, DTO, and exception modules in `out/production/`
- Required libraries in `lib/` directory
