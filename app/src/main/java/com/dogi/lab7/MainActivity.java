package com.dogi.lab7;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.database.Cursor;
import com.google.android.material.snackbar.Snackbar;

import android.view.ContextMenu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import java.util.ArrayList;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dogi.lab7.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TabHost;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    public TabHost tabs;
    ListView listView;
    ListView listView2;
    DBHelper mDatabaseHelper;
    SQLiteDatabase mSqLiteDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tabs = (TabHost) findViewById(android.R.id.tabhost);
        tabs.setup();
//первая вкладка
        TabHost.TabSpec spec = tabs.newTabSpec("tag1");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Не выполнено");
        tabs.addTab(spec);
//вторая вкладка
        spec = tabs.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Выполнено");
        tabs.addTab(spec);
//выводим на передний план первую вкладку
        tabs.setCurrentTab(0);

        mDatabaseHelper = new DBHelper(this, "mydatabase.db", null, 1);
        mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
        listView = (ListView)findViewById(R.id.listView);
        listView2 = (ListView)findViewById(R.id.listView2);
        registerForContextMenu(listView);
        registerForContextMenu(listView2);
        myList();

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View form = MainActivity.this.getLayoutInflater().inflate(R.layout.add_task,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Новая задача:")
                        .setView(form).setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText et = (EditText)form.findViewById(R.id.editText);
                                String mtask = et.getText().toString();
                                if (!mtask.isEmpty()){
                                    Insert(mtask,"0");}
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();

            }
        });
        binding.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSqLiteDatabase.delete("mytask", null, null);
                myList();
            }
        });
    }

    public void Insert (String mtask, String done){
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_NAME_COLUMN, mtask);
        values.put(DBHelper.TASK_DONE_COLUMN, "0");
        mSqLiteDatabase.insert("mytask", null, values);
        myList();
        tabs.setCurrentTab(0);

    }

    public void Execute(String mtask){
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_DONE_COLUMN, "1");
        Toast.makeText(MainActivity.this, "Выполнено: " + mtask,Toast.LENGTH_SHORT).show();
        mSqLiteDatabase.update("mytask", values, DBHelper.TASK_NAME_COLUMN + "= ?", new String[]{mtask});
        myList();

    }

    public void Cancel(String mtask){
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_DONE_COLUMN, "0");
        Toast.makeText(MainActivity.this, "Изменено: " + mtask,Toast.LENGTH_SHORT).show();
        mSqLiteDatabase.update("mytask", values, DBHelper.TASK_NAME_COLUMN + "= ?", new String[]{mtask});
        myList();
    }

    public void Deleteall(String mtask){

    }
    public void Delete(String mtask){
        Toast.makeText(MainActivity.this, "Удалено: " + mtask,Toast.LENGTH_SHORT).show();
        mSqLiteDatabase.delete("mytask", DBHelper.TASK_NAME_COLUMN + "= ?", new String[]{mtask});
        myList();

    }

    public void Сhange(final String oldTaskName){
        final View form = MainActivity.this.getLayoutInflater().inflate(R.layout.add_task, null);
        final EditText et = (EditText) form.findViewById(R.id.editText);

        et.setText(oldTaskName);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Изменить задачу:")
                .setView(form)
                .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newTaskName = et.getText().toString();

                        if (!newTaskName.isEmpty()) {
                            // Вызываем метод обновления
                            updateTaskInDb(oldTaskName, newTaskName);
                        }
                    }
                })
                .setNegativeButton("Отмена", null);

        builder.create().show();
    }

    private void updateTaskInDb(String oldName, String newName) {
        ContentValues values = new ContentValues();
        values.put(DBHelper.TASK_NAME_COLUMN, newName);


        mSqLiteDatabase.update("mytask",
                values,
                DBHelper.TASK_NAME_COLUMN + " = ?",
                new String[]{oldName});


        myList();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void myList (){
        Cursor cursor = mSqLiteDatabase.query("mytask", new String[]{DBHelper._ID, DBHelper.TASK_NAME_COLUMN,
                        DBHelper.TASK_DONE_COLUMN},
                null, null,
                null, null, null);
        final ArrayList<String> task = new ArrayList<String>();
        final ArrayList<String> task2 = new ArrayList<String>();
        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, task);
        final ArrayAdapter<String> adapter2;
        adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, task2);
        listView.setAdapter(adapter);
        listView2.setAdapter(adapter2);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(DBHelper._ID));
                String taskname = cursor.getString(cursor.getColumnIndex(DBHelper.TASK_NAME_COLUMN));
                int taskdone = cursor.getInt(cursor.getColumnIndex(DBHelper.TASK_DONE_COLUMN));
                if (taskdone == 0){
                    task.add(0, taskname);
                    adapter.notifyDataSetChanged();
                }
                else {
                    task2.add(taskname);
                    adapter2.notifyDataSetChanged();
                }
            } while (cursor.moveToNext());
        }else Toast.makeText(this,"задач нет", Toast.LENGTH_SHORT).show();
        cursor.close();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        int id = v.getId();
        if (id == R.id.listView) {
            getMenuInflater().inflate(R.menu.context_list1, menu);
        }
            else if (id == R.id.listView2){
                getMenuInflater().inflate(R.menu.context_list2, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        String task;
        int id = item.getItemId();
            //пункт выполнить
            if (id == R.id.execute) {
                task = (String) listView.getItemAtPosition(info.position);
                Execute(task);
            }
            //пункт изменить
            if (id == R.id.change){
                task = (String)listView.getItemAtPosition(info.position);
                Сhange(task);
            }
            //пункт удалить
            if (id == R.id.delete){
                task = (String)listView.getItemAtPosition(info.position);
                Delete(task);
            }
            //пункт отменить
            if (id == R.id.cancel){
                task = (String)listView2.getItemAtPosition(info.position);
                Cancel(task);
            }
            //пункт удалить 2
            if (id == R.id.delete2) {
                task = (String) listView2.getItemAtPosition(info.position);
                Delete(task);
            }
        return super.onContextItemSelected(item);
    }

}