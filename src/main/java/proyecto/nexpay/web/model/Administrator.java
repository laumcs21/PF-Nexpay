package proyecto.nexpay.web.model;

import java.io.Serializable;

public class Administrator extends Person implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Administrator instance;

    private String password;

    private Administrator(String id, String password, String name, String email, String phone, String address) {
        super(id, password, name, email, phone, address);
        this.password = password;
    }

    public static Administrator getInstance() {
        if (instance == null) {
            instance = new Administrator(
                    "1001198723",
                    "1111",
                    "Laura Milena Cardenas",
                    "lauram.cardenass@uqvirtual.edu.co",
                    "3003710163",
                    "Villa carolina primera etapa mz.M #12");
        }
        return instance;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
