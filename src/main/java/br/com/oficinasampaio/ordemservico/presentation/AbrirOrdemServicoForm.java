package br.com.oficinasampaio.ordemservico.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class AbrirOrdemServicoForm {

    @NotNull(message = "Veículo é obrigatório")
    private UUID veiculoId;

    @NotBlank(message = "Relato do problema é obrigatório")
    @Size(max = 1000, message = "Relato do problema deve possuir no máximo 1000 caracteres")
    private String relatoProblema;

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(UUID veiculoId) {
        this.veiculoId = veiculoId;
    }

    public String getRelatoProblema() {
        return relatoProblema;
    }

    public void setRelatoProblema(String relatoProblema) {
        this.relatoProblema = relatoProblema;
    }
}
