package project1;
import project1.DBConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

//@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject jsonResponse = new JSONObject();

        try {
            // Read request body
            String fullname = request.getParameter("fullname");
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            // Validate input
            if (fullname == null || username == null || password == null || fullname.trim().isEmpty() || username.trim().isEmpty() || password.trim().isEmpty()) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "All fields are required.");
            } else {
                // Hash password
                String hashedPassword = hashPassword(password);

                try (Connection conn = DBConnection.getConnection()) {
                    String query = "INSERT INTO users (fullname, username, password) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, fullname);
                        stmt.setString(2, username);
                        stmt.setString(3, hashedPassword);

                        int rowsInserted = stmt.executeUpdate();
                        if (rowsInserted > 0) {
                            jsonResponse.put("success", true);
                            jsonResponse.put("message", "User registered successfully.");
                        } else {
                            jsonResponse.put("success", false);
                            jsonResponse.put("message", "Registration failed.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("error", "Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        // Send JSON response
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse.toString());
            out.flush();
        }
    }

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
