package com.example.countrymanager.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class RestSecurity extends WebSecurityConfigurerAdapter {

    @Value("${userrole.username}")
    private String userRoleUsername;

    @Value("${userrole.password}")
    private String userRolePassword;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser(userRoleUsername)
                .password(encoder.encode(userRolePassword))
                .roles("USER")
                .and()
                .withUser(adminUsername)
                .password(encoder.encode(adminPassword))
                .roles("USER", "ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers(HttpMethod.POST).access("hasRole('ADMIN')")
                .antMatchers(HttpMethod.PUT).access("hasRole('ADMIN')")
                .antMatchers(HttpMethod.DELETE).access("hasRole('ADMIN')")
                .antMatchers(HttpMethod.GET).access("hasAnyRole('USER', 'ADMIN')")
                .and()
                .httpBasic();

    }

}
