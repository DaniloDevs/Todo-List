package com.todo.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.todo.user.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var servlet = request.getServletPath();

        if (servlet.startsWith("/tasks/")) {
            // Get Authorization
            var authorization = request.getHeader("Authorization");
            var userPassword = authorization.substring("Basic".length()).trim();

            byte[] authDecode = Base64.getDecoder().decode(userPassword);
            var authString = new String(authDecode);

            String[] credential = authString.split(":");
            String username = credential[0];
            String password = credential[1];

            // Validate Credential
            var existUser = this.userRepository.findByUsername(username);

            if (existUser == null) {
                response.sendError(401, "Credentials Are Invalid");
            } else {

                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), existUser.getPassword());

                if (passwordVerify.verified) {
                    request.setAttribute("userId", existUser.getId());

                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401, "Incorrect Password");
                }
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
