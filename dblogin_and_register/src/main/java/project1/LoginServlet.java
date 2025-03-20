package project1;
import project1.DBConnection;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

//@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JSONObject jsonResponse = new JSONObject();

        try {
            // Read form data
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            // Validate input
            if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Username and password are required.");
            } else {
                // Hash password before checking
                String hashedPassword = hashPassword(password);

                try (Connection conn = DBConnection.getConnection()) {
                    String query = "SELECT fullname FROM users WHERE username = ? AND password = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, username);
                        stmt.setString(2, hashedPassword);

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                jsonResponse.put("success", true);
                                jsonResponse.put("fullname", rs.getString("fullname"));
                            } else {
                                jsonResponse.put("success", false);
                                jsonResponse.put("message", "Invalid username or password.");
                            }
                        }
                    }
                } catch (SQLException e) {
                    jsonResponse.put("success", false);
                    jsonResponse.put("error", "Database error: " + e.getMessage());
                    e.printStackTrace();
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
