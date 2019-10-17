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


/**
 *
 * @author Desenvolvimento
 */
public class Formatacao {
    
    //@@@@@@@@@@ FORMATAÇÃO DE DADOS PROTOCOLO COMUNICAÇÃO @@@@@@@@@@@@@@//
    
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
            if (temVirgula && !sobrecarga && !saturado) { //Se tiver virgula nos dados e não tiver sobrecarga/saturada
                peso_bru = dado.substring(3, 10).replaceAll(",", "."); // Peso Bruto ( 00,000) 
                peso_liq = dado.substring(3, 10).replaceAll(",", "."); // Peso Liquido ( 00,000) 
                tara = dado.substring(13, 20).replaceAll(",", "."); // Tara( 00,000) 
                casasDecimais = (6 - tara.indexOf(".")); // Conta as casas decimais
            } else if (!temVirgula && !sobrecarga && !saturado) { //Se não tiver virgula nos dados e não tiver sobrecarga/saturada
                peso_bru = dado.substring(3, 9); // Peso Bruto ( 00000) 
                peso_liq = dado.substring(3, 9); // Peso Liquido ( 00000) 
                tara = dado.substring(12, 18); // Tara ( 00000) 
            }

            //
            if (!sobrecarga && !saturado) { //Se não estiver com sobrecarga/saturado
                List<String> pesos = new ArrayList<String>();
                pesos.add(peso_bru);
                pesos.add(peso_liq);
                pesos.add(tara);
                int cnt = 0;
                for (String peso : pesos) { //Para cada tipo de peso formatar
                    if (peso.equals(" 00000")) { //Se todos os digitos forem zero
                        peso = "0";
                    } else {
                        if (peso.contains("-")) { //Se tiver sinal negativo
                            if (peso.contains(".")) { // Se tiver ponto decimal
                                int pos = peso.indexOf(".");
                                //Retira os zeros a esquerda em excesso do sinal negativo até o ponto decimal
                                peso = "-" + peso.replace(peso.substring(0, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", ""));
                            } else { //Se não tiver ponto decimal
                                //Retira os zeros a esquerda mantém o sinal negativo
                                peso = "-" + peso.substring(1, 6).replaceFirst("0*", ""); //OK
                            }
                        } else { //Se tiver sinal positivo
                            if (peso.contains(".")) { //Se tiver ponto decimal
                                int pos = peso.indexOf(".");
                                //Retira zeros a esquerda em excesso até o ponto decimal
                                peso = (pos <= 2) ? peso.replaceFirst(peso.substring(1, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", "")) : peso.replace(peso.substring(1, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", ""));
                            } else { //Se não tiver ponto decimal
                                try {//Tente realizar essa substituição
                                    //Retira zeros a esquerda
                                    peso = peso.substring(1, 6).replaceFirst("0*", ""); //OK
                                } catch (Exception ex) { //Se houver problema use este bloco
                                    //Retira zeros a esquerda
                                    peso = peso.substring(1, 5).replaceFirst("0*", ""); //OK
                                }
                            }
                        }
                    }

                    //Retira espaços em branco do peso
                    peso = peso.trim();

                    //Atualiza o valor na lista de pesos
                    pesos.set(cnt, peso);

                    //Pega o valor de acordo com a contagem e atribui a variavel correta
                    switch (cnt) {
                        case 0:
                            peso_bru = peso;
                            break;
                        case 1:
                            peso_liq = peso;
                            break;
                        case 2:
                            //Identifica se tem tara(Valor diferente de 0)
                            if (Double.parseDouble(peso) != 0.0) {
                                comTara = true;
                            }
                            tara = peso;
                            break;
                    }
                    cnt++;
                }

                if (comTara) { //Se tiver tara
                    if (temVirgula) { //Se a tara tiver ponto decimal
                        //Faz o calculo do peso liquido com tara para resultar o peso bruto(com ponto decimal).
                        Float pb = Float.parseFloat(peso_liq) + Float.parseFloat(tara);
                        dados.put("peso_bru", formatoDecimal(casasDecimais, pb));
                    } else { // Se não tiver ponto decimal
                        //Faz o calculo do peso liquido com tara para resultar o peso bruto(sem ponto decimal).
                        int pb = Integer.parseInt(peso_liq) + Integer.parseInt(tara);
                        dados.put("peso_bru", String.valueOf(pb));
                    }
                } else { //Se não tiver tara
                    dados.put("peso_bru", peso_bru);
                }
                dados.put("estavel", !dado.contains("*") ? "E" : "O");
                dados.put("peso_liq", peso_liq);
                dados.put("tara", tara);
            } else if (sobrecarga) { //Se estiver com sobrecarga
                dados.put("estavel", "SOB");
                dados.put("peso_bru", "0");
                dados.put("peso_liq", "0");
                dados.put("tara", "0");
            } else if (saturado) { //Se estiver saturado
                dados.put("estavel", "SAT");
                dados.put("peso_bru", "0");
                dados.put("peso_liq", "0");
                dados.put("tara", "0");
            }
        } catch (Exception ex) { //Se houver erro na formatação dos dados
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
        boolean sobrecarga = dado.contains("OL"); //Indica se está com sobrecarga

        try {
            if (!sobrecarga) { //Se não estiver com sobrecarga
                //Atribuição de valores
                String peso_bru = dado.substring(2, 9);
                String peso_liq = dado.substring(18, 25);
                String tara = dado.substring(10, 17);
                //Cria uma lista de Strings para adicionar os valores(Bruto, Liquido e Tara)
                List<String> pesos = new ArrayList<String>();
                pesos.add(peso_bru);
                pesos.add(peso_liq);
                pesos.add(tara);
                int cnt = 0;
                for (String peso : pesos) { //Para cada valor na lista iniciar a formatação
                    if (peso.equals("000000 ") || peso.equals("0000000")) {
                        peso = "0";
                    } else {
                        if (peso.contains("-")) { //Se o valor for negativo
                            if (peso.contains(".")) { //Se o valor tiver ponto
                                int pos = peso.indexOf("."); //Pega a posição do ponto
                                //Retira os zeros a esquerda em excesso do sinal negativo até o ponto
                                peso = "-" + peso.replace(peso.substring(0, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", ""));
                            } else { // Se não tiver ponto
                                //Retira os zeros a esquerda em excesso do sinal negativo até o final
                                peso = "-" + peso.substring(1, 7).replaceFirst("0*", "");
                            }
                        } else { //Se o valor for positivo
                            if (peso.contains(".")) { //Se o valor tiver ponto
                                int pos = peso.indexOf("."); //Pega a posição do ponto
                                //Retira os zeros a esquerda em excesso até o ponto
                                peso = (pos <= 3) ? peso.replaceFirst(peso.substring(0, (pos - 1)), peso.substring(0, (pos - 1)).replaceFirst("0*", "")) : peso.replace(peso.substring(0, (pos - 1)), peso.substring(0, (pos - 1)).replaceFirst("0*", ""));
                            } else {
                                //Retira os zeros a esquerda em excesso
                                peso = peso.replaceFirst("0*", "");
                            }
                        }
                    }   
                    pesos.set(cnt, peso); //Atualiza o valor na lista
                    
                    //Observa a contagem e atribui os valores em suas devidas variaveis.
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
                //Coloca os valores formatados em um Array Map para saída
                dados.put("estavel", dado.substring(0, 1).equals("0") ? "E" : "O");
                dados.put("peso_bru", peso_bru);
                dados.put("tara", tara);
                dados.put("peso_liq", peso_liq);
            } else { //Se tiver com sobrecarga
                //Coloca os valores formatados em um Array Map para saída
                dados.put("estavel", "SOB");
                dados.put("peso_bru", "0");
                dados.put("tara", "0");
                dados.put("peso_liq", "0");
            }
        } catch (Exception e) { //Se houver erro na formatação dos dados
            //Coloca os valores formatados em um Array Map para saída
            dados.put("estavel", "ERR");
            dados.put("peso_bru", "0");
            dados.put("peso_liq", "0");
            dados.put("tara", "0");
        }
        System.out.println("Peso Bruto: " + dados.get("peso_bru") + "/  Peso Liquido : " + dados.get("peso_liq") + "/  Tara : " + dados.get("tara"));
        //Saída dos dados
        return dados;
    }

    //Formatação e tratamento dos dados do indicador WT1000N
    public static Map<String, String> formatarDadosWT27(String dado) {
        Map<String, String> dados = new HashMap<String, String>();
        boolean sobrecarga = dado.contains("OL"); //Indica se está com sobrecarga
        try {
            if (!sobrecarga) { //Se não estiver com sobrecarga
                //Atribuição de valores
                String peso_bru = dado.substring(5, 12);
                String peso_liq = dado.substring(24, 31);
                String tara = dado.substring(15, 21);
                //Cria uma lista de Strings para adicionar os valores(Bruto, Liquido e Tara)
                List<String> pesos = new ArrayList<String>();
                pesos.add(peso_bru);
                pesos.add(peso_liq);
                pesos.add(tara);
                int cnt = 0;
                for (String peso : pesos) { //Para cada valor na lista iniciar a formatação
                    if (peso.equals(" 000000") || peso.equals("000000")) {
                        peso = "0";
                    } else {
                        if (peso.contains("-")) { //Se o valor for negativo
                            if (peso.contains(".")) { //Se o valor tiver ponto
                                int pos = peso.indexOf("."); //Pega a posição do ponto
                                //Retira os zeros a esquerda em excesso do sinal negativo até o ponto
                                peso = "-" + peso.replace(peso.substring(0, (pos - 1)), peso.substring(1, (pos - 1)).replaceFirst("0*", ""));
                            } else { //Se não tiver ponto
                                //Retira os zeros a esquerda em excesso do sinal negativo até o final
                                peso = "-" + peso.substring(1, 7).replaceFirst("0*", "");
                            }
                        } else { //Se o valor for positivo
                            peso = peso.trim(); //Retira espaço vazio do valor
                            if (peso.contains(".")) { //Se o valor tiver ponto
                                int pos = peso.indexOf("."); //Pega a posição do ponto
                                //Retira os zeros a esquerda em excesso até o ponto
                                peso = (pos <= 3) ? peso.replaceFirst(peso.substring(0, (pos - 1)), peso.substring(0, (pos - 1)).replaceFirst("0*", "")) : peso.replace(peso.substring(0, (pos - 1)), peso.substring(0, (pos - 1)).replaceFirst("0*", ""));
                            } else {
                                //Retira os zeros a esquerda em excesso
                                peso = peso.replaceFirst("0*", "");
                            }
                        }
                    }
                    pesos.set(cnt, peso); //Atualiza o valor na lista
                    //Observa a contagem e atribui os valores em suas devidas variaveis.
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
                //Coloca os valores formatados em um Array Map para saída
                dados.put("estavel", dado.substring(0, 1).equals("E") ? "E" : "O");
                dados.put("peso_bru", peso_bru);
                dados.put("tara", tara);
                dados.put("peso_liq", peso_liq);
            } else { //Se tiver com sobrecarga
                //Coloca os valores formatados em um Array Map para saída
                dados.put("estavel", "SOB");
                dados.put("peso_bru", "0");
                dados.put("tara", "0");
                dados.put("peso_liq", "0");
            }
        } catch (Exception e) { //Se houver erro na formatação
            //Coloca os valores formatados em um Array Map para saída
            dados.put("estavel", "ERR");
            dados.put("peso_bru", "0");
            dados.put("peso_liq", "0");
            dados.put("tara", "0");
        }
        System.out.println("Peso Bruto: " + dados.get("peso_bru") + "/  Peso Liquido : " + dados.get("peso_liq") + "/  Tara : " + dados.get("tara"));
        //Saída dos dados
        return dados;
    }
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//
    
    //@@@@@@@@@@@@@@@@@ CONVERSÃO PROTOCOLO COMUNICAÇÃO @@@@@@@@@@@@@@@//
    
    //CONVERTE OS VALORES DE PESAGEM PARA O PROTOCOLO DE COMUNICAÇÃO WT1000N (0,-000000,-000000,-000000)
    public static String formatarParaWT1000N(String peso_bru, String peso_liq, String tara, String estabilidade) {
        String[] dados = new String[4];
        //Se algum valor vier NULL substituir para um valor padrão
        dados[0] = (estabilidade == null) ? "E" : estabilidade; //ESTABILIDADE - dados[0]
        dados[1] = (peso_bru == null) ? "0" : peso_bru; //PESO BRUTO - dados[1]
        dados[2] = (tara == null) ? "0" : tara; //TARA - dados[2]
        dados[3] = (peso_liq == null) ? "0" : peso_liq; //PESO LIQUIDO - dados[3]

        String resultado = "";
        //Formatação do estado de estabilidade
        if (!dados[0].equals("SOB")) { //Se não estiver em SOBRECARGA
            switch (dados[0]) {
                case "E":
                    dados[0] = "0";
                    break;
                case "O":
                    dados[0] = "1";
                    break;
                case "ERR":
                    dados[0] = "0";
                    break;
            }

            for (int i = 1; i < dados.length; i++) { //Percorre o array de dados a partir do 1(Peso Bruto)
                if (dados[i].contains("-")) { //Se o valor tiver sinal negativo
                    //Substitui o sinal negativo pelo sinal negativo mais os zeros restantes
                    dados[i] = dados[i].replace("-", "-" + adicionarZerosWT1000N(dados[i]));
                } else { //Se o valor tiver sinal positivo
                    //Coloca zeros a esquerda se necessário para completar a formatação
                    dados[i] = adicionarZerosWT1000N(dados[i]) + dados[i];
                }
            }
            //Montagem do protocolo para saída
            resultado += dados[0] + "," + dados[1] + "," + dados[2] + "," + dados[3];
        } else { //Se estiver em SOBRECARGA
            //Montagem do protocolo para saida (Sobrecarga)
            resultado = "0,     ol,     ol,     OL";
        }

        //Saida do protocolo de comunicação
        return resultado + "\r\n";
    }
    
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//
    
    
    //@@@@@@@@@@@@@@@@@@@ METODOS UTILITÁRIOS @@@@@@@@@@@@@@@@@@@@@@@@@//
    
    //RETORNA A QUANTIDADE DE CASAS DECIMAIS DE UMA STRING
    public static int qtdCasasDecimais(String num) {
        return ((num.length() - 1) - num.indexOf("."));
    }
    
    //RETORNA UMA STRING FORMATADA DE ACORDO COM AS CASAS DECIMAIS INFORMADAS
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
    
    //RETORNA UMA STRING FORMATADA DE ACORDO COM AS CASAS DECIMAIS INFORMADAS
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


    private static String adicionarZerosWT1000N(String valor) {
        String formato = "";
        for (int i = 1; i <= (7 - valor.length()); i++) {
            formato += "0";
        }
        return formato;
    }
    
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@//

}
