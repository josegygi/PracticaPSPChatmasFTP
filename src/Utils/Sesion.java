package Utils;

import java.io.Serializable;

public class Sesion implements Serializable {

    private String nombre, pass, servidor, puerto;
    private boolean estado;

    public Sesion(String nombre, String pass, String servidor, String puerto, boolean estado) {
        this.nombre = nombre;
        this.pass = pass;
        this.servidor = servidor;
        this.puerto = puerto;
        this.estado = estado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPass() {
        return pass;
    }

    public String getServidor() {
        return servidor;
    }

    public String getPuerto() {
        return puerto;
    }

    public boolean isEstado() {
        return estado;
    }
}
