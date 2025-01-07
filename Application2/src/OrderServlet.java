import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.DriverManager.getConnection;

@WebServlet(urlPatterns = "/orders")
public class OrderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            ResultSet resultSetOrders = connection.prepareStatement("SELECT * FROM orders").executeQuery();

            JsonArrayBuilder allOrders = Json.createArrayBuilder();

            while (resultSetOrders.next()) {
                String orderId = resultSetOrders.getString(1);
                String date = resultSetOrders.getString(2);
                String customerId = resultSetOrders.getString(3);
                String discount = resultSetOrders.getString(4);
                String cash = resultSetOrders.getString(5);
                String balance = resultSetOrders.getString(6);

                JsonObjectBuilder orderBuilder = Json.createObjectBuilder();
                orderBuilder.add("orderId", orderId);
                orderBuilder.add("date", date);
                orderBuilder.add("customerId", customerId);
                orderBuilder.add("discount", discount);
                orderBuilder.add("cash", cash);
                orderBuilder.add("balance", balance);

                // Get order details
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM orderDetail WHERE orderId = ?");
                preparedStatement.setString(1, orderId);
                ResultSet resultSetOrderDetail = preparedStatement.executeQuery();

                JsonArrayBuilder orderDetailsArray = Json.createArrayBuilder();
                while (resultSetOrderDetail.next()) {
                    String oId = resultSetOrderDetail.getString(1);
                    String code = resultSetOrderDetail.getString(2);
                    String qty = resultSetOrderDetail.getString(3);

                    JsonObjectBuilder orderDetailBuilder = Json.createObjectBuilder();
                    orderDetailBuilder.add("orderId", oId);
                    orderDetailBuilder.add("itemCode", code);
                    orderDetailBuilder.add("qty", qty);

                    // Add the order detail to the order details array
                    orderDetailsArray.add(orderDetailBuilder.build());
                }

                // Add the order details array to the order object
                orderBuilder.add("orderDetail", orderDetailsArray.build());

                // Add the order object to the main orders array
                allOrders.add(orderBuilder.build());
            }

            // Write the final JSON array of all orders to the response
            resp.getWriter().write(allOrders.build().toString());

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonObjectBuilder response = Json.createObjectBuilder();

        try {
            // Parse request body
            JsonReader jsonReader = Json.createReader(req.getReader());
            JsonObject orderData = jsonReader.readObject();

            // Get connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");
            connection.setAutoCommit(false);

            try {
                // Insert order
                PreparedStatement orderStmt = connection.prepareStatement(
                        "INSERT INTO orders (orderId, date, customerId, discount, cash, balance) VALUES (?, ?, ?, ?, ?, ?)"
                );
                orderStmt.setString(1, orderData.getString("orderId"));
                orderStmt.setString(2, orderData.getString("date"));
                orderStmt.setString(3, orderData.getString("customerId"));
                orderStmt.setDouble(4, orderData.getJsonNumber("discount").doubleValue());
                orderStmt.setBigDecimal(5, new BigDecimal(orderData.getJsonNumber("cash").toString()));
                orderStmt.setBigDecimal(6, new BigDecimal(orderData.getJsonNumber("balance").toString()));
                orderStmt.executeUpdate();

                // Insert order details and update item quantities
                JsonArray items = orderData.getJsonArray("items");
                PreparedStatement detailStmt = connection.prepareStatement(
                        "INSERT INTO orderDetail (orderId, itemCode, qty) VALUES (?, ?, ?)"
                );
                PreparedStatement updateItemStmt = connection.prepareStatement(
                        "UPDATE item SET qtyOnHand = qtyOnHand - ? WHERE itemCode = ?"
                );

                for (JsonValue item : items) {
                    JsonObject orderDetail = item.asJsonObject();
                    String itemCode = orderDetail.getString("itemCode");
                    int qty = orderDetail.getInt("qty");

                    // Insert order detail
                    detailStmt.setString(1, orderData.getString("orderId"));
                    detailStmt.setString(2, itemCode);
                    detailStmt.setInt(3, qty);
                    detailStmt.executeUpdate();

                    // Update item quantity
                    updateItemStmt.setInt(1, qty);
                    updateItemStmt.setString(2, itemCode);
                    updateItemStmt.executeUpdate();
                }

                connection.commit();
                response.add("status", "success");
                response.add("message", "Order placed successfully");

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (Exception e) {
            response.add("status", "error");
            response.add("message", e.getMessage());
        }

        resp.getWriter().write(response.build().toString());
    }

}
