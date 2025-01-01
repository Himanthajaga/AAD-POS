import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/generateOrderId")
public class OrderIdServlet extends HttpServlet {
    private Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

   @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        try (Connection connection = getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT MAX(oid) AS maxOrderId FROM orders");
            if (resultSet.next()) {
                String maxOrderId = resultSet.getString("maxOrderId");
                String newOrderId = generateNewOrderId(maxOrderId);
                JsonObject jsonResponse = Json.createObjectBuilder().add("newOrderId", newOrderId).build();
                resp.getWriter().write(jsonResponse.toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
   }

    private String generateNewOrderId(String maxOrderId) {
        if (maxOrderId == null) {
            return "ORD-100000";
        }
        int orderIdNumber = Integer.parseInt(maxOrderId.split("-")[1]);
        return "ORD-" + (orderIdNumber + 1);
    }
}