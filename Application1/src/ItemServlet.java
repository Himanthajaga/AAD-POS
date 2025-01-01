import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@WebServlet(urlPatterns = "/item")
public class ItemServlet extends HttpServlet {
    List<ItemDTO> itemDTOList = new ArrayList<>();

    private Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");
            return connection;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        itemDTOList.clear();

        resp.setContentType("application/json");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            ResultSet resultSet = connection.prepareStatement("SELECT * FROM item").executeQuery();

            JsonArrayBuilder allItem = Json.createArrayBuilder();

            while (resultSet.next()) {
                String code = resultSet.getString(1);
                String description = resultSet.getString(2);
                double unitPrice = resultSet.getDouble(3);
                int qtyOnHand = resultSet.getInt(4);

                JsonObjectBuilder item = Json.createObjectBuilder();
                item.add("code", code);
                item.add("description", description);
                item.add("unitPrice", unitPrice);
                item.add("qtyOnHand", qtyOnHand);

                allItem.add(item);
            }

            PrintWriter out = resp.getWriter();
            out.println(allItem.build().toString());

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String description = req.getParameter("description");
        String qtyOnHand = req.getParameter("qtyOnHand");
        String unitPrice = req.getParameter("unitPrice");

        if (id == null || description == null || qtyOnHand == null || unitPrice == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"Invalid request\"}");
            return;
        }
        Connection connection = getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO item VALUES(?, ?, ?, ?)");
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, qtyOnHand);
            preparedStatement.setString(4, unitPrice);
            preparedStatement.executeUpdate();
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reader = req.getReader();
        JsonObject jsonObject = Json.createReader(reader).readObject();

        String id = jsonObject.getString("code");
        String description = jsonObject.getString("description");
        int qtyOnHand;
        double unitPrice;

        try {
            qtyOnHand = Integer.parseInt(jsonObject.getString("qtyOnHand"));
            unitPrice = Double.parseDouble(jsonObject.getString("unitPrice"));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"Invalid number format\"}");
            return;
        }

        if (id == null || description == null || qtyOnHand == 0 || unitPrice == 0.0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"Invalid request\"}");
            return;
        } else {
            Connection connection = getConnection();
            ItemDTO item = findById(id);
            try {
                if (item != null) {
                    PreparedStatement preparedStatement = connection.prepareStatement("UPDATE item SET description = ?, qtyOnHand = ?, unitPrice = ? WHERE code = ?");
                    preparedStatement.setString(1, description);
                    preparedStatement.setInt(2, qtyOnHand);
                    preparedStatement.setDouble(3, unitPrice);
                    preparedStatement.setString(4, id);
                    preparedStatement.executeUpdate();
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ItemDTO findById(String id) {
        Connection connection = getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM item WHERE code = ?");
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String code = resultSet.getString(1);
                String description = resultSet.getString(2);

                int qtyOnHand = resultSet.getInt(3);
                double unitPrice = resultSet.getDouble(4);

                return new ItemDTO(code, description, qtyOnHand, unitPrice);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        if (id == null || id.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Item ID is required.\"}");
            return;
        }

        try (Connection connection = getConnection()) {
            ItemDTO item = findById(id);
            if (item != null) {
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "DELETE FROM item WHERE code = ?");
                preparedStatement.setString(1, id);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"error\":\"Failed to delete the customer.\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\":\"Customer not found.\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Failed to delete customer..\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
