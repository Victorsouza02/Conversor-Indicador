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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private ComboBox cb_baud;
    @FXML
    private ComboBox cb_datasize;
    @FXML
    private ComboBox cb_parity;
    @FXML
    private ComboBox cb_stopbit;
    @FXML
    private Button iniciar;
    @FXML
    private Button parar;
    @FXML
    private ImageView imagem;

    Thread serialThread;

    boolean primeiroInicio;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        primeiroInicio = true;
        atribuirValores();
        eventos();
        serialThread = new Thread(acoesSerial);
    }

    private void atribuirValores() {
        //ATRIBUIÇÃO DE VALORES E ATRIBUTOS NOS ELEMENTOS
        Image img = new Image(Principal.class.getResourceAsStream("/conversorformato/imgs/logoebm.png"));
        imagem.setImage(img);
        parar.setDisable(true);
        cb_porta_origem.setValue(Serial.listaPortas().get(0));
        cb_porta_destino.setValue(Serial.listaPortas().get(0));
        cb_formato.setValue("WT1000N");
        cb_equip.setValue("3101C");
        cb_baud.setValue("9600");
        cb_datasize.setValue("8");
        cb_parity.setValue("none");
        cb_stopbit.setValue("1");
        cb_porta_origem.setItems(FXCollections.observableArrayList(Serial.listaPortas()));
        cb_porta_destino.setItems(FXCollections.observableArrayList(Serial.listaPortas()));
        cb_equip.setItems(FXCollections.observableArrayList("3101C", "WT1000N", "WT27"));
        cb_formato.setItems(FXCollections.observableArrayList("WT1000N"));
        cb_baud.setItems(FXCollections.observableArrayList("4800", "9600", "19200", "38400", "56000", "57600", "115200"));
        cb_datasize.setItems(FXCollections.observableArrayList("7", "8"));
        cb_parity.setItems(FXCollections.observableArrayList("none", "even", "odd"));
        cb_stopbit.setItems(FXCollections.observableArrayList("1", "2"));

    }

    private void eventos() {
        //Ao clicar no botao de iniciar
        iniciar.setOnMouseClicked((event) -> {
            boolean ok = true;
            //Instancia novo serial com os valores atuais
            Principal.setSerial(new Serial(
                cb_porta_origem.getValue().toString(),
                cb_porta_destino.getValue().toString(),
                cb_equip.getValue().toString(),
                cb_baud.getValue().toString(),
                cb_parity.getValue().toString(),
                cb_datasize.getValue().toString(),
                cb_stopbit.getValue().toString()
            ));
            try { //Tenta se conectar com as duas portas informadas
                Principal.getSerial().conectarPorta();
                Principal.getSerial().conectarPortaEmulada();
            } catch (SerialPortException e) { //Se houver erro na conexão
                if (e.getPortName().equals(cb_porta_origem.getValue().toString())) {
                    //Exibe mensagem de erro
                    JOptionPane.showMessageDialog(null, "Falha ao abrir porta origem " + e.getPortName() + " - Não existe ou está ocupada");
                } else {
                    //Fecha a porta que foi aberta com sucesso anteriormente e exibe mensagem de erro.
                    Principal.getSerial().fecharPorta();
                    JOptionPane.showMessageDialog(null, "Falha ao abrir porta destino " + e.getPortName() + " - Não existe ou está ocupada");
                }
                ok = false; //Sinaliza que houve erro ao abrir portas
            }

            if (ok) { // Se não houver erros ao abrir portas
                Principal.setIniciado(true); // Indica que o procedimento de conversão foi iniciado
                if (primeiroInicio) { //Se for o primeiro inicio do procedimento
                    serialThread.start(); // Inicia a thread de conversão na serial
                    primeiroInicio = false; //Indica que o procedimento já foi iniciado
                }
                iniciar.setDisable(true); //Desativa botão de iniciar
                parar.setDisable(false); //Ativa botão de parar
            }
        });

        //AO CLICAR NO BOTÃO PARAR
        parar.setOnMouseClicked((event) -> {
            Principal.setIniciado(false); //Indica que o processo de conversão foi interrompido
            //Fecha as portas anteriormente abertas
            Principal.getSerial().fecharPorta();
            Principal.getSerial().fecharPortaEmulada();
            iniciar.setDisable(false); //Ativa botão de iniciar
            parar.setDisable(true); //Desativa botão de parar
        });
    }

    private Runnable acoesSerial = new Runnable() { //INICIA THREAD SERIAL
        public void run() {
            Threads th = new Threads();
            th.threadSerial();
        }
    };

}
