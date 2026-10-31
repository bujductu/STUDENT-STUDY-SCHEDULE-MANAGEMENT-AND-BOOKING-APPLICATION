package com.example.student_schedule.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.student_schedule.MainActivity;
import com.example.student_schedule.R;
import com.example.student_schedule.dal.AccountDAO;
import com.example.student_schedule.dal.StudentDAO;
import com.example.student_schedule.databinding.ActivityLoginBinding;
import com.example.student_schedule.models.Account;
import com.example.student_schedule.models.Student;
import com.example.student_schedule.utils.SessionManager;

/**
 * Login screen for both roles. The role is read from the Account table after a
 * successful credential check, so students cannot sign in as administrators.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AccountDAO accountDAO;
    private StudentDAO studentDAO;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        accountDAO = new AccountDAO(this);
        studentDAO = new StudentDAO(this);
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            if (isSessionStillValid()) {
                openMain();
                return;
            }
            // The account or linked student row no longer exists.
            session.clear();
        }

        binding.btnLogin.setOnClickListener(view -> attemptLogin());
        binding.edtPassword.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    private boolean isSessionStillValid() {
        Account account = accountDAO.findById(session.getAccountId());
        if (account == null) {
            return false;
        }
        if (account.isStudent()) {
            return studentDAO.findByAccountId(account.getId()) != null;
        }
        return true;
    }

    private void attemptLogin() {
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);

        String username = binding.edtUsername.getText() == null
                ? "" : binding.edtUsername.getText().toString().trim();
        String password = binding.edtPassword.getText() == null
                ? "" : binding.edtPassword.getText().toString();

        if (username.isEmpty()) {
            binding.tilUsername.setError(getString(R.string.error_username_required));
            binding.edtUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_password_required));
            binding.edtPassword.requestFocus();
            return;
        }

        Account account = accountDAO.login(username, password);
        if (account == null) {
            binding.tilPassword.setError(getString(R.string.error_invalid_credentials));
            binding.edtPassword.setText("");
            binding.edtPassword.requestFocus();
            return;
        }

        int studentId = -1;
        if (account.isStudent()) {
            Student student = studentDAO.findByAccountId(account.getId());
            if (student == null) {
                binding.tilUsername.setError(getString(R.string.error_missing_student_profile));
                return;
            }
            studentId = student.getId();
        }

        session.saveSession(account, studentId);
        openMain();
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
