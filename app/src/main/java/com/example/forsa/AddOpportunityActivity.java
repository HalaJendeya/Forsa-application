package com.example.forsa;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.forsa.database.DatabaseHelper;
import com.example.forsa.models.Opportunity;
import com.example.forsa.utils.SessionManager;

import java.util.Calendar;
import java.util.Locale;

public class AddOpportunityActivity extends AppCompatActivity {

    private static final String KEY_TITLE = "opportunity_title";
    private static final String KEY_ORGANIZATION = "opportunity_organization";
    private static final String KEY_CATEGORY_POS = "opportunity_category_pos";
    private static final String KEY_DESCRIPTION = "opportunity_description";
    private static final String KEY_DEADLINE = "opportunity_deadline";
    private static final String KEY_LOCATION = "opportunity_location";
    private static final String KEY_LINK = "opportunity_link";
    private static final String KEY_EMAIL = "opportunity_email";
    private static final String KEY_STATUS_POS = "opportunity_status_pos";
    private static final String KEY_PRIORITY_ID = "opportunity_priority_id";
    private static final String KEY_NOTES = "opportunity_notes";
    private static final String KEY_IMAGE_URI = "opportunity_image_uri";

    private EditText etOpportunityTitle;
    private EditText etOpportunityOrganization;
    private Spinner spOpportunityCategory;
    private EditText etOpportunityDescription;
    private EditText etOpportunityDeadline;
    private EditText etOpportunityLocation;
    private EditText etOpportunityLink;
    private EditText etOpportunityEmail;
    private Spinner spOpportunityStatus;
    private RadioGroup rgOpportunityPriority;
    private EditText etOpportunityNotes;
    private ImageView ivOpportunityImage;
    private Button btnSelectOpportunityImage;
    private Button btnSaveOpportunity;
    private Button btnCancelOpportunity;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private long userId;
    private String selectedImageUriStr = null;

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri == null) {
                            return;
                        }
                        selectedImageUriStr = uri.toString();
                        ivOpportunityImage.setImageURI(uri);
                        ivOpportunityImage.setVisibility(View.VISIBLE);
                        Toast.makeText(AddOpportunityActivity.this, "Opportunity image selected", Toast.LENGTH_SHORT).show();
                        try {
                            getContentResolver().takePersistableUriPermission(
                                     uri,
                                     Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException ignored) {
                            // Catch provider security exception gracefully
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        if (userId == -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_add_opportunity);
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etOpportunityTitle = findViewById(R.id.etOpportunityTitle);
        etOpportunityOrganization = findViewById(R.id.etOpportunityOrganization);
        spOpportunityCategory = findViewById(R.id.spOpportunityCategory);
        etOpportunityDescription = findViewById(R.id.etOpportunityDescription);
        etOpportunityDeadline = findViewById(R.id.etOpportunityDeadline);
        etOpportunityLocation = findViewById(R.id.etOpportunityLocation);
        etOpportunityLink = findViewById(R.id.etOpportunityLink);
        etOpportunityEmail = findViewById(R.id.etOpportunityEmail);
        spOpportunityStatus = findViewById(R.id.spOpportunityStatus);
        rgOpportunityPriority = findViewById(R.id.rgOpportunityPriority);
        etOpportunityNotes = findViewById(R.id.etOpportunityNotes);
        ivOpportunityImage = findViewById(R.id.ivOpportunityImage);
        btnSelectOpportunityImage = findViewById(R.id.btnSelectOpportunityImage);
        btnSaveOpportunity = findViewById(R.id.btnSaveOpportunity);
        btnCancelOpportunity = findViewById(R.id.btnCancelOpportunity);
    }

    private void setupListeners() {
        etOpportunityDeadline.setOnClickListener(v -> showDeadlineDatePicker());

        btnSelectOpportunityImage.setOnClickListener(v ->
                imagePickerLauncher.launch(new String[]{"image/*"})
        );

        btnSaveOpportunity.setOnClickListener(v -> saveOpportunity());
        btnCancelOpportunity.setOnClickListener(v -> {
            Toast.makeText(AddOpportunityActivity.this, "Adding opportunity cancelled", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showDeadlineDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );
                    etOpportunityDeadline.setText(formattedDate);
                    etOpportunityDeadline.setError(null);
                    Toast.makeText(AddOpportunityActivity.this, "Deadline date selected: " + formattedDate, Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Date picker must not permit dates before today
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void saveOpportunity() {
        if (!validateForm()) {
            return;
        }

        // Get priority value
        String priority = "Medium";
        int checkedId = rgOpportunityPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rbPriorityLow) {
            priority = "Low";
        } else if (checkedId == R.id.rbPriorityHigh) {
            priority = "High";
        }

        Opportunity opportunity = new Opportunity(
                userId,
                etOpportunityTitle.getText().toString().trim(),
                etOpportunityOrganization.getText().toString().trim(),
                spOpportunityCategory.getSelectedItem().toString(),
                etOpportunityDescription.getText().toString().trim(),
                etOpportunityDeadline.getText().toString().trim(),
                etOpportunityLocation.getText().toString().trim(),
                etOpportunityLink.getText().toString().trim(),
                etOpportunityEmail.getText().toString().trim(),
                spOpportunityStatus.getSelectedItem().toString(),
                priority,
                etOpportunityNotes.getText().toString().trim(),
                selectedImageUriStr
        );

        long result = databaseHelper.addOpportunity(opportunity);

        if (result != -1) {
            Toast.makeText(this, R.string.msg_opportunity_saved, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.msg_opportunity_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateForm() {
        String title = etOpportunityTitle.getText().toString().trim();
        String organization = etOpportunityOrganization.getText().toString().trim();
        String description = etOpportunityDescription.getText().toString().trim();
        String deadline = etOpportunityDeadline.getText().toString().trim();
        String location = etOpportunityLocation.getText().toString().trim();
        String link = etOpportunityLink.getText().toString().trim();
        String email = etOpportunityEmail.getText().toString().trim();

        // Title
        if (title.isEmpty()) {
            showError(etOpportunityTitle, getString(R.string.err_title_req));
            return false;
        }
        if (title.length() < 3) {
            showError(etOpportunityTitle, getString(R.string.err_title_len));
            return false;
        }

        // Organization
        if (organization.isEmpty()) {
            showError(etOpportunityOrganization, getString(R.string.err_organization_req));
            return false;
        }
        if (organization.length() < 2) {
            showError(etOpportunityOrganization, getString(R.string.err_organization_len));
            return false;
        }

        // Category Spinner
        if (spOpportunityCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.err_category_select, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Description
        if (description.isEmpty()) {
            showError(etOpportunityDescription, getString(R.string.err_description_req));
            return false;
        }
        if (description.length() < 10) {
            showError(etOpportunityDescription, getString(R.string.err_description_len));
            return false;
        }

        // Deadline
        if (deadline.isEmpty()) {
            showError(etOpportunityDeadline, getString(R.string.err_deadline_req));
            return false;
        }

        // Location
        if (!location.isEmpty() && location.length() < 2) {
            showError(etOpportunityLocation, getString(R.string.err_location_len));
            return false;
        }

        // Application Link
        if (!link.isEmpty() && !Patterns.WEB_URL.matcher(link).matches()) {
            showError(etOpportunityLink, getString(R.string.err_link_invalid));
            return false;
        }

        // Email
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etOpportunityEmail, getString(R.string.err_email_invalid));
            return false;
        }

        // Status Spinner
        if (spOpportunityStatus.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.err_status_select, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Priority RadioGroup selection validation (always true since medium is selected by default, but let's confirm)
        if (rgOpportunityPriority.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.err_priority_select, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showError(EditText field, String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        field.setError(message);
        field.requestFocus();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_TITLE, etOpportunityTitle.getText().toString());
        outState.putString(KEY_ORGANIZATION, etOpportunityOrganization.getText().toString());
        outState.putInt(KEY_CATEGORY_POS, spOpportunityCategory.getSelectedItemPosition());
        outState.putString(KEY_DESCRIPTION, etOpportunityDescription.getText().toString());
        outState.putString(KEY_DEADLINE, etOpportunityDeadline.getText().toString());
        outState.putString(KEY_LOCATION, etOpportunityLocation.getText().toString());
        outState.putString(KEY_LINK, etOpportunityLink.getText().toString());
        outState.putString(KEY_EMAIL, etOpportunityEmail.getText().toString());
        outState.putInt(KEY_STATUS_POS, spOpportunityStatus.getSelectedItemPosition());
        outState.putInt(KEY_PRIORITY_ID, rgOpportunityPriority.getCheckedRadioButtonId());
        outState.putString(KEY_NOTES, etOpportunityNotes.getText().toString());
        if (selectedImageUriStr != null) {
            outState.putString(KEY_IMAGE_URI, selectedImageUriStr);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        etOpportunityTitle.setText(savedInstanceState.getString(KEY_TITLE));
        etOpportunityOrganization.setText(savedInstanceState.getString(KEY_ORGANIZATION));
        spOpportunityCategory.setSelection(savedInstanceState.getInt(KEY_CATEGORY_POS));
        etOpportunityDescription.setText(savedInstanceState.getString(KEY_DESCRIPTION));
        etOpportunityDeadline.setText(savedInstanceState.getString(KEY_DEADLINE));
        etOpportunityLocation.setText(savedInstanceState.getString(KEY_LOCATION));
        etOpportunityLink.setText(savedInstanceState.getString(KEY_LINK));
        etOpportunityEmail.setText(savedInstanceState.getString(KEY_EMAIL));
        spOpportunityStatus.setSelection(savedInstanceState.getInt(KEY_STATUS_POS));

        int priorityId = savedInstanceState.getInt(KEY_PRIORITY_ID, -1);
        if (priorityId != -1) {
            rgOpportunityPriority.check(priorityId);
        }

        etOpportunityNotes.setText(savedInstanceState.getString(KEY_NOTES));

        selectedImageUriStr = savedInstanceState.getString(KEY_IMAGE_URI);
        if (selectedImageUriStr != null) {
            try {
                Uri uri = Uri.parse(selectedImageUriStr);
                ivOpportunityImage.setImageURI(uri);
                ivOpportunityImage.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {
                // Catch parsing errors gracefully
            }
        }
    }
}
