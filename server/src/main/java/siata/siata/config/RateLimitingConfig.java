package siata.siata.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingConfig implements Filter {

    // Cache untuk menyimpan jumlah request per IP address
    private final LoadingCache<String, Integer> requestCountsPerIpAddress;

    public RateLimitingConfig() {
        super();
        // Cache expire setiap 1 menit
        requestCountsPerIpAddress = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        
        String clientIpAddress = getClientIP(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();
        String method = httpServletRequest.getMethod();
        
        // Tentukan limit berdasarkan endpoint dan method
        int limit = determineLimit(requestURI, method);
        
        try {
            int requests = requestCountsPerIpAddress.get(clientIpAddress);
            
            if (requests >= limit) {
                httpServletResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpServletResponse.getWriter().write("Terlalu banyak request. Silakan coba lagi dalam 1 menit.");
                return;
            }
            
            requestCountsPerIpAddress.put(clientIpAddress, requests + 1);
            filterChain.doFilter(servletRequest, servletResponse);
            
        } catch (ExecutionException e) {
            throw new ServletException(e);
        }
    }

    private int determineLimit(String requestURI, String method) {
        // Login/Auth endpoints: 5 request/menit (prevent brute force)
        if (requestURI.contains("/api/auth/login") || requestURI.contains("/api/auth/register")) {
            return 5;
        }
        
        // Create/Update/Delete operations: 30 request/menit
        if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE")) {
            return 30;
        }
        
        // Read operations (GET): 100 request/menit
        if (method.equals("GET")) {
            return 100;
        }
        
        // Default: 60 request/menit
        return 60;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
