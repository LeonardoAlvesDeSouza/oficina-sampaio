package br.com.oficinasampaio.usuario.application;

import br.com.oficinasampaio.shared.domain.RegraNegocioException;
import br.com.oficinasampaio.usuario.domain.Usuario;
import br.com.oficinasampaio.usuario.domain.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioUseCasesTest {

    @Test
    void usuarioCadastradoPodeSerConsultado() {
        var repositorio = new UsuarioRepositoryEmMemoria();
        CodificadorSenha codificador = senha -> "codificada:" + senha;
        var cadastrar = new CadastrarUsuario(repositorio, codificador);
        var listar = new ListarUsuarios(repositorio);

        cadastrar.executar(new CadastrarUsuarioCommand(
                "Maria Administradora", "MARIA", "segredo123", PerfilUsuarioView.ADMIN
        ));

        var usuarios = listar.executar();

        assertEquals(1, usuarios.size());
        assertEquals("Maria Administradora", usuarios.getFirst().nome());
        assertEquals("maria", usuarios.getFirst().login());
        assertEquals(PerfilUsuarioView.ADMIN, usuarios.getFirst().perfil());
    }

    @Test
    void impedeCadastroComLoginDuplicado() {
        var repositorio = new UsuarioRepositoryEmMemoria();
        var cadastrar = new CadastrarUsuario(repositorio, senha -> "codificada:" + senha);
        cadastrar.executar(new CadastrarUsuarioCommand(
                "Maria", "maria", "segredo123", PerfilUsuarioView.ADMIN
        ));

        var erro = assertThrows(RegraNegocioException.class, () ->
                cadastrar.executar(new CadastrarUsuarioCommand(
                        "Outra Maria", " MARIA ", "outraSenha", PerfilUsuarioView.FUNCIONARIO
                ))
        );

        assertEquals("Login já cadastrado", erro.getMessage());
        assertEquals(1, new ListarUsuarios(repositorio).executar().size());
    }

    @Test
    void garanteUmUnicoAdministradorInicial() {
        var repositorio = new UsuarioRepositoryEmMemoria();
        var garantirAdministrador = new GarantirAdministradorInicial(
                repositorio, senha -> "codificada:" + senha
        );

        garantirAdministrador.executar("Administrador", "admin", "segredo123");
        garantirAdministrador.executar("Administrador", "admin", "segredo123");

        var usuarios = new ListarUsuarios(repositorio).executar();
        assertEquals(1, usuarios.size());
        assertEquals(PerfilUsuarioView.ADMIN, usuarios.getFirst().perfil());
        assertEquals("admin", usuarios.getFirst().login());
    }

    @Test
    void bootstrapContinuaIdempotenteComLeituraDeExistenciaConcorrente() {
        var repositorio = new UsuarioRepositoryEmMemoria() {
            @Override
            public boolean existePorLogin(String login) {
                return false;
            }
        };
        var garantirAdministrador = new GarantirAdministradorInicial(
                repositorio, senha -> "codificada:" + senha
        );

        garantirAdministrador.executar("Administrador", "admin", "segredo123");
        garantirAdministrador.executar("Administrador", "admin", "segredo123");

        assertEquals(1, new ListarUsuarios(repositorio).executar().size());
    }

    @Test
    void impedeCadastroComSenhaForaDoTamanhoPermitido() {
        var cadastrar = new CadastrarUsuario(
                new UsuarioRepositoryEmMemoria(),
                senha -> "codificada:" + senha
        );

        var erro = assertThrows(RegraNegocioException.class, () ->
                cadastrar.executar(new CadastrarUsuarioCommand(
                        "João", "joao", "curta", PerfilUsuarioView.FUNCIONARIO
                ))
        );

        assertEquals("Senha deve possuir de 8 a 72 caracteres", erro.getMessage());
    }

    private static class UsuarioRepositoryEmMemoria implements UsuarioRepository {

        private final List<Usuario> usuarios = new ArrayList<>();

        @Override
        public Usuario salvar(Usuario usuario) {
            usuarios.add(usuario);
            return usuario;
        }

        @Override
        public boolean salvarSeLoginAusente(Usuario usuario) {
            if (usuarios.stream().anyMatch(existente -> existente.getLogin().equals(usuario.getLogin()))) {
                return false;
            }
            usuarios.add(usuario);
            return true;
        }

        @Override
        public boolean existePorLogin(String login) {
            return usuarios.stream().anyMatch(usuario -> login.equals(usuario.getLogin()));
        }

        @Override
        public Optional<Usuario> buscarAtivoPorLogin(String login) {
            return usuarios.stream()
                    .filter(Usuario::isAtivo)
                    .filter(usuario -> login.equals(usuario.getLogin()))
                    .findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorId(UUID usuarioId) {
            return Optional.empty();
        }

        @Override
        public List<Usuario> listar() {
            return List.copyOf(usuarios);
        }
    }
}
