package proyecto.nexpay.web.model;

import java.util.Optional;
import proyecto.nexpay.web.persistence.UserPersistence;
import proyecto.nexpay.web.datastructures.SimpleList;

import java.io.Serializable;

public class UserCRUD implements CRUD<User>, Serializable {

    private Nexpay nexpay;
    private UserPersistence persistence = new UserPersistence();

    public UserCRUD(Nexpay nexpay) {
        this.nexpay = nexpay;
    }

    public Optional<User> findUserById(String id) {
        return findUserRecursively(nexpay.getUsers(), id, 0);
    }

    private Optional<User> findUserRecursively(SimpleList<User> users, String id, int index) {
        if (index >= users.getSize()) {
            return Optional.empty();
        }

        User user = users.get(index);
        if (user.getId().equals(id)) {
            return Optional.of(user);
        }

        return findUserRecursively(users, id, index + 1);
    }

    public static boolean isEmailValid(String email) {
        String emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }

    public static boolean isPasswordValid(String password) {
        boolean hasLetter = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
            if (hasLetter && hasNumber) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void update(User user) {
        delete(user.getId());
        nexpay.getUsers().addLast(user);
        persistence.saveAllUsers(nexpay.getUsers());
    }

    @Override
    public User create(User user) {
        if (findUserById(user.getId()).isPresent()) {
            throw new IllegalArgumentException("The user is already registered.");
        }

        nexpay.getUsers().addLast(user);
        persistence.saveAllUsers(nexpay.getUsers());
        return user;
    }

    @Override
    public void delete(String id) {
        User user = read(id);
        nexpay.getUsers().remove(user);
        persistence.saveAllUsers(nexpay.getUsers());
    }

    @Override
    public User read(String id) {
        return findUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("The user is not registered."));
    }

    public User safeRead(String id) {
        return findUserById(id).orElse(null);
    }

    public Nexpay getNexpay() {
        return nexpay;
    }

    public void setNexpay(Nexpay nexpay) {
        this.nexpay = nexpay;
    }

    public UserPersistence getPersistence() {
        return persistence;
    }

    public void setPersistence(UserPersistence persistence) {
        this.persistence = persistence;
    }
}
