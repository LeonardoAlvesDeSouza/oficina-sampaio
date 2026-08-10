package br.com.oficinasampaio.cliente.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "cpf_cnpj", length = 14, unique = true)
    private String cpfCnpj;

    @Column(length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false)
    private boolean ativo;

    @Version
    private long versao;

    protected Cliente() {
    }

    private Cliente(String nome, String cpfCnpj, String telefone, String email) {
        this.nome = textoObrigatorio(nome, "Nome");
        this.cpfCnpj = somenteDigitosOuNulo(cpfCnpj);
        this.telefone = somenteDigitosOuNulo(telefone);
        this.email = textoOuNulo(email, true);
        this.ativo = true;
    }

    public static Cliente cadastrar(String nome, String cpfCnpj, String telefone, String email) {
        return new Cliente(nome, cpfCnpj, telefone, email);
    }

    public void inativar() {
        this.ativo = false;
    }

    private static String textoObrigatorio(String valor, String campo) {
        var normalizado = textoOuNulo(valor, false);
        if (normalizado == null) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
        return normalizado;
    }

    private static String textoOuNulo(String valor, boolean minusculo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        var normalizado = valor.trim();
        return minusculo ? normalizado.toLowerCase(Locale.ROOT) : normalizado;
    }

    private static String somenteDigitosOuNulo(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean isInativo() {
        return !ativo;
    }
}
