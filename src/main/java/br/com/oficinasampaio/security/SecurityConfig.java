package br.com.oficinasampaio.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/error").permitAll()
                        // A identidade visual precisa carregar antes do login:
                        // a tela de entrada usa a mesma folha, o mesmo script e
                        // as mesmas fontes.
                        .requestMatchers("/css/**", "/js/**", "/fontes/**", "/img/**",
                                "/favicon.svg", "/favicon.ico").permitAll()
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        // O balcão registra pagamento e vê o que falta receber;
                        // a posição do caixa e o lançamento de saída são do dono.
                        .requestMatchers("/financeiro/**").hasRole("ADMIN")
                        // Faturamento e caixa também. Já a via impressa da ordem é
                        // documento de atendimento, não relatório gerencial: fica
                        // com quem está no balcão.
                        .requestMatchers("/relatorios/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/clientes", true)
                        .failureUrl("/login?erro")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
