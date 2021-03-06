package christmas.models;
import java.util.List;

public class User {
    private final String name;
    private final String email;
    private int _id;
    private List<Integer> parentIds;
    private String username;
    private String password;
    private int secretSanta;


    public User(int _id, String name, String email, List<Integer> parentIds, String username, String password) {
        this._id = _id;
        this.name = name;
        this.email = email;
        this.parentIds = parentIds;
        this.username = username;
        this.password = password;
    }

    public User(int _id, String name, String email, List<Integer> parentIds) {
        this._id = _id;
        this.name = name;
        this.email = email;
        this.parentIds = parentIds;
    }

    public String getName() { return name; }

    public String getEmail() { return email; }

    public int getId() { return _id; }

    public List<Integer> getParentIds() { return parentIds; }

    public void setParentIds(List<Integer> ids) {
        parentIds.addAll(ids);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public void setSecretSanta(int secretSanta) {
        this.secretSanta = secretSanta;
    }

    public int getSecretSanta() {
        return secretSanta;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
