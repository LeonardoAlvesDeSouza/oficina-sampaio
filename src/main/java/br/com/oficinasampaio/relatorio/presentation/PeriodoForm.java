package br.com.oficinasampaio.relatorio.presentation;

import br.com.oficinasampaio.shared.domain.Periodo;
import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.shared.presentation.FormatoOficina;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * A janela de tempo pedida na tela. Dois dias de calendário viram instantes com o
 * fuso da oficina — o mesmo que as telas usam, para o relatório não discordar da
 * data que o usuário viu.
 */
public class PeriodoForm {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate inicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fim;

    /**
     * Data que falta ou período invertido são regra de negócio e viram aviso na
     * tela de erro do sistema, como qualquer outra recusa do domínio.
     */
    Periodo paraDominio() {
        if (inicio == null || fim == null) {
            throw new RegraNegocioException("Informe o início e o fim do período");
        }
        return Periodo.deDias(inicio, fim, FormatoOficina.FUSO_DA_OFICINA);
    }

    String descricao() {
        return FormatoOficina.data(inicio) + " a " + FormatoOficina.data(fim);
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }
}
