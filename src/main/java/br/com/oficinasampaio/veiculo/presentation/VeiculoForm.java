package br.com.oficinasampaio.veiculo.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class VeiculoForm {

    @NotBlank(message = "Placa é obrigatória")
    @Pattern(
            regexp = "(?i)^[A-Z]{3}-?(?:[0-9]{4}|[0-9][A-Z][0-9]{2})$",
            message = "Placa inválida"
    )
    private String placa;

    @NotBlank(message = "Marca é obrigatória")
    @Size(max = 80, message = "Marca deve possuir no máximo 80 caracteres")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    @Size(max = 100, message = "Modelo deve possuir no máximo 100 caracteres")
    private String modelo;

    @Min(value = 1886, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    private Integer ano;

    @Size(max = 50, message = "Cor deve possuir no máximo 50 caracteres")
    private String cor;

    @PositiveOrZero(message = "Quilometragem não pode ser negativa")
    private Long quilometragem;

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public Long getQuilometragem() {
        return quilometragem;
    }

    public void setQuilometragem(Long quilometragem) {
        this.quilometragem = quilometragem;
    }
}
