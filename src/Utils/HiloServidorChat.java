package Utils;

import javax.net.ssl.SSLSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HiloServidorChat extends Thread {
	private DataInputStream fentrada;
	private SSLSocket socket;
	private ComunHilos comun;
	private String historial = "";
    private String[] partes;

	public HiloServidorChat(SSLSocket s, ComunHilos comun) {
		this.socket = s;
		this.comun = comun;
		try {

			fentrada = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("ERROR DE E/S");
			e.printStackTrace();
		}
	}

	public void run() {
        boolean primeravez = true;
		System.out.println("NUMERO DE CONEXIONES ACTUALES: " + comun.getActuales());


		String texto = comun.getMensajes();
		EnviarMensajesaTodos(texto);

        while (true) {
			String cadena;
			try {
				cadena = fentrada.readUTF();
				if (primeravez){
				    partes = cadena.split("... ");
				    primeravez = false;
                }
				if (cadena.trim().equals("*")) {
					comun.setActuales(comun.getActuales() - 1);
					System.out.println("NUMERO DE CONEXIONES ACTUALES: " + comun.getActuales());

                    ProcessBuilder pb = new ProcessBuilder("cmd", "/C","powershell '"+ historial +"' > C:\\java_code\\PracticaPSP2\\historial"+ partes[4] +".txt");
                    try
                    {
                        Process process = pb.start();
                        process.waitFor();
                        if (process.exitValue() != 0)
                        {
                            System.err.println("Error al guardar el historial");
                        }
                    }
                    catch (IOException | InterruptedException e)
                    {
                        System.err.println(e.getMessage());
                    }

					break;
				}
				comun.setMensajes(comun.getMensajes() + cadena + "\n");
				EnviarMensajesaTodos(comun.getMensajes());
				historial += cadena + " ";
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}


		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	private void EnviarMensajesaTodos(String texto) {
		int i;

		for (i = 0; i < comun.getConexiones(); i++) {
			SSLSocket s1 = comun.getElementoTabla(i);
			if (!s1.isClosed()) {
				try {
					DataOutputStream fsalida = new DataOutputStream(s1.getOutputStream());
					fsalida.writeUTF(texto);					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}