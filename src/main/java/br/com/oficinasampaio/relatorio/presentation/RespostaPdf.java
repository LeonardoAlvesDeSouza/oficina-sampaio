package br.com.oficinasampaio.relatorio.presentation;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Entrega o PDF para o navegador abrir na própria janela, não baixar: quem pede a
 * via da ordem quer conferir e mandar para a impressora, e o nome do arquivo só
 * importa quando decide salvar.
 */
final class RespostaPdf {

    private RespostaPdf() {
    }

    static ResponseEntity<byte[]> de(String nomeArquivo, byte[] conteudo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(nomeArquivo)
                        .build()
                        .toString())
                .body(conteudo);
    }
}
