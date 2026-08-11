package br.com.oficinasampaio.usuario.application;

import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.usuario.domain.Usuario;
import br.com.oficinasampaio.usuario.domain.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarUsuario {

    private final UsuarioRepository usuarioRepository;
    private final CodificadorSenha codificadorSenha;

    public CadastrarUsuario(UsuarioRepository usuarioRepository, CodificadorSenha codificadorSenha) {
        this.usuarioRepository = usuarioRepository;
        this.codificadorSenha = codificadorSenha;
    }

    @Transactional
    public UsuarioView executar(CadastrarUsuarioCommand command) {
        PoliticaSenhaUsuario.validar(command.senha());
        var usuario = Usuario.cadastrar(
                command.nome(),
                command.login(),
                codificadorSenha.codificar(command.senha()),
                command.perfil().paraDominio()
        );
        if (usuarioRepository.existePorLogin(usuario.getLogin())) {
            throw new RegraNegocioException("Login já cadastrado");
        }
        return UsuarioView.de(usuarioRepository.salvar(usuario));
    }

}
