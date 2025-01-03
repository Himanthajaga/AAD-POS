import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(urlPatterns = "/lifecycle")
public class LifeCycleServlet extends HttpServlet {
    public LifeCycleServlet() {
        System.out.println("LifeCycleServlet Constructor");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("LifeCycleServlet Get Request received");

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("LifeCycleServlet Init Method");
    }

    @Override
    public void destroy() {
        System.out.println("LifeCycleServlet Destroy Method");
    }
}
