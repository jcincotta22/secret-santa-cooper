package christmas.services;

import christmas.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserResource {
    private int _id;
    private String name;
    private List<Integer> parentIds;
    private String username;

    public UserResource() {

    }

    static UserResource from(User user) {
        UserResource userResource = new UserResource();
        userResource._id = user.getId();
        userResource.name = user.getName();
        userResource.parentIds = user.getParentIds();
        userResource.username = user.getUsername();

        return userResource;
    }
}
