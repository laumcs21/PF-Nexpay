package proyecto.nexpay.web.model;

import proyecto.nexpay.web.datastructures.SimpleList;

public class UserSearchByID {

    private static Nexpay nexpay;

    static {
        nexpay = Nexpay.getInstance();
    }

    public static User findUserById(String id) {
        return findUserRecursive(nexpay.getUsers(), id, 0);
    }

    private static User findUserRecursive(SimpleList<User> users, String id, int index) {
        if (index >= users.getSize()) {
            return null;
        }

        User user = users.get(index);
        if (user.getId().equals(id)) {
            return user;
        }

        return findUserRecursive(users, id, index + 1);
    }
}
