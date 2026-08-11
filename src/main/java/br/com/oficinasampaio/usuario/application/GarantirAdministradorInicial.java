package br.com.oficinasampaio.usuario.application;

import br.com.oficinasampaio.usuario.domain.UsuarioRepository;
import br.com.oficinasampaio.usuario.domain.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GarantirAdministradorInicial {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorSenha codificadorSenha;

    public GarantirAdministradorInicial(
            UsuarioRepository usuarioRepository,
            CodificadorSenha codificadorSenha
    ) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorSenha = codificadorSenha;
    }

    @Transactional
    public void executar(String nome, String login, String senha) {
        PoliticaSenhaUsuario.validar(senha);
        var administrador = Usuario.cadastrar(
                nome,
                login,
                codificadorSenha.codificar(senha),
                PerfilUsuarioView.ADMIN.paraDominio()
        );
        usuarioRepository.salvarSeLoginAusente(administrador);
    }
}
