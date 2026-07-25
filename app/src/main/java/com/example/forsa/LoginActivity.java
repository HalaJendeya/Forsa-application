package com.example.forsa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.forsa.database.DatabaseHelper;
import com.example.forsa.models.AuthenticationResult;
import com.example.forsa.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etLoginIdentifier;
    private EditText etLoginPassword;
    private CheckBox cbRememberMe;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.shouldRememberUser() &&
                sessionManager.getUserId() != -1) {
            openHome();
            return;
        }

        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);

        etLoginIdentifier =
                findViewById(R.id.etLoginIdentifier);

        etLoginPassword =
                findViewById(R.id.etLoginPassword);

        cbRememberMe =
                findViewById(R.id.cbRememberMe);

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvCreateAccount =
                findViewById(R.id.tvCreateAccount);

        btnLogin.setOnClickListener(view -> login());

        tvCreateAccount.setOnClickListener(view -> {
            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
        });
    }

    private void login() {
        etLoginIdentifier.setError(null);
        etLoginPassword.setError(null);

        String identifier = etLoginIdentifier
                .getText()
                .toString()
                .trim();

        String password = etLoginPassword
                .getText()
                .toString();

        if (identifier.isEmpty()) {
            showError(
                    etLoginIdentifier,
                    "Email or username is required"
            );
            return;
        }

        if (identifier.contains("@") &&
                !Patterns.EMAIL_ADDRESS
                        .matcher(identifier)
                        .matches()) {

            showError(
                    etLoginIdentifier,
                    "Enter a valid email address"
            );
            return;
        }

        if (password.isEmpty()) {
            showError(
                    etLoginPassword,
                    "Password is required"
            );
            return;
        }

        AuthenticationResult result =
                databaseHelper.authenticateUser(
                        identifier,
                        password
                );

        switch (result.getStatus()) {
            case USER_NOT_FOUND:
                showError(
                        etLoginIdentifier,
                        "User does not exist"
                );
                break;

            case WRONG_PASSWORD:
                showError(
                        etLoginPassword,
                        "Incorrect password"
                );
                break;

            case PROFILE_INCOMPLETE:
                Toast.makeText(
                        this,
                        "Please complete your profile",
                        Toast.LENGTH_SHORT
                ).show();

                Intent profileIntent = new Intent(
                        this,
                        CompleteProfileActivity.class
                );

                profileIntent.putExtra(
                        CompleteProfileActivity.EXTRA_USER_ID,
                        result.getUserId()
                );

                startActivity(profileIntent);
                break;

            case SUCCESS:
                sessionManager.saveSession(
                        result.getUserId(),
                        cbRememberMe.isChecked()
                );

                Toast.makeText(
                        this,
                        "Login successful",
                        Toast.LENGTH_SHORT
                ).show();

                openHome();
                break;
        }
    }

    private void openHome() {
        Intent intent = new Intent(
                this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }
}