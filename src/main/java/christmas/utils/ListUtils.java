package christmas.utils;

import christmas.models.User;

import java.util.List;
import java.util.Random;

public class ListUtils {
    public ListUtils() {}

    public int randomIndexNoRepeat(List<User> list, List<Integer> excludes) {
        Random rand = new Random();

        int randomIndex = getRandomIndex(rand, list.size());
        int userId = list.get(randomIndex).getId();

        while (includes(userId, excludes)) {
            randomIndex = getRandomIndex(rand, list.size());
            userId = list.get(randomIndex).getId();
        }
        list.remove(randomIndex);
        return userId;
    }

    public static boolean includes(int value, List<Integer> excludes) {
        return excludes.indexOf(value) > -1;
    }

    public static int getRandomIndex(Random rand, int size) {
        return rand.nextInt(size);
    }
}
