package proyecto.nexpay.web.model;

public interface CRUD<T> {
    T create(T object);

    T read(String id);

    void update(T object);

    void delete(String id);
}

