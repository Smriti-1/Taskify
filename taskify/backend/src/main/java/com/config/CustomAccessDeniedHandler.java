//package com.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
//
//import java.io.IOException;
//import java.util.Map;
//
//@Component
//public class CustomAccessDeniedHandler implements AccessDeniedHandler {
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @Override
//    public void handle(HttpServletRequest request,
//                       HttpServletResponse response,
//                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
//
//        String accept = request.getHeader("Accept");
//
//        if (accept != null && accept.contains("application/json")) {
//            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//            response.setContentType("application/json");
//            Map<String, Object> body = Map.of(
//                "status", 403,
//                "error", "Forbidden",
//                "message", "You do not have permission to access this resource.",
//                "path", request.getRequestURI()
//            );
//            response.getWriter().write(mapper.writeValueAsString(body));
//        } else {
//            response.sendRedirect("/403.html");
//        }
//    }
//}
