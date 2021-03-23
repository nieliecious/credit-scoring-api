package com.enigma.creditscoringapi.security;

import com.enigma.creditscoringapi.security.jwt.AuthEntryPointJwt;
import com.enigma.creditscoringapi.security.service.UserDetailsServiceImpl;
import com.enigma.creditscoringapi.services.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    RoleService roleService;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter(){
        return new AuthTokenFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        String[] roles = new String[roleService.allRoleName().size()];
        roles = roleService.allRoleName().toArray(roles);

        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/auth/**").permitAll()
                .antMatchers(HttpMethod.GET,"/users").hasAnyAuthority("SUPERVISOR", "MASTER")
                .antMatchers(HttpMethod.PATCH, "/users/**").hasAnyAuthority(roles)
                .antMatchers(HttpMethod.GET, "/users/email/**").hasAnyAuthority(roles)
                .antMatchers(HttpMethod.GET, "/users/password/**").hasAnyAuthority(roles)
                .antMatchers("/customer/**").hasAnyAuthority("STAFF", "MASTER")
                .antMatchers(HttpMethod.POST, "/transaction").hasAnyAuthority("STAFF", "MASTER")
                .antMatchers("/transaction/**").hasAnyAuthority("SUPERVISOR", "MASTER")
                .antMatchers(HttpMethod.POST, "/approval").hasAnyAuthority("SUPERVISOR", "MASTER")
                .antMatchers("/approval/**").permitAll()
                .antMatchers("/report/**").permitAll()
                .anyRequest().hasAuthority("MASTER");

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
