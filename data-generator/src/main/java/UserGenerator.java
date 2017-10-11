import com.github.javafaker.Faker;
import model.User;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class UserGenerator {

    private static final Locale LOCALE = new Locale.Builder().setLanguage("en").setRegion("US").build();
    private static Faker faker = new Faker(LOCALE);

    private static final String[][] PREDEFINED_USERS = {
            {"John", "Doe"},
            {"Simone", "Davis"},
            {"Mike", "Dupont"}
    };

    private UserGenerator() {
    }

    public static Set<User> generate(int num) {

        Set<User> users = new HashSet<>();
        while (users.size() < num) {
            users.add((users.size() < PREDEFINED_USERS.length) ? getPredefinedUser(users.size()) : generateRandomUser());
        }

        return users;
    }

    private static User getPredefinedUser(int index) {

        User user = new User();
        user.setFirstName(PREDEFINED_USERS[index][0]);
        user.setLastName(PREDEFINED_USERS[index][1]);
        user.setId(constructUserName(user));

        return user;
    }

    private static User generateRandomUser() {

        User user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setId(constructUserName(user));

        return user;
    }

    private static String constructUserName(User user) {
        String userName = user.getFirstName().charAt(0) + user.getLastName();
        return userName.toLowerCase();
    }
}
