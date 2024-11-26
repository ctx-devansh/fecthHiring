package com.example.fetchhiring;

/**
 * Class to store the item data
 * @author Devansh Gupta
 */
public class Item {
    final private int id; // id field of the item
    final private int listId; // listId field of the item
    final private String name; // name field of the item

    /**
     * Contructor for the item
     * @param id id of the item
     * @param listId listId of the item
     * @param name name of the item
     */
    public Item(int id,int listId,String name){
        this.id = id;
        this.listId = listId;
        this.name = name;
    }

    /**
     * Getter for the id of the item
     * @return id of the item
     */
    public int getId() {
        return this.id;
    }

    /**
     * Getter for the listId of the item
     * @return listId of the item
     */
    public int getListId() {
        return this.listId;
    }

    /**
     * Getter for the name of the item
     * @return name of the item
     */
    public String getName() {
        return this.name;
    }

    /**
     * Make the string representation of the item
     * @return string representation of the item
     */
    @Override
    public String toString() {
        return "id: " + this.id + " listId: " + this.listId + " name: " + this.name;
    }

}
