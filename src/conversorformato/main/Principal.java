/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package conversorformato.main;

import conversorformato.models.Serial;
import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Desenvolvimento
 */
public class Principal extends Application {

    private static Serial serial;

    private static String peso_bru;
    private static String peso_liq;
    private static String tara;
    private static String estabilidade;
    private static boolean iniciado;
    private static boolean threadsLigadas = false;

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/conversorformato/views/telaPrincipal.fxml"));
            Scene scene = new Scene(root, 464, 411);
            primaryStage.setTitle("Conversor de Protocolo de Comunicação - Serial");
            primaryStage.getIcons().addAll(new Image(Principal.class.getResourceAsStream("/conversorformato/imgs/icone.png")));
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent arg0) {
                    System.exit(0);
                }
            });
            primaryStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public static String getPeso_bru() {
        return peso_bru;
    }

    public static void setPeso_bru(String peso_bru) {
        Principal.peso_bru = peso_bru;
    }

    public static String getPeso_liq() {
        return peso_liq;
    }

    public static void setPeso_liq(String peso_liq) {
        Principal.peso_liq = peso_liq;
    }

    public static String getTara() {
        return tara;
    }

    public static void setTara(String tara) {
        Principal.tara = tara;
    }

    public static String getEstabilidade() {
        return estabilidade;
    }

    public static void setEstabilidade(String estabilidade) {
        Principal.estabilidade = estabilidade;
    }

    public static Serial getSerial() {
        return serial;
    }

    public static void setSerial(Serial serial) {
        Principal.serial = serial;
    }

    public static boolean isIniciado() {
        return iniciado;
    }

    public static void setIniciado(boolean iniciado) {
        Principal.iniciado = iniciado;
    }

    public static boolean isThreadsLigadas() {
        return threadsLigadas;
    }

    public static void setThreadsLigadas(boolean threadsLigadas) {
        Principal.threadsLigadas = threadsLigadas;
    }

}
