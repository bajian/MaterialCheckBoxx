package com.example.bajian.materialcheckboxx;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.example.bajian.materialcheckbox.MaterialCheckBox;

public class MainActivity extends AppCompatActivity implements MaterialCheckBox.OnCheckedChangeListener{
    private static final String TAG ="MainActivity";
    private MaterialCheckBox cb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        MaterialCheckBox mMaterialCheckBox1 = (MaterialCheckBox) findViewById(R.id.checkbox_1);
        cb = (MaterialCheckBox)findViewById(R.id.cb);
        mMaterialCheckBox1.setChecked(false, false);
        MaterialCheckBox mMaterialCheckBox2 = (MaterialCheckBox) findViewById(R.id.checkbox_2);
        mMaterialCheckBox2.setChecked(false, false);
        mMaterialCheckBox1.setOnCheckedChangeListener(MainActivity.this);
        mMaterialCheckBox2.setOnCheckedChangeListener(MainActivity.this);


        LinearLayout mLlCheckbox = (LinearLayout) findViewById(R.id.ll_checkbox);
        mLlCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cb.callOnClick();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onChange(boolean checked) {
        Log.d(TAG, "onChange"+checked);
    }
}
