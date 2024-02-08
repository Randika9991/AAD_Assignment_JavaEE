package lk.ijse.gdse66.api;

import jakarta.json.*;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.gdse66.bo.BOFactory;
import lk.ijse.gdse66.bo.custom.CustomerBO;
import lk.ijse.gdse66.dto.CustomerDTO;
import lk.ijse.gdse66.dto.OrderDTO;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

@WebServlet(name = "customer", urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {
    CustomerBO customerBO = BOFactory.getBOFactory().getBO(BOFactory.BOTypes.CUSTOMER_BO);
    DataSource connectionPool;
    @Override
    public void init() throws ServletException {
        try {
            InitialContext ic = new InitialContext();
            connectionPool = (DataSource) ic.lookup("java:/comp/env/jdbc/PosAssignmentEe");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject object = reader.readObject();
        String id  = object.getString("id");
        String name  = object.getString("name");
        String address  = object.getString("address");
        Double salary  = Double.valueOf(object.getString("salary"));
        try (Connection connection = connectionPool.getConnection()){
            CustomerDTO customerDTO = new CustomerDTO(id,name,address,salary);
            boolean isSaved = customerBO.saveCustomer(connection, customerDTO);
            if(isSaved){
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else{
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "failed to save customer");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Duplicate values. Please check again");
        }catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request.");
        }
    }
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String function = req.getParameter("function");

        if (function.equals("getAll")) {
            try (Connection connection = connectionPool.getConnection()) {
                // Use the connection to interact with the database
                ArrayList<CustomerDTO> customerDTOList = customerBO.getAllCustomers(connection);
                JsonArrayBuilder allCustomers = Json.createArrayBuilder();
                for (CustomerDTO customer : customerDTOList) {
                    String id = customer.getId();
                    String name = customer.getName();
                    String address = customer.getAddress();
                    Double salary = customer.getSalary();

                    JsonObjectBuilder customerpart2 = Json.createObjectBuilder();

                    customerpart2.add("id", id);
                    customerpart2.add("name", name);
                    customerpart2.add("address", address);
                    customerpart2.add("salary", salary);
                    allCustomers.add(customerpart2.build());
                    System.out.println(id);
                }
                // Convert the data to JSON using JSON-B
                resp.addHeader("Content-Type", "application/json");
                JsonObjectBuilder responseObject = Json.createObjectBuilder();
                responseObject.add("state", "OK");
                responseObject.add("message", "Data retrieved successfully");
                responseObject.add("data", allCustomers);

                try (PrintWriter out = resp.getWriter()) {
                    out.print(responseObject.build());
                }

            } catch (SQLException | JsonbException e) {
                // Handle exceptions and send an error response
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else if(function.equals("getById")){
            String selectedId = req.getParameter("selectedId");
            if (selectedId != null) {
                try (Connection connection = connectionPool.getConnection()) {
                    CustomerDTO orderDTO = customerBO.getCustomerById(connection, selectedId);

                    Jsonb jsonb = JsonbBuilder.create();
                    String json = jsonb.toJson(orderDTO);
                    resp.getWriter().write(json);
                } catch (JsonbException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                } catch (IOException | SQLException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No customer ID provided");
            }
        }


    }
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        resp.setContentType("application/json");
        try (Connection connection = connectionPool.getConnection()){
            boolean isDeleted = customerBO.removeCustomer(connection, id);
            if(isDeleted){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "failed to delete customer");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject object = reader.readObject();
        String id  = object.getString("id");
        String name  = object.getString("name");
        String address  = object.getString("address");
        Double salary  = Double.valueOf(object.getString("salary"));
        try (Connection connection = connectionPool.getConnection()){
            CustomerDTO customerDTO = new CustomerDTO(id,name,address,salary);
            boolean isUpdated = customerBO.updateCustomer(connection, customerDTO);
            if(isUpdated){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "failed to update customer");
            }
        }catch (SQLIntegrityConstraintViolationException e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, "Duplicate values. Please check again");
        }catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred while processing the request.");
        }
    }

    private void handleException(HttpServletResponse resp, Exception e, int status) throws IOException {
        JsonObjectBuilder error = Json.createObjectBuilder();
        error.add("state", "error");
        error.add("message", e.getLocalizedMessage());
        error.add("date", "");
        resp.setStatus(status);
        resp.getWriter().print(error.build());
    }
}
