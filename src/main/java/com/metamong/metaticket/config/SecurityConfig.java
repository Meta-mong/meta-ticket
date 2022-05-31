package com.metamong.metaticket.config;

import com.metamong.metaticket.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        // /main은 로그인 여부와 상관없이 접근 가능
        // /user/mu~는 USER 권한이 있어야만 접근 가능
        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/sign/mypage").hasRole("USER");

        http.formLogin()
                .loginPage("/sign/signin")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .failureUrl("/sign/signin")
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/sign/signout"))
                .logoutSuccessUrl("/");
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}