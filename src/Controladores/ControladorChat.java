package Controladores;

import Modelo.Modelo;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ResourceBundle;

public class ControladorChat implements Initializable {

    @FXML
    private JFXButton enviar, salir;
    @FXML
    private JFXTextField mensaje;
    @FXML
    private JFXTextArea textarea;

    private Modelo m;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        String nombre = (String) resources.getObject("nombre");

        enviar.setOnAction(new Acciones());
        salir.setOnAction(new Acciones());

        int puerto = 44444;

        try {

            FileInputStream ficCerConf = new FileInputStream("src/Certificados/AlmacenSrv2");
            String claveCerConf = "1234567";

            KeyStore almacenConf = KeyStore.getInstance(KeyStore.getDefaultType());
            almacenConf.load(ficCerConf, claveCerConf.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(almacenConf);

            SSLContext contextoSSL = SSLContext.getInstance("TLS");
            contextoSSL.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory sfact = contextoSSL.getSocketFactory();
            SSLSocket Cliente;

            if (nombre.trim().length() == 0) {
                System.err.println("El nombre esta vacio....");
                return;
            }else if(!nombre.matches("[A-ZÑÁÉÍÓÚ][a-zñáéíóúü]*")) {
                System.err.println("El nombre tiene que comenzar por mayuscula (no se admiten segundos nombres)");
                return;
            }
            try {

                Cliente = (SSLSocket) sfact.createSocket("localhost", puerto);
                m = new Modelo(nombre,Cliente,textarea);
                new Thread(m).start();

            } catch (IOException e) {
                Alert dialogoError = new Alert(Alert.AlertType.ERROR);
                dialogoError.setTitle("ERROR!");
                dialogoError.setHeaderText("IMPOSIBLE CONECTAR CON EL SERVIDOR");
                dialogoError.setContentText(e.getMessage());
                dialogoError.showAndWait();
                ((Stage) textarea.getScene().getWindow()).close();
            }

        }catch (Exception e){
            e.printStackTrace();
        }


    }

    class Acciones implements EventHandler<ActionEvent>{

        @Override
        public void handle(ActionEvent event) {

            if (m == null)
                ((Stage)textarea.getScene().getWindow()).close();
            else {

                if (event.getSource() == salir)
                    m.salir();
                else {
                    m.mandarMensaje(mensaje);
                }
            }
        }
    }
}
