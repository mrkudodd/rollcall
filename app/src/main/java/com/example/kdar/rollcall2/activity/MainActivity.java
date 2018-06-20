package com.example.kdar.rollcall2.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kdar.rollcall2.R;
import com.example.kdar.rollcall2.adapter.StudentAdapter;
import com.example.kdar.rollcall2.model.Classes;
import com.example.kdar.rollcall2.model.Student;
import com.example.kdar.rollcall2.utils.GlobalHelper;
import com.example.kdar.rollcall2.utils.PreferenceHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.opencv.android.OpenCVLoader;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.tv_nameClass)
    TextView nameClass;
    @BindView(R.id.tv_hasBeen)
    TextView hasBeen;
    @BindView(R.id.tv_day)
    TextView day;
    @BindView(R.id.lv_student)
    ListView lv_student;

    private PreferenceHelper preferenceHelper;
    private static final String TAG = "MainActivity";
    private int REQUEST_CLASS = 98;
    private int REQUEST_STUDENT = 99;
    private int REQUEST_ROLL = 100;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d(TAG, "OpenCV not loaded");
        else
            Log.d(TAG, "OpenCV loaded");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferenceHelper = new PreferenceHelper(getApplicationContext(), GlobalHelper.PREFERENCE_NAME_ROLLCALL);
        ButterKnife.bind(this);
        initUI();
    }

    private void initUI() {
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        DateFormat df = new SimpleDateFormat("dd-MM-yy");
        day.setText(df.format(Calendar.getInstance().getTime()));

        setClassIndex();

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
        if (classes.getStudents() == null) {
            StudentAdapter adapter = new StudentAdapter(this,new ArrayList<Student>());
            lv_student.setAdapter(adapter);
            hasBeen.setText("0/0");
            return;
        }
        final ArrayList<Student> students = classes.getStudents();
        StudentAdapter adapter = new StudentAdapter(this,students);
        lv_student.setAdapter(adapter);

        int roll = 0;
        for (Student student : students){
            if (student.isRolled())
                roll++;
        }
        hasBeen.setText(roll + "/" + students.size());
    }

    private void setClassIndex() {
        String jsonClass = preferenceHelper.getClassList();
        if (jsonClass.length() == 0)
            return;
        Type listType = new TypeToken<ArrayList<Classes>>() {
        }.getType();
        List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        nameClass.setText(classList.get(preferenceHelper.getClassPosition()).getNameClass());
    }

    @OnClick(R.id.btnRecognize)
    void onRecognize() {
        String jsonClass = preferenceHelper.getClassList();
        if (jsonClass.length() == 0) {
            Toast.makeText(getApplicationContext(),"Chưa có sinh viên",Toast.LENGTH_SHORT).show();
            return;
        }
        Type listType = new TypeToken<ArrayList<Classes>>() {}.getType();
        List<Classes> classList = new Gson().fromJson(jsonClass, listType);
        Classes classes = classList.get(preferenceHelper.getClassPosition());
        if (classes.getStudents() == null) {
            Toast.makeText(getApplicationContext(),"Chưa có sinh viên",Toast.LENGTH_SHORT).show();
            return;
        }else if (classes.getStudents().size() == 0){
            Toast.makeText(getApplicationContext(),"Chưa có sinh viên",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MainActivity.this, RecognizeActivity.class);
        startActivityForResult(intent,REQUEST_ROLL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_history:
                View menuItemView = findViewById(R.id.menu_history); // SAME ID AS MENU ID
                PopupMenu popupMenu = new PopupMenu(this, menuItemView);
                popupMenu.inflate(R.menu.main_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_history:
                                startActivity(new Intent(MainActivity.this,HistoryActivity.class));
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_class) {
            Intent intent = new Intent(MainActivity.this, ClassActivity.class);
            startActivityForResult(intent,REQUEST_CLASS);
        } else if (id == R.id.nav_student) {
            Intent intent = new Intent(MainActivity.this, StudentActivity.class);
            startActivityForResult(intent,REQUEST_STUDENT);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CLASS){
            if (resultCode == RESULT_OK){
                setClassIndex();
                loadData();
            }
        }
        if (requestCode == REQUEST_STUDENT){
            if (resultCode == RESULT_OK){
                loadData();
            }
        }
        if (requestCode == REQUEST_ROLL){
            if (resultCode == RESULT_OK){
                loadData();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
