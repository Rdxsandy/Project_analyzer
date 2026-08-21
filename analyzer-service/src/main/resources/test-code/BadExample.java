public class BadExample {

    public void process(String input) throws Exception {

        if (input == null) {
            return;
        }

        String password = "admin123";

        System.out.println("Password: " + password);

        Runtime.getRuntime().exec(input);
        try {
            int x = 10 / 0;
        } catch (Exception e) {
        }
    }
}
