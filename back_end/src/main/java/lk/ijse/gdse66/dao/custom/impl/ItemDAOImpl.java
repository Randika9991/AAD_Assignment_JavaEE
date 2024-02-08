package lk.ijse.gdse66.dao.custom.impl;

import lk.ijse.gdse66.dao.custom.ItemDAO;
import lk.ijse.gdse66.dao.util.CrudUtil;
import lk.ijse.gdse66.entity.Item;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemDAOImpl implements ItemDAO {
    @Override
    public boolean save(Connection connection, Item entity) throws SQLException {
        String sql ="INSERT INTO item (item_id,item_description,item_price,qty) VALUES (?,?,?,?)";
        return CrudUtil.execute(connection, sql, entity.getItemCode(), entity.getItemDescription(), entity.getItemPrice(), entity.getItemQty());
    }

    @Override
    public boolean update(Connection connection, Item entity) throws SQLException {
        String sql = "UPDATE item SET item_description = ?, item_price = ?, qty = ? WHERE item_id = ?";
        return CrudUtil.execute(connection, sql, entity.getItemDescription(), entity.getItemPrice(), entity.getItemQty(), entity.getItemCode());
    }

    @Override
    public ArrayList<Item> getAll(Connection connection) throws SQLException {
        String sql = "SELECT * FROM item";
        ArrayList<Item> itemList = new ArrayList<Item>();
        ResultSet rs = CrudUtil.execute(connection, sql);

        while(rs.next()){
            Item item = new Item(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getDouble(3),
                    rs.getInt(4)
            );

            itemList.add(item);
        }
        return itemList;
    }

    @Override
    public boolean delete(Connection connection, String id) throws SQLException {
        String sql = "DELETE FROM item WHERE item_id = ?";
        return CrudUtil.execute(connection, sql, id);
    }


    @Override
    public Item findBy(Connection connection, String id) throws SQLException {
        String sql = "SELECT * FROM item WHERE item_id = ?";
        Item item = new Item();
        ResultSet rs = CrudUtil.execute(connection, sql, id);

        if(rs.next()) {
            item.setItemCode(rs.getString(1));
            item.setItemDescription(rs.getString(2));
            item.setItemPrice(rs.getDouble(3));
            item.setItemQty(rs.getInt(4));
        }
        return item;
    }

    @Override
    public boolean reduceQty(Connection connection, Item item) throws SQLException {
        String sql = "UPDATE item SET qty = ( qty - ? ) WHERE item_id = ?";
        return CrudUtil.execute(connection, sql, item.getItemQty(), item.getItemCode());
    }
}
