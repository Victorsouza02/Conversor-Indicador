/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package conversorformato.controllers;

import conversorformato.main.Principal;
import conversorformato.models.Serial;
import conversorformato.models.Threads;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javax.swing.JOptionPane;
import jssc.SerialPortException;

/**
 * FXML Controller class
 *
 * @author Desenvolvimento
 */
public class TelaPrincipalController implements Initializable {

    @FXML
    private ComboBox cb_equip;
    @FXML
    private ComboBox cb_porta_origem;
    @FXML
    private ComboBox cb_formato;
    @FXML
    private ComboBox cb_porta_destino;
    @FXML
    private Button iniciar;
    @FXML
    private Button parar;
    
    Thread serialThread;

    
    boolean primeiroInicio = true;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        atribuirValores();
        eventos();
        serialThread = new Thread(acoesSerial);
    }

    private void atribuirValores(){
        //CARREGA COMBOBOX COM DADOS DE PORTAS E EQUIPAMENTOS DISPONIVEIS
        parar.setDisable(true);
        cb_porta_origem.setValue(Serial.listaPortas().get(0));
        cb_porta_destino.setValue(Serial.listaPortas().get(0));
        cb_formato.setValue("WT1000N");
        cb_equip.setValue("3101C");
        cb_porta_origem.setItems(FXCollections.observableArrayList(Serial.listaPortas()));
        cb_porta_destino.setItems(FXCollections.observableArrayList(Serial.listaPortas()));
        cb_equip.setItems(FXCollections.observableArrayList("3101C", "WT1000N", "WT27"));
        cb_formato.setItems(FXCollections.observableArrayList("WT1000N"));
    }
    
    private void eventos(){
        iniciar.setOnMouseClicked((event)-> {
            boolean ok = true;
            Principal.setSerial(new Serial(cb_porta_origem.getValue().toString(), cb_porta_destino.getValue().toString(), cb_equip.getValue().toString()));
            try{
                Principal.getSerial().conectarPorta();
                Principal.getSerial().conectarPortaEmulada();
            } catch (SerialPortException e){
                if(e.getPortName().equals(cb_porta_origem.getValue().toString())){
                    JOptionPane.showMessageDialog(null, "Falha ao abrir porta origem " + e.getPortName()+ " - Não existe ou está ocupada");
                }else {
                    Principal.getSerial().fecharPorta();
                    JOptionPane.showMessageDialog(null, "Falha ao abrir porta destino " + e.getPortName()+ " - Não existe ou está ocupada");
                }
                ok = false;
            }
            
            if(ok){
                Principal.setIniciado(true);
                if(primeiroInicio){
                    serialThread.start();
                    primeiroInicio = false;
                }
                iniciar.setDisable(true);
                parar.setDisable(false);
            }
        });
        
        parar.setOnMouseClicked((event)-> {
            Principal.setIniciado(false);
            Principal.getSerial().fecharPorta();
            Principal.getSerial().fecharPortaEmulada();
            iniciar.setDisable(false);
            parar.setDisable(true);
        });
    }
    
    
    private Runnable acoesSerial = new Runnable() { //INICIA THREAD LEITURA SERIAL
        public void run() {
            Threads th = new Threads();
            th.threadSerial();
        }
    };
    
}
