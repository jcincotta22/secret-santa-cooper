package christmas.services;

import christmas.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserRequestBody  extends User {
    public UserRequestBody(int _id, String name, String email, List<Integer> parentIds, String username, String passoword) {
        super(_id, name, email, parentIds, username, passoword);
    }
}
