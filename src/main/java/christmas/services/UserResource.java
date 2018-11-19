package christmas.services;

import christmas.models.User;

import java.util.ArrayList;

public class UserResource {
    private int _id;
    private String name;
    private ArrayList<Integer> parentIds;

    public UserResource() {

    }

    static UserResource from(User user) {
        UserResource userResource = new UserResource();
        userResource._id = user.getId();
        userResource.name = user.getName();
        userResource.parentIds = user.getParentIds();

        return userResource;
    }
}
