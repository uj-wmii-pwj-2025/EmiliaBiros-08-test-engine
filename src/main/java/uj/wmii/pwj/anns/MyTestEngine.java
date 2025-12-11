package uj.wmii.pwj.anns;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyTestEngine {

    private final String className;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please specify test class name");
            System.exit(-1);
        }

        displayHeader();

        String className = args[0].trim();
        System.out.printf("Class under test: %s\n\n", className);

        MyTestEngine engine = new MyTestEngine(className);
        engine.runTests();
    }

    public MyTestEngine(String className) {
        this.className = className;
    }

    public void runTests() {
        final Object testInstance = instantiateTestClass(className);

        if (testInstance == null) {
            System.err.println("ERROR: Unable to create instance of test class");
            return;
        }

        List<Method> testMethods = discoverTestMethods(testInstance);

        if (testMethods.isEmpty()) {
            System.out.println("No test methods discovered in class.");
            return;
        }

        displayTestOverview(testMethods);

        System.out.println("\n" + "─".repeat(70));
        System.out.println("EXECUTING TESTS");
        System.out.println("─".repeat(70) + "\n");

        int totalPassed = 0;
        int totalFailed = 0;
        int totalErrors = 0;

        for (Method method : testMethods) {
            List<TestResult> methodResults = executeTestMethod(method, testInstance);

            for (TestResult res : methodResults) {
                if (res == TestResult.PASS) totalPassed++;
                else if (res == TestResult.FAIL) totalFailed++;
                else if (res == TestResult.ERROR) totalErrors++;
            }
        }

        displaySummary(totalPassed, totalFailed, totalErrors);
    }

    private List<TestResult> executeTestMethod(Method method, Object testInstance) {
        List<TestResult> results = new ArrayList<>();
        MyTest annotation = method.getAnnotation(MyTest.class);

        String[] params = annotation.params();
        String[] expectedResults = annotation.expectedResults();
        double tolerance = annotation.tolerance();

        System.out.println("▶ Test: " + method.getName());

        if (params.length == 0) {
            TestResult result = runSingleTest(method, testInstance, null,
                    expectedResults.length > 0 ? expectedResults[0] : null,
                    tolerance);
            results.add(result);
        } else {
            for (int i = 0; i < params.length; i++) {
                String expectedValue = (expectedResults.length > i) ? expectedResults[i] : null;
                TestResult result = runSingleTest(method, testInstance, params[i], expectedValue, tolerance);
                results.add(result);
            }
        }

        System.out.println();
        return results;
    }

    private TestResult runSingleTest(Method method, Object instance, String param,
                                     String expectedValue, double tolerance) {
        try {
            Object actualResult;

            if (param == null) {
                actualResult = method.invoke(instance);
            } else {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length > 0) {
                    Object convertedParam = convertStringToType(param, paramTypes[0]);
                    actualResult = method.invoke(instance, convertedParam);
                } else {
                    actualResult = method.invoke(instance);
                }
            }

            if (expectedValue == null) {
                String paramInfo = param != null ? " with param: " + param : "";
                System.out.println("  ✓ PASS" + paramInfo + " (no expected result defined)");
                return TestResult.PASS;
            }

            boolean matches = compareValues(actualResult, expectedValue, tolerance);

            if (matches) {
                String paramInfo = param != null ? " [param: " + param + "]" : "";
                System.out.printf("  ✓ PASS%s → result: %s\n", paramInfo, actualResult);
                return TestResult.PASS;
            } else {
                String paramInfo = param != null ? " [param: " + param + "]" : "";
                System.out.printf("  ✗ FAIL%s → expected: %s, got: %s\n",
                        paramInfo, expectedValue, actualResult);
                return TestResult.FAIL;
            }

        } catch (Exception e) {
            Throwable rootCause = e.getCause() != null ? e.getCause() : e;
            String paramInfo = param != null ? " [param: " + param + "]" : "";
            System.out.printf("  ⚠ ERROR%s → %s: %s\n",
                    paramInfo, rootCause.getClass().getSimpleName(),
                    rootCause.getMessage());
            return TestResult.ERROR;
        }
    }

    private Object convertStringToType(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == char.class || targetType == Character.class) {
            return value.length() > 0 ? value.charAt(0) : '\0';
        }
        return value;
    }

    private boolean compareValues(Object actual, String expected, double tolerance) {
        if (actual == null) {
            return expected.equals("null");
        }

        String actualStr = String.valueOf(actual);

        if (actualStr.equals(expected)) {
            return true;
        }

        try {
            if (actual instanceof Number) {
                double actualNum = ((Number) actual).doubleValue();
                double expectedNum = Double.parseDouble(expected);
                return Math.abs(actualNum - expectedNum) <= tolerance;
            }
        } catch (NumberFormatException e) {
            // nan comparison
        }

        return false;
    }

    private static List<Method> discoverTestMethods(Object testInstance) {
        Method[] allMethods = testInstance.getClass().getDeclaredMethods();
        return Arrays.stream(allMethods)
                .filter(m -> m.getAnnotation(MyTest.class) != null)
                .collect(Collectors.toList());
    }

    private static Object instantiateTestClass(String className) {
        try {
            Class<?> testClass = Class.forName(className);
            return testClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void displayTestOverview(List<Method> methods) {
        System.out.println("╔" + "═".repeat(68) + "╗");
        System.out.printf("║ %-66s ║\n", "TEST DISCOVERY");
        System.out.println("╠" + "═".repeat(68) + "╣");
        System.out.printf("║ %-66s ║\n", "Found " + methods.size() + " test method(s):");
        System.out.println("╠" + "═".repeat(68) + "╣");

        for (int i = 0; i < methods.size(); i++) {
            Method m = methods.get(i);
            MyTest ann = m.getAnnotation(MyTest.class);
            int testCases = Math.max(1, ann.params().length);
            String info = String.format("%d. %s → %d test case(s)",
                    i + 1, m.getName(), testCases);
            System.out.printf("║ %-67s║\n", "  " + info);
        }

        System.out.println("╚" + "═".repeat(68) + "╝");
    }

    private void displaySummary(int passed, int failed, int errors) {
        int total = passed + failed + errors;
        double passRate = total > 0 ? (passed * 100.0 / total) : 0;

        System.out.println("─".repeat(70));
        System.out.println("RESULTS SUMMARY");
        System.out.println("─".repeat(70));
        System.out.printf("Total executed: %d\n", total);
        System.out.printf("  ✓ PASSED:  %d\n", passed);
        System.out.printf("  ✗ FAILED:  %d\n", failed);
        System.out.printf("  ⚠ ERRORS:  %d\n", errors);
        System.out.printf("\nSuccess rate: %.1f%%\n", passRate);
        System.out.println("─".repeat(70));

        if (failed == 0 && errors == 0) {
            System.out.println("\n🎉 All tests passed successfully!");
        } else if (errors > 0) {
            System.out.println("\n⚠ Some tests encountered errors.");
        }
    }

    private static void displayHeader() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("                         TEST ENGINE                              ");
        System.out.println("═".repeat(70));
        System.out.println();
    }
}