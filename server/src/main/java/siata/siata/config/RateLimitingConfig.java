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
        // Rate limiting dinonaktifkan sesuai request
        filterChain.doFilter(servletRequest, servletResponse);
        
        /* Logika rate limiting sebelumnya di-skip
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        
        String clientIpAddress = getClientIP(httpServletRequest);
        ...
        */
    }

    private int determineLimit(String requestURI, String method) {
        // Login/Auth endpoints: 10 request/menit (prevent brute force)
        if (requestURI.contains("/api/auth/login") || requestURI.contains("/api/auth/register")) {
            return 10;
        }
        
        // Create/Update/Delete operations: 200 request/menit (lebih permisif untuk development)
        if (method.equals("POST") || method.equals("PUT") || method.equals("DELETE") || method.equals("PATCH")) {
            return 200;
        }
        
        // Read operations (GET): 500 request/menit
        if (method.equals("GET")) {
            return 500;
        }
        
        // Default: 300 request/menit
        return 300;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
    
    private boolean isLocalhost(String ipAddress) {
        return ipAddress.equals("127.0.0.1") || 
               ipAddress.equals("0:0:0:0:0:0:0:1") || 
               ipAddress.equals("::1") ||
               ipAddress.equals("localhost");
    }
}
