package org.example;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import static java.lang.Thread.sleep;

abstract class Persona {
    private final String nombre;
    private final String dni;
    private final String pin;
    private final String rol;
    Random r = new Random();
    private double saldo;


    public Persona(String id, String name, String secret_pass, String rolear) {
        this.dni = id;
        this.rol = rolear;
        this.nombre = name;
        this.pin = secret_pass;
    }

    public String getDni() {
        return dni;
    }

    public String getPin() {
        return pin;
    }

    public String getRol() {
        return rol;
    }

    public String getNombre() {
        return nombre;
    }
}

class Cliente extends Persona {

    public Cliente(String dni, String name, String pin, String rollin) {
        super(dni, name, pin, rollin);

    }
}

class Empleado extends Persona {

    public Empleado(String dni, String name, String pin, String rollin) {
        super(dni, name, pin, rollin);

    }

}

class Cajero {
    int id;
    String direccion;
    String poblacion;
    int billetes5;
    int billetes10;
    int billetes20;
    int billetes50;

    int total;

    public Cajero(int identificador, String direction, String population, int cinco, int diez, int veinte, int cincuenta) {
        this.id = identificador;
        this.direccion = direction;
        this.poblacion = population;
        this.billetes5 = cinco;
        this.billetes10 = diez;
        this.billetes20 = veinte;
        this.billetes50 = cincuenta;
    }

    public void sumarCheles() {
        this.total = billetes5 + billetes10 + billetes20 + billetes50;
    }

    public void c_billetes(int dineros) {
        while (dineros > 0) {
            if (dineros > 50) {
                dineros -= 50;
                billetes50--;
            } else if (dineros > 20) {
                dineros -= 20;
                billetes20--;
            } else if (dineros > 10) {
                dineros -= 10;
                billetes10--;
            } else if (dineros >= 5) {
                dineros -= 5;
                billetes5--;
            }
        }
    }

    public int getTotal() {
        return total;
    }

    public int getId() {
        return id;
    }

    public int getBilletes5() {
        return billetes5;
    }

    public int getBilletes10() {
        return billetes10;
    }

    public int getBilletes20() {
        return billetes20;
    }

    public int getBilletes50() {
        return billetes50;
    }
}

class Cuenta {
    int iban;
    String dni;
    int saldo;

    public Cuenta(int account_id, String idnetificacion, int saldo) {
        this.iban = account_id;
        this.dni = idnetificacion;
        this.saldo = saldo;
    }

    public void retirar(int cheles) {
        saldo = -cheles;
    }

    public int getSaldo() {
        return saldo;
    }

    public int getIban() {
        return iban;
    }
}

class Conectar {

    static Connection c = null;

    public static void conectar() {
        try {
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Banco", "postgres", "S3rvid@r");
            System.out.println("Se ha conectado");
        } catch (SQLException e) {

        }
    }

    public static void desconectar() {
        if (c != null) {
            try {
                c.close();
            } catch (SQLException e) {

            }
        }
    }
}

class Sql {
    Scanner s = new Scanner(System.in);

    public Persona login() {
        System.out.println("Introduce tu dni");
        String id = s.next();
        System.out.println("Introduce tu pin");
        String code = s.next();
        try {
            PreparedStatement ps = Conectar.c.prepareStatement("Select * from empleados where dni =? and pin=? union select * from clientes where dni =? and pin=?");
            ps.setString(1, id);
            ps.setString(2, code);
            ps.setString(3, id);
            ps.setString(4, code);
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {

                if (resultSet.getString("rol").equalsIgnoreCase("E")) {
                    return new Empleado(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4));
                } else {
                    return new Cliente(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4));
                }

            }
            resultSet.close();
            ps.close();
            Conectar.c.close();
        } catch (SQLException e) {
        }
        System.out.println("Las claves introducidas no existen");
        System.exit(0);
        return null;

    }

    public void rellenar() throws SQLException {

        try {
            Conectar.c.setAutoCommit(false);

            System.out.println("Indique el numero del cajero en el desea hacer el ingreso");
            mostrarCajerosDisponibles();
            int cajero = s.nextInt();

            System.out.println("Cuantos billetes de 5 tienes?");
            int billetes5 = s.nextInt();
            System.out.println("Cuantos billetes de 10 tienes?");
            int billetes10 = s.nextInt();
            System.out.println("Cuantos billetes de 20 tienes?");
            int billetes20 = s.nextInt();
            System.out.println("Cuantos billetes de 50 tienes?");
            int billetes50 = s.nextInt();

            PreparedStatement statement = Conectar.c.prepareStatement("UPDATE cajeros set billetes5 = billetes5 + ?, billetes10 = billetes + ?, billetes20 = billetes20+ ?, billetes50 = billetes50+ ? WHERE id=?");
            statement.setInt(1, billetes5);
            statement.setInt(2, billetes10);
            statement.setInt(3, billetes20);
            statement.setInt(4, billetes50);
            statement.setInt(5, cajero);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException ex) {
            System.out.println("Se ha producido un error");
            Conectar.c.rollback();
        }
    }

    public void meterDinero(String dni) throws SQLException {
        ArrayList<Integer> cuentas = obtenerCuentas(dni);

        try {
            Conectar.c.setAutoCommit(false);


            System.out.println("Selecciona la cuenta a la que quieres ingresar dinero:");
            mostrarCuentas(cuentas);
            int seleccionCuenta = s.nextInt();

            System.out.println("Indique el numero del cajero en el desea hacer el ingreso");
            mostrarCajerosDisponibles();
            int cajero = s.nextInt();

            System.out.println("Cuantos billetes de 5 tienes?");
            int billetes5 = s.nextInt();
            System.out.println("Cuantos billetes de 10 tienes?");
            int billetes10 = s.nextInt();
            System.out.println("Cuantos billetes de 20 tienes?");
            int billetes20 = s.nextInt();
            System.out.println("Cuantos billetes de 50 tienes?");
            int billetes50 = s.nextInt();
            int montoIngreso = billetes5 + billetes10 + billetes20 + billetes50;

            PreparedStatement ps = Conectar.c.prepareStatement("UPDATE cuentas set saldo = saldo + ? where numero = ?");
            ps.setInt(1, montoIngreso);
            ps.setInt(2, seleccionCuenta);
            ps.executeUpdate();

            PreparedStatement statement = Conectar.c.prepareStatement("UPDATE cajeros set billetes5 = billetes5 + ?, billetes10 = billetes + ?, billetes20 = billetes20+ ?, billetes50 = billetes50+ ? WHERE id=?");
            statement.setInt(1, billetes5);
            statement.setInt(2, billetes10);
            statement.setInt(3, billetes20);
            statement.setInt(4, billetes50);
            statement.setInt(5, cajero);
            statement.executeUpdate();

            ps.close();
            statement.close();
            Conectar.c.commit();
        } catch (SQLException ex) {
            System.out.println("Se ha producido un error");
            Conectar.c.rollback();
        }
    }


    public void sacarDinero(String dni) throws IOException, SQLException {
        ArrayList<Integer> cuentas = obtenerCuentas(dni);
        int saldoActual;

        try {
            Conectar.c.setAutoCommit(false);


            System.out.println("Selecciona la cuenta de la que quieres sacar dinero:");
            mostrarCuentas(cuentas);

            int seleccionCuenta = s.nextInt();
            System.out.println("Cuánto deseas sacar?");
            int montoRetirada = s.nextInt();

            PreparedStatement ps = Conectar.c.prepareStatement("SELECT saldo FROM cuentas WHERE numero = ?");
            ps.setInt(1, seleccionCuenta);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                saldoActual = rs.getInt("saldo");
                while (saldoActual < montoRetirada) {
                    System.out.println("No tienes suficiente dinero. Puedes volver a probar más tarde.");
                    System.out.println("Cuánto dinero deseas retirar?");
                    montoRetirada = s.nextInt();
                }

                mostrarCajerosDisponibles();
                int seleccionCajero = s.nextInt();
                switch (seleccionCajero) {
                    case 1, 2, 3, 4:
                        realizarOperacionCajero(seleccionCajero, montoRetirada, cuentas, dni);
                        break;
                    default:
                        System.out.println("Opción de cajero no válida.");
                        break;
                }
            }
            ps.close();
            rs.close();
            Conectar.c.commit();

        } catch (SQLException ex) {
            System.out.println("Se ha producido un error");
            Conectar.c.rollback();
        }

    }

    public ArrayList<Integer> obtenerCuentas(String dni) throws SQLException {
        ArrayList<Integer> cuentas = new ArrayList<>();
        PreparedStatement ps = Conectar.c.prepareStatement("SELECT numero FROM cuentas WHERE dni_titular = ?");
        ps.setString(1, dni);
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()) {
            int numCuenta = resultSet.getInt("numero");
            cuentas.add(numCuenta);
        }
        ps.close();
        resultSet.close();
        return cuentas;
    }

    public void mostrarCuentas(ArrayList<Integer> cuentas) {
        System.out.println("Cuentas disponibles:");
        for (int cuenta : cuentas) {
            System.out.println(cuenta);
        }
    }

    public void mostrarCajerosDisponibles() throws SQLException {
        PreparedStatement ps = Conectar.c.prepareStatement("SELECT id, direccion, poblacion from cajeros ");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getInt("id") + ".  " + rs.getString("direccion") + rs.getString("poblacion"));
        }
        ps.close();
        rs.close();
    }

    public void realizarOperacionCajero(int seleccionCajero, int montoRetirada, ArrayList<Integer> cuentas, String dni) throws SQLException {
        PreparedStatement statement = Conectar.c.prepareStatement("SELECT * FROM cajeros WHERE id = ?");
        statement.setInt(1, seleccionCajero);
        ResultSet sr = statement.executeQuery();

        if (sr.next()) {
            Cajero atm = new Cajero(sr.getInt("id"), sr.getString("direccion"), sr.getString("poblacion"), sr.getInt("billetes5"), sr.getInt("billetes10"), sr.getInt("billetes20"), sr.getInt("billetes50"));
            atm.c_billetes(montoRetirada);

            PreparedStatement stmt = Conectar.c.prepareStatement("UPDATE cajeros SET billetes5 = ?, billetes10 = ?, billetes20 = ?, billetes50 = ? WHERE id = ?");
            stmt.setInt(1, atm.getBilletes5());
            stmt.setInt(2, atm.getBilletes10());
            stmt.setInt(3, atm.getBilletes20());
            stmt.setInt(4, atm.getBilletes50());
            stmt.setInt(5, atm.getId());
            stmt.executeUpdate();

            actualizarSaldoCuenta(montoRetirada, dni);
        }
        statement.close();
        sr.close();
    }

    public void actualizarSaldoCuenta(int montoRetirada, String dni) throws SQLException {
        PreparedStatement preparedStatement = Conectar.c.prepareStatement("SELECT * FROM cuentas WHERE dni_titular = ?");
        preparedStatement.setString(1, dni);
        ResultSet rst = preparedStatement.executeQuery();
        if (rst.next()) {
            Cuenta cuenta = new Cuenta(rst.getInt("numero"), rst.getString("dni_titular"), rst.getInt("saldo"));
            cuenta.retirar(montoRetirada);

            PreparedStatement lastStatment = Conectar.c.prepareStatement("UPDATE cuentas SET saldo = ? WHERE numero = ?");
            lastStatment.setInt(1, cuenta.getSaldo());
            lastStatment.setInt(2, cuenta.getIban());
            lastStatment.executeUpdate();
        }
    }

    public void cambiarPin(String dni) throws SQLException {
        System.out.println("Deseas cambiar el pin");
        String respuesta = s.next();
        while (!respuesta.equalsIgnoreCase("si")) {
            respuesta = s.next();
        }
        System.out.println("Introduzca el nuevo pin");
        int newpin = s.nextInt();

        PreparedStatement ps = Conectar.c.prepareStatement("UPDATE clientes set pin =? where dni = ?");
        ps.setInt(1, newpin);
        ps.setString(2, dni);
        ps.executeUpdate();
        ps.close();
    }

    public void mostrarpersonas() throws SQLException {
        PreparedStatement ps = Conectar.c.prepareStatement("SELECT nombre FROM clientes UNION SELECT nombre FROM empleados");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.println(rs.getString("nombre"));
        }
    }

    public String datospersona(String nombre) throws SQLException {
        try {
            PreparedStatement ps = Conectar.c.prepareStatement("SELECT dni FROM clientes UNION SELECT dni FROM empleados");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("dni");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }



    public void transferencia(Persona persona) throws SQLException {
        Conectar.c.setAutoCommit(false);
        ArrayList<Integer> cuentas ;
        if (persona.getRol().equalsIgnoreCase("E")) {
            System.out.println("Desde que cuentan deseas hacer la transferencia");
            String dni_titular = persona.getDni();
            ArrayList<Integer> cuentas_titular = obtenerCuentas(dni_titular);
            mostrarCuentas(cuentas_titular);
            int cuenta_origen = s.nextInt();
            System.out.println("A quien le desea realizar una transferencia?");
            mostrarpersonas();
            String persona_escogida = s.next();
            String dni = datospersona(persona_escogida);
            cuentas = obtenerCuentas(dni);
            mostrarCuentas(cuentas);
            System.out.println("Elige la cuenta a la que deseas realizar la transferencia");
            int cuenta_destino = s.nextInt();
            System.out.println("Introduce el importe que desees transferir");
            int dinero = s.nextInt();

            PreparedStatement ps = Conectar.c.prepareStatement("UPDATE cuentas set saldo = saldo - ? WHERE numero = ?");
            ps.setInt(1,dinero);
            ps.setInt(2,cuenta_origen);
            ps.executeUpdate();

            PreparedStatement ps1 = Conectar.c.prepareStatement("UPDATE cuentas set saldo = saldo + ? WHERE numero = ?");
            ps1.setInt(1,dinero);
            ps1.setInt(2,cuenta_destino);
            ps1.executeUpdate();
        } else if (persona.getRol().equalsIgnoreCase("C")) {
            System.out.println("Desde que cuentan deseas hacer la transferencia");
            cuentas = obtenerCuentas(persona.getDni());
            mostrarCuentas(cuentas);
            int cuenta_origen = s.nextInt();
            System.out.println("Elige la cuenta a la que deseas realizar la transferencia");
            mostrarCuentas(cuentas);
            int cuenta_destino = s.nextInt();
            while (!cuentas.contains(cuenta_destino)){
                System.out.println("La cuenta que has introducido no existe, prueba de nuevo");
                cuenta_destino = s.nextInt();
            }
            System.out.println("Introduce el importe que desees transferir");
            int dinero = s.nextInt();

            PreparedStatement ps = Conectar.c.prepareStatement("UPDATE cuentas set saldo = saldo - ? WHERE numero = ?");
            ps.setInt(1,dinero);
            ps.setInt(2,cuenta_origen);
            ps.executeUpdate();
            ps.close();

            PreparedStatement ps1 = Conectar.c.prepareStatement("UPDATE cuentas set saldo = saldo + ? WHERE numero = ?");
            ps1.setInt(1,dinero);
            ps1.setInt(2,cuenta_destino);
            ps1.executeUpdate();
            ps1.close();
            Conectar.c.commit();
        }


    }

    public void consultarSaldo(String dni) throws SQLException, InterruptedException {

        CallableStatement call = Conectar.c.prepareCall("{call obtenersaldo(?)}");
        call.setString(1,dni);
        ResultSet rs = call.executeQuery();
        while (rs.next()){
            System.out.println("Tienes "+rs.getString("cheles")+" en la cuenta "+rs.getString("iban") );
        }
        sleep(5000);
        call.close();
    }


    public void anadircliente() throws SQLException {
        System.out.println("Introduce tu DNI:");
        String dni = s.next();
        System.out.println("Introduce tu nombre");
        String name = s.next();
        System.out.println("Introduce el PIN");
        int pin = s.nextInt();
        System.out.println("Vuelve a introducir tu PIN");
        int pin2 = s.nextInt();
        while (pin2 != pin){
            System.out.println("Vuelva a introducir tu PIN");
            pin2  = s.nextInt();
        }
        String rol = s.next();
        while(!rol.equalsIgnoreCase("C")|| !rol.equalsIgnoreCase("E")){
            System.out.println("No existe ninguno de estos roles, vuelve a probar");
            rol = s.next();
        }
        PreparedStatement ps = Conectar.c.prepareStatement("INSERT INTO clientes (dni, nombre, pin, rol) values (?,?,?,?,?)");
        ps.setString(1,dni);
        ps.setString(2,name);
        ps.setInt(3, pin2);
        ps.setString(4,rol);
        ps.executeUpdate();

        PreparedStatement ps1 = Conectar.c.prepareStatement("INSERT INTO cuentas(numero, dni_titular,saldo) values (numero, ?,?,?)");
        ps1.setString(1,dni);
        ps1.setInt(2,0);
        ps1.executeUpdate();
    }

    public void eliminarcliente() throws SQLException {
        Conectar.c.setAutoCommit(false);

        System.out.println(" Que cliente deseas eliminar ? ");
        mostrarpersonas();
        String persona= s.next();
        String dni = datospersona(persona);
        PreparedStatement ps = Conectar.c.prepareStatement("DELETE FROM cuentas where dni_titular = ?");
        ps.setString(1,dni);
        ps.executeUpdate();

        PreparedStatement ps1 = Conectar.c.prepareStatement("DELETE from clientes WHERE dni =? ");
        ps1.setString(1,dni);
        ps1.executeUpdate();
        Conectar.c.commit();
    }
}


public class Banco {

    public static void main(String[] args) throws SQLException, IOException, InterruptedException {
        Conectar.conectar();
        Sql sql = new Sql();
        Scanner s = new Scanner(System.in);
        var objeto = sql.login();
        switch (objeto.getRol()) {
            case "E":
                while (true) {
                    System.out.println("   Que deseas hacer " + objeto.getNombre() + " ?");
                    System.out.println("-------------------------");
                    System.out.println("   1. Rellenar el cajero      ");
                    System.out.println("   2. Añadir cliente    ");
                    System.out.println("   3. Eliminar cliente");
                    System.out.println("   4. Realizar transferencia");
                    System.out.println("   5. Consultar saldo");
                    System.out.println("   6. Cambiar PIN");
                    System.out.println("   7. Salir");
                    int opcion = s.nextInt();
                    switch (opcion){
                        case 1:
                            sql.rellenar();
                            break;
                        case 2:
                            sql.anadircliente();
                            break;
                        case 3:
                            sql.eliminarcliente();
                            break;
                        case 4:
                            sql.transferencia(objeto);
                            break;
                        case 5:
                            sql.consultarSaldo(objeto.getDni());
                            break;
                        case 6:
                            sql.cambiarPin(objeto.getDni());
                        case 7:
                            System.exit(0);
                    }

                }

            case "C":
                while (true) {
                    System.out.println("   Que deseas hacer      ");
                    System.out.println("-------------------------");
                    System.out.println("   1. Sacar dinero       ");
                    System.out.println("   2. Ingresar dinero    ");
                    System.out.println("   3. Realizar transferencia");
                    System.out.println("   4. Consultar saldo");
                    System.out.println("   5. Cambiar PIN");
                    System.out.println("   6. Salir");
                    int opcion = s.nextInt();
                        switch (opcion) {
                            case 1:
                                sql.sacarDinero(objeto.getDni());
                                break;
                            case 2:
                                sql.meterDinero(objeto.getDni());
                                break;
                            case 3:
                                sql.transferencia(objeto);
                                break;
                            case 4:
                                sql.consultarSaldo(objeto.getDni());
                                break;
                            case 5:
                                sql.cambiarPin(objeto.getDni());
                                break;
                            case 6:
                                System.exit(0);
                        }
                    }
                }


        }


    }