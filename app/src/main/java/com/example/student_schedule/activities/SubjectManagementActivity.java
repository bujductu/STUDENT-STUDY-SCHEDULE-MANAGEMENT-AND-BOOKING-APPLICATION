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
import com.example.student_schedule.adapters.SubjectAdapter;
import com.example.student_schedule.dal.SubjectDAO;
import com.example.student_schedule.databinding.ActivitySubjectManagementBinding;
import com.example.student_schedule.models.Subject;
import com.example.student_schedule.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Administrator screen to view, search, add, edit and delete subjects.
 */
public class SubjectManagementActivity extends AppCompatActivity
        implements SubjectAdapter.OnSubjectActionListener {

    private ActivitySubjectManagementBinding binding;
    private SubjectAdapter adapter;
    private SubjectDAO subjectDAO;
    private String currentQuery = "";

    private final TextWatcher searchWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            currentQuery = s.toString().trim();
            loadSubjects();
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
        binding = ActivitySubjectManagementBinding.inflate(getLayoutInflater());
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

        subjectDAO = new SubjectDAO(this);
        adapter = new SubjectAdapter(this);

        binding.recyclerSubjects.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSubjects.setAdapter(adapter);
        binding.edtSearch.addTextChangedListener(searchWatcher);
        binding.fabAdd.setOnClickListener(view ->
                startActivity(new Intent(this, SubjectFormActivity.class)));

        loadSubjects();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (subjectDAO != null) {
            loadSubjects();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadSubjects() {
        List<Subject> subjects = currentQuery.isEmpty()
                ? subjectDAO.getAll()
                : subjectDAO.search(currentQuery);
        adapter.submitList(subjects);
        binding.txtEmpty.setVisibility(subjects.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onEdit(Subject subject) {
        Intent intent = new Intent(this, SubjectFormActivity.class);
        intent.putExtra(SubjectFormActivity.EXTRA_SUBJECT_ID, subject.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Subject subject) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_subject_title)
                .setMessage(R.string.delete_subject_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete,
                        (dialog, which) -> deleteSubject(subject))
                .show();
    }

    private void deleteSubject(Subject subject) {
        int rows = subjectDAO.delete(subject.getId());
        Toast.makeText(this,
                rows > 0 ? R.string.subject_deleted : R.string.error_delete_failed,
                Toast.LENGTH_SHORT).show();
        loadSubjects();
    }
}
