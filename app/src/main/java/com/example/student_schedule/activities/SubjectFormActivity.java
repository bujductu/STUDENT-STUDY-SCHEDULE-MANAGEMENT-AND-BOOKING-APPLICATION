package com.example.student_schedule.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.student_schedule.R;
import com.example.student_schedule.dal.SubjectDAO;
import com.example.student_schedule.databinding.ActivitySubjectFormBinding;
import com.example.student_schedule.models.Subject;
import com.example.student_schedule.utils.SessionManager;

/**
 * Add/Edit form for a subject.
 */
public class SubjectFormActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "extra_subject_id";

    private static final int MAX_CREDITS = 30;

    private ActivitySubjectFormBinding binding;
    private SubjectDAO subjectDAO;
    private int subjectId;
    private boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!new SessionManager(this).isAdmin()) {
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivitySubjectFormBinding.inflate(getLayoutInflater());
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

        subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, 0);
        isEdit = subjectId > 0;
        setTitle(isEdit ? R.string.title_edit_subject : R.string.title_add_subject);

        if (isEdit) {
            Subject subject = subjectDAO.findById(subjectId);
            if (subject == null) {
                Toast.makeText(this, R.string.error_subject_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            binding.edtCode.setText(subject.getSubjectCode());
            binding.edtName.setText(subject.getSubjectName());
            binding.edtDescription.setText(subject.getDescription());
            binding.edtCredits.setText(String.valueOf(subject.getCredits()));
        }

        binding.btnSave.setOnClickListener(view -> save());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void save() {
        clearErrors();

        String code = textOf(binding.edtCode);
        String name = textOf(binding.edtName);
        String description = textOf(binding.edtDescription);
        String creditsText = textOf(binding.edtCredits);

        if (code.isEmpty()) {
            binding.tilCode.setError(getString(R.string.error_subject_code_required));
            binding.edtCode.requestFocus();
            return;
        }
        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.error_subject_name_required));
            binding.edtName.requestFocus();
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
        } catch (NumberFormatException exception) {
            binding.tilCredits.setError(getString(R.string.error_credits_invalid));
            binding.edtCredits.requestFocus();
            return;
        }
        if (credits < 0 || credits > MAX_CREDITS) {
            binding.tilCredits.setError(getString(R.string.error_credits_invalid));
            binding.edtCredits.requestFocus();
            return;
        }

        int excludeId = isEdit ? subjectId : 0;
        if (subjectDAO.subjectCodeExists(code, excludeId)) {
            binding.tilCode.setError(getString(R.string.error_subject_code_exists));
            binding.edtCode.requestFocus();
            return;
        }

        if (isEdit) {
            Subject subject = subjectDAO.findById(subjectId);
            if (subject == null) {
                Toast.makeText(this, R.string.error_subject_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            subject.setSubjectCode(code);
            subject.setSubjectName(name);
            subject.setDescription(description);
            subject.setCredits(credits);
            if (subjectDAO.update(subject) > 0) {
                Toast.makeText(this, R.string.subject_updated, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Subject subject = new Subject(code, name, description, credits);
        long id = subjectDAO.insert(subject);
        if (id == -1) {
            Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, R.string.subject_added, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void clearErrors() {
        binding.tilCode.setError(null);
        binding.tilName.setError(null);
        binding.tilDescription.setError(null);
        binding.tilCredits.setError(null);
    }

    private static String textOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
