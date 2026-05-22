package br.com.luquinhas.apiimunedata.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "registro_vacinacao")
public class RegistroVacinacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "municipio", nullable = false, length = 100)
    private String municipio;

    @Column(name = "estado", nullable = false, length = 2)
    private String estado;

    @Column(name = "vacina", nullable = false, length = 50)
    private String vacina;

    @Enumerated(EnumType.STRING)
    @Column(name = "dose", nullable = false, length = 20)
    private Dose dose;

    @Column(name = "quantidade_aplicada", nullable = false)
    private Integer quantidadeAplicada;

    @Column(name = "data_registro", nullable = false)
    private LocalDate dataRegistro;

    // Construtor vazio obrigatorio pelo JPA
    public RegistroVacinacao() {
    }

    // Construtor com todos os campos (sem id, pois eh gerado pelo banco)
    public RegistroVacinacao(String municipio, String estado, String vacina,
                             Dose dose, Integer quantidadeAplicada, LocalDate dataRegistro) {
        this.municipio = municipio;
        this.estado = estado;
        this.vacina = vacina;
        this.dose = dose;
        this.quantidadeAplicada = quantidadeAplicada;
        this.dataRegistro = dataRegistro;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getVacina() {
        return vacina;
    }

    public void setVacina(String vacina) {
        this.vacina = vacina;
    }

    public Dose getDose() {
        return dose;
    }

    public void setDose(Dose dose) {
        this.dose = dose;
    }

    public Integer getQuantidadeAplicada() {
        return quantidadeAplicada;
    }

    public void setQuantidadeAplicada(Integer quantidadeAplicada) {
        this.quantidadeAplicada = quantidadeAplicada;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;
    }
}
