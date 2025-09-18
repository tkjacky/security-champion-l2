<%@ page contentType="application/octet-stream" language="java" %>
<%@ page import="java.io.*" %>
<%@ page import="java.nio.file.*" %>
<%
    // This JSP serves compiled Java class files for dynamic loading
    String className = request.getParameter("className");
    
    if (className == null) {
        response.setStatus(400);
        out.print("Error: className parameter required");
        return;
    }
    
    // Security check - only allow secchamp classes
    if (!className.startsWith("secchamp.")) {
        response.setStatus(403);
        out.print("Error: Only secchamp classes are served from this endpoint");
        return;
    }
    
    try {
        // Convert class name to file path
        String classPath = className.replace(".", "/") + ".class";
        String fullPath = application.getRealPath("/WEB-INF/classes/" + classPath);
        
        File classFile = new File(fullPath);
        if (!classFile.exists()) {
            response.setStatus(404);
            out.print("Error: Class file not found: " + className);
            return;
        }
        
        // Set appropriate headers for binary content
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + className + ".class\"");
        response.setContentLength((int) classFile.length());
        
        // Send the class file
        FileInputStream fis = new FileInputStream(classFile);
        ServletOutputStream sos = response.getOutputStream();
        
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            sos.write(buffer, 0, bytesRead);
        }
        
        fis.close();
        sos.flush();
        
    } catch (Exception e) {
        response.setStatus(500);
        out.print("Error serving class: " + e.getMessage());
    }
%>
