package proyecto.nexpay.web;

import proyecto.nexpay.web.model.*;
import proyecto.nexpay.web.service.*;
import proyecto.nexpay.web.persistence.*;
import proyecto.nexpay.web.datastructures.*;

import java.time.LocalDate;

public class main {

    public static void main(String[] args) {

        Nexpay nexpay = Nexpay.getInstance();
        SimpleList<Account> cuentas = nexpay.getAccounts();

        if (cuentas.isEmpty()) {
            System.out.println("No se encontraron cuentas.");
        } else {
            System.out.println("cuentas encontradas:");
            for (Account cuenta : cuentas) {
                System.out.println("Numero: " + cuenta.getAccountNumber());
                System.out.println("Código: " + cuenta.getId());
                System.out.println("Usuario: " + cuenta.getUserId());
                System.out.println("Banco: " + cuenta.getBankName());
                System.out.println("---------------------------");
            }
        }
    }

}

