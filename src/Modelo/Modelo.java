package Modelo;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.Optional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

public class Modelo implements Runnable, Serializable{

    private ObservableList<String> modelolistafx;
    private FTPClient cliente;
    private String servidor;
    private int port;
    private String direcInicial;
    private String direcSelec;
    private String ficheroSelec;
    private String user;
    private SSLSocket socket;
    private DataInputStream fentrada;
    private DataOutputStream fsalida;
    private boolean repetir = true;
    private JFXTextArea textarea;

    public Modelo(String user, String pass, String ser, int port) {
        instancias(ser,port);
        conectar(user,pass);
    }
    public Modelo(String nombre, SSLSocket s, JFXTextArea textarea){

        socket = s;
        user = nombre;
        this.textarea = textarea;
        try {
            fentrada = new DataInputStream(socket.getInputStream());
            fsalida = new DataOutputStream(socket.getOutputStream());
            String texto = "Entra en el Chat ... " + nombre;
            fsalida.writeUTF(texto);
        } catch (IOException e) {
            System.out.println("ERROR DE E/S");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void mandarFichero(String host,String user, String pass, String receptor, String cuerpo, String cabecera){

        String directorio=direcSelec;
        if (!direcSelec.equals("/"))
            directorio =directorio +"/";
        if(!ficheroSelec.equals("")) {

            File file = new File(directorio + ficheroSelec);
            Properties properties = new Properties();
            properties.put("mail.smtp.host", host);
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.port", "587");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.user", user);
            properties.put("mail.password", pass);
            Session session = Session.getInstance(properties, null);
            MimeMessage mimeMessage = new MimeMessage(session);

            try {
                MimeMultipart mimeMultipart = new MimeMultipart();
                MimeBodyPart mimeBodyPartTexto = new MimeBodyPart();
                mimeBodyPartTexto.setText(cuerpo);
                MimeBodyPart mimeBodyPartAdjunto = new MimeBodyPart();
                mimeBodyPartAdjunto.attachFile(file);
                mimeMultipart.addBodyPart(mimeBodyPartAdjunto);
                mimeMultipart.addBodyPart(mimeBodyPartTexto);
                mimeMessage.setContent(mimeMultipart);
                mimeMessage.setFrom(new InternetAddress(properties.getProperty("mail.user")));
                mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(receptor));
                mimeMessage.setSubject(cabecera);
                Transport transport = session.getTransport("smtp");
                transport.connect(properties.getProperty("mail.user"), properties.getProperty("mail.password"));
                transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
                transport.close();
                lanzarDialogo(Alert.AlertType.INFORMATION, "Success!","Fichero Mandado!",
                        ficheroSelec + " Se ha enviado por mail a usu2@pruebas del servidor "+properties.getProperty("mail.smtp.host"));
            }catch (MessagingException | IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void subirFichero(Stage stage,Label directorio, Label directorioactual, ListView<String> listadirec){

        FileChooser f = new FileChooser();
        f.setTitle("Selecciona el Fichero a SUBIR AL SERVIDOR FTP");
        File file = f.showOpenDialog(stage);
        if (file != null) {
            String archivo = file.getAbsolutePath();
            String nombreArchivo = file.getName();
            try {
                System.out.println("Archivo : " +archivo);

                cliente.setFileType(FTP.BINARY_FILE_TYPE);
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(archivo));
                cliente.changeWorkingDirectory(direcSelec);
                if (cliente.storeFile(nombreArchivo, in)) {
                    String s = "  " + nombreArchivo + " ha sido subido correctamente... ";
                    directorio.setText(s);
                    directorioactual.setText("Se va a actualizar el Árbol de directorios...");
                    lanzarDialogo(Alert.AlertType.INFORMATION, "Success!","Fichero Subido!",s);
                    FTPFile[] ff2 = cliente.listFiles();
                    llenarLista(ff2, direcSelec, listadirec);
                } else
                    directorio.setText("No se ha podido subir... " + nombreArchivo);
            }catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void borrarFichero(Label d, ListView<String> listadirec){
        String directorio=direcSelec;
        if (!direcSelec.equals("/"))
            directorio =directorio +"/";
        if(!ficheroSelec.equals("")) {
            Alert dialogoConfirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            dialogoConfirmacion.setTitle("Borrar");
            dialogoConfirmacion.setHeaderText("Borrado de Fichero");
            dialogoConfirmacion.setContentText("¿Desea eliminar el fichero seleccionado?");
            Optional<ButtonType> opciones = dialogoConfirmacion.showAndWait();
            if (opciones.isPresent()) {
                if (opciones.get() == ButtonType.OK) {
                    try {
                        if (cliente.deleteFile(directorio + ficheroSelec)) {
                            String m = ficheroSelec + " ha sido eliminado correctamente... ";
                            lanzarDialogo(Alert.AlertType.INFORMATION, "Success!","Borrado completado!",m);
                            d.setText(m);
                            cliente.changeWorkingDirectory(direcSelec);
                            FTPFile[] ff2 = cliente.listFiles();
                            llenarLista(ff2, direcSelec, listadirec);
                        } else
                            lanzarDialogo(Alert.AlertType.ERROR, "Success!","Borrado fallido!",ficheroSelec + " No se ha podido eliminar ...");
                    }catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public void descargarFichero(Stage stage){

        String directorio=direcSelec;
        if (!direcSelec.equals("/"))
            directorio =directorio +"/";
        if(!ficheroSelec.equals(""))
        {
            String archivoyCarpetaDestino;
            String carpetaDestino;
            FileChooser f = new FileChooser();
            f.setTitle("Selecciona el Directorio donde DESCARGAR el fichero");
            File file = f.showOpenDialog(stage);
            if (file != null) {

                carpetaDestino = (file.getAbsolutePath());
                archivoyCarpetaDestino = carpetaDestino + File.separator + ficheroSelec;

                try {
                    cliente.setFileType(FTP.BINARY_FILE_TYPE);
                    BufferedOutputStream out;
                    out = new BufferedOutputStream(new FileOutputStream(archivoyCarpetaDestino));

                    if (cliente.retrieveFile(directorio + ficheroSelec, out))
                        lanzarDialogo(Alert.AlertType.INFORMATION, "Success!","Descarga completada!",ficheroSelec + " ha sido descargado correctamente ...");
                    else
                        lanzarDialogo(Alert.AlertType.ERROR, "Success!","Descarga fallida!",ficheroSelec + " No se ha podido descargar ...");
                    out.close();
                }catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void borrarCarpeta(Label d, ListView<String> listadirec){
        TextInputDialog dialogoTexto = new TextInputDialog("Nombre de la carpeta");
        dialogoTexto.setTitle("Carpeta");
        dialogoTexto.setHeaderText("Borrado de Carpeta");
        dialogoTexto.setContentText("Introduce el nombre del directorio");
        Optional<String> texto = dialogoTexto.showAndWait();
        String nombreCarpeta = null;
        if (texto.isPresent()) {
            nombreCarpeta = texto.get();
        }
        if (!(nombreCarpeta == null)) {
            String directorio = direcSelec;
            if (!direcSelec.equals("/"))
                directorio = directorio +"/";

            directorio += nombreCarpeta.trim() ;

            try {
                if (cliente.removeDirectory(directorio)) {
                    String m  =  nombreCarpeta.trim()	+ "  ha sido borrado correctamente ...";
                    lanzarDialogo(Alert.AlertType.INFORMATION, "Success!","Borrado completado!",m);
                    d.setText(m);
                    cliente.changeWorkingDirectory(direcSelec);
                    FTPFile[] ff2 = cliente.listFiles();
                    llenarLista(ff2, direcSelec, listadirec);
                }else
                    lanzarDialogo(Alert.AlertType.ERROR, "Error!","Borrado fallado",nombreCarpeta.trim() + " No se ha podido borrar ...");
            }catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void crearCarpeta(Label d, ListView<String> listadirec){

        TextInputDialog dialogoTexto = new TextInputDialog("Nombre de la carpeta");
        dialogoTexto.setTitle("Carpeta");
        dialogoTexto.setHeaderText("Creacion de Carpeta");
        dialogoTexto.setContentText("Introduce el nombre del directorio");
        Optional<String> texto = dialogoTexto.showAndWait();
        String nombreCarpeta = null;

        if (texto.isPresent()) {
             nombreCarpeta = texto.get();
        }
        if (!(nombreCarpeta == null)) {
            String directorio = direcSelec;
            if (!direcSelec.equals("/"))
                directorio = directorio +"/";

            directorio += nombreCarpeta.trim() ;

            try {
                if (cliente.makeDirectory(directorio)) {
                    String m  =  nombreCarpeta.trim()	+ "  ha sido creado correctamente ...";
                    lanzarDialogo(Alert.AlertType.INFORMATION, "Success!","Creacion completada!",m);
                    d.setText(m);
                    cliente.changeWorkingDirectory(direcSelec);
                    FTPFile[] ff2 = cliente.listFiles();
                    llenarLista(ff2, direcSelec, listadirec);
                }else
                    lanzarDialogo(Alert.AlertType.ERROR, "Error!","Creacion Fallada!",nombreCarpeta.trim() + " No se ha podido crear ...");
            }catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void desconectar(){
        try {
            cliente.disconnect();
        }catch (IOException e1) {
            e1.printStackTrace();
        }
        System.exit(0);
    }

    public void cambiarDatos(ListView<String> listadirec, Label directorio, Label directorioactual){
        ficheroSelec = "";

        String fic = listadirec.getSelectionModel().getSelectedItem();

        if (listadirec.getSelectionModel().getSelectedIndex() == 0) {
            if (!fic.equals(direcInicial)) {
                try {
                    cliente.changeToParentDirectory();
                    direcSelec = cliente.printWorkingDirectory();
                    cliente.changeWorkingDirectory(direcSelec);
                    FTPFile[] ff2 = cliente.listFiles();
                    directorio.setText("");
                    llenarLista(ff2, direcSelec, listadirec);
                }catch (IOException e) {e.printStackTrace();}
            }
        }else {
            if (fic.substring(0, 6).equals("(DIR) ")) {
                try {
                    fic = fic.substring(6);
                    String direcSelec2;
                    if (direcSelec.equals("/"))
                        direcSelec2 = direcSelec + fic;
                    else
                        direcSelec2=direcSelec + "/" + fic;
                    FTPFile[] ff2;
                    cliente.changeWorkingDirectory(direcSelec2);
                    ff2 = cliente.listFiles();
                    directorio.setText("DIRECTORIO:  "+ fic + ", " + ff2.length + " elementos");
                    direcSelec = direcSelec2;
                    llenarLista(ff2, direcSelec, listadirec);
                }catch (IOException e2) {e2.printStackTrace();}
            }else {
                ficheroSelec = direcSelec;
                if (direcSelec.equals("/"))
                    ficheroSelec += fic;
                else
                    ficheroSelec += "/" + fic;
                directorio.setText("FICHERO seleccionado:" +
                        ficheroSelec);
                ficheroSelec = fic;
            }
        }
        directorioactual.setText("DIRECTORIO ACTUAL: " + direcSelec);
    }


    public void rellenarDatos(ListView<String> listadirec, Label s, Label usuario, Label directorioraiz, Label directorio, Label directorioactual) {
        try {
            cliente.changeWorkingDirectory(direcInicial);
            FTPFile[] files = cliente.listFiles();
            llenarLista(files, direcInicial, listadirec);

            directorio.setText("<<   ARBOL DE DIRECTORIOS CONSTRUIDO    >>");
            directorioactual.setText("");

            s.setText("Servidor FTP: "+servidor);
            usuario.setText("Usuario: "+ user);
            directorioraiz.setText("DIRECTORIO RAIZ: "+direcInicial);


        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void conectar(String user,String pass) {
        System.out.println("Conectandose a " + servidor);

        cliente.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        try {
            if (port>-1)
                cliente.connect(servidor,port);
            else
                cliente.connect(servidor);
            cliente.enterLocalPassiveMode();
            this.user = user;
            cliente.login(user, pass);
        }catch (IOException e) {
            System.err.println("Error al conectar");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void instancias(String ser,int port) {
        modelolistafx = FXCollections.observableArrayList();
        cliente = new FTPClient();
        servidor = ser;
        this.port = port;
        direcInicial = "/";
        direcSelec = direcInicial;
        ficheroSelec ="";
    }

    private void llenarLista(FTPFile[] files, String direc2, ListView<String> listadirec) {
        if (files == null)	return;

        modelolistafx.clear();

        try {
            cliente.changeWorkingDirectory(direc2);
        }catch (IOException e) {
            e.printStackTrace();
        }
        direcSelec = direc2;
        modelolistafx.add(direc2);
        for (FTPFile file : files) {
            if (!(file.getName()).equals(".") && !(file.getName()).equals("..")) {
                String f = file.getName();
                if (file.isDirectory())
                    f = "(DIR) " + f;
                modelolistafx.add(f);
            }
        }
        try {
            listadirec.setItems(modelolistafx);
        }catch (NullPointerException n) {
            System.out.println("linea 334 - llega al ultimo");
        }
    }

    private void lanzarDialogo(Alert.AlertType tipo, String titulo, String cabecera, String cuerpo){
        Alert dialogoInfo = new Alert(tipo);
        dialogoInfo.setTitle(titulo);
        dialogoInfo.setHeaderText(cabecera);
        dialogoInfo.setContentText(cuerpo);
        dialogoInfo.showAndWait();
    }

    public void mandarMensaje(JFXTextField mensaje){
        if (mensaje.getText().trim().length() == 0)
            return;
        String texto = user + ": " + mensaje.getText();

        try {
            mensaje.setText("");
            fsalida.writeUTF(texto);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void salir(){
        String texto = "Abandona el Chat ... " + user;
        try {
            fsalida.writeUTF(texto);
            fsalida.writeUTF("*");
            repetir = false;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void run() {
        String texto;
        while (repetir) {
            try {
                texto = fentrada.readUTF();
                textarea.setText(texto);
            } catch (IOException e) {
                lanzarDialogo(Alert.AlertType.ERROR,"ERROR!","IMPOSIBLE CONECTAR CON EL SERVIDOR",e.getMessage());
                repetir = false;
                ((Stage) textarea.getScene().getWindow()).close();
            }
        }
        try {
            socket.close();
            System.exit(0);
            ((Stage) textarea.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            ((Stage) textarea.getScene().getWindow()).close();
        }
    }
}