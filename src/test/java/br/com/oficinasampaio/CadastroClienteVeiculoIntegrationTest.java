package br.com.oficinasampaio;

import br.com.oficinasampaio.cliente.application.CadastrarCliente;
import br.com.oficinasampaio.cliente.application.CadastrarClienteCommand;
import br.com.oficinasampaio.security.AdministradorInicialProperties;
import br.com.oficinasampaio.security.AdministradorInicializador;
import br.com.oficinasampaio.ordemservico.application.AbrirOrdemServico;
import br.com.oficinasampaio.ordemservico.application.AbrirOrdemServicoCommand;
import br.com.oficinasampaio.ordemservico.application.AdicionarItemOrdemServico;
import br.com.oficinasampaio.ordemservico.application.AdicionarItemOrdemServicoCommand;
import br.com.oficinasampaio.ordemservico.application.AcaoOrdemServicoView;
import br.com.oficinasampaio.ordemservico.application.AlterarStatusOrdemServico;
import br.com.oficinasampaio.ordemservico.application.AlterarStatusOrdemServicoCommand;
import br.com.oficinasampaio.ordemservico.application.BuscarOrdemServico;
import br.com.oficinasampaio.ordemservico.application.ListarOrdensServico;
import br.com.oficinasampaio.ordemservico.application.StatusOrdemServicoView;
import br.com.oficinasampaio.ordemservico.application.TipoItemOrdemServicoView;
import br.com.oficinasampaio.veiculo.application.CadastrarVeiculo;
import br.com.oficinasampaio.veiculo.application.CadastrarVeiculoCommand;
import br.com.oficinasampaio.veiculo.application.ListarVeiculosDoCliente;
import br.com.oficinasampaio.usuario.application.CadastrarUsuario;
import br.com.oficinasampaio.usuario.application.CadastrarUsuarioCommand;
import br.com.oficinasampaio.usuario.application.ListarUsuarios;
import br.com.oficinasampaio.usuario.application.PerfilUsuarioView;
import br.com.oficinasampaio.usuario.application.GarantirAdministradorInicial;
import jakarta.persistence.EntityManager;
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
import java.math.BigDecimal;
import java.util.Map;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
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
    private AbrirOrdemServico abrirOrdemServico;

    @Autowired
    private AdicionarItemOrdemServico adicionarItemOrdemServico;

    @Autowired
    private AlterarStatusOrdemServico alterarStatusOrdemServico;

    @Autowired
    private BuscarOrdemServico buscarOrdemServico;

    @Autowired
    private ListarOrdensServico listarOrdensServico;

    @Autowired
    private CadastrarUsuario cadastrarUsuario;

    @Autowired
    private ListarUsuarios listarUsuarios;

    @Autowired
    private GarantirAdministradorInicial garantirAdministradorInicial;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

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
    void persisteOrdemServicoESeusItensNoPostgresql() {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "José Sampaio", "168.995.350-09", "11999998888", null
        ));
        var veiculo = cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                cliente.id(), "GHI-7J89", "Chevrolet", "Onix",
                2023, "Preto", 18_500L
        ));
        var ordem = abrirOrdemServico.executar(new AbrirOrdemServicoCommand(
                veiculo.id(), "Troca das pastilhas de freio"
        ));

        adicionarItemOrdemServico.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.PECA, "Jogo de pastilhas",
                new BigDecimal("1"), new BigDecimal("280.00")
        ));

        var detalhe = buscarOrdemServico.executar(ordem.id());
        assertAll(
                () -> assertNotNull(detalhe.id()),
                () -> assertEquals(1, detalhe.itens().size()),
                () -> assertEquals(new BigDecimal("280.00"), detalhe.totalPecas()),
                () -> assertEquals(new BigDecimal("280.00"), detalhe.total())
        );
    }

    @Test
    void abreOrdemEAdicionaServicoPelaInterfaceWeb() throws Exception {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Paulo da Oficina", "013.837.760-05", null, null
        ));
        var veiculo = cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                cliente.id(), "JKL-2M34", "Ford", "Ka",
                2020, "Branco", 48_000L
        ));

        mockMvc.perform(post("/ordens-servico")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("veiculoId", veiculo.id().toString())
                        .param("relatoProblema", "Barulho ao frear"))
                .andExpect(status().is3xxRedirection());

        var ordem = listarOrdensServico.executar().getFirst();
        mockMvc.perform(post("/ordens-servico/" + ordem.id() + "/itens")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("tipo", "SERVICO")
                        .param("descricao", "Diagnóstico do sistema de freios")
                        .param("quantidade", "1")
                        .param("valorUnitario", "90.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ordens-servico/" + ordem.id()));

        mockMvc.perform(get("/ordens-servico/" + ordem.id())
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(status().isOk())
                .andExpect(view().name("ordensservico/detalhe"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Barulho ao frear")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Diagnóstico do sistema de freios")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("90,00")));

        mockMvc.perform(get("/ordens-servico")
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(status().isOk())
                .andExpect(view().name("ordensservico/lista"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Barulho ao frear")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("90,00")));
    }

    @Test
    void avancaCicloOperacionalPelaInterfaceWeb() throws Exception {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Oficina Ciclo", "028.286.100-08", null, null
        ));
        var veiculo = cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                cliente.id(), "MNO-5P67", "Honda", "Fit",
                2019, "Cinza", 62_000L
        ));
        var ordem = abrirOrdemServico.executar(new AbrirOrdemServicoCommand(
                veiculo.id(), "Revisão do sistema elétrico"
        ));
        adicionarItemOrdemServico.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.SERVICO, "Diagnóstico elétrico",
                BigDecimal.ONE, new BigDecimal("150.00")
        ));

        var caminhoStatus = "/ordens-servico/" + ordem.id() + "/status";
        mockMvc.perform(post(caminhoStatus)
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("acao", "INICIAR_EXECUCAO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ordens-servico/" + ordem.id()));

        mockMvc.perform(get("/ordens-servico/" + ordem.id())
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("EM_EXECUCAO")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Aguardar peça")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Adicionar item")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Cancelar")
                )));

        mockMvc.perform(post(caminhoStatus)
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("acao", "AGUARDAR_PECA"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/ordens-servico/" + ordem.id() + "/itens")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("tipo", "PECA")
                        .param("descricao", "Sensor de rotação")
                        .param("quantidade", "1")
                        .param("valorUnitario", "210.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeCount(0));

        entityManager.flush();
        entityManager.clear();
        var detalhe = buscarOrdemServico.executar(ordem.id());
        assertAll(
                () -> assertEquals(StatusOrdemServicoView.AGUARDANDO_PECA, detalhe.status()),
                () -> assertEquals(new BigDecimal("210.00"), detalhe.totalPecas()),
                () -> assertEquals(new BigDecimal("360.00"), detalhe.total())
        );
    }

    @Test
    void somenteAdministradorCancelaOrdemPelaInterfaceWeb() throws Exception {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Oficina Cancelamento", "064.503.780-06", null, null
        ));
        var veiculo = cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                cliente.id(), "PQR-8S90", "Fiat", "Argo",
                2021, "Vermelho", 30_000L
        ));
        var ordem = abrirOrdemServico.executar(new AbrirOrdemServicoCommand(
                veiculo.id(), "Revisão dos freios"
        ));
        var caminhoStatus = "/ordens-servico/" + ordem.id() + "/status";

        mockMvc.perform(post(caminhoStatus)
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("acao", "CANCELAR"))
                .andExpect(status().isForbidden());

        entityManager.flush();
        entityManager.clear();
        assertEquals(StatusOrdemServicoView.ABERTA, buscarOrdemServico.executar(ordem.id()).status());

        mockMvc.perform(get("/ordens-servico/" + ordem.id())
                        .with(user("administrador").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cancelar")));

        mockMvc.perform(post(caminhoStatus)
                        .with(user("administrador").roles("ADMIN"))
                        .with(csrf())
                        .param("acao", "CANCELAR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("sucesso", "Status da ordem atualizado"));

        entityManager.flush();
        entityManager.clear();
        assertEquals(StatusOrdemServicoView.CANCELADA, buscarOrdemServico.executar(ordem.id()).status());
    }

    @Test
    void informaRegraDeNegocioAoTentarAdicionarItemEmOrdemFinalizada() throws Exception {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Oficina Finalizada", "088.755.230-09", null, null
        ));
        var veiculo = cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                cliente.id(), "STU-1V23", "Chevrolet", "Onix",
                2022, "Prata", 15_000L
        ));
        var ordem = abrirOrdemServico.executar(new AbrirOrdemServicoCommand(
                veiculo.id(), "Troca de pastilhas"
        ));
        adicionarItemOrdemServico.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.SERVICO, "Troca de pastilhas",
                BigDecimal.ONE, new BigDecimal("180.00")
        ));
        alterarStatusOrdemServico.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.INICIAR_EXECUCAO
        ));
        alterarStatusOrdemServico.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.FINALIZAR
        ));

        mockMvc.perform(post("/ordens-servico/" + ordem.id() + "/itens")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf())
                        .param("tipo", "PECA")
                        .param("descricao", "Filtro de óleo")
                        .param("quantidade", "1")
                        .param("valorUnitario", "35.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ordens-servico/" + ordem.id()))
                .andExpect(flash().attribute(
                        "erro",
                        "Itens só podem ser alterados enquanto a ordem não está finalizada"
                ));

        entityManager.flush();
        entityManager.clear();
        assertEquals(1, buscarOrdemServico.executar(ordem.id()).itens().size());
    }

    @Test
    void removeItemPelaInterfaceWebEnquantoAOrdemAceitaAlteracao() throws Exception {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Oficina Retirada", "398.286.760-27", null, null
        ));
        var veiculo = cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                cliente.id(), "VWX-4Y56", "Renault", "Sandero",
                2018, "Azul", 71_000L
        ));
        var ordem = abrirOrdemServico.executar(new AbrirOrdemServicoCommand(
                veiculo.id(), "Revisão da suspensão"
        ));
        adicionarItemOrdemServico.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.SERVICO, "Alinhamento",
                BigDecimal.ONE, new BigDecimal("120.00")
        ));
        var comPeca = adicionarItemOrdemServico.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.PECA, "Amortecedor lançado por engano",
                BigDecimal.ONE, new BigDecimal("350.00")
        ));
        entityManager.flush();
        var pecaId = comPeca.itens().getLast().id();
        assertNotNull(pecaId);

        mockMvc.perform(get("/ordens-servico/" + ordem.id())
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Remover")));

        mockMvc.perform(post("/ordens-servico/" + ordem.id() + "/itens/" + pecaId + "/remover")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ordens-servico/" + ordem.id()))
                .andExpect(flash().attribute("sucesso", "Item removido da ordem"));

        entityManager.flush();
        entityManager.clear();
        var detalhe = buscarOrdemServico.executar(ordem.id());
        assertAll(
                () -> assertEquals(1, detalhe.itens().size()),
                () -> assertEquals("Alinhamento", detalhe.itens().getFirst().descricao()),
                () -> assertEquals(new BigDecimal("0.00"), detalhe.totalPecas()),
                () -> assertEquals(new BigDecimal("120.00"), detalhe.total())
        );
    }

    @Test
    void recusaRemocaoDeItemDeOrdemFinalizada() throws Exception {
        var cliente = cadastrarCliente.executar(new CadastrarClienteCommand(
                "Oficina Valor Fechado", "191.181.130-05", null, null
        ));
        var veiculo = cadastrarVeiculo.executar(new CadastrarVeiculoCommand(
                cliente.id(), "YZA-7B89", "Hyundai", "HB20",
                2021, "Preto", 25_000L
        ));
        var ordem = abrirOrdemServico.executar(new AbrirOrdemServicoCommand(
                veiculo.id(), "Troca de correia"
        ));
        var comItens = adicionarItemOrdemServico.executar(new AdicionarItemOrdemServicoCommand(
                ordem.id(), TipoItemOrdemServicoView.SERVICO, "Troca de correia",
                BigDecimal.ONE, new BigDecimal("240.00")
        ));
        entityManager.flush();
        var itemId = comItens.itens().getFirst().id();
        alterarStatusOrdemServico.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.INICIAR_EXECUCAO
        ));
        alterarStatusOrdemServico.executar(new AlterarStatusOrdemServicoCommand(
                ordem.id(), AcaoOrdemServicoView.FINALIZAR
        ));

        mockMvc.perform(post("/ordens-servico/" + ordem.id() + "/itens/" + itemId + "/remover")
                        .with(user("funcionario").roles("FUNCIONARIO"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ordens-servico/" + ordem.id()))
                .andExpect(flash().attribute(
                        "erro",
                        "Itens só podem ser alterados enquanto a ordem não está finalizada"
                ));

        entityManager.flush();
        entityManager.clear();
        assertEquals(1, buscarOrdemServico.executar(ordem.id()).itens().size());

        mockMvc.perform(get("/ordens-servico/" + ordem.id())
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Remover")
                )));
    }

    @Test
    void exibePagamentosFinanceiroERelatoriosComoModulosEmConstrucao() throws Exception {
        var modulos = Map.of(
                "/pagamentos", "Pagamentos",
                "/financeiro", "Financeiro",
                "/relatorios", "Relatórios"
        );

        for (var modulo : modulos.entrySet()) {
            mockMvc.perform(get(modulo.getKey())
                            .with(user("funcionario").roles("FUNCIONARIO")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("standby/modulo"))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString(modulo.getValue())))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Módulo em construção")));
        }

        mockMvc.perform(get("/pagamentos")
                        .with(user("funcionario").roles("FUNCIONARIO")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/pagamentos")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/financeiro")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/relatorios")));
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
                // O cadastro guarda só dígitos, mas a listagem devolve o documento e
                // o telefone pontuados: é assim que se confere em voz alta no balcão.
                .andExpect(content().string(org.hamcrest.Matchers.containsString("111.444.777-35")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("(11) 98888-7777")));
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
