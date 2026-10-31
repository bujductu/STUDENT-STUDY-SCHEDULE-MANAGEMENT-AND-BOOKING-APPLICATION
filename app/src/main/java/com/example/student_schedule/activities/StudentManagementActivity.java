package com.example.student_schedule.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.student_schedule.R;
import com.example.student_schedule.adapters.StudentAdapter;
import com.example.student_schedule.dal.StudentDAO;
import com.example.student_schedule.databinding.ActivityStudentManagementBinding;
import com.example.student_schedule.models.Student;
import com.example.student_schedule.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Administrator screen to view, search, add, edit and delete students.
 */
public class StudentManagementActivity extends AppCompatActivity
        implements StudentAdapter.OnStudentActionListener {

    private ActivityStudentManagementBinding binding;
    private StudentAdapter adapter;
    private StudentDAO studentDAO;
    private String currentQuery = "";

    private final TextWatcher searchWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            currentQuery = s.toString().trim();
            loadStudents();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!new SessionManager(this).isAdmin()) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityStudentManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        studentDAO = new StudentDAO(this);
        adapter = new StudentAdapter(this);

        binding.recyclerStudents.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerStudents.setAdapter(adapter);
        binding.edtSearch.addTextChangedListener(searchWatcher);
        binding.fabAdd.setOnClickListener(view ->
                startActivity(new Intent(this, StudentFormActivity.class)));

        loadStudents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (studentDAO != null) {
            loadStudents();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadStudents() {
        List<Student> students = currentQuery.isEmpty()
                ? studentDAO.getAll()
                : studentDAO.search(currentQuery);
        adapter.submitList(students);
        binding.txtEmpty.setVisibility(students.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(Student student) {
        Intent intent = new Intent(this, StudentFormActivity.class);
        intent.putExtra(StudentFormActivity.EXTRA_STUDENT_ID, student.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Student student) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_student_title)
                .setMessage(R.string.delete_student_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete,
                        (dialog, which) -> deleteStudent(student))
                .show();
    }

    private void deleteStudent(Student student) {
        int rows = studentDAO.deleteWithAccount(student.getId());
        Toast.makeText(this,
                rows > 0 ? R.string.student_deleted : R.string.error_delete_failed,
                Toast.LENGTH_SHORT).show();
        loadStudents();
    }
}
