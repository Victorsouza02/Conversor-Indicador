/*
    *CLASSE : Formatacao
    *FUNCÃO : Metodos de formatação de campos e dados da serial
 */
package conversorformato.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 *
 * @author Desenvolvimento
 */
public class Formatacao {

    //METODO PARA ADICIONAR LIMITE DE TEXTO EM UM CAMPO
    public static void addTextLimiter(final TextField tf, final int maxLength) {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (tf.getText().length() > maxLength) {
                    String s = tf.getText().substring(0, maxLength);
                    tf.setText(s);
                }
            }
        });
    }

    public static void onlyNumber(final TextField tf) {
        tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if (!newValue.matches("\\d*")) {
                    tf.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }

    //MUDA O STATUS DE ESTABILIDADE DO DISPLAY DE ACORDO COM O CODIGO DE ESTABILIDADE
    public static void estabilizacaoDisplay(Label labelEstabilizacao, String codEstabilidade) {
        switch (codEstabilidade) {
            case "E":
                labelEstabilizacao.setText("Estável");
                labelEstabilizacao.setStyle("-fx-text-fill: green;");
                break;
            case "O":
                labelEstabilizacao.setText("Oscilando");
                labelEstabilizacao.setStyle("-fx-text-fill: red;");
                break;
            case "SOB":
                labelEstabilizacao.setText("Sobrecarga");
                labelEstabilizacao.setStyle("-fx-text-fill: yellow;");
                break;
            case "SAT":
                labelEstabilizacao.setText("Saturado");
                labelEstabilizacao.setStyle("-fx-text-fill: #fc5e14;");
                break;
            case "ERR":
                labelEstabilizacao.setText("Erro - Format");
                labelEstabilizacao.setStyle("-fx-text-fill: #8434e0;");
                break;
        }
    }

    //Formatação e tratamento dos dados do indicador ALFA 3101C
    public static Map<String, String> formatarDados3101C(String dado) {
        Map<String, String> dados = new HashMap<String, String>();

        //String dado = dataSerial();
        String peso_bru = "";
        String peso_liq = "";
        String tara = "";

        boolean sobrecarga = dado.contains("S<BRE");
        boolean saturado = dado.contains("SATURA");
        boolean temVirgula = dado.contains(",");
        boolean comTara = false;
        int casasDecimais = 0;

        try {
            //ALOCAÇÃO DE VALORES
            if (temVirgula && !sobrecarga && !saturado) { //SE TIVER VIRGULA NOS DADOS
                peso_bru = dado.substring(3, 10).replaceAll(",", "."); // PARTE ( 00,000) 
                peso_liq = dado.substring(3, 10).replaceAll(",", "."); // PARTE ( 00,000) 
                tara = dado.substring(13, 20).replaceAll(",", "."); // PARTE ( 00,000) 
                casasDecimais = (6 - tara.indexOf(".")); // CONTA QUANTAS CASAS DECIMAIS
            } else if (!temVirgula && !sobrecarga && !saturado) { //SE NAO TIVER VIRGULA OU SOBRECARGA
                peso_bru = dado.substring(3, 9); // PARTE ( 00000) 
                peso_liq = dado.substring(3, 9); // PARTE ( 00000) 
                tara = dado.substring(12, 18); // PARTE ( 00000) 
            }

            //
            if (!sobrecarga && !saturado) { //SE NÃO ESTIVER COM SOBRECARGA
                List<String> pesos = new ArrayList<String>();
                pesos.add(peso_bru);
                pesos.add(peso_liq);
                pesos.add(tara);
                int cnt = 0;
                for (String peso : pesos) { //PARA CADA TIPO DE PESO FORMATAR
                    if (peso.equals(" 00000")) { //SE OS DIGITOS FOREM TODOS ZERO
                        peso = "0";
                    } else {
                        if (peso.contains("-")) { //SE TIVER SINAL NEGATIVO
                            if (peso.contains(".")) { // SE TIVER PONTO DECIMAL
                                int pos = peso.indexOf(".");
                                //RETIRA OS ZEROS A ESQUERDA DA PARTE INTEIRA E MANTÉM O SINAL NEGATIVO
                                peso = "-" + peso.replace(peso.substring(0, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", ""));
                            } else { //SE NÃO TIVER PONTO DECIMAL
                                //RETIRA OS ZEROS A ESQUERDA E MANTEM O SINAL NEGATIVO
                                peso = "-" + peso.substring(1, 6).replaceFirst("0*", ""); //OK
                            }
                        } else { //SE TIVER SINAL POSITIVO
                            if (peso.contains(".")) { //SE TIVER PONTO DECIMAL
                                int pos = peso.indexOf(".");
                                //RETIRA ZEROS A ESQUERDA DA PARTE INTEIRA
                                peso = (pos <= 2) ? peso.replaceFirst(peso.substring(1, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", "")) : peso.replace(peso.substring(1, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", ""));
                            } else { //SE NÃO TIVER PONTO DECIMAL
                                try {//TENTE REALIZAR ESSA SUBSTITUIÇÃO
                                    //RETIRA ZEROS A ESQUERDA
                                    peso = peso.substring(1, 6).replaceFirst("0*", ""); //OK
                                } catch (Exception ex) { //SE HOUVER PROBLEMA USE ESSA
                                    //RETIRA ZEROS A ESQUERDA
                                    peso = peso.substring(1, 5).replaceFirst("0*", ""); //OK
                                }
                            }
                        }
                    }

                    //RETIRA ESPAÇOS EM BRANCO DO PESO
                    if (peso.contains(" ")) {
                        peso = peso.replace(" ", "");
                    };

                    //ATUALIZA O VALOR NA LISTA DE PESOS
                    pesos.set(cnt, peso);

                    //PEGA O VALOR DE ACORDO COM A CONTAGEM E ATRIBUI A VARIAVEL CORRETA
                    switch (cnt) {
                        case 0:
                            peso_bru = peso;
                            break;
                        case 1:
                            peso_liq = peso;
                            break;
                        case 2:
                            //IDENTIFICA SE TEM TARA(SE TARA TIVER ALGUM VALOR)
                            if (Double.parseDouble(peso) != 0.0) {
                                comTara = true;
                            }
                            tara = peso;
                            break;
                    }
                    cnt++;
                }

                if (comTara) { //SE FOR DETECTADO QUE EXISTE TARA
                    if (temVirgula) { //SE A TARA TIVER PONTO DECIMAL
                        //FAZ O CALCULO DO PESO LIQUIDO COM TARA PARA RESULTAR PESO BRUTO (COM PONTO)
                        Float pb = Float.parseFloat(peso_liq) + Float.parseFloat(tara);
                        dados.put("peso_bru", formatoDecimal(casasDecimais, pb));
                    } else { // SE NÃO TIVER PONTO DECIMAL
                        //FAZ O CALCULO DO PESO LIQUIDO COM TARA PARA RESULTAR PESO BRUTO (SEM PONTO)
                        int pb = Integer.parseInt(peso_liq) + Integer.parseInt(tara);
                        dados.put("peso_bru", String.valueOf(pb));
                    }
                } else { //SE NÃO TIVER TARA
                    dados.put("peso_bru", peso_bru);
                }
                dados.put("estavel", !dado.contains("*") ? "E" : "O");
                dados.put("peso_liq", peso_liq);
                dados.put("tara", tara);
            } else if (sobrecarga) { //SE ESTIVER COM SOBRECARGA
                dados.put("estavel", "SOB");
                dados.put("peso_bru", "0");
                dados.put("peso_liq", "0");
                dados.put("tara", "0");
            } else if (saturado) { //SE ESTIVER SATURADO
                dados.put("estavel", "SAT");
                dados.put("peso_bru", "0");
                dados.put("peso_liq", "0");
                dados.put("tara", "0");
            }
        } catch (Exception ex) {
            dados.put("estavel", "ERR");
            dados.put("peso_bru", "0");
            dados.put("peso_liq", "0");
            dados.put("tara", "0");
        }

        System.out.println("Peso Bruto: " + dados.get("peso_bru") + "/  Peso Liquido : " + dados.get("peso_liq") + "/  Tara : " + dados.get("tara"));

        return dados;
    }

    //Formatação e tratamento dos dados do indicador WT1000N
    public static Map<String, String> formatarDadosWT1000N(String dado) {
        Map<String, String> dados = new HashMap<String, String>();
        boolean sobrecarga = dado.contains("OL");

        try {
            if (!sobrecarga) {
                String peso_bru = dado.substring(2, 9);
                String peso_liq = dado.substring(18, 25);
                String tara = dado.substring(10, 17);
                List<String> pesos = new ArrayList<String>();
                pesos.add(peso_bru);
                pesos.add(peso_liq);
                pesos.add(tara);
                int cnt = 0;
                for (String peso : pesos) {
                    if (peso.equals("000000 ") || peso.equals("0000000")) {
                        peso = "0";
                    } else {
                        if (peso.contains("-")) {
                            if (peso.contains(".")) {
                                int pos = peso.indexOf(".");
                                peso = "-" + peso.replace(peso.substring(0, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", ""));
                            } else {
                                peso = "-" + peso.substring(1, 7).replaceFirst("0*", "");
                            }
                        } else {
                            if (peso.contains(".")) {
                                int pos = peso.indexOf(".");
                                peso = (pos <= 3) ? peso.replaceFirst(peso.substring(0, (pos - 1)), peso.substring(0, (pos - 1)).replaceFirst("0*", "")) : peso.replace(peso.substring(0, (pos - 1)), peso.substring(0, (pos - 1)).replaceFirst("0*", ""));
                            } else {
                                peso = peso.replaceFirst("0*", "");
                            }
                        }
                    }
                    pesos.set(cnt, peso);
                    switch (cnt) {
                        case 0:
                            peso_bru = peso;
                            break;
                        case 1:
                            peso_liq = peso;
                            break;
                        case 2:
                            tara = peso;
                            break;
                    }
                    cnt++;
                }
                dados.put("estavel", dado.substring(0, 1).equals("0") ? "E" : "O");
                dados.put("peso_bru", peso_bru);
                dados.put("tara", tara);
                dados.put("peso_liq", peso_liq);
            } else {
                dados.put("estavel", "SOB");
                dados.put("peso_bru", "0");
                dados.put("tara", "0");
                dados.put("peso_liq", "0");
            }
        } catch (Exception e) {
            dados.put("estavel", "ERR");
            dados.put("peso_bru", "0");
            dados.put("peso_liq", "0");
            dados.put("tara", "0");
            e.printStackTrace();
        }
        System.out.println("Peso Bruto: " + dados.get("peso_bru") + "/  Peso Liquido : " + dados.get("peso_liq") + "/  Tara : " + dados.get("tara"));
        return dados;
    }
    
     //Formatação e tratamento dos dados do indicador WT1000N
    public static Map<String, String> formatarDadosWT27(String dado) {
        Map<String, String> dados = new HashMap<String, String>();
        boolean sobrecarga = dado.contains("OL");

        try {
            if (!sobrecarga) {
                String peso_bru = dado.substring(5, 12);
                String peso_liq = dado.substring(24, 31);
                String tara = dado.substring(15, 21);
                List<String> pesos = new ArrayList<String>();
                pesos.add(peso_bru);
                pesos.add(peso_liq);
                pesos.add(tara);
                int cnt = 0;
                for (String peso : pesos) {
                    if (peso.equals(" 000000") || peso.equals("000000")) {
                        peso = "0";
                    } else {
                        if (peso.contains("-")) {
                            if (peso.contains(".")) {
                                int pos = peso.indexOf(".");
                                peso = "-" + peso.replace(peso.substring(0, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", ""));
                            } else {
                                peso = "-" + peso.substring(1, 7).replaceFirst("0*", "");
                            }
                        } else {
                            peso = peso.trim();
                            if (peso.contains(".")) {
                                int pos = peso.indexOf(".");
                                peso = (pos <= 3) ? peso.replaceFirst(peso.substring(0, (pos - 1)), peso.substring(0, (pos - 1)).replaceFirst("0*", "")) : peso.replace(peso.substring(0, (pos - 1)), peso.substring(0, (pos - 1)).replaceFirst("0*", ""));
                            } else {
                                peso = peso.replaceFirst("0*", "");
                            }
                        }
                    }
                    pesos.set(cnt, peso);
                    switch (cnt) {
                        case 0:
                            peso_bru = peso;
                            break;
                        case 1:
                            peso_liq = peso;
                            break;
                        case 2:
                            tara = peso;
                            break;
                    }
                    cnt++;
                }
                dados.put("estavel", dado.substring(0, 1).equals("E") ? "E" : "O");
                dados.put("peso_bru", peso_bru);
                dados.put("tara", tara);
                dados.put("peso_liq", peso_liq);
            } else {
                dados.put("estavel", "SOB");
                dados.put("peso_bru", "0");
                dados.put("tara", "0");
                dados.put("peso_liq", "0");
            }
        } catch (Exception e) {
            dados.put("estavel", "ERR");
            dados.put("peso_bru", "0");
            dados.put("peso_liq", "0");
            dados.put("tara", "0");
            e.printStackTrace();
        }
        System.out.println("Peso Bruto: " + dados.get("peso_bru") + "/  Peso Liquido : " + dados.get("peso_liq") + "/  Tara : " + dados.get("tara"));
        return dados;
    }

    public static int qtdCasasDecimais(String num) {
        return ((num.length() - 1) - num.indexOf("."));
    }

    public static String formatoDecimalDouble(int casasdecimais, Double valor) {
        String formato = "";
        switch (casasdecimais) {
            case 1:
                formato = "0.0";
                break;
            case 2:
                formato = "0.00";
                break;
            case 3:
                formato = "0.000";
                break;
            case 4:
                formato = "0.0000";
                break;
            case 5:
                formato = "0.00000";
                break;
            default:
                formato = "0.0";
                break;
        }

        DecimalFormat df = new DecimalFormat(formato);
        return df.format(valor).replaceAll(",", ".");
    }

    public static String formatoDecimal(int casasdecimais, Float valor) {
        String formato = "";
        switch (casasdecimais) {
            case 1:
                formato = "0.0";
                break;
            case 2:
                formato = "0.00";
                break;
            case 3:
                formato = "0.000";
                break;
            case 4:
                formato = "0.0000";
                break;
            case 5:
                formato = "0.00000";
                break;
            default:
                formato = "0.0";
                break;
        }

        DecimalFormat df = new DecimalFormat(formato);
        return df.format(valor).replaceAll(",", ".");
    }

    public static String formatarParaWT1000N(String peso_bru, String peso_liq, String tara, String estabilidade) {
        //0,0000000,0000000,0000000
        estabilidade = (estabilidade == null) ? "E" : estabilidade;
        peso_bru = (peso_bru == null) ? "0" : peso_bru;
        peso_liq = (peso_liq == null) ? "0" : peso_liq;
        tara = (tara == null) ? "0" : tara;

        String resultado = "";
        if (!estabilidade.equals("SOB")) {
            switch (estabilidade) {
                case "E":
                    resultado = "0,";
                    break;
                case "O":
                    resultado = "1,";
                    break;
                 case "ERR":
                    resultado = "0,";
                    break;
            }
            //FORMATAR PESO BRUTO
            String pb_dados_add = "";
            for (int i = 1; i <= (7 - peso_bru.length()); i++) {
                pb_dados_add += "0";
            }
            if (peso_bru.contains("-")) {
                resultado += peso_bru.replace("-", "-" + pb_dados_add)+",";
            } else {
                resultado += pb_dados_add + peso_bru + ",";
            }
            
            //FORMATAR TARA
            String tara_dados_add = "";
            for (int i = 1; i <= (7 - tara.length()); i++) {
                tara_dados_add += "0";
            }
            if (tara.contains("-")) {
                resultado += tara.replace("-", "-" + tara_dados_add)+",";
            } else {
                resultado += tara_dados_add + tara +",";
            }
            
            //FORMATAR PESO LIQUIDO
            String pl_dados_add = "";
            for (int i = 1; i <= (7 - peso_liq.length()); i++) {
                pl_dados_add += "0";
            }
            if (peso_liq.contains("-")) {
                resultado += peso_liq.replace("-", "-" + pl_dados_add);
            } else {
                resultado += pl_dados_add + peso_liq;
            }  
        } else {
            resultado = "0,     ol,     ol,     OL";
        }
        return resultado+"\r\n";
    }

}
