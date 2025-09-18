<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="secchamp.Info" %>
<%
response.setContentType("application/json");
response.setHeader("Access-Control-Allow-Origin", "*");
response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
response.setHeader("Access-Control-Allow-Headers", "Content-Type");

try {
    // Check if a specific class is being requested
    String requestedClass = request.getParameter("className");
    
    if (requestedClass != null) {
        if ("evil.Info".equals(requestedClass)) {
            // SecChamp API should not have access to evil.Info class
            String errorResponse = "{"
                + "\"status\": \"error\","
                + "\"message\": \"Class not found: evil.Info is not available in SecChamp API\","
                + "\"service\": \"SecChamp Book Categorization API\","
                + "\"type\": \"legitimate\","
                + "\"error\": \"ClassNotFoundException: evil.Info not found in secchamp namespace\""
                + "}";
            out.print(errorResponse);
            return;
        } else if ("secchamp.secchamp_http".equals(requestedClass)) {
            // Load the actual compiled secchamp.secchamp_http class and test real API calls
            try {
                secchamp.secchamp_http httpService = new secchamp.secchamp_http();
                String bookData = httpService.fetchBookData();
                String reviewData = httpService.fetchBookReviews();
                String searchData = httpService.searchBooksHttp("spring");
                
                String jsonResponse = "{"
                    + "\"status\": \"success\","
                    + "\"service\": \"SecChamp HTTP Book Service\","
                    + "\"className\": \"secchamp.secchamp_http\","
                    + "\"info\": \"" + httpService.getInfo().replace("\"", "\\\"") + "\","
                    + "\"httpRequest\": \"" + httpService.prepareHttpRequest().replace("\"", "\\\"") + "\","
                    + "\"classInfo\": \"" + secchamp.secchamp_http.getClassInfo().replace("\"", "\\\"") + "\","
                    + "\"liveBookData\": \"" + bookData.replace("\"", "\\\"") + "\","
                    + "\"reviewData\": \"" + reviewData.replace("\"", "\\\"") + "\","
                    + "\"searchData\": \"" + searchData.replace("\"", "\\\"") + "\","
                    + "\"type\": \"http_entity\""
                    + "}";
                out.print(jsonResponse);
                return;
            } catch (Exception e) {
                String errorResponse = "{"
                    + "\"status\": \"error\","
                    + "\"service\": \"SecChamp HTTP Book Service\","
                    + "\"className\": \"secchamp.secchamp_http\","
                    + "\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\","
                    + "\"type\": \"http_entity\""
                    + "}";
                out.print(errorResponse);
                return;
            }
        } else if (!"secchamp.Info".equals(requestedClass)) {
            // Only secchamp classes are available in this API
            String errorResponse = "{"
                + "\"status\": \"error\","
                + "\"message\": \"Class not found: " + requestedClass + " is not available in SecChamp API\","
                + "\"service\": \"SecChamp Book Categorization API\","
                + "\"type\": \"legitimate\","
                + "\"error\": \"ClassNotFoundException: Only secchamp.Info is available\""
                + "}";
            out.print(errorResponse);
            return;
        }
    }
    
    // Load the actual compiled secchamp.Info class
    Info secchampInfo = new Info();
    
    String appInfo = secchampInfo.getAppInfo();
    String timestamp = secchampInfo.getTimestamp();
    String categoryInfo = secchampInfo.getCategoryInfo();
    String message = secchampInfo.getMessage();
    
    // Test the new API integration methods
    String bookData = secchampInfo.getBookData();
    String bookCount = secchampInfo.getBookCount();
    String searchResult = secchampInfo.searchBooks("spring");
    
    String jsonResponse = "{"
        + "\"status\": \"success\","
        + "\"service\": \"SecChamp Book Categorization API\","
        + "\"appInfo\": \"" + appInfo.replace("\"", "\\\"") + "\","
        + "\"timestamp\": \"" + timestamp.replace("\"", "\\\"") + "\","
        + "\"categoryInfo\": \"" + categoryInfo.replace("\"", "\\\"") + "\","
        + "\"message\": \"" + message.replace("\"", "\\\"") + "\","
        + "\"liveBookData\": \"" + bookData.replace("\"", "\\\"") + "\","
        + "\"bookCount\": \"" + bookCount.replace("\"", "\\\"") + "\","
        + "\"searchResult\": \"" + searchResult.replace("\"", "\\\"") + "\","
        + "\"type\": \"legitimate\""
        + "}";
    
    out.print(jsonResponse);
} catch (Exception e) {
    String errorResponse = "{"
        + "\"status\": \"error\","
        + "\"message\": \"Service error: " + e.getMessage().replace("\"", "\\\"") + "\","
        + "\"type\": \"legitimate\""
        + "}";
    out.print(errorResponse);
}
%>
