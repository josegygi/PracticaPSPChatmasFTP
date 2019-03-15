package Servidor;

import Utils.ComunHilos;
import Utils.HiloServidorChat;

import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;

public class ServidorChat  {
	private static final int MAXIMO = 5;
	
	public static void main(String args[]) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, KeyManagementException {

        final int PUERTO = 44444;

        //Definir el fichero almacén que contiene el certificado y la clave //para acceder a  él
        FileInputStream ficAlmacen = new FileInputStream("src/Certificados/AlmacenSrv2");
        String claveAlmacen = "1234567";

        //Cargar en un KeyStore el almacén que contiene el certificado
        KeyStore almacen = KeyStore.getInstance(KeyStore.getDefaultType());
        almacen.load(ficAlmacen, claveAlmacen.toCharArray());

        //Crear el gestor de claves a partir del objeto KeyStore e //inicializarlo con la clave del almacén
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(almacen, claveAlmacen.toCharArray());

        //Creación del contexto con soporte TLS
        SSLContext contextoSSL = SSLContext.getInstance("TLS");
        contextoSSL.init(kmf.getKeyManagers(), null, null);

        SSLServerSocketFactory sfact = contextoSSL.getServerSocketFactory();
        SSLServerSocket servidorSSL = (SSLServerSocket) sfact.createServerSocket(PUERTO);


        DataInputStream flujoEntrada = null;//FLUJO DE ENTRADA DE CLIENTE
        DataOutputStream flujoSalida = null;//FLUJO DE SALIDA AL CLIENTE

        System.out.println("Servidor iniciado...");

        SSLSocket tabla[] = new SSLSocket[MAXIMO];
        ComunHilos comun = new ComunHilos(0, 0, tabla);

            try {

            while (comun.getConexiones() < MAXIMO) {
                SSLSocket clienteConectado = null;

                    clienteConectado = (SSLSocket) servidorSSL.accept();
                    comun.addTabla(clienteConectado, comun.getConexiones());
                    comun.setActuales(comun.getActuales() + 1);
                    comun.setConexiones(comun.getConexiones() + 1);

                    HiloServidorChat hilo = new HiloServidorChat(clienteConectado, comun);
                    hilo.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        comun.guardarMensajes();
        servidorSSL.close();

	}
}

