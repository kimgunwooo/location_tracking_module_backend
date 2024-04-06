package org.changppo.cost_management_service.security;

import lombok.RequiredArgsConstructor;
import org.changppo.cost_management_service.security.oauth2.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JdbcTemplate jdbcTemplate;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomLoginFailureHandler customLoginFailureHandler;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)

                .securityContext((securityContext) -> {
                    securityContext.securityContextRepository(delegatingSecurityContextRepository());
                    securityContext.requireExplicitSave(true);
                })

                .exceptionHandling((exceptionConfig) ->
                        exceptionConfig.authenticationEntryPoint(customAuthenticationEntryPoint).accessDeniedHandler(customAccessDeniedHandler)
                )

                .addFilterBefore(
                        new PreOAuth2AuthorizationRequestFilter(clientRegistrationRepository, new HttpSessionOAuth2AuthorizationRequestRepository()),
                        OAuth2LoginAuthenticationFilter.class)

                .oauth2Login((oauth2) -> oauth2
                        .authorizedClientRepository(authorizedClientRepository())
                        .authorizedClientService(oAuth2AuthorizedClientService(jdbcTemplate, clientRegistrationRepository))
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                            .userService(customOAuth2UserService))
                        .successHandler(customLoginSuccessHandler)
                        .failureHandler(customLoginFailureHandler))

                .logout(logout -> logout
                        .logoutUrl("/logout/oauth2")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(customLogoutSuccessHandler))

                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/**").hasAnyRole("FREE", "NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/members/**").hasAnyRole("FREE", "NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/apikeys/v1/createFreeKey").hasAnyRole("FREE", "NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/apikeys/v1/createClassicKey").hasAnyRole("NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/apikeys/**").hasAnyRole("FREE", "NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/apikeys/**").hasAnyRole("FREE", "NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/cards/v1/kakaopay/**").hasAnyRole("FREE", "NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cards/v1/kakaopay/**").hasAnyRole("FREE", "NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cards/**").hasAnyRole("NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/cards/**").hasAnyRole("NORMAL", "ADMIN")
                        .requestMatchers(HttpMethod.GET).permitAll()
                        .anyRequest().hasAnyRole("ADMIN"));

        return http.build();
    }

    @Bean
    public DelegatingSecurityContextRepository delegatingSecurityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

    @Bean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(JdbcTemplate jdbcTemplate, ClientRegistrationRepository clientRegistrationRepository) {
        return new JdbcOAuth2AuthorizedClientService(jdbcTemplate ,clientRegistrationRepository);
    }
}
