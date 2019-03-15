package Controladores;

import Modelo.Modelo;
import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ControladorFTP implements Initializable {

    @FXML
    Label ls,lu,ldr,ld,lda;

    @FXML
    JFXButton btnsf,btndf,btnef,btncc,btnbc,btnmf,btns;

    @FXML
    ListView<String> lv;

    private String user, pass, ser;
    private int port;

    private Modelo m;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        user = (String) resources.getObject("user");
        pass = (String) resources.getObject("pass");
        ser = (String) resources.getObject("ser");
        port = (int) resources.getObject("port");
        instancias();
        m.rellenarDatos(lv,ls,lu,ldr,ld,lda);
        acciones();
    }

    private void acciones() {
        btnsf.setOnAction(new ManejoBotones());
        btndf.setOnAction(new ManejoBotones());
        btnef.setOnAction(new ManejoBotones());
        btncc.setOnAction(new ManejoBotones());
        btnbc.setOnAction(new ManejoBotones());
        btnmf.setOnAction(new ManejoBotones());
        btns.setOnAction(new ManejoBotones());
        try {
            lv.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> m.cambiarDatos(lv,ld,lda));
        }catch (IndexOutOfBoundsException | NullPointerException ignored){}

    }

    private void instancias() {
        if (port>-1)
            m = new Modelo(user,pass,ser,port);
        else
            m = new Modelo(user,pass,ser,-1);
    }

    class ManejoBotones implements EventHandler<ActionEvent>{

        @Override
        public void handle(ActionEvent event) {
            if (event.getSource() == btnsf){

                m.subirFichero((Stage) btnsf.getScene().getWindow(),ld,lda,lv);

            }else if (event.getSource() == btndf){

                m.descargarFichero((Stage) btndf.getScene().getWindow());

            }else if (event.getSource() == btnef){

                m.borrarFichero(ld,lv);

            }else if (event.getSource() == btncc){

                m.crearCarpeta(ld,lv);

            }else if (event.getSource() == btnbc){

                m.borrarCarpeta(ld,lv);

            }else if (event.getSource() == btnmf){

                FXMLLoader loader = new FXMLLoader(getClass().getResource("../Vistas/Mail.fxml"));
                ControladorMail controladora;
                try {
                    Parent parent = loader.load();
                    Stage stage = new Stage();
                    Scene scene = new Scene(parent,600,400);
                    stage.setScene(scene);
                    controladora = loader.getController();
                    controladora.setModelo(m);
                    stage.setTitle("Mandar mail");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }else if (event.getSource() == btns){

                m.desconectar();

            }
        }
    }

}
