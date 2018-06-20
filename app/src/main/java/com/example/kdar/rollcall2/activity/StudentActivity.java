package com.example.kdar.rollcall2.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.kdar.rollcall2.R;
import com.example.kdar.rollcall2.adapter.StudentAdapter;
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

public class StudentActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.lv_student)
    ListView lv_student;

    private PreferenceHelper preferenceHelper;
    private int ADD_STUDENT = 97;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);
        preferenceHelper = new PreferenceHelper(getApplicationContext(), GlobalHelper.PREFERENCE_NAME_ROLLCALL);
        ButterKnife.bind(this);
        initUI();
    }

    private void initUI() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle(getResources().getString(R.string.listStudent));
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
        Classes classes = classList.get(preferenceHelper.getClassPosition());
        if (classes.getStudents() == null)
            return;
        final ArrayList<Student> students = classes.getStudents();
        StudentAdapter adapter = new StudentAdapter(this,students);
        lv_student.setAdapter(adapter);
        lv_student.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                PopupMenu popupMenu = new PopupMenu(StudentActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.student_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                deleteStudent(position);
                                return true;
                            case R.id.change:
                                changeStudent(position);
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
    private void changeStudent(final int position) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_entername, null);
        final EditText etName = alertLayout.findViewById(R.id.etName);
        final EditText etNumber = alertLayout.findViewById(R.id.etNumber);
        String jsonClass = preferenceHelper.getClassList();
        Type listType = new TypeToken<ArrayList<Classes>>() {}.getType();
        final List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        etName.setText(classList.get(preferenceHelper.getClassPosition()).getStudents().get(position).getName());
        etNumber.setText(classList.get(preferenceHelper.getClassPosition()).getStudents().get(position).getNumber());

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertLayout);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!etName.getText().toString().equals("") || !etNumber.getText().toString().equals("")) {
                    classList.get(preferenceHelper.getClassPosition()).getStudents().get(position).setName(etName.getText().toString());
                    classList.get(preferenceHelper.getClassPosition()).getStudents().get(position).setNumber(etNumber.getText().toString());

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

    private void deleteStudent(int position) {
        String jsonClass = preferenceHelper.getClassList();
        Type listType = new TypeToken<ArrayList<Classes>>() {}.getType();
        List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        classList.get(preferenceHelper.getClassPosition()).getStudents().remove(position);
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

    @OnClick(R.id.btnAddPerson)
    void onAddPerson() {
        String jsonClass = preferenceHelper.getClassList();
        if (jsonClass.length() == 0) {
            Toast.makeText(getApplicationContext(),"Vui lòng thêm lớp !",Toast.LENGTH_SHORT).show();
            return;
        }
        enterName();
    }

    public void enterName() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_entername, null);
        final EditText etName = alertLayout.findViewById(R.id.etName);
        final EditText etNumber = alertLayout.findViewById(R.id.etNumber);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(alertLayout);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!etName.getText().toString().equals("") || !etNumber.getText().toString().equals("")) {
                    Intent intent = new Intent(StudentActivity.this, TrainingActivity.class);
                    intent.putExtra("name", etName.getText().toString().trim());
                    intent.putExtra("number", etNumber.getText().toString().trim());
                    startActivityForResult(intent,ADD_STUDENT);
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.enterFullInfo), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_STUDENT){
            if (resultCode == RESULT_OK){
                loadData();
                Toast.makeText(getApplicationContext(),"Đã cập nhật danh sách",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
