package br.com.oficinasampaio.security;

import br.com.oficinasampaio.usuario.application.GarantirAdministradorInicial;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdministradorInicializador implements ApplicationRunner {

    private final AdministradorInicialProperties properties;
    private final GarantirAdministradorInicial garantirAdministradorInicial;

    public AdministradorInicializador(
            AdministradorInicialProperties properties,
            GarantirAdministradorInicial garantirAdministradorInicial
    ) {
        this.properties = properties;
        this.garantirAdministradorInicial = garantirAdministradorInicial;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.enabled()) {
            garantirAdministradorInicial.executar(
                    properties.nome(),
                    properties.login(),
                    properties.senha()
            );
        }
    }
}
