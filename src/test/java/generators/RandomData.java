package generators;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomData {

    private RandomData() {

    }

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(3, 15);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toLowerCase() +
                RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomNumeric(5) + "!$";
    }
}
