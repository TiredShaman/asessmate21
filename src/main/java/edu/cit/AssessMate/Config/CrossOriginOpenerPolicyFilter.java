package edu.cit.AssessMate.Config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CrossOriginOpenerPolicyFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Allow popups to communicate with the parent window
            httpResponse.setHeader("Cross-Origin-Opener-Policy", "unsafe-none");
            httpResponse.setHeader("Cross-Origin-Embedder-Policy", "unsafe-none");
            httpResponse.setHeader("Cross-Origin-Resource-Policy", "cross-origin");
            
            // Add additional security headers
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            chain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Error in CrossOriginOpenerPolicyFilter", e);
        }
    }
}