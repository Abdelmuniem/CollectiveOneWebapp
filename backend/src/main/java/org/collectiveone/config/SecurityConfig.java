package org.collectiveone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.auth0.spring.security.api.JwtWebSecurityConfigurer;

@EnableWebSecurity(debug = false)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${auth0.issuer}")
    private String issuer;

    @Value("${auth0.audience}")
    private String audience;
    
    @Value("${auth0.secret}")
    private String secret;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	 JwtWebSecurityConfigurer.forHS256(audience, issuer, secret.getBytes())
         .configure(http)
         .authorizeRequests()
         .antMatchers("/1/**").fullyAuthenticated();
    	 
    	 http.headers().frameOptions().disable();
    	
    	//http.authorizeRequests().antMatchers("/").permitAll();
    }
    
}