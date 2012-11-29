/**
 * 
 */
package com.thundersnatch;

import java.util.ArrayList;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * @author kwilliams
 *
 */
public class LobbyLists extends ListActivity {

	//LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();

    //DEFINING STRING ADAPTER WHICH WILL HANDLE DATA OF LISTVIEW
    ArrayAdapter<String> adapter;
    
    ListView view;

    public LobbyLists(ListView v){
    	view = v;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        adapter=new ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1,
            listItems);
        setListAdapter(adapter);
    }

    //METHOD WHICH WILL HANDLE DYNAMIC INSERTION
    public void addItems(String s) {
    	if(!listItems.contains(s)){
    		listItems.add(s);
    		adapter.notifyDataSetChanged();
    	}
    }
}
