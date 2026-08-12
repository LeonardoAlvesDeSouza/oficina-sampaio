package br.com.oficinasampaio.shared.presentation;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * O cadastro guarda documento e telefone só com dígitos. Estes testes fixam o
 * que o balcão vê, incluindo o caso do dado que não cabe em nenhum formato
 * conhecido: ele aparece como está, porque esconder um cadastro estranho é pior
 * do que mostrá-lo.
 */
class FormatoOficinaTest {

    @Test
    void pontuaCpfDeOnzeDigitos() {
        assertEquals("529.982.247-25", FormatoOficina.documento("52998224725"));
    }

    @Test
    void pontuaCnpjDeQuatorzeDigitos() {
        assertEquals("11.222.333/0001-81", FormatoOficina.documento("11222333000181"));
    }

    @Test
    void repontuaDocumentoQueJaVeioPontuado() {
        assertEquals("529.982.247-25", FormatoOficina.documento("529.982.247-25"));
    }

    @Test
    void devolveDocumentoDeTamanhoDesconhecidoComoEstaEscrito() {
        assertEquals("1234", FormatoOficina.documento(" 1234 "));
    }

    @Test
    void naoInventaDocumentoQuandoNaoHaNenhum() {
        assertNull(FormatoOficina.documento(null));
        assertNull(FormatoOficina.documento("   "));
    }

    @Test
    void escreveCelularComDddECincoDigitosNoPrefixo() {
        assertEquals("(11) 98877-1200", FormatoOficina.telefone("11988771200"));
    }

    @Test
    void escreveTelefoneFixoComDddEQuatroDigitosNoPrefixo() {
        assertEquals("(11) 3344-8800", FormatoOficina.telefone("1133448800"));
    }

    @Test
    void devolveTelefoneDeTamanhoDesconhecidoComoEstaEscrito() {
        assertEquals("0800 123 4567", FormatoOficina.telefone("0800 123 4567"));
    }

    @Test
    void naoInventaTelefoneQuandoNaoHaNenhum() {
        assertNull(FormatoOficina.telefone(null));
        assertNull(FormatoOficina.telefone(""));
    }

    @Test
    void mostraDataHoraNoFusoDaOficinaENaoEmUtc() {
        // 12/08/2026 às 17:07 UTC é 14:07 em Brasília
        var instante = Instant.parse("2026-08-12T17:07:00Z");

        assertEquals("12/08/26 às 14:07", FormatoOficina.dataHora(instante));
    }

    @Test
    void mostraTracoQuandoNaoHaData() {
        assertEquals("—", FormatoOficina.dataHora(null));
    }

    @Test
    void encurtaOIdDaOrdemParaOitoCaracteresCitaveisAoTelefone() {
        var id = UUID.fromString("4a9c0039-1f2e-4c3b-9a8d-7e6f5d4c3b2a");

        assertEquals("4a9c0039", FormatoOficina.numeroOrdem(id));
    }
}
