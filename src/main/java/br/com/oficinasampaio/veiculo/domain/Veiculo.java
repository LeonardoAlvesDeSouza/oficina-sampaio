package br.com.oficinasampaio.veiculo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(nullable = false, length = 7, unique = true)
    private String placa;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    private Integer ano;

    @Column(length = 50)
    private String cor;

    private Long quilometragem;

    @Column(nullable = false)
    private boolean ativo;

    @Version
    private long versao;

    protected Veiculo() {
    }

    private Veiculo(
            UUID clienteId,
            String placa,
            String marca,
            String modelo,
            Integer ano,
            String cor,
            Long quilometragem
    ) {
        this.clienteId = Objects.requireNonNull(clienteId, "Cliente é obrigatório");
        this.placa = normalizarPlaca(placa);
        this.marca = textoObrigatorio(marca, "Marca");
        this.modelo = textoObrigatorio(modelo, "Modelo");
        this.ano = ano;
        this.cor = textoOuNulo(cor);
        this.quilometragem = validarQuilometragem(quilometragem);
        this.ativo = true;
    }

    public static Veiculo cadastrar(
            UUID clienteId,
            String placa,
            String marca,
            String modelo,
            Integer ano,
            String cor,
            Long quilometragem
    ) {
        return new Veiculo(clienteId, placa, marca, modelo, ano, cor, quilometragem);
    }

    private static String normalizarPlaca(String valor) {
        var placa = textoObrigatorio(valor, "Placa")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase(Locale.ROOT);
        if (placa.length() != 7) {
            throw new IllegalArgumentException("Placa deve possuir 7 caracteres");
        }
        return placa;
    }

    private static String textoObrigatorio(String valor, String campo) {
        var normalizado = textoOuNulo(valor);
        if (normalizado == null) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
        return normalizado;
    }

    private static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static Long validarQuilometragem(Long quilometragem) {
        if (quilometragem != null && quilometragem < 0) {
            throw new IllegalArgumentException("Quilometragem não pode ser negativa");
        }
        return quilometragem;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public String getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public String getCor() {
        return cor;
    }

    public Long getQuilometragem() {
        return quilometragem;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
