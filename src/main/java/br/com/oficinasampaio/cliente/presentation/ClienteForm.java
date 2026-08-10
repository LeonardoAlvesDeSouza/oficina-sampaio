package br.com.oficinasampaio.cliente.presentation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve possuir no máximo 150 caracteres")
    private String nome;

    @Size(max = 18, message = "CPF/CNPJ inválido")
    private String cpfCnpj;

    @Size(max = 20, message = "Telefone deve possuir no máximo 20 caracteres")
    private String telefone;

    @Email(message = "E-mail inválido")
    @Size(max = 150, message = "E-mail deve possuir no máximo 150 caracteres")
    private String email;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
