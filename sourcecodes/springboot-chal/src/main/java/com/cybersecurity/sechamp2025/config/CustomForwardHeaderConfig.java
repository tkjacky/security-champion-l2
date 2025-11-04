package com.cybersecurity.sechamp2025.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

@Configuration
public class CustomForwardHeaderConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(CustomForwardHeaderConfig.class);

    @Value("${app.custom.forward-prefix-header:X-Forwarded-Prefix-2}")
    private String customPrefixHeader;

    @Value("${app.custom.enable-custom-prefix:true}")
    private boolean enableCustomPrefix;

    /**
     * Disable Spring Boot's default ForwardedHeaderFilter and replace with our custom one
     */
    @Bean
    @ConditionalOnProperty(name = "app.custom.enable-custom-prefix", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        // Disable the default ForwardedHeaderFilter
        FilterRegistrationBean<ForwardedHeaderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ForwardedHeaderFilter());
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<CustomForwardedHeaderFilter> customForwardedHeaderFilter() {
        FilterRegistrationBean<CustomForwardedHeaderFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CustomForwardedHeaderFilter(customPrefixHeader, enableCustomPrefix));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE); // Execute first
        registrationBean.setName("customForwardedHeaderFilter");
        logger.info("Registering CustomForwardedHeaderFilter with header: {} enabled: {}", 
                   customPrefixHeader, enableCustomPrefix);
        return registrationBean;
    }

    /**
     * Custom filter to handle custom X-Forwarded-Prefix header
     */
    public static class CustomForwardedHeaderFilter implements Filter {
        
        private static final Logger logger = LoggerFactory.getLogger(CustomForwardedHeaderFilter.class);
        
        private final String customPrefixHeader;
        private final boolean enableCustomPrefix;
        
        public CustomForwardedHeaderFilter(String customPrefixHeader, boolean enableCustomPrefix) {
            this.customPrefixHeader = customPrefixHeader;
            this.enableCustomPrefix = enableCustomPrefix;
        }
        
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            // Initialization if needed
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            
            // Check if custom prefix handling is enabled
            if (!enableCustomPrefix) {
                logger.debug("Custom prefix handling is disabled, passing through");
                chain.doFilter(request, response);
                return;
            }
            
            // Get both headers
            String customPrefix = httpRequest.getHeader(customPrefixHeader);
            String originalPrefix = httpRequest.getHeader("X-Forwarded-Prefix");
            
            logger.info("=== CUSTOM FORWARD HEADER FILTER ===");
            logger.info("Request URI: {}", httpRequest.getRequestURI());
            logger.info("Custom header '{}': '{}'", customPrefixHeader, customPrefix);
            logger.info("Original X-Forwarded-Prefix: '{}'", originalPrefix);
            
            // Determine which prefix to use (prioritize custom header)
            final String prefixToUse = (customPrefix != null && !customPrefix.isEmpty()) ? customPrefix : originalPrefix;
            
            logger.info("Prefix to use: '{}'", prefixToUse);
            
            if (prefixToUse != null && !prefixToUse.isEmpty()) {
                // Create enhanced wrapped request
                CustomHttpServletRequestWrapper wrappedRequest = new CustomHttpServletRequestWrapper(httpRequest, prefixToUse, customPrefixHeader);
                chain.doFilter(wrappedRequest, response);
            } else {
                chain.doFilter(request, response);
            }
        }

        @Override
        public void destroy() {
            // Cleanup if needed
        }
    }
    
    /**
     * Enhanced request wrapper that handles forwarded headers and path manipulation
     */
    private static class CustomHttpServletRequestWrapper extends HttpServletRequestWrapper {
        
        private final String forwardedPrefix;
        private final String customHeaderName;
        
        public CustomHttpServletRequestWrapper(HttpServletRequest request, String forwardedPrefix, String customHeaderName) {
            super(request);
            this.forwardedPrefix = forwardedPrefix;
            this.customHeaderName = customHeaderName;
        }
        
        @Override
        public String getHeader(String name) {
            if ("X-Forwarded-Prefix".equalsIgnoreCase(name)) {
                return forwardedPrefix;
            }
            // Hide custom header
            if (customHeaderName.equalsIgnoreCase(name)) {
                return null;
            }
            return super.getHeader(name);
        }
        
        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            if ("X-Forwarded-Prefix".equalsIgnoreCase(name)) {
                return java.util.Collections.enumeration(java.util.Arrays.asList(forwardedPrefix));
            }
            if (customHeaderName.equalsIgnoreCase(name)) {
                return java.util.Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }
        
        @Override
        public java.util.Enumeration<String> getHeaderNames() {
            java.util.Set<String> headerNames = new java.util.HashSet<>();
            java.util.Enumeration<String> originalHeaders = super.getHeaderNames();
            
            while (originalHeaders.hasMoreElements()) {
                String headerName = originalHeaders.nextElement();
                if (!customHeaderName.equalsIgnoreCase(headerName)) {
                    headerNames.add(headerName);
                }
            }
            
            // Ensure X-Forwarded-Prefix is present
            headerNames.add("X-Forwarded-Prefix");
            
            return java.util.Collections.enumeration(headerNames);
        }
        
        @Override
        public String getContextPath() {
            String originalContextPath = super.getContextPath();
            if (forwardedPrefix != null && !forwardedPrefix.isEmpty()) {
                return forwardedPrefix + originalContextPath;
            }
            return originalContextPath;
        }
        
        @Override
        public String getRequestURI() {
            String originalURI = super.getRequestURI();
            if (forwardedPrefix != null && !forwardedPrefix.isEmpty()) {
                return forwardedPrefix + originalURI;
            }
            return originalURI;
        }
        
        @Override
        public StringBuffer getRequestURL() {
            StringBuffer originalURL = super.getRequestURL();
            if (forwardedPrefix != null && !forwardedPrefix.isEmpty()) {
                String urlStr = originalURL.toString();
                String contextPath = super.getContextPath();
                
                // Find where to insert the prefix
                int contextIndex = urlStr.indexOf(contextPath);
                if (contextIndex != -1) {
                    String beforeContext = urlStr.substring(0, contextIndex);
                    String afterContext = urlStr.substring(contextIndex);
                    return new StringBuffer(beforeContext + forwardedPrefix + afterContext);
                }
            }
            return originalURL;
        }
    }
    
    // Filter destroy method should be in the Filter class, not the wrapper
    // Adding it back to the CustomForwardedHeaderFilter class
}