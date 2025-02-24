// package com.altloc.backend.config;

// import java.io.IOException;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import com.altloc.backend.utils.JwtCore;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// public class TokenFilter extends OncePerRequestFilter {

//     @Autowired
//     private JwtCore jwtCore;

//     @Autowired
//     private UserDetailsService userDetailsService;

//     @Override
//     protected void doFilterInternal(
//             HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain filterChain)
//             throws ServletException, IOException {

//         String jwt = null;
//         String email = null;
//         UserDetails userDetails = null;

//         try {
//             String headerAuth = request.getHeader("Authorization");
//             if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
//                 jwt = headerAuth.substring(7);
//                 System.out.println("JWT: " + jwt);
//             }

//             if (jwt != null && jwtCore.validateToken(jwt)) {
//                 email = jwtCore.getUsernameFromToken(jwt);
//                 System.out.println("Username from JWT: " + email);

//                 if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                     userDetails = userDetailsService.loadUserByUsername(email);

//                     if (userDetails != null) {
//                         UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
//                                 null, userDetails.getAuthorities());

//                         SecurityContextHolder.getContext().setAuthentication(auth);
//                     }
//                 }
//             }
//         } catch (Exception e) {
//             System.err.println("Error in JWT processing: " + e.getMessage());
//             e.printStackTrace();
//         }

//         filterChain.doFilter(request, response);
//     }
// }

package com.altloc.backend.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.altloc.backend.utils.JwtCore;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtCore jwtCore;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @SuppressWarnings("null") HttpServletRequest request,
            @SuppressWarnings("null") HttpServletResponse response,
            @SuppressWarnings("null") FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;
        String email = null;
        UserDetails userDetails = null;
        String refreshToken = null;

        try {
            String headerAuth = request.getHeader("Authorization");
            if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
                jwt = headerAuth.substring(7);
            }

            // Проверка Access Token
            if (jwt != null && jwtCore.validateToken(jwt)) {
                email = jwtCore.getUsernameFromToken(jwt);
                System.out.println("Username from JWT: " + email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    userDetails = userDetailsService.loadUserByUsername(email);

                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } else {
                // Если Access Token не валиден, проверим Refresh Token
                String refreshTokenHeader = request.getHeader("Refresh-Token");
                if (refreshTokenHeader != null) {
                    refreshToken = refreshTokenHeader;

                    // Валидируем Refresh Token
                    if (jwtCore.validateToken(refreshToken)) {
                        email = jwtCore.getUsernameFromToken(refreshToken);
                        System.out.println("Username from Refresh Token: " + email);

                        // Если Refresh Token валиден, создаем новый Access Token
                        if (email != null) {
                            String newAccessToken = jwtCore.generateToken(
                                    SecurityContextHolder.getContext().getAuthentication());
                            response.setHeader("Authorization", "Bearer " + newAccessToken); // Устанавливаем новый
                                                                                             // Access Token в ответ

                            // Восстановление Authentication
                            userDetails = userDetailsService.loadUserByUsername(email);
                            if (userDetails != null) {
                                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null, userDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in JWT processing: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
