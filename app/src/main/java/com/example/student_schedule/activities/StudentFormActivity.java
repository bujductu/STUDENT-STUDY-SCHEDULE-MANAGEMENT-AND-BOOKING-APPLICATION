package com.example.student_schedule.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.student_schedule.R;
import com.example.student_schedule.dal.AccountDAO;
import com.example.student_schedule.dal.StudentDAO;
import com.example.student_schedule.databinding.ActivityStudentFormBinding;
import com.example.student_schedule.models.Student;
import com.example.student_schedule.utils.SessionManager;

/**
 * Add/Edit form for a student. In add mode a login account is created with the
 * student code as both username and password.
 */
public class StudentFormActivity extends AppCompatActivity {

    public static final String EXTRA_STUDENT_ID = "extra_student_id";

    private ActivityStudentFormBinding binding;
    private StudentDAO studentDAO;
    private AccountDAO accountDAO;
    private int studentId;
    private boolean isEdit;

    private final TextWatcher codeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            updateLoginPreview(s.toString().trim());
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
        binding = ActivityStudentFormBinding.inflate(getLayoutInflater());
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
        accountDAO = new AccountDAO(this);

        studentId = getIntent().getIntExtra(EXTRA_STUDENT_ID, 0);
        isEdit = studentId > 0;
        setTitle(isEdit ? R.string.title_edit_student : R.string.title_add_student);

        if (isEdit) {
            Student student = studentDAO.findById(studentId);
            if (student == null) {
                Toast.makeText(this, R.string.error_student_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            binding.edtCode.setText(student.getStudentCode());
            binding.edtFullname.setText(student.getFullname());
            binding.edtEmail.setText(student.getEmail());
            binding.edtPhone.setText(student.getPhone());
            binding.txtLoginPreview.setVisibility(View.GONE);
        } else {
            binding.edtCode.addTextChangedListener(codeWatcher);
            updateLoginPreview("");
        }

        binding.btnSave.setOnClickListener(view -> save());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void updateLoginPreview(String code) {
        binding.txtLoginPreview.setText(code.isEmpty()
                ? getString(R.string.login_preview_hint)
                : getString(R.string.login_preview, code, code));
    }

    private void save() {
        clearErrors();

        String code = textOf(binding.edtCode);
        String fullname = textOf(binding.edtFullname);
        String email = textOf(binding.edtEmail);
        String phone = textOf(binding.edtPhone);

        if (code.isEmpty()) {
            binding.tilCode.setError(getString(R.string.error_student_code_required));
            binding.edtCode.requestFocus();
            return;
        }
        if (fullname.isEmpty()) {
            binding.tilFullname.setError(getString(R.string.error_fullname_required));
            binding.edtFullname.requestFocus();
            return;
        }
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            binding.edtEmail.requestFocus();
            return;
        }

        int excludeId = isEdit ? studentId : 0;
        if (studentDAO.studentCodeExists(code, excludeId)) {
            binding.tilCode.setError(getString(R.string.error_student_code_exists));
            binding.edtCode.requestFocus();
            return;
        }
        if (!isEdit && accountDAO.usernameExists(code)) {
            binding.tilCode.setError(getString(R.string.error_username_taken));
            binding.edtCode.requestFocus();
            return;
        }

        if (isEdit) {
            Student student = studentDAO.findById(studentId);
            if (student == null) {
                Toast.makeText(this, R.string.error_student_not_found, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            student.setStudentCode(code);
            student.setFullname(fullname);
            student.setEmail(email);
            student.setPhone(phone);
            if (studentDAO.update(student) > 0) {
                Toast.makeText(this, R.string.student_updated, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Student student = new Student(code, fullname, email, phone, 0);
        long id = studentDAO.insertWithAccount(student, code, code);
        if (id == -1) {
            Toast.makeText(this, R.string.error_save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(R.string.student_added, code, code), Toast.LENGTH_LONG).show();
        finish();
    }

    private void clearErrors() {
        binding.tilCode.setError(null);
        binding.tilFullname.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
    }

    private static String textOf(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
