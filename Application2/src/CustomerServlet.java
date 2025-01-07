import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            ResultSet resultSet = connection.prepareStatement("SELECT * FROM customer").executeQuery();

            JsonArrayBuilder allCustomers = Json.createArrayBuilder();

            while (resultSet.next()) {
                String id = resultSet.getString(1);
                String name = resultSet.getString(2);
                String phone = resultSet.getString(3);
                String address = resultSet.getString(4);

                JsonObjectBuilder customerBuilder = Json.createObjectBuilder();
                customerBuilder.add("id", id);
                customerBuilder.add("name", name);
                customerBuilder.add("phone", phone);
                customerBuilder.add("address", address);
                allCustomers.add(customerBuilder.build());
            }

            resp.getWriter().write(allCustomers.build().toString());

        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");

        if (id == null || name == null || phone == null || address == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"id,name, phone and address are required\"}");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            connection.prepareStatement("INSERT INTO customer VALUES ('" + id + "', '" + name + "', '" + phone + "', '" + address + "')").executeUpdate();
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");

        if (id == null || name == null || phone == null || address == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"id, name, phone and age are required\"}");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE customer SET name = ?, phone = ?, address = ? WHERE id = ?");

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, phone);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, id);

            preparedStatement.executeUpdate();

            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");

        if (id == null){
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\" : \"id required\"}");
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/company",
                    "root",
                    "Ijse@123");

            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM customer WHERE id = ?");

            preparedStatement.setString(1, id);

            preparedStatement.executeUpdate();

            resp.setStatus(HttpServletResponse.SC_CREATED);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}