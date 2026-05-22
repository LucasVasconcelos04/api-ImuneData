package br.com.luquinhas.apiimunedata.entity;

import java.util.Arrays;
import java.util.List;

public enum Regiao {

    NORTE(List.of("AC", "AP", "AM", "PA", "RO", "RR", "TO")),
    NORDESTE(List.of("AL", "BA", "CE", "MA", "PB", "PE", "PI", "RN", "SE")),
    CENTRO_OESTE(List.of("DF", "GO", "MT", "MS")),
    SUDESTE(List.of("ES", "MG", "RJ", "SP")),
    SUL(List.of("PR", "RS", "SC"));

    private final List<String> estados;

    Regiao(List<String> estados) {
        this.estados = estados;
    }

    public List<String> getEstados() {
        return estados;
    }

    // Metodo utilitario que converte uma String em Regiao
    // Ex: "sudeste" -> Regiao.SUDESTE
    public static Regiao fromString(String texto) {
        return Arrays.stream(Regiao.values())
                .filter(r -> r.name().equalsIgnoreCase(texto))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Regiao invalida: " + texto + ". Valores aceitos: NORTE, NORDESTE, CENTRO_OESTE, SUDESTE, SUL"
                ));
    }
}
