package lk.ijse.gdse66.api;

import jakarta.json.*;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import lk.ijse.gdse66.bo.BOFactory;
import lk.ijse.gdse66.bo.custom.ItemBO;
import lk.ijse.gdse66.dto.CustomerDTO;
import lk.ijse.gdse66.dto.ItemDTO;

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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

@WebServlet(name = "item", urlPatterns = "/item")
public class ItemServlet extends HttpServlet {
    ItemBO itemBO = BOFactory.getBOFactory().getBO(BOFactory.BOTypes.ITEM_BO);
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
        String description  = object.getString("description");
        Double price  = Double.valueOf(object.getString("price"));
        int quantity = Integer.parseInt(object.getString("quantity"));
        try (Connection connection = connectionPool.getConnection()){
            ItemDTO itemDTO = new ItemDTO(id,description,price,quantity);
            boolean isSaved = itemBO.saveItem(connection, itemDTO);
            if(isSaved){
                System.out.println("save");
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

    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String id = req.getParameter("id");
        resp.setContentType("application/json");
        try (Connection connection = connectionPool.getConnection()){
            boolean isDeleted = itemBO.removeItem(connection, id);
            if(isDeleted){
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else{
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "failed to delete customer");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String function = req.getParameter("function");
        if (function.equals("getAll")) {
            try (Connection connection = connectionPool.getConnection()) {
                ArrayList<ItemDTO> itemDtoList = itemBO.getAllItems(connection);
                JsonArrayBuilder allItems = Json.createArrayBuilder();
                for (ItemDTO item : itemDtoList) {
                    String code = item.getItemCode();
                    String description = item.getItemDescription();
                    double price = item.getItemPrice(); // Changed from salary to price
                    int qty = item.getItemQty();

                    JsonObjectBuilder itemJson = Json.createObjectBuilder();
                    itemJson.add("code", code);
                    itemJson.add("description", description);
                    itemJson.add("price", price); // Changed from salary to price
                    itemJson.add("qty", qty);
                    allItems.add(itemJson);
                    System.out.println(code);
                }

                resp.addHeader("Content-Type", "application/json");
                JsonObjectBuilder responseObject = Json.createObjectBuilder();
                responseObject.add("state", "OK");
                responseObject.add("message", "Data retrieved successfully");
                responseObject.add("data", allItems.build()); // Convert array to JsonArray

                try (PrintWriter out = resp.getWriter()) {
                    out.print(responseObject.build());
                }
            } catch (SQLException | JsonbException e) {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
        }else if (function.equals("getById")) {
            String selectedCode = req.getParameter("selectedcode");
            if (selectedCode != null) {
                try (Connection connection = connectionPool.getConnection()) {
                    ItemDTO itemDTO = itemBO.getItemByCode(connection, selectedCode);
                    if (itemDTO != null) {
                        JsonObjectBuilder itemJson = Json.createObjectBuilder();
                        itemJson.add("code", itemDTO.getItemCode());
                        itemJson.add("description", itemDTO.getItemDescription());
                        itemJson.add("price", itemDTO.getItemPrice());
                        itemJson.add("qty", itemDTO.getItemQty());

                        resp.setContentType("application/json");
                        try (PrintWriter out = resp.getWriter()) {
                            out.print(itemJson.build());
                        }
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Item not found with code: " + selectedCode);
                    }
                } catch (SQLException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No item code provided");
            }
        }

    }

    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject object = reader.readObject();
        String id  = object.getString("id");
        String description  = object.getString("description");
        Double price  = Double.valueOf(object.getString("price"));
        int quantity = Integer.parseInt(object.getString("quantity"));
        try (Connection connection = connectionPool.getConnection()){
            ItemDTO itemDTO = new ItemDTO(id,description,price,quantity);
            boolean isUpdated = itemBO.updateItem(connection, itemDTO);
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
}
