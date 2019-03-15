package Utils;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ComunHilos {
	private int conexiones;
	private int actuales;
	private SSLSocket tabla[];
	private String mensajes;
	
	public ComunHilos(int actuales, int conexiones, SSLSocket[] tabla) {
	    this.actuales = actuales;
		this.conexiones = conexiones;
		this.tabla = tabla;  
		mensajes="";        
	}

    public int getConexiones() { return conexiones;	}
	public synchronized void setConexiones(int conexiones) {
        this.conexiones = conexiones;
	}

	String getMensajes() { return mensajes; }
	synchronized void setMensajes(String mensajes) {
		this.mensajes = mensajes;
	}

	public int getActuales() { return actuales; }
	public synchronized void setActuales(int actuales) {
        this.actuales = actuales;
	}

	public synchronized void addTabla(SSLSocket s, int i) {
		tabla[i] = s;
	}
	SSLSocket getElementoTabla(int i) { return tabla[i]; }

	public void guardarMensajes(){

        System.out.println(getMensajes());

        String chat = getMensajes().replaceAll("\n","  ");

        ProcessBuilder pb = new ProcessBuilder("cmd", "/C","powershell echo '"+ chat +"'> C:\\\\java_code\\\\PracticaPSP2\\\\historialCompleto.txt\"");
        try
        {
            Process process = pb.start();
            TimeUnit.SECONDS.sleep(10);
            if (process.exitValue() != 0)
            {
                System.err.println("Error al guardar el historial");
            }
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println(e.getMessage());
        }

    }
		
}
