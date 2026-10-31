package com.example.student_schedule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.student_schedule.activities.BookingFormActivity;
import com.example.student_schedule.activities.CalendarActivity;
import com.example.student_schedule.activities.LoginActivity;
import com.example.student_schedule.activities.ScheduleListActivity;
import com.example.student_schedule.activities.StudentManagementActivity;
import com.example.student_schedule.activities.SubjectManagementActivity;
import com.example.student_schedule.databinding.ActivityMainBinding;
import com.example.student_schedule.utils.SessionManager;

/**
 * Role-aware dashboard shown after login. Administrators get the student and
 * subject management entries; students get booking, schedule and calendar
 * entries.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager session;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> {
                        // The app keeps working without notifications.
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        bindDashboard();
    }

    private void bindDashboard() {
        binding.txtWelcome.setText(getString(R.string.welcome_user, session.getUsername()));
        binding.txtRole.setText(session.isAdmin() ? R.string.role_admin : R.string.role_student);

        boolean isAdmin = session.isAdmin();
        int adminVisibility = isAdmin ? View.VISIBLE : View.GONE;
        int studentVisibility = isAdmin ? View.GONE : View.VISIBLE;

        binding.btnStudentManagement.setVisibility(adminVisibility);
        binding.btnSubjectManagement.setVisibility(adminVisibility);
        binding.btnBooking.setVisibility(studentVisibility);
        binding.btnSchedule.setVisibility(studentVisibility);
        binding.btnCalendar.setVisibility(studentVisibility);

        binding.btnStudentManagement.setOnClickListener(view ->
                startActivity(new Intent(this, StudentManagementActivity.class)));
        binding.btnSubjectManagement.setOnClickListener(view ->
                startActivity(new Intent(this, SubjectManagementActivity.class)));
        binding.btnBooking.setOnClickListener(view ->
                startActivity(new Intent(this, BookingFormActivity.class)));
        binding.btnSchedule.setOnClickListener(view ->
                startActivity(new Intent(this, ScheduleListActivity.class)));
        binding.btnCalendar.setOnClickListener(view ->
                startActivity(new Intent(this, CalendarActivity.class)));

        if (session.isStudent()) {
            requestNotificationPermission();
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            confirmLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.action_logout, (dialog, which) -> logout())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void logout() {
        session.clear();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
