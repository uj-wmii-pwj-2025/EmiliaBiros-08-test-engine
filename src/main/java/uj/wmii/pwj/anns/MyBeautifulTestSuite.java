package uj.wmii.pwj.anns;

public class MyBeautifulTestSuite {

    @MyTest
    public void simpleTest() {
        System.out.println("Executing simple test without parameters");
    }

    @MyTest(params = {"10", "20", "30"})
    public void testWithMultipleParams(int value) {
        System.out.printf("Processing value: %d\n", value);
    }

    public void notAnnotatedMethod() {
        System.out.println("This method should not be executed");
    }

    @MyTest
    public void testThatThrowsError() {
        System.out.println("About to throw an exception...");
        throw new RuntimeException("Intentional error for testing");
    }

    @MyTest(params = {"5", "10"}, expectedResults = {"25", "100"})
    public int square(int num) {
        return num * num;
    }

    @MyTest(params = {"hello", "world"}, expectedResults = {"HELLO", "WORLD"})
    public String convertToUpperCase(String text) {
        return text.toUpperCase();
    }

    @MyTest(params = {"-5", "0", "7"}, expectedResults = {"false", "false", "true"})
    public boolean isPositive(int number) {
        return number > 0;
    }

    @MyTest(params = {"3.14159", "2.71828"}, expectedResults = {"3.14", "2.72"}, tolerance = 0.01)
    public double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @MyTest(params = {"10", "5"}, expectedResults = {"50", "999"})
    public int multiplyByFive(int num) {
        return num * 5;
    }

    @MyTest(expectedResults = {"42"})
    public int returnConstant() {
        return 42;
    }

    @MyTest(params = {"test"})
    public void voidMethodWithParam(String str) {
        System.out.println("Void method called with: " + str);
    }

    @MyTest
    public void anotherErrorTest() {
        int result = 100 / 0;
    }
}