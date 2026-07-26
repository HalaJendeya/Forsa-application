package com.example.forsa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.ImageView;
import android.net.Uri;
import android.content.res.ColorStateList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.forsa.database.DatabaseHelper;
import com.example.forsa.models.User;
import com.example.forsa.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    private TextView tvDashboardWelcome;
    private TextView tvTotalOpportunities;
    private TextView tvAppliedOpportunities;
    private TextView tvAcceptedOpportunities;
    private ImageView ivDashboardProfileImage;
    
    private View btnViewOpportunities;
    private View btnOpenProfile;
    private View btnLogout;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        // PART 3: SESSION CHECK
        if (userId == -1) {
            redirectToLogin();
            return;
        }

        // PART 4: INITIALIZE DASHBOARD
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != -1) {
            loadDashboardData();
        }
    }

    private void initializeViews() {
        tvDashboardWelcome = findViewById(R.id.tvDashboardWelcome);
        tvTotalOpportunities = findViewById(R.id.tvTotalOpportunities);
        tvAppliedOpportunities = findViewById(R.id.tvAppliedOpportunities);
        tvAcceptedOpportunities = findViewById(R.id.tvAcceptedOpportunities);

        btnViewOpportunities = findViewById(R.id.btnViewOpportunities);
        btnOpenProfile = findViewById(R.id.btnOpenProfile);
        btnLogout = findViewById(R.id.btnLogout);
        ivDashboardProfileImage = findViewById(R.id.ivDashboardProfileImage);
    }

    private void setupListeners() {
        // PART 6: OPPORTUNITIES NAVIGATION
        btnViewOpportunities.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Opening opportunities...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, OpportunitiesActivity.class);
            startActivity(intent);
        });

        btnOpenProfile.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Opening profile...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, CompleteProfileActivity.class);
            intent.putExtra(CompleteProfileActivity.EXTRA_USER_ID, userId);
            intent.putExtra(CompleteProfileActivity.EXTRA_EDIT_MODE, true);
            startActivity(intent);
        });

        // PART 8: LOGOUT
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Showing logout confirmation...", Toast.LENGTH_SHORT).show();
            showLogoutConfirmationDialog();
        });
    }

    // PART 5: LOAD USER AND STATISTICS
    private void loadDashboardData() {
        User user = databaseHelper.getUserById(userId);
        if (user == null) {
            sessionManager.clearSession();
            redirectToLogin();
            return;
        }

        String welcomeText = getString(R.string.dashboard_welcome, user.getFullName());
        tvDashboardWelcome.setText(welcomeText);

        int total = databaseHelper.getTotalOpportunityCount(userId);
        int applied = databaseHelper.getOpportunityCountByStatus(userId, "Applied");
        int accepted = databaseHelper.getOpportunityCountByStatus(userId, "Accepted");

        tvTotalOpportunities.setText(String.valueOf(total));
        tvAppliedOpportunities.setText(String.valueOf(applied));
        tvAcceptedOpportunities.setText(String.valueOf(accepted));

        // Load profile image or set default
        String profileImage = user.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                ivDashboardProfileImage.setImageURI(Uri.parse(profileImage));
                ivDashboardProfileImage.setImageTintList(null);
            } catch (Exception e) {
                ivDashboardProfileImage.setImageResource(R.drawable.ic_profile);
                ivDashboardProfileImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary, getTheme())));
            }
        } else {
            ivDashboardProfileImage.setImageResource(R.drawable.ic_profile);
            ivDashboardProfileImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.primary, getTheme())));
        }
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_logout_title_p7)
                .setMessage(R.string.dialog_logout_message_p7)
                .setPositiveButton(R.string.btn_dashboard_logout, (dialog, which) -> {
                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    sessionManager.clearSession();
                    redirectToLogin();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}