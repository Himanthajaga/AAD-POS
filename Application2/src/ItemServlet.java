import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

@WebServlet(urlPatterns = "/item")
public class ItemServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            ResultSet resultSet = connection.prepareStatement("SELECT * FROM item").executeQuery();

            JsonArrayBuilder allItems = Json.createArrayBuilder();

            while (resultSet.next()) {
                String itemCode = resultSet.getString(1);
                String description = resultSet.getString(2);
                String qtyOnHand = resultSet.getString(3);
                String unitPrice = resultSet.getString(4);

                JsonObjectBuilder itemBuilder = Json.createObjectBuilder();
                itemBuilder.add("itemCode", itemCode);
                itemBuilder.add("description", description);
                itemBuilder.add("qtyOnHand", qtyOnHand);
                itemBuilder.add("unitPrice", unitPrice);
                allItems.add(itemBuilder.build());
            }

            resp.getWriter().write(allItems.build().toString());

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String itemCode = req.getParameter("itemCode");
        String description = req.getParameter("description");
        String qtyOnHand = req.getParameter("qtyOnHand");
        String unitPrice = req.getParameter("unitPrice");

        if (itemCode == null || description == null || qtyOnHand == null || unitPrice == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"itemCode,description, qtyOnHand and unitPrice are required\"}");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO item VALUES (?, ?, ?, ?)");

            preparedStatement.setString(1, itemCode);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, Integer.parseInt(qtyOnHand));
            preparedStatement.setBigDecimal(4, new BigDecimal(unitPrice));

            preparedStatement.executeUpdate();

            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String itemCode = req.getParameter("itemCode");
        String description = req.getParameter("description");
        String qtyOnHand = req.getParameter("qtyOnHand");
        String unitPrice = req.getParameter("unitPrice");

        if (itemCode == null || description == null || qtyOnHand == null || unitPrice == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"itemCode,description, qtyOnHand and unitPrice are required\"}");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE item SET description = ?, qtyOnHand = ?, unitPrice = ? WHERE itemCode = ?");

            preparedStatement.setString(1, description);
            preparedStatement.setInt(2, Integer.parseInt(qtyOnHand));
            preparedStatement.setBigDecimal(3, new BigDecimal(unitPrice));
            preparedStatement.setString(4, itemCode);

            preparedStatement.executeUpdate();

            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String itemCode = req.getParameter("itemCode");

        if (itemCode == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"itemCode required\"}");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM item WHERE itemCode = ?");

            preparedStatement.setString(1, itemCode);

            preparedStatement.executeUpdate();

            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
