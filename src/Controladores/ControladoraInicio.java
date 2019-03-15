package Controladores;

import Utils.Sesion;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.net.URL;
import java.security.Key;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class ControladoraInicio implements Initializable {

    @FXML
    JFXTextField tfs,tfp,tfu,tfn;
    @FXML
    JFXPasswordField pfp;
    @FXML
    JFXCheckBox cbr;
    @FXML
    JFXButton login, entrar;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        login.setOnAction(new Botones());
        entrar.setOnAction(new Botones());
        cbr.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !(new File("src/Recursos/Sesion.cif").exists())){
                if (!tfu.getText().isEmpty() && !pfp.getText().isEmpty()) {
                    File f = new File("src/Recursos/Sesion.obj");
                    escribirObjeto(f);
                    cifrarFichero(f);
                }
            }else {
                File f = new File("src/Recursos/Sesion.cif");
                File f2 = new File("src/Recursos/Sesion.obj");
                f.delete();
                f2.delete();
            }
        });
        if ((new File("src/Recursos/Sesion.cif").exists())){
            descifrarFichero();
            leerObjetos(new File("src/Recursos/Sesion.obj"));
            cifrarFichero(new File("src/Recursos/Sesion.obj"));
        }

    tfs.setOnKeyReleased(new KeyListener());
    tfp.setOnKeyReleased(new KeyListener());
    tfu.setOnKeyReleased(new KeyListener());
    pfp.setOnKeyReleased(new KeyListener());

    }

    class KeyListener implements EventHandler<KeyEvent>{

        @Override
        public void handle(KeyEvent event) {
            cbr.setSelected(false);
        }
    }

    private void descifrarFichero() {
        try {
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream("src/Certificados/Clave.secreta"));
            Key clavesecreta = (Key) oin.readObject();
            oin.close();
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, clavesecreta);

            CipherInputStream in = new CipherInputStream(new FileInputStream("src/Recursos/Sesion.cif"), c);

            int tambloque = c.getBlockSize();
            byte[] bytes = new byte[tambloque];

            int i = in.read(bytes);
            FileOutputStream fileout = new FileOutputStream("src/Recursos/Sesion.obj");

            while (i != -1)	{
                fileout.write(bytes, 0, i);
                i = in.read(bytes);
            }
            fileout.close();
            in.close();
            System.out.println("Fichero descifrado con clave secreta.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cifrarFichero(File f) {

        try {
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream("src/Certificados/Clave.secreta"));
            Key clavesecreta = (Key) oin.readObject();
            oin.close();
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, clavesecreta);
            FileInputStream filein = new FileInputStream(f.getPath());

            CipherOutputStream out = new CipherOutputStream(new FileOutputStream("src/Recursos/Sesion.cif"), c);
            int tambloque = c.getBlockSize();
            byte[] bytes = new byte[tambloque];

            int i = filein.read(bytes);
            while (i != -1) {
                out.write(bytes, 0, i);
                i = filein.read(bytes);
            }
            out.flush();
            out.close();
            filein.close();
            System.out.println("Fichero cifrado con clave secreta.");
            f.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void escribirObjeto(File f){

        ObjectOutputStream oos = null;

        System.out.println("fichero guardado");

        try {
            oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(new Sesion(tfu.getText(),pfp.getText(),tfs.getText(),tfp.getText(),true));
        } catch (IOException e) {
            System.out.println("no se pudo exportar el objeto");
            e.printStackTrace();
        }
        finally {
            try {
                if (oos != null)
                    oos.close();
            } catch (IOException e) {
                System.out.println("no se pudo cerrar correctamente la exportacion");
                e.printStackTrace();
            }
        }

    }

    private void leerObjetos(File f){

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Sesion sesionrecuperada = (Sesion) ois.readObject();
            if (sesionrecuperada.isEstado()) {
                tfu.setText(sesionrecuperada.getNombre());
                pfp.setText(sesionrecuperada.getPass());
                tfs.setText(sesionrecuperada.getServidor());
                tfp.setText(sesionrecuperada.getPuerto());
                cbr.setSelected(sesionrecuperada.isEstado());
            }

        } catch (IOException | ClassNotFoundException e) {
            //System.out.println("no se pudo importar el objeto");
            //e.printStackTrace();
        }

    }

    class Botones implements EventHandler<ActionEvent>{

        @Override
        public void handle(ActionEvent event) {
            FXMLLoader loader;
            if (event.getSource() == login){
                loader = new FXMLLoader(getClass().getResource("../Vistas/FTP.fxml"));
                try {
                    loader.setResources(new ResourceBundle() {
                        @Override
                        protected Object handleGetObject(String key) {
                            switch (key){
                                case "user":
                                    return tfu.getText();
                                case "pass":
                                    return pfp.getText();
                                case "ser":
                                    return tfs.getText();
                                case "port":
                                    if (tfp.getText().isEmpty())
                                        return -1;
                                    else
                                        return Integer.parseInt(tfp.getText());
                            }
                            return null;
                        }

                        @Override
                        public Enumeration<String> getKeys() {
                            return null;
                        }
                    });
                    Parent parent = loader.load();
                    Stage stage = new Stage();
                    Scene scene = new Scene(parent,600,700);
                    stage.setScene(scene);
                    stage.setTitle("Proyecto PSP 2ª Evaluación FTP");
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                loader = new FXMLLoader(getClass().getResource("../Vistas/Chat.fxml"));
                try {
                    loader.setResources(new ResourceBundle() {
                        @Override
                        protected Object handleGetObject(String key) {
                            switch (key){
                                case "nombre":
                                    return tfn.getText();
                            }
                            return null;
                        }

                        @Override
                        public Enumeration<String> getKeys() {
                            return null;
                        }
                    });
                    Parent parent = loader.load();
                    Stage stage = new Stage();
                    Scene scene = new Scene(parent,500,400);
                    stage.initStyle(StageStyle.UNDECORATED);
                    stage.setScene(scene);
                    stage.setTitle("Proyecto PSP 2ª Evaluación Chat");
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (login.getScene().getWindow() != null)
                ((Stage)login.getScene().getWindow()).close();
        }
    }

}
