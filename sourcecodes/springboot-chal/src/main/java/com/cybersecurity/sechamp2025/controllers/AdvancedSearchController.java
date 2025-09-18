package com.cybersecurity.sechamp2025.controllers;

import com.cybersecurity.sechamp2025.utils.JsonPegParser;
import com.cybersecurity.sechamp2025.utils.UrlPegParser;
import com.cybersecurity.sechamp2025.utils.XssPegParser;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpEntity;
import java.net.URLDecoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/advanced_search")
public class AdvancedSearchController {

    private static final Pattern XSS_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_.,!?();\"'+<>/=%]+$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?://|javascript:).*");
    private static final Pattern CLASS_PATTERN = Pattern.compile("^[a-zA-Z0-9.]+$");

    private final XssPegParser xssParser = Parboiled.createParser(XssPegParser.class);
    private final UrlPegParser urlParser = Parboiled.createParser(UrlPegParser.class);
    private final JsonPegParser jsonParser = Parboiled.createParser(JsonPegParser.class);

    @GetMapping
    public String advancedSearchForm() {
        return "advanced_search";
    }

    @PostMapping("/xss")
    public String testXSSValidation(@RequestParam String userInput, Model model, RedirectAttributes redirectAttributes) {
        String result = "Invalid input";
        String validationStatus = "Failed";
        String validationMethod = "";
        
        boolean regexValid = XSS_PATTERN.matcher(userInput).matches();
        
        ParsingResult<Object> pegResult = new ReportingParseRunner<Object>(xssParser.SafeInput()).run(userInput);
        boolean pegValid = pegResult.matched;
        
        if (regexValid || pegValid) {
            validationStatus = "Passed";
            validationMethod = regexValid ? (pegValid ? "Both Regex & PEG" : "Regex only") : "PEG only";
            try {
                // Double decode to handle both single and double encoded script payloads
                String decodedOnce = URLDecoder.decode(userInput, StandardCharsets.UTF_8);
                result = URLDecoder.decode(decodedOnce, StandardCharsets.UTF_8);
            } catch (Exception e) {
                try {
                    // If double decoding fails, try single decoding
                    result = URLDecoder.decode(userInput, StandardCharsets.UTF_8);
                } catch (Exception e2) {
                    result = userInput;
                }
            }
        }
        
        redirectAttributes.addFlashAttribute("xssInput", userInput);
        redirectAttributes.addFlashAttribute("xssResult", result);
        redirectAttributes.addFlashAttribute("xssStatus", validationStatus);
        redirectAttributes.addFlashAttribute("validationMethod", validationMethod);
        redirectAttributes.addFlashAttribute("activeSection", "xss");
        return "redirect:/advanced_search#xss-section";
    }

    @PostMapping("/url")
    public String testURLValidation(@RequestParam String url, Model model, RedirectAttributes redirectAttributes) {
        String result = "Invalid URL";
        String validationStatus = "Failed";
        String normalizedUrl = "";
        String validationMethod = "";
        String browserHost = "";
        String appValidationHost = "";
        
        try {
            // Double decode like the publisher endpoint
            String decodedOnce = URLDecoder.decode(url, StandardCharsets.UTF_8);
            normalizedUrl = URLDecoder.decode(decodedOnce, StandardCharsets.UTF_8);
            
            // Get host that browser would use (original URL)
            try {
                URI rawUri = new URI(url);
                browserHost = rawUri.getHost();
            } catch (Exception e) {
                browserHost = "browser parse error";
            }

            // Get host that app validation uses (double decoded URL)
            try {
                URI appUri = new URI(normalizedUrl);
                appValidationHost = appUri.getHost();
            } catch (Exception e) {
                appValidationHost = "parsing error";
            }
            
            boolean regexValid = URL_PATTERN.matcher(normalizedUrl).matches();
            
            ParsingResult<Object> pegResult = new ReportingParseRunner<Object>(urlParser.Url()).run(normalizedUrl);
            boolean pegValid = pegResult.matched;
            
            if (regexValid || pegValid) {
                validationStatus = "Passed";
                validationMethod = regexValid ? (pegValid ? "Both Regex & PEG" : "Regex only") : "PEG only";
                
                // Show the URL encoding vulnerability
                if (browserHost != null && appValidationHost != null && !browserHost.equalsIgnoreCase(appValidationHost)) {
                    result = "URL ENCODING VULNERABILITY DETECTED! Browser will navigate to: " + browserHost + " but validation checked: " + appValidationHost;
                } else {
                    result = "URL validation passed for: " + normalizedUrl;
                }
            }
        } catch (Exception e) {
            result = "Error processing URL: " + e.getMessage();
        }
        
        redirectAttributes.addFlashAttribute("urlInput", url);
        redirectAttributes.addFlashAttribute("urlResult", result);
        redirectAttributes.addFlashAttribute("urlStatus", validationStatus);
        redirectAttributes.addFlashAttribute("normalizedUrl", normalizedUrl);
        redirectAttributes.addFlashAttribute("validationMethod", validationMethod);
        redirectAttributes.addFlashAttribute("browserHost", browserHost);
        redirectAttributes.addFlashAttribute("appValidationHost", appValidationHost);
        redirectAttributes.addFlashAttribute("activeSection", "url");
        return "redirect:/advanced_search#url-section";
    }

    @PostMapping("/reflection")
    public String testReflectionValidation(@RequestParam String apiUrl, 
                                         @RequestParam(required = false) String customApiUrl,
                                         @RequestParam String className, 
                                         Model model, RedirectAttributes redirectAttributes) {
        String result = "Invalid parameters";
        String validationStatus = "Failed";
        String reflectionResult = "";
        
                String finalApiUrl = apiUrl;
        if ("custom".equals(apiUrl) && customApiUrl != null && !customApiUrl.trim().isEmpty()) {
            finalApiUrl = customApiUrl.trim();
        }
        
        try {
            if (URL_PATTERN.matcher(finalApiUrl).matches() && CLASS_PATTERN.matcher(className).matches()) {
                validationStatus = "Passed";
                
                if (className.equals("secchamp.Info")) {
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        String apiUrlWithParam = finalApiUrl + "?className=" + java.net.URLEncoder.encode(className, "UTF-8");
                        String apiResponse = restTemplate.getForObject(apiUrlWithParam, String.class);
                        
                        result = "SecChamp API accessed successfully! Response: " + 
                               (apiResponse != null ? apiResponse : "No response");
                        reflectionResult = apiResponse != null ? apiResponse : "No response from SecChamp API";
                    } catch (Exception e) {
                        result = "SecChamp API call failed: " + e.getMessage();
                        reflectionResult = "API class failed: secchamp.Info - ERROR: Could not access secchamp.Info service at " + finalApiUrl + " - Error: " + e.getMessage();
                    }
                } else if (className.equals("evil.Info")) {
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        String apiUrlWithParam = finalApiUrl + "?className=" + java.net.URLEncoder.encode(className, "UTF-8");
                        String apiResponse = restTemplate.getForObject(apiUrlWithParam, String.class);
                        
                        result = "Evil API accessed! Response: " + 
                               (apiResponse != null ? apiResponse : "No response");
                        reflectionResult = apiResponse != null ? apiResponse : "No response from Evil API";
                    } catch (Exception e) {
                        result = "Evil API call failed: " + e.getMessage();
                        reflectionResult = "API class failed: evil.Info - Could not access evil.Info service at " + finalApiUrl + " - Error: " + e.getMessage();
                    }
                } else {
                    try {
                        RestTemplate restTemplate = new RestTemplate();
                        String generalApiUrl = "http://tomcat-chal:8080/api/general/?className=" + java.net.URLEncoder.encode(className, "UTF-8");
                        String apiResponse = restTemplate.getForObject(generalApiUrl, String.class);
                        
                        result = "Class reflection via General API: " + 
                               (apiResponse != null ? apiResponse : "No response");
                        reflectionResult = apiResponse != null ? apiResponse : "No response from General API";
                    } catch (Exception e) {
                        result = "General API call failed for class " + className + ": " + e.getMessage();
                        reflectionResult = "General API failed: " + className + " - ERROR: " + e.getMessage();
                    }
                }
            }
        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }
        
        redirectAttributes.addFlashAttribute("apiUrl", finalApiUrl);
        redirectAttributes.addFlashAttribute("className", className);
        redirectAttributes.addFlashAttribute("reflectionResult", reflectionResult);
        redirectAttributes.addFlashAttribute("reflectionStatus", validationStatus);
        redirectAttributes.addFlashAttribute("reflectionFinalResult", result);
        redirectAttributes.addFlashAttribute("activeSection", "reflection");
        return "redirect:/advanced_search#reflection-section";
    }

    @PostMapping("/prototype")
    public String testPrototypePollution(@RequestParam String jsonInput, Model model, RedirectAttributes redirectAttributes) {
        String result = "Invalid JSON";
        String validationStatus = "Failed";
        String validationMethod = "";
        
        boolean regexValid = jsonInput.matches("^\\{.*\\}$");
        
        ParsingResult<Object> pegResult = new ReportingParseRunner<Object>(jsonParser.JsonValue()).run(jsonInput);
        boolean pegValid = pegResult.matched;
        
        if (regexValid || pegValid) {
            validationStatus = "Passed";
            validationMethod = regexValid ? (pegValid ? "Both Regex & PEG" : "Regex only") : "PEG only";
            
            if (jsonInput.contains("__proto__") || jsonInput.contains("constructor") || jsonInput.contains("prototype")) {
                result = "Potential prototype pollution detected in: " + jsonInput + " (Method: " + validationMethod + ")";
            } else {
                result = "JSON structure validated: " + jsonInput + " (Method: " + validationMethod + ")";
            }
        }
        
        redirectAttributes.addFlashAttribute("jsonInput", jsonInput);
        redirectAttributes.addFlashAttribute("prototypeResult", result);
        redirectAttributes.addFlashAttribute("prototypeStatus", validationStatus);
        redirectAttributes.addFlashAttribute("validationMethod", validationMethod);
        redirectAttributes.addFlashAttribute("activeSection", "prototype");
        return "redirect:/advanced_search#prototype-section";
    }

    @PostMapping("/publisher")
    public String verifyPublisher(@RequestParam String publisherUrl,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        String result = "Invalid publisher URL";
        String status = "Failed";
        String processedUrl = publisherUrl;
        String browserHost = "";
        String appValidationHost = "";

        try {
            String decodedOnce = URLDecoder.decode(publisherUrl, StandardCharsets.UTF_8);
            processedUrl = URLDecoder.decode(decodedOnce, StandardCharsets.UTF_8);

            try {
                URI appUri = new URI(processedUrl);
                appValidationHost = appUri.getHost();
            } catch (Exception e) {
                appValidationHost = "parsing error";
            }

            try {
                URI rawUri = new URI(publisherUrl);
                browserHost = rawUri.getHost();
            } catch (Exception e) {
                browserHost = "browser parse error";
            }

            if (appValidationHost != null && browserHost != null) {
                if (!browserHost.equalsIgnoreCase(appValidationHost)) {
                    status = "Danger";
                    result = "Raw URL will navigate to host: " + browserHost + "; processed URL host: " + appValidationHost;
                } else {
                    status = "Verified";
                    result = "Processed and raw URL resolve to the same host: " + appValidationHost;
                }
            } else {
                result = "Publisher URL does not meet security requirements";
            }

        } catch (Exception e) {
            result = "Error processing publisher URL: " + e.getMessage();
        }

        redirectAttributes.addFlashAttribute("publisherUrl", publisherUrl);
        redirectAttributes.addFlashAttribute("processedUrl", processedUrl);
        redirectAttributes.addFlashAttribute("publisherResult", result);
        redirectAttributes.addFlashAttribute("publisherStatus", status);
        redirectAttributes.addFlashAttribute("appValidationHost", appValidationHost);
        redirectAttributes.addFlashAttribute("browserHost", browserHost);
        redirectAttributes.addFlashAttribute("activeSection", "publisher");
        return "redirect:/advanced_search#publisher-section";
    }

    @PostMapping("/book-service")
    public String integrateBookService(@RequestParam String serviceUrl, 
                                      @RequestParam String requestClass, 
                                      @RequestParam String responseClass,
                                      Model model, RedirectAttributes redirectAttributes) {
        String result = "Book creation failed";
        String status = "Failed";
        
        try {
            // Validate URL format
            if (URL_PATTERN.matcher(serviceUrl).matches()) {
                RestTemplate restTemplate = new RestTemplate();
                
                String classInfoUrl = serviceUrl + "?className=" + requestClass;
                String classInfo = "Unknown class";
                
                try {
                    String classResponse = restTemplate.getForObject(classInfoUrl, String.class);
                    classInfo = classResponse != null ? classResponse : "No class info available";
                } catch (Exception e) {
                    classInfo = "Error loading class info: " + e.getMessage();
                }
                
                // SECURITY FIX: Validate response class before reflection
                String[] allowedResponseClasses = {
                    "java.lang.String",
                    "java.lang.Object", 
                    "java.lang.Integer",
                    "java.lang.Boolean",
                    "java.lang.Runtime",
                    "java.util.Map",                    
                    "java.util.List"
                };
                
                boolean isResponseClassAllowed = false;
                for (String allowed : allowedResponseClasses) {
                    if (allowed.equals(responseClass)) {
                        isResponseClassAllowed = true;
                        break;
                    }
                }
                
                if (!isResponseClassAllowed) {
                    result = "Security Error: Response class '" + responseClass + "' is not allowed. Only safe classes are permitted for deserialization.";
                    status = "Failed";
                } else {
                    // Safe to use reflection with whitelisted response class
                    Class<?> responseType = Class.forName(responseClass);
                    
                    // Create a simple serializable request entity
                    // Instead of using anonymous inner class, create a Map that can be serialized
                    java.util.Map<String, String> requestData = new java.util.HashMap<>();
                    requestData.put("requestClass", requestClass);
                    requestData.put("action", "createBook");
                    requestData.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    
                    // Following Baeldung pattern: restTemplate.postForObject(url, HttpEntity<T> request, responseType)
                    HttpEntity<java.util.Map<String, String>> request = new HttpEntity<>(requestData);
                    
                    // Make the actual POST request to the service
                    Object response = restTemplate.postForObject(serviceUrl, request, responseType);
                    
                    if (response != null) {
                        status = "Success";
                        result = String.format("Book service integration successful! Class info: %s -> Response: %s", 
                                              classInfo.replace("\n", " ").replace("\r", ""), 
                                              response.toString());
                    } else {
                        result = String.format("Book service integration completed but returned no data. Class info: %s", 
                                              classInfo.replace("\n", " ").replace("\r", ""));
                    }
                }
            } else {
                result = "Invalid book service URL format";
            }
        } catch (ClassNotFoundException e) {
            result = "Error: Response class not found: " + e.getMessage();
        } catch (Exception e) {
            result = "Integration error: " + e.getMessage();
            // Detection for dangerous classes
            if (requestClass.contains("Runtime") || requestClass.contains("ProcessBuilder") || 
                responseClass.contains("Runtime") || responseClass.contains("ProcessBuilder") || 
                requestClass.contains("System") || responseClass.contains("System")) {
                result += " [SECURITY WARNING: Dangerous class detected]";
            }
        }
        
        redirectAttributes.addFlashAttribute("serviceUrl", serviceUrl);
        redirectAttributes.addFlashAttribute("requestClass", requestClass);
        redirectAttributes.addFlashAttribute("responseClass", responseClass);
        redirectAttributes.addFlashAttribute("bookServiceResult", result);
        redirectAttributes.addFlashAttribute("bookServiceStatus", status);
        redirectAttributes.addFlashAttribute("activeSection", "book-service");
        return "redirect:/advanced_search#book-service-section";
    }
}
