/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package conversorformato.models;

import conversorformato.main.Principal;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;

/**
 *
 * @author Desenvolvimento
 */
public class Threads {

    public void threadSerial() {
        Map<String, String> dados = new HashMap<String, String>();
        //THREAD PARA LEITURA DE SERIAL CONTINUA
        boolean ok = true;
        while (true) {
            ok = Principal.isIniciado();        
            if (Principal.getSerial() != null && ok == true) {
                dados = Principal.getSerial().selecionarDadosEquipamento();
                String codEstabilidade = dados.get("estavel");
                String peso_bru_var = dados.get("peso_bru");
                String peso_liq_var = dados.get("peso_liq");
                String tara_var = dados.get("tara");
                Principal.setPeso_bru(peso_bru_var);
                Principal.setPeso_liq(peso_liq_var);
                Principal.setTara(tara_var);
                Principal.setEstabilidade(codEstabilidade);
                Principal.getSerial().escreverLinha();
                try {
                    java.lang.Thread.sleep(50);
                } catch (InterruptedException iex) {
                    iex.printStackTrace();
                }
            }
        }
    }

}
