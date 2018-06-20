package com.example.kdar.rollcall2.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kdar.rollcall2.R;
import com.example.kdar.rollcall2.model.Classes;
import com.example.kdar.rollcall2.model.Student;
import com.example.kdar.rollcall2.utils.GlobalHelper;
import com.example.kdar.rollcall2.utils.PreferenceHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ClassActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_classRoll)
    TextView tv_classRoll;
    @BindView(R.id.lv_class)
    ListView lv_class;

    private PreferenceHelper preferenceHelper;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);
        preferenceHelper = new PreferenceHelper(getApplicationContext(), GlobalHelper.PREFERENCE_NAME_ROLLCALL);
        ButterKnife.bind(this);
        initUI();
    }

    private void initUI() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(getResources().getString(R.string.listClass));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadData();
    }

    private void loadData() {
        String jsonClass = preferenceHelper.getClassList();
        if (jsonClass.length() == 0)
            return;
        Type listType = new TypeToken<ArrayList<Classes>>() {
        }.getType();
        List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        if (preferenceHelper.getClassPosition() != -1)
            tv_classRoll.setText(getResources().getString(R.string.class_roll) + " " + classList.get(preferenceHelper.getClassPosition()).getNameClass());
        final ArrayList<String> listData = new ArrayList<>();
        for (Classes classes : classList) {
            listData.add(classes.getNameClass());
        }
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listData) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(Color.BLACK);
                // Generate ListView Item using TextView
                return view;
            }
        };
        lv_class.setAdapter(adapter);

        lv_class.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                PopupMenu popupMenu = new PopupMenu(ClassActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.poupup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                deleteClass(position);
                                return true;
                            case R.id.change:
                                changeClass(position);
                                return true;
                            case R.id.choose:
                                chosseClass(position);
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
    }

    private void chosseClass(int position) {
       preferenceHelper.setClassPosition(position);
        loadData();
    }

    private void changeClass(int position) {
        changeNameClass(position);
        loadData();
    }

    private void deleteClass(int position) {
        if (position == preferenceHelper.getClassPosition()) {
            Toast.makeText(getApplicationContext(), "Lớp đang được chọn không thể xoá!", Toast.LENGTH_SHORT).show();
            return;
        }
        String jsonClass = preferenceHelper.getClassList();
        Type listType = new TypeToken<ArrayList<Classes>>() {}.getType();
        List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        classList.remove(position);
        if (position < preferenceHelper.getClassPosition()) {
            preferenceHelper.setClassPosition(preferenceHelper.getClassPosition() - 1);
        }
        Toast.makeText(getApplicationContext(),"Xoá thành công", Toast.LENGTH_SHORT).show();
        preferenceHelper.setClassList(new Gson().toJson(classList));
        loadData();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.btnAddClass)
    void addClass() {
        enterClass();
    }

    private void enterClass() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_enterclass, null);
        final EditText etName = alertLayout.findViewById(R.id.etName);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertLayout);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!etName.getText().toString().equals("")) {
                    String jsonClass = preferenceHelper.getClassList();
                    ArrayList<Classes> classList = new ArrayList<>();
                    if (jsonClass.length() > 0) {
                        Type listType = new TypeToken<ArrayList<Classes>>() {
                        }.getType();
                        classList = new Gson().fromJson(jsonClass, listType);
                    }
                    classList.add(new Classes(etName.getText().toString()));
                    if (classList.size() == 1)
                        preferenceHelper.setClassPosition(0);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.add_class_success), Toast.LENGTH_SHORT).show();
                    preferenceHelper.setClassList(new Gson().toJson(classList));
                    loadData();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enterFullInfo), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }

    private void changeNameClass(final int position) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_enterclass, null);
        String jsonClass = preferenceHelper.getClassList();
        Type listType = new TypeToken<ArrayList<Classes>>() {}.getType();
        final List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        final EditText etName = alertLayout.findViewById(R.id.etName);
        etName.setText(classList.get(position).getNameClass());

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertLayout);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!etName.getText().toString().equals("")) {
                    classList.get(position).setNameClass(etName.getText().toString());

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_class_success), Toast.LENGTH_SHORT).show();
                    preferenceHelper.setClassList(new Gson().toJson(classList));
                    loadData();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enterFullInfo), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
