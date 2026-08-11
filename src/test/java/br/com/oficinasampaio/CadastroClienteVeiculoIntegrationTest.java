package br.com.oficinasampaio;

import br.com.oficinasampaio.cliente.application.CadastrarCliente;
import br.com.oficinasampaio.cliente.application.CadastrarClienteCommand;
import br.com.oficinasampaio.security.AdministradorInicialProperties;
import br.com.oficinasampaio.security.AdministradorInicializador;
import br.com.oficinasampaio.veiculo.application.CadastrarVeiculo;
import br.com.oficinasampaio.veiculo.application.CadastrarVeiculoCommand;
import br.com.oficinasampaio.veiculo.application.ListarVeiculosDoCliente;
import br.com.oficinasampaio.usuario.application.CadastrarUsuario;
import br.com.oficinasampaio.usuario.application.CadastrarUsuarioCommand;
import br.com.oficinasampaio.usuario.application.ListarUsuarios;
import br.com.oficinasampaio.usuario.application.PerfilUsuarioView;
import br.com.oficinasampaio.usuario.application.GarantirAdministradorInicial;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = "app.bootstrap-admin.enabled=false")
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class CadastroClienteVeiculoIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("oficina_sampaio_test")
            .withUsername("oficina")
            .withPassword("oficina");

    @Autowired
    private CadastrarCliente cadastrarCliente;

    @Autowired
    private CadastrarVeiculo cadastrarVeiculo;

    @Autowired
    private ListarVeiculosDoCliente listarVeiculosDoCliente;

    @Autowired
    private CadastrarUsuario cadastrarUsuario;

    @Autowired
    private ListarUsuarios listarUsuarios;

    @Autowired
    private GarantirAdministradorInicial garantirAdministradorInicial;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void persisteClienteESeuVeiculoNoPostgresql() {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Maria da Silva", "529.982.247-25", "11999998888", "maria@example.com"
        ));

        cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                cliente.id(), "ABC-1D23", "Volkswagen", "Gol",
                2022, "Prata", 35_000L
        ));

        var veiculos = listarVeiculosDoCliente.executar(cliente.id());

        assertNotNull(cliente.id());
        assertEquals(1, veiculos.size());
        assertAll(
                () -> assertEquals(cliente.id(), veiculos.getFirst().clienteId()),
                () -> assertEquals("ABC1D23", veiculos.getFirst().placa())
        );
    }

    @Test
    void cadastraClientePelaInterfaceWebEExibeNaListagem() throws Exception {
        mockMvc.perform(post("/clientes")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("nome", "João da Oficina")
                        .param("cpfCnpj", "111.444.777-35")
                        .param("telefone", "(11) 98888-7777")
                        .param("email", "joao@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clientes"));

        mockMvc.perform(get("/clientes")
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/lista"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("João da Oficina")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("11144477735")));
    }

    @Test
    void cadastraVeiculoPelaInterfaceWebDoCliente() throws Exception {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Carlos Mecânico", "935.411.347-80", null, null
        ));
        var caminho = "/clientes/" + cliente.id() + "/veiculos";

        mockMvc.perform(post(caminho)
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("placa", "def-4g56")
                        .param("marca", "Fiat")
                        .param("modelo", "Strada")
                        .param("ano", "2024")
                        .param("cor", "Branca")
                        .param("quilometragem", "12000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(caminho));

        mockMvc.perform(get(caminho)
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(status().isOk())
                .andExpect(view().name("veiculos/lista"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Carlos Mecânico")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("DEF4G56")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Fiat Strada")));
    }

    @Test
    void rejeitaPlacaInvalidaNaInterfaceWeb() throws Exception {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Ana da Oficina", "390.533.447-05", null, null
        ));

        mockMvc.perform(post("/clientes/" + cliente.id() + "/veiculos")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("placa", "ABC")
                        .param("marca", "Fiat")
                        .param("modelo", "Uno"))
                .andExpect(status().isOk())
                .andExpect(view().name("veiculos/lista"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Placa inválida")));
    }

    @Test
    void respondeNaoEncontradoAoCadastrarVeiculoParaClienteDesconhecido() throws Exception {
        mockMvc.perform(post("/clientes/" + UUID.randomUUID() + "/veiculos")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("placa", "ABC-1D23")
                        .param("marca", "Fiat")
                        .param("modelo", "Uno"))
                .andExpect(status().isNotFound());
    }

    @Test
    void persisteUsuarioNoPostgresql() {
        cadastrarUsuario.executar(new CadastrarUsuarioCommand(
                "Maria Administradora", "maria", "segredo123", PerfilUsuarioView.ADMIN
        ));

        var usuarios = listarUsuarios.executar();

        assertEquals(1, usuarios.size());
        assertEquals("maria", usuarios.getFirst().login());
        assertEquals(PerfilUsuarioView.ADMIN, usuarios.getFirst().perfil());
    }

    @Test
    void exigeAutenticacaoParaAcessarClientes() throws Exception {
        mockMvc.perform(get("/clientes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void autenticaUsuarioPersistidoComSenhaBCrypt() throws Exception {
        cadastrarUsuario.executar(new CadastrarUsuarioCommand(
                "Maria Administradora", "maria", "segredo123", PerfilUsuarioView.ADMIN
        ));

        mockMvc.perform(formLogin().user("maria").password("segredo123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clientes"))
                .andExpect(authenticated().withUsername("maria"));
    }

    @Test
    void administradorCadastraUsuarioPelaInterfaceWeb() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("nome", "João Funcionário")
                        .param("login", "joao")
                        .param("senha", "segredo123")
                        .param("perfil", "FUNCIONARIO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios"));

        mockMvc.perform(get("/usuarios")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("João Funcionário")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FUNCIONARIO")));
    }

    @Test
    void funcionarioNaoAcessaGestaoDeUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios")
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejeitaSenhaInicialCurtaNaInterfaceWeb() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("nome", "João Funcionário")
                        .param("login", "joao")
                        .param("senha", "curta")
                        .param("perfil", "FUNCIONARIO"))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/lista"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(
                        "Senha deve possuir de 8 a 72 caracteres"
                )));
    }

    @Test
    void inicializadorCriaAdministradorUmaUnicaVezNoPostgresql() throws Exception {
        var properties = new AdministradorInicialProperties(
                true, "Administrador Inicial", "bootstrap", "segredo123"
        );
        var inicializador = new AdministradorInicializador(properties, garantirAdministradorInicial);

        inicializador.run(null);
        inicializador.run(null);

        var usuarios = listarUsuarios.executar();
        assertEquals(1, usuarios.size());
        assertEquals("bootstrap", usuarios.getFirst().login());

        mockMvc.perform(formLogin().user("bootstrap").password("segredo123"))
                .andExpect(authenticated().withUsername("bootstrap"));
    }

    @Test
    void usuarioAutenticadoPodeEncerrarSessao() throws Exception {
        cadastrarUsuario.executar(new CadastrarUsuarioCommand(
                "Maria Administradora", "maria", "segredo123", PerfilUsuarioView.ADMIN
        ));
        var login = mockMvc.perform(formLogin().user("maria").password("segredo123"))
                .andExpect(authenticated())
                .andReturn();
        var sessao = (MockHttpSession) login.getRequest().getSession(false);

        mockMvc.perform(post("/logout").session(sessao).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());
    }
}
