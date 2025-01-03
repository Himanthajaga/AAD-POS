import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.sql.*;
@WebServlet(urlPatterns = "/order")
public class OrderServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String customerId = req.getParameter("customerId");
        String fetchCustomerIds = req.getParameter("fetchCustomerIds");
        String itemCode = req.getParameter("itemCode");
        String fetchItemCodes = req.getParameter("fetchItemCodes");
        String generateOrderId = req.getParameter("generateOrderId");

        if ("true".equals(fetchCustomerIds)) {
            fetchCustomerIds(resp);
        } else if ("true".equals(fetchItemCodes)) {
            fetchItemCodes(resp);
        } else if ("true".equals(generateOrderId)) {
            generateOrderId(resp);
        } else if (customerId != null && !customerId.trim().isEmpty()) {
            fetchCustomerDetails(customerId, resp);
        } else if (itemCode != null && !itemCode.trim().isEmpty()) {
            fetchItemdetails(itemCode, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Invalid request\"}");
        }
    }


    private void generateOrderId(HttpServletResponse resp) throws IOException {
        String nextOrderId = null;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123")) {
            String query = "SELECT order_id FROM orders ORDER BY order_id DESC LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String lastOrderId = rs.getString("order_id");
                    int nextId = Integer.parseInt(lastOrderId.replace("OID-", "")) + 1;
                    nextOrderId = "OID-" + String.format("%03d", nextId);
                } else {
                    nextOrderId = "OID-001"; // Default to ORD001 if no orders exist
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error occurred while generating order ID\"}");
            return;
        }

        JsonObject jsonResponse = Json.createObjectBuilder()
                .add("orderId", nextOrderId)
                .build();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse.toString());
    }


    private void fetchItemdetails(String itemCode, HttpServletResponse resp) throws IOException {
        JsonObjectBuilder itemDetails = Json.createObjectBuilder();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123")) {
            String query = "SELECT code, description, unitPrice, qtyOnHand FROM item WHERE code = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, itemCode);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        itemDetails.add("itemCode", rs.getString("code"));
                        itemDetails.add("description", rs.getString("description"));
                        itemDetails.add("unitPrice", rs.getDouble("unitPrice"));
                        itemDetails.add("qtyOnHand", rs.getInt("qtyOnHand"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error occurred while fetching item details\"}");
            return;
        }
        JsonObject jsonResponse = itemDetails.build();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse.toString());

    }

    private void fetchItemCodes(HttpServletResponse resp) throws IOException {
        JsonArrayBuilder itemCodesArray = Json.createArrayBuilder();
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123")) {
            String query = "SELECT code FROM item";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    itemCodesArray.add(rs.getString("code"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error occurred while fetching item codes\"}");
            return;
        }
        JsonObject jsonResponse = Json.createObjectBuilder().add("itemCodes", itemCodesArray).build();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse.toString());
    }

    private void fetchCustomerDetails(String customerId, HttpServletResponse resp) throws IOException {
        JsonObjectBuilder customerDetails = Json.createObjectBuilder();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123")) {
            String query = "SELECT id, name FROM customer WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, customerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        customerDetails.add("customerId", rs.getString("id"));
                        customerDetails.add("customerName", rs.getString("name"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error occurred while fetching customer details\"}");
            return;
        }

        JsonObject jsonResponse = customerDetails.build();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse.toString());
    }

    private void fetchCustomerIds(HttpServletResponse resp) throws IOException {
        JsonArrayBuilder customerIdsArray = Json.createArrayBuilder();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123")) {
            String query = "SELECT id FROM customer";
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    customerIdsArray.add(rs.getString("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error occurred while fetching customer IDs\"}");
            return;
        }

        JsonObject jsonResponse = Json.createObjectBuilder()
                .add("customerIds", customerIdsArray)
                .build();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse.toString());
    }



    private void searchItem(String itemCode, HttpServletResponse resp) {
        JsonArrayBuilder itemsArray = Json.createArrayBuilder();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123")) {
            String query = "SELECT code, description, unitPrice, qtyOnHand FROM item WHERE code LIKE ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, "%" + itemCode + "%");  // Only set one parameter

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        JsonObjectBuilder item = Json.createObjectBuilder();
                        item.add("itemCode", rs.getString("code"));
                        item.add("description", rs.getString("description"));
                        item.add("unitPrice", rs.getDouble("unitPrice"));
                        item.add("qtyOnHand", rs.getInt("qtyOnHand"));

                        itemsArray.add(item);
                    }
                }

            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                resp.getWriter().write("{\"error\":\"Database error occurred while searching items\"}");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        JsonObject jsonResponse = Json.createObjectBuilder()
                .add("items", itemsArray)
                .build();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            resp.getWriter().write(jsonResponse.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void searchCustomer(String customerName, HttpServletResponse resp) {
        JsonArrayBuilder customersArray = Json.createArrayBuilder();

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123")) {
            String query = "SELECT id, name FROM customer WHERE id LIKE ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, "%" + customerName + "%");

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        JsonObjectBuilder customer = Json.createObjectBuilder();
                        customer.add("customerId", rs.getString("id"));
                        customer.add("customerName", rs.getString("name"));

                        customersArray.add(customer);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                resp.getWriter().write("{\"error\":\"Database error occurred while searching customers\"}");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return;
        }

        JsonObject jsonResponse = Json.createObjectBuilder()
                .add("customers", customersArray)
                .build();

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            resp.getWriter().write(jsonResponse.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String orderId = req.getParameter("order_id");
        String customerId = req.getParameter("customer_id");
        String itemId = req.getParameter("item_code");

        double totalAmount;
        int quantity;

        try {
            totalAmount = Double.parseDouble(req.getParameter("total_amount"));
            quantity = Integer.parseInt(req.getParameter("qty"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"Invalid number format for totalAmount or quantity\"}");
            return;
        }

        if (orderId == null || orderId.isEmpty() || customerId == null || customerId.isEmpty() || itemId == null || itemId.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"orderId, customerId, or itemId is null or empty\"}");
            return;
        }

        Connection connection = null;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company", "root", "Ijse@123");


            connection.setAutoCommit(false);


            String insertOrderSql = "INSERT INTO orders (order_id, customer_id, item_code, qty, total_amount) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertOrderStmt = connection.prepareStatement(insertOrderSql);
            insertOrderStmt.setString(1, orderId);
            insertOrderStmt.setString(2, customerId);
            insertOrderStmt.setString(3, itemId);
            insertOrderStmt.setInt(4, quantity);
            insertOrderStmt.setDouble(5, totalAmount);

            int rowsInserted = insertOrderStmt.executeUpdate();

            if (rowsInserted > 0) {

                String updateQtySql = "UPDATE item SET qtyOnHand = qtyOnHand - ? WHERE code = ?";
                PreparedStatement updateQtyStmt = connection.prepareStatement(updateQtySql);
                updateQtyStmt.setInt(1, quantity);
                updateQtyStmt.setString(2, itemId);

                int rowsUpdated = updateQtyStmt.executeUpdate();

                if (rowsUpdated > 0) {

                    connection.commit();
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\" : \"Order placed successfully and qtyOnHand updated\"}");
                } else {
                    connection.rollback();
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"error\" : \"Failed to update qtyOnHand\"}");
                }
            } else {
                connection.rollback();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\" : \"Failed to place order\"}");
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\" : \"Database error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
