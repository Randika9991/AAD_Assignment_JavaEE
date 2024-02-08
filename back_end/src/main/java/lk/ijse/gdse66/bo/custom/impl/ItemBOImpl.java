package lk.ijse.gdse66.bo.custom.impl;

import lk.ijse.gdse66.bo.custom.ItemBO;
import lk.ijse.gdse66.dao.DAOFactory;
import lk.ijse.gdse66.dao.custom.ItemDAO;
import lk.ijse.gdse66.dto.ItemDTO;
import lk.ijse.gdse66.entity.Item;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemBOImpl implements ItemBO {

    ItemDAO itemDAO = DAOFactory.getDAOFactory().getDAO(DAOFactory.DAOTypes.ITEM_DAO);

    @Override
    public boolean saveItem(Connection connection, ItemDTO dto) throws SQLException {
        return itemDAO.save(connection, new Item(dto.getItemCode(), dto.getItemDescription(), dto.getItemPrice(), dto.getItemQty()));
    }

    @Override
    public boolean updateItem(Connection connection, ItemDTO dto) throws SQLException {
        return itemDAO.update(connection, new Item(dto.getItemCode(), dto.getItemDescription(), dto.getItemPrice(), dto.getItemQty()));
    }

    @Override
    public ArrayList<ItemDTO> getAllItems(Connection connection) throws SQLException {
        ArrayList<Item> itemList = itemDAO.getAll(connection);

        ArrayList<ItemDTO> itemDTOList = new ArrayList<ItemDTO>();

        for(Item item : itemList){
            ItemDTO dto = new ItemDTO(
                    item.getItemCode(),
                    item.getItemDescription(),
                    item.getItemPrice(),
                    item.getItemQty()
            );

            itemDTOList.add(dto);
        }
        return itemDTOList;
    }

    @Override
    public ItemDTO getItemByCode(Connection connection, String id) throws SQLException {
        Item item = itemDAO.findBy(connection, id);

        return new ItemDTO(
                item.getItemCode(),
                item.getItemDescription(),
                item.getItemPrice(),
                item.getItemQty()
        );
    }

    @Override
    public boolean removeItem(Connection connection, String id) throws SQLException {
        return itemDAO.delete(connection, id);
    }
}
