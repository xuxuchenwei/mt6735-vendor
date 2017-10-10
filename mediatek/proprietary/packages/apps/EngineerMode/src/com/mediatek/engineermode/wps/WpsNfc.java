package com.mediatek.engineermode.wps;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mediatek.engineermode.R;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;

import android.app.ActionBar;
import android.view.MenuItem;

/**
 * wifi wps NFC test_bed in engineermode
 *
 * @author mtk54040
 *
 */
public class WpsNfc extends ListActivity {

    private static final String TAG = "EM/WpsNfc";

    private ArrayList<String> mModuleList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wps_nfc);


        mModuleList = new ArrayList<String>();
        mModuleList.add(getString(R.string.wps_role));
        mModuleList.add(getString(R.string.wps_write_tag));

        ArrayAdapter<String> moduleAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mModuleList);
        setListAdapter(moduleAdapter);

        ActionBar actionBar = getActionBar();			// add 2015-12
        if (actionBar != null) {
        	actionBar.setDisplayHomeAsUpEnabled(true);	// add 2015-12
        	actionBar.setHomeButtonEnabled(true);		// add 2015-12
        }
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {		// add 2015-12
    	if (item.getItemId() == android.R.id.home) {				
    		finish();
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (mModuleList.get(position).equals(
                getString(R.string.wps_role))) {
            Xlog.v(TAG, "-->onListItemClick wps_role");
            startActivity(new Intent(WpsNfc.this, WpsNfcRole.class));
        } else if (mModuleList.get(position).equals(
                getString(R.string.wps_write_tag))) {
            Xlog.v(TAG, "-->onListItemClick wps_write_tag");
            startActivity(new Intent(WpsNfc.this, WpsNfcTag.class));
        }
    }



}
