package com.example.forsa;
import com.example.forsa.database.DatabaseHelper;
import com.example.forsa.models.User;
import com.example.forsa.utils.PasswordUtils;

import java.util.Locale;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Intent;


import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etUsername;
    private EditText etEmail;
    private EditText etConfirmEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private CheckBox cbConfirmInformation;
    private Button btnRegister;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etConfirmEmail = findViewById(R.id.etConfirmEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbConfirmInformation = findViewById(R.id.cbConfirmInformation);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        databaseHelper = new DatabaseHelper(this);
        btnRegister.setOnClickListener(view -> validateRegistration());
        tvLogin.setOnClickListener(view -> {
            Toast.makeText(RegisterActivity.this, "Returning to Login...", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void validateRegistration() {
        clearErrors();

        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String confirmEmail = etConfirmEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        if (fullName.isEmpty()) {
            showError(etFullName, "Full name is required");
            return;
        }

        if (fullName.length() < 3) {
            showError(etFullName, "Full name must contain at least 3 characters");
            return;
        }

        if (!fullName.matches("[\\p{L} .'-]+")) {
            showError(etFullName, "Full name can only contain letters");
            return;
        }

        if (username.isEmpty()) {
            showError(etUsername, "Username is required");
            return;
        }

        if (!username.matches("[A-Za-z0-9_]{3,20}")) {
            showError(
                    etUsername,
                    "Use 3–20 letters, numbers, or underscores"
            );
            return;
        }

        if (email.isEmpty()) {
            showError(etEmail, "Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, "Enter a valid email address");
            return;
        }

        if (confirmEmail.isEmpty()) {
            showError(etConfirmEmail, "Please confirm your email");
            return;
        }

        if (!email.equalsIgnoreCase(confirmEmail)) {
            showError(etConfirmEmail, "Email addresses do not match");
            return;
        }

        if (password.isEmpty()) {
            showError(etPassword, "Password is required");
            return;
        }

        if (password.length() < 8) {
            showError(etPassword, "Password must contain at least 8 characters");
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            showError(etPassword, "Password must contain an uppercase letter");
            return;
        }

        if (!password.matches(".*[a-z].*")) {
            showError(etPassword, "Password must contain a lowercase letter");
            return;
        }

        if (!password.matches(".*\\d.*")) {
            showError(etPassword, "Password must contain a number");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, "Passwords do not match");
            return;
        }

        if (!cbConfirmInformation.isChecked()) {
            Toast.makeText(
                    this,
                    "Please confirm that your information is correct",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (databaseHelper.usernameExists(username)) {
            showError(etUsername, "This username is already used");
            return;
        }

        if (databaseHelper.emailExists(email)) {
            showError(etEmail, "This email is already registered");
            return;
        }

        String passwordHash = PasswordUtils.hashPassword(password);

        User user = new User(
                fullName,
                username,
                email.toLowerCase(Locale.ROOT),
                passwordHash
        );

        long userId = databaseHelper.addUser(user);

        if (userId != -1) {
            Toast.makeText(
                    this,
                    "Account created successfully",
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(
                    RegisterActivity.this,
                    CompleteProfileActivity.class
            );

            intent.putExtra(CompleteProfileActivity.EXTRA_USER_ID, userId);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(
                    this,
                    "Could not create the account",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void showError(EditText field, String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        field.setError(message);
        field.requestFocus();
    }

    private void clearErrors() {
        etFullName.setError(null);
        etUsername.setError(null);
        etEmail.setError(null);
        etConfirmEmail.setError(null);
        etPassword.setError(null);
        etConfirmPassword.setError(null);
    }

    private void clearForm() {
        etFullName.setText("");
        etUsername.setText("");
        etEmail.setText("");
        etConfirmEmail.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
        cbConfirmInformation.setChecked(false);

        etFullName.requestFocus();
    }
}