package com.example.fetchhiring;

import android.content.ClipData;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import com.example.fetchhiring.databinding.LayoutBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Class for the backend of the app
 * @author Devansh Gupta
 */
public class MainActivity extends AppCompatActivity {

    ArrayAdapter<String> list1Adapter; // Adapter for the ListView
    LayoutBinding binding; // Binding for the layout
    Handler mainHandler = new Handler(); // Handler to make updates to the layout
    // A sorted hashmap that stores key, values pairs
    // Keys refer to unique listId and values refer to the ArrayList containing the Item objects
    // SortedMap has been used to eliminate the need to sort the hashmap by key or listId
    SortedMap<Integer,ArrayList<Item>> itemsByGroup = new TreeMap<Integer,ArrayList<Item>>();
    // ArrayList to store the headings of each group
    // and the string representation of each item
    List<String> itemsList = new ArrayList<String>(); // ArrayList to store the headings of each group

    /**
     * Code to execute when the app is started
     * @param savedInstance If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        binding = LayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeList();
        new fetchData().start();
    }

    /**
     * Set the adapter for putting items into listview.
     * Items are in the form of strings.
     */
    private void initializeList() {
        list1Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,itemsList);
        binding.List1.setAdapter(list1Adapter);
    }

    class fetchData extends Thread {

        @Override
        public void run() {

            try {
                // Get the url of the JSON data and form a connection
                URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // Get the input stream for the JSON data and get the buffered reader for it
                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                // Variable for storing each JSON line
                String line;
                // Clear the hashmap for the data grouped by listId
                itemsByGroup.clear();
                // Keep reading lines until the value is not null
                while ((line = br.readLine()) != null) {
                    // Ignore the JSON array brackets
                    if (line.equals("[") || line.equals("]")) {
                        continue;
                    }
                    // Transform the JSON line into a JSON object so that we can
                    // query data according to fields
                    JSONObject obj = new JSONObject(line);
                    // Ignore items where name is null or empty i.e ("")
                    if (obj.getString("name").isEmpty() || obj.getString("name").equals("null")) {
                        continue;
                    }
                    // After making the necessary checks take the necessary data
                    // and make it into an Item object
                    // (custom class check Item.java for implementation)
                    Item newItem = new Item(obj.getInt("id"), obj.getInt("listId"), obj.getString("name"));
                    // Call the addItem function to add the Item into the hashmap
                    // storing data according to groups
                    addItem(itemsByGroup, newItem.getListId(), newItem);
                }
                // After all data has been added sort each group of data
                // (according to listId) by the name
                sortByName(itemsByGroup);
                // Form the list of Items by getting the key set of the hashmap storing data according
                // to listId groups. Unique listId's are used are keys here.
                // Get the string representation of each Item
                // and add it to an array list.
                // Before adding the items of a certain group we also add the heading
                // containing the current listId group being added. This helps visualize the
                // data in groups
                for (int key : itemsByGroup.keySet()) {
                    itemsList.add("List Id :" + key);
                    for (Item currItem : itemsByGroup.get(key)) {
                        itemsList.add("\t->\t\t"+currItem.toString());
                    }
                }
                // Use the handler to inform the adapter for the ListView that data has been added
                // to make necessary changes to the ListView and so that items are displayed
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        list1Adapter.notifyDataSetChanged();
                    }
                });
            } catch (MalformedURLException e) {
                // Catch the exception thrown when trying to make the URL object
                Log.e("Error: ", "Could not get URL");
            } catch (JSONException e) {
                // Catch the exception thrown when trying to make the JSON object
                Log.e("Error: ", "Could not parse JSON");
            } catch (IOException e) {
                // Catch the exception thrown when trying to establish
                // connection to the JSON data URL
                Log.e("Error: ", "Could not form connection with URL");
            }

        }
    }

    /**
     * Function to sort Items by their name
     * @param list the hashmap sotring the data in groups
     */
    public static void sortByName(SortedMap<Integer,ArrayList<Item>> list) {
        // Get all the unique data groups i.e unique listIds
        // by which the data is grouped
        for (int itemsListId : list.keySet()) {
            // Get the arraylist at each key containing the Item of that specific listId
            ArrayList<Item> itemList = list.get(itemsListId);
            // Call the sort function of Collections with the custom sorting as a lambda expression
            itemList.sort((item1,item2) -> {
                // Since the name of each Item is formatted as "Item " followed by a number
                // we sort the names by comparing the numbers at the end.
                // The numbers at the end are retrieved by splitting the name at the
                // space which gives an array containing "Item" at 0 index and the number at index 1
                // We parse and store the number given to us as a string
                int item1Num = Integer.parseInt(item1.getName().split(" ")[1]);
                int item2Num = Integer.parseInt(item2.getName().split(" ")[1]);

                // If number of the item1 is smaller than that of item2
                // return -1 saying that the name of item1 is smaller
                if (item1Num < item2Num) {
                    return -1;
                    // If number of the item1 is greater than that of item2
                    // return -1 saying that the name of item1 is greater
                }else if (item1Num > item2Num) {
                    return 1;
                }
                // Return 0 if they are equal
                return 0;
            });
        }
    }

    /**
     * Function to add a new Item object in the correct group
     * @param map hashmap storing grouped data
     * @param key listId of the Item and also the key at
     *            which the Item should be stored
     * @param itemToAdd the new Item object that is to be added
     */
    public void addItem(SortedMap<Integer,ArrayList<Item>> map, int key,Item itemToAdd) {
        // Get the reference of the arraylist of Items at the given key (listId)
        ArrayList<Item> itemList = map.get(key);
        // Check if the there is no arraylist related to the key
        if (itemList == null) {
            // If not then make a new arraylist object
            itemList = new ArrayList<Item>();
            // Put the arraylist at the key
            map.put(key, itemList);
        }
        // Add the item to the arraylist of the given key
        itemList.add(itemToAdd);
    }
}