package Controladores;

import Modelo.Modelo;
import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class ControladorMail implements Initializable {

    @FXML
    private JFXTextField tfu, tfp, tfc;
    @FXML
    private JFXPasswordField pfp;
    @FXML
    private JFXCheckBox cbg;
    @FXML
    private JFXTextArea tac;
    @FXML
    private JFXButton btne, btnc;

    private Modelo m;

    private String host;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        btne.setOnAction(new Acciones());
        btnc.setOnAction(new Acciones());

        host = "localhost";

        cbg.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue){
                host = "smtp.gmail.com";
            }else {
                host = "localhost";
            }
        });

    }

    class Acciones implements EventHandler<ActionEvent>{

        @Override
        public void handle(ActionEvent event) {
            if (event.getSource() == btne){
                m.mandarFichero(host,tfu.getText(),pfp.getText(),tfp.getText(),tac.getText(),tfc.getText());
            }
            ((Stage)btnc.getScene().getWindow()).close();
        }
    }

    public void setModelo(Modelo m){
        this.m = m;
    }

}
