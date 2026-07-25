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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditOpportunityActivity extends AppCompatActivity {

    public static final String EXTRA_OPPORTUNITY = "EXTRA_OPPORTUNITY";

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
    private static final String KEY_OPPORTUNITY_OBJ = "opportunity_obj";

    private EditText etEditOpportunityTitle;
    private EditText etEditOpportunityOrganization;
    private Spinner spEditOpportunityCategory;
    private EditText etEditOpportunityDescription;
    private EditText etEditOpportunityDeadline;
    private EditText etEditOpportunityLocation;
    private EditText etEditOpportunityLink;
    private EditText etEditOpportunityEmail;
    private Spinner spEditOpportunityStatus;
    private RadioGroup rgEditOpportunityPriority;
    private EditText etEditOpportunityNotes;
    private ImageView ivEditOpportunityImage;
    private Button btnChangeOpportunityImage;
    private Button btnUpdateOpportunity;
    private Button btnCancelEditOpportunity;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private long userId;
    private Opportunity opportunity;
    private String selectedImageUriStr = null;

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri == null) {
                            return;
                        }
                        selectedImageUriStr = uri.toString();
                        ivEditOpportunityImage.setImageURI(uri);
                        ivEditOpportunityImage.setVisibility(View.VISIBLE);
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

        opportunity = (Opportunity) getIntent().getSerializableExtra(EXTRA_OPPORTUNITY);
        if (opportunity == null) {
            Toast.makeText(this, R.string.msg_data_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (opportunity.getUserId() != userId) {
            Toast.makeText(this, R.string.msg_cannot_edit, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_edit_opportunity);
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupListeners();

        if (savedInstanceState == null) {
            prepopulateForm();
        }
    }

    private void initializeViews() {
        etEditOpportunityTitle = findViewById(R.id.etEditOpportunityTitle);
        etEditOpportunityOrganization = findViewById(R.id.etEditOpportunityOrganization);
        spEditOpportunityCategory = findViewById(R.id.spEditOpportunityCategory);
        etEditOpportunityDescription = findViewById(R.id.etEditOpportunityDescription);
        etEditOpportunityDeadline = findViewById(R.id.etEditOpportunityDeadline);
        etEditOpportunityLocation = findViewById(R.id.etEditOpportunityLocation);
        etEditOpportunityLink = findViewById(R.id.etEditOpportunityLink);
        etEditOpportunityEmail = findViewById(R.id.etEditOpportunityEmail);
        spEditOpportunityStatus = findViewById(R.id.spEditOpportunityStatus);
        rgEditOpportunityPriority = findViewById(R.id.rgEditOpportunityPriority);
        etEditOpportunityNotes = findViewById(R.id.etEditOpportunityNotes);
        ivEditOpportunityImage = findViewById(R.id.ivEditOpportunityImage);
        btnChangeOpportunityImage = findViewById(R.id.btnChangeOpportunityImage);
        btnUpdateOpportunity = findViewById(R.id.btnUpdateOpportunity);
        btnCancelEditOpportunity = findViewById(R.id.btnCancelEditOpportunity);
    }

    private void setupListeners() {
        etEditOpportunityDeadline.setOnClickListener(v -> showDeadlineDatePicker());

        btnChangeOpportunityImage.setOnClickListener(v ->
                imagePickerLauncher.launch(new String[]{"image/*"})
        );

        btnUpdateOpportunity.setOnClickListener(v -> updateOpportunity());
        btnCancelEditOpportunity.setOnClickListener(v -> finish());
    }

    private void prepopulateForm() {
        etEditOpportunityTitle.setText(opportunity.getTitle());
        etEditOpportunityOrganization.setText(opportunity.getOrganization());
        etEditOpportunityDescription.setText(opportunity.getDescription());
        etEditOpportunityDeadline.setText(opportunity.getDeadline());
        etEditOpportunityLocation.setText(opportunity.getLocation());
        etEditOpportunityLink.setText(opportunity.getApplicationLink());
        etEditOpportunityEmail.setText(opportunity.getContactEmail());
        etEditOpportunityNotes.setText(opportunity.getNotes());

        // Category Spinner position selection
        spEditOpportunityCategory.setSelection(findSpinnerPosition(spEditOpportunityCategory, opportunity.getCategory()));

        // Status Spinner position selection
        spEditOpportunityStatus.setSelection(findSpinnerPosition(spEditOpportunityStatus, opportunity.getStatus()));

        // Priority checking
        String priority = opportunity.getPriority();
        if ("Low".equalsIgnoreCase(priority)) {
            rgEditOpportunityPriority.check(R.id.rbEditPriorityLow);
        } else if ("High".equalsIgnoreCase(priority)) {
            rgEditOpportunityPriority.check(R.id.rbEditPriorityHigh);
        } else {
            rgEditOpportunityPriority.check(R.id.rbEditPriorityMedium);
        }

        // Image display
        selectedImageUriStr = opportunity.getImageUri();
        if (selectedImageUriStr != null && !selectedImageUriStr.isEmpty()) {
            try {
                ivEditOpportunityImage.setImageURI(Uri.parse(selectedImageUriStr));
                ivEditOpportunityImage.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {
            }
        }
    }

    private int findSpinnerPosition(Spinner spinner, String value) {
        if (value == null) return 0;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }

    private void showDeadlineDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String currentDeadline = etEditOpportunityDeadline.getText().toString().trim();
        if (!currentDeadline.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = sdf.parse(currentDeadline);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (Exception ignored) {
                // Keep today's date if parsing fails
            }
        }

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
                    etEditOpportunityDeadline.setText(formattedDate);
                    etEditOpportunityDeadline.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void updateOpportunity() {
        if (!validateForm()) {
            return;
        }

        // Get priority value
        String priority = "Medium";
        int checkedId = rgEditOpportunityPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rbEditPriorityLow) {
            priority = "Low";
        } else if (checkedId == R.id.rbEditPriorityHigh) {
            priority = "High";
        }

        opportunity.setTitle(etEditOpportunityTitle.getText().toString().trim());
        opportunity.setOrganization(etEditOpportunityOrganization.getText().toString().trim());
        opportunity.setCategory(spEditOpportunityCategory.getSelectedItem().toString());
        opportunity.setDescription(etEditOpportunityDescription.getText().toString().trim());
        opportunity.setDeadline(etEditOpportunityDeadline.getText().toString().trim());
        opportunity.setLocation(etEditOpportunityLocation.getText().toString().trim());
        opportunity.setApplicationLink(etEditOpportunityLink.getText().toString().trim());
        opportunity.setContactEmail(etEditOpportunityEmail.getText().toString().trim());
        opportunity.setStatus(spEditOpportunityStatus.getSelectedItem().toString());
        opportunity.setPriority(priority);
        opportunity.setNotes(etEditOpportunityNotes.getText().toString().trim());
        opportunity.setImageUri(selectedImageUriStr);

        boolean updated = databaseHelper.updateOpportunity(opportunity);

        if (updated) {
            Toast.makeText(this, R.string.msg_update_success, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.msg_update_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateForm() {
        String title = etEditOpportunityTitle.getText().toString().trim();
        String organization = etEditOpportunityOrganization.getText().toString().trim();
        String description = etEditOpportunityDescription.getText().toString().trim();
        String deadline = etEditOpportunityDeadline.getText().toString().trim();
        String location = etEditOpportunityLocation.getText().toString().trim();
        String link = etEditOpportunityLink.getText().toString().trim();
        String email = etEditOpportunityEmail.getText().toString().trim();

        // Title
        if (title.isEmpty()) {
            showError(etEditOpportunityTitle, getString(R.string.err_title_req));
            return false;
        }
        if (title.length() < 3) {
            showError(etEditOpportunityTitle, getString(R.string.err_title_len));
            return false;
        }

        // Organization
        if (organization.isEmpty()) {
            showError(etEditOpportunityOrganization, getString(R.string.err_organization_req));
            return false;
        }
        if (organization.length() < 2) {
            showError(etEditOpportunityOrganization, getString(R.string.err_organization_len));
            return false;
        }

        // Category Spinner
        if (spEditOpportunityCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.err_category_select, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Description
        if (description.isEmpty()) {
            showError(etEditOpportunityDescription, getString(R.string.err_description_req));
            return false;
        }
        if (description.length() < 10) {
            showError(etEditOpportunityDescription, getString(R.string.err_description_len));
            return false;
        }

        // Deadline
        if (deadline.isEmpty()) {
            showError(etEditOpportunityDeadline, getString(R.string.err_deadline_req));
            return false;
        }

        // Location
        if (!location.isEmpty() && location.length() < 2) {
            showError(etEditOpportunityLocation, getString(R.string.err_location_len));
            return false;
        }

        // Application Link
        if (!link.isEmpty() && !Patterns.WEB_URL.matcher(link).matches()) {
            showError(etEditOpportunityLink, getString(R.string.err_link_invalid));
            return false;
        }

        // Email
        if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEditOpportunityEmail, getString(R.string.err_email_invalid));
            return false;
        }

        // Status Spinner
        if (spEditOpportunityStatus.getSelectedItemPosition() == 0) {
            Toast.makeText(this, R.string.err_status_select, Toast.LENGTH_SHORT).show();
            return false;
        }

        // Priority validation
        if (rgEditOpportunityPriority.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, R.string.err_priority_select, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showError(EditText field, String message) {
        field.setError(message);
        field.requestFocus();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_TITLE, etEditOpportunityTitle.getText().toString());
        outState.putString(KEY_ORGANIZATION, etEditOpportunityOrganization.getText().toString());
        outState.putInt(KEY_CATEGORY_POS, spEditOpportunityCategory.getSelectedItemPosition());
        outState.putString(KEY_DESCRIPTION, etEditOpportunityDescription.getText().toString());
        outState.putString(KEY_DEADLINE, etEditOpportunityDeadline.getText().toString());
        outState.putString(KEY_LOCATION, etEditOpportunityLocation.getText().toString());
        outState.putString(KEY_LINK, etEditOpportunityLink.getText().toString());
        outState.putString(KEY_EMAIL, etEditOpportunityEmail.getText().toString());
        outState.putInt(KEY_STATUS_POS, spEditOpportunityStatus.getSelectedItemPosition());
        outState.putInt(KEY_PRIORITY_ID, rgEditOpportunityPriority.getCheckedRadioButtonId());
        outState.putString(KEY_NOTES, etEditOpportunityNotes.getText().toString());
        if (selectedImageUriStr != null) {
            outState.putString(KEY_IMAGE_URI, selectedImageUriStr);
        }
        if (opportunity != null) {
            outState.putSerializable(KEY_OPPORTUNITY_OBJ, opportunity);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        etEditOpportunityTitle.setText(savedInstanceState.getString(KEY_TITLE));
        etEditOpportunityOrganization.setText(savedInstanceState.getString(KEY_ORGANIZATION));
        spEditOpportunityCategory.setSelection(savedInstanceState.getInt(KEY_CATEGORY_POS));
        etEditOpportunityDescription.setText(savedInstanceState.getString(KEY_DESCRIPTION));
        etEditOpportunityDeadline.setText(savedInstanceState.getString(KEY_DEADLINE));
        etEditOpportunityLocation.setText(savedInstanceState.getString(KEY_LOCATION));
        etEditOpportunityLink.setText(savedInstanceState.getString(KEY_LINK));
        etEditOpportunityEmail.setText(savedInstanceState.getString(KEY_EMAIL));
        spEditOpportunityStatus.setSelection(savedInstanceState.getInt(KEY_STATUS_POS));

        int priorityId = savedInstanceState.getInt(KEY_PRIORITY_ID, -1);
        if (priorityId != -1) {
            rgEditOpportunityPriority.check(priorityId);
        }

        etEditOpportunityNotes.setText(savedInstanceState.getString(KEY_NOTES));

        selectedImageUriStr = savedInstanceState.getString(KEY_IMAGE_URI);
        if (selectedImageUriStr != null) {
            try {
                Uri uri = Uri.parse(selectedImageUriStr);
                ivEditOpportunityImage.setImageURI(uri);
                ivEditOpportunityImage.setVisibility(View.VISIBLE);
            } catch (Exception ignored) {
            }
        }

        opportunity = (Opportunity) savedInstanceState.getSerializable(KEY_OPPORTUNITY_OBJ);
    }
}
