/*
   * CLASSE : LerSerial
   * FUNÇÃO : Tudo relacionado a leitura dos dados seriais e conversão de acordo com o equipamento.
 */
package conversorformato.models;

import conversorformato.main.Principal;
import conversorformato.utils.Formatacao;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 *
 * @author Desenvolvimento
 */
public class Serial {

    private byte[] buffer;
    private static SerialPort serialPort;
    private static SerialPort serialPort_emu;
    private String port;
    private String port_emu;
    private String equipamento;
    private int baud;
    private int databits;
    private int stopbit;
    private int parity;
    private int total_bytes;
    private static boolean ok = false;
    public static List<String> portas = new ArrayList<String>();
    public static List<String> equipamentos = new ArrayList<String>();

    public Serial(String porta, String porta_emulada, String equipamento) {
        // AO INICIAR A CLASSE
        setPort(porta); //PEGAR PORTA
        setEquipamento(equipamento); //PEGAR EQUIPAMENTO
        setPort_emu(porta_emulada);
        selecionarConfigEquipamento(); //SELECIONA CONFIGURACAO DE ACORDO COM EQUIPAMENTO
    }

    public void conectarPorta() throws SerialPortException {
        serialPort = new SerialPort(getPort());
        serialPort.openPort();
        serialPort.setParams(baud, databits, stopbit, parity, false, false);
        serialPort.addEventListener(new SerialPortReader()); //ESCUTANDO EVENTOS DA PORTA SERIAL

    }

    public void conectarPortaEmulada() throws SerialPortException {
        serialPort_emu = new SerialPort(getPort_emu());
        serialPort_emu.openPort();
        serialPort_emu.setParams(baud, databits, stopbit, parity, false, false);
    }

    public void fecharPorta() {
        try {
            serialPort.closePort();
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }
    
    public void fecharPortas() {
        try {
            serialPort.closePort();
            serialPort_emu.closePort();
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    public void fecharPortaEmulada() {
        try {
            serialPort_emu.closePort();
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    public String lerLinha() {
        String linha = "";
        boolean ler = true;
        while (ler == true) {
            try {
                byte buffer[] = serialPort.readBytes(1);
                byte b = buffer[0];
                char c = (char) b;
                linha += c;
                if (c == '\n') {
                    ler = false;
                }
            } catch (SerialPortException ex) {
                ler = true;
            }
        }
        return linha;
    }

    public void escreverLinha() {
        try {
            serialPort_emu.writeString(Formatacao.formatarParaWT1000N(Principal.getPeso_bru(), Principal.getPeso_liq(), Principal.getTara(), Principal.getEstabilidade()));
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    //RETORNA DADOS DA SERIAL
    public String receberDadosSerial() {
        if (ok == true) {
            return lerLinha();
        }
        return padraoString();
    }

    //PEGA AS PORTAS DISPONIVEIS E ADICIONA A UMA LISTA DE PORTAS
    public static List<String> listaPortas() {
        List<String> portas = new ArrayList<String>();
        String[] portNames = SerialPortList.getPortNames();
        for (int i = 0; i < portNames.length; i++) {
            portas.add(portNames[i]);
        }
        return portas;
    }

    //PEGA OS EQUIPAMENTOS COMPATIVEIS E ADICIONA A UMA LISTA DE EQUIPAMENTOS
    public static List<String> listaEquipamentos() {
        List<String> equipamentos = new ArrayList<String>();
        equipamentos.add("WT1000N"); //INDICADOR WEIGHTECH WT1000N
        equipamentos.add("3101C"); // INDICADOR ALFA LINHA 3100
        equipamentos.add("WT27"); // INDICADOR AWEIGHTECH WT27

        return equipamentos;
    }

    //SELEÇÃO DE CONFIGURAÇÃO DE EQUIPAMENTO
    public void selecionarConfigEquipamento() {
        switch (equipamento) {
            case "WT1000N":
                configWT1000N();
                break;
            case "3101C":
                config3101C();
                break;  
            case "WT27":
                configWT1000N();
                break;
        }
    }

    //PADRAO DE STRING PARA CADA EQUIPAMENTO
    public String padraoString() {
        String padrao = null;
        switch (equipamento) {
            case "WT1000N":
                padrao = "0,0000000,0000000,0000000";
                break;
            case "3101C":
                padrao = "PB: 00000 T: 00000";
                break;
            case "WT27":
                padrao = "EB,B: 000000,T:000000,L: 000000";
                break;
        }
        return padrao;
    }

    //SELECIONA O TIPO DE FORMATAÇÃO DE ACORDO COM O EQUIPAMENTO
    public Map<String, String> selecionarDadosEquipamento() {
        switch (equipamento) {
            case "WT1000N":
                return Formatacao.formatarDadosWT1000N(receberDadosSerial());
            case "3101C":
                return Formatacao.formatarDados3101C(receberDadosSerial());
            case "WT27":
                return Formatacao.formatarDadosWT27(receberDadosSerial());
        }
        return null;
    }

    /**
     * *******CONFIGURAÇÃO DE COMUNICAÇÃO ALFA 3101C****
     */
    public void config3101C() {
        setBaud(9600);
        setDatabits(8);
        setStopbit(1);
        setParity(0);
    }

    /**
     * *****************************************
     */

    /**********CONFIGURAÇÃO DE COMUNICAÇÃO WEIGHTECH WT1000N*******/
    public void configWT1000N() {
        setBaud(9600);
        setDatabits(8);
        setStopbit(1);
        setParity(0);
    }

    /***********************************************/
    
    /**********CONFIGURAÇÃO DE COMUNICAÇÃO WEIGHTECH WT27*******/
    public void configWT27() {
        setBaud(9600);
        setDatabits(8);
        setStopbit(1);
        setParity(0);
    }
    /***********************************************/
    

    //OUVINDO PORTA SERIAL E RETIRANDO DADOS DESNECESSÁRIOS
    static class SerialPortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() > 0) {
                while (ok == false) {
                    try {
                        byte buffer[] = serialPort.readBytes(1);
                        byte b = buffer[0];
                        char c = (char) b;
                        if (c == '\n') {
                            ok = true;
                        }
                    } catch (SerialPortException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    // GETTERS/SETTERS
    public static boolean isOk() {
        return ok;
    }

    public static void setOk(boolean ok) {
        Serial.ok = ok;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public int getBaud() {
        return baud;
    }

    public void setBaud(int baud) {
        this.baud = baud;
    }

    public int getDatabits() {
        return databits;
    }

    public void setDatabits(int databits) {
        this.databits = databits;
    }

    public int getStopbit() {
        return stopbit;
    }

    public void setStopbit(int stopbit) {
        this.stopbit = stopbit;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getTotal_bytes() {
        return total_bytes;
    }

    public void setTotal_bytes(int total_bytes) {
        this.total_bytes = total_bytes;
    }

    public String getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(String equipamento) {
        this.equipamento = equipamento;
    }

    public String getPort_emu() {
        return port_emu;
    }

    public void setPort_emu(String port_emu) {
        this.port_emu = port_emu;
    }

}
