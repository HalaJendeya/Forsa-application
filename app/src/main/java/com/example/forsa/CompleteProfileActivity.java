package com.example.forsa;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.forsa.database.DatabaseHelper;
import com.example.forsa.models.User;
import com.example.forsa.utils.PasswordUtils;
import com.example.forsa.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompleteProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "USER_ID";
    public static final String EXTRA_EDIT_MODE = "EXTRA_EDIT_MODE";

    private TextView tvProfileTitle;
    private TextView tvProfileSubtitle;
    private TextView tvProfileFullName;
    private TextView tvProfileUsername;
    private TextView tvProfileEmail;

    private ImageView ivProfileImage;
    private Button btnSelectImage;
    private Button btnSaveProfile;
    private Button btnEmailProfile;
    private Button btnChangePassword;

    private EditText etPhone;
    private EditText etBirthDate;
    private EditText etCity;
    private EditText etInstitution;
    private EditText etMajor;

    private Spinner spEducationLevel;

    private CheckBox cbScholarships;
    private CheckBox cbInternships;
    private CheckBox cbTraining;
    private CheckBox cbVolunteering;
    private CheckBox cbCompetitions;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    private long userId;
    private Uri selectedImageUri;
    private Calendar selectedBirthDate;
    private boolean isEditMode = false;
    private User currentUser;

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri == null) {
                            return;
                        }

                        selectedImageUri = uri;
                        ivProfileImage.setImageURI(uri);
                        Toast.makeText(CompleteProfileActivity.this, "Profile image selected", Toast.LENGTH_SHORT).show();

                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException ignored) {
                            // Some image providers do not allow persistent access.
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        databaseHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        isEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);

        if (userId == -1) {
            Toast.makeText(
                    this,
                    "Invalid user account",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        currentUser = databaseHelper.getUserById(userId);
        if (currentUser == null) {
            Toast.makeText(this, R.string.msg_profile_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // PART 2: PROFILE OWNERSHIP
        if (isEditMode) {
            long sessionUserId = sessionManager.getUserId();
            if (sessionUserId == -1 || sessionUserId != userId) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        }

        initializeViews();
        configureListeners();
        setupModeUIAndLoad();
    }

    private void initializeViews() {
        tvProfileTitle = findViewById(R.id.tvProfileTitle);
        tvProfileSubtitle = findViewById(R.id.tvProfileSubtitle);
        tvProfileFullName = findViewById(R.id.tvProfileFullName);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnEmailProfile = findViewById(R.id.btnEmailProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        etPhone = findViewById(R.id.etPhone);
        etBirthDate = findViewById(R.id.etBirthDate);
        etCity = findViewById(R.id.etCity);
        etInstitution = findViewById(R.id.etInstitution);
        etMajor = findViewById(R.id.etMajor);

        spEducationLevel = findViewById(R.id.spEducationLevel);

        cbScholarships = findViewById(R.id.cbScholarships);
        cbInternships = findViewById(R.id.cbInternships);
        cbTraining = findViewById(R.id.cbTraining);
        cbVolunteering = findViewById(R.id.cbVolunteering);
        cbCompetitions = findViewById(R.id.cbCompetitions);
    }

    private void configureListeners() {
        btnSelectImage.setOnClickListener(view ->
                imagePickerLauncher.launch(
                        new String[]{"image/*"}
                )
        );

        etBirthDate.setOnClickListener(view ->
                showBirthDatePicker()
        );

        btnSaveProfile.setOnClickListener(view ->
                validateAndSaveProfile()
        );

        btnEmailProfile.setOnClickListener(view -> {
            Toast.makeText(CompleteProfileActivity.this, "Opening email client...", Toast.LENGTH_SHORT).show();
            sendProfileEmail();
        });

        btnChangePassword.setOnClickListener(view -> {
            Toast.makeText(CompleteProfileActivity.this, "Opening change password dialog...", Toast.LENGTH_SHORT).show();
            showChangePasswordDialog();
        });
    }

    private void setupModeUIAndLoad() {
        // Populate read-only account values
        tvProfileFullName.setText(currentUser.getFullName());
        tvProfileUsername.setText(currentUser.getUsername());
        tvProfileEmail.setText(currentUser.getEmail());

        if (isEditMode) {
            tvProfileTitle.setText(R.string.title_my_profile_p8);
            tvProfileSubtitle.setText(R.string.subtitle_my_profile);
            btnSaveProfile.setText(R.string.btn_update_profile);
            btnEmailProfile.setVisibility(View.VISIBLE);
            btnChangePassword.setVisibility(View.VISIBLE);

            loadUserProfile(currentUser);
        } else {
            tvProfileTitle.setText(R.string.title_complete_profile_p8);
            tvProfileSubtitle.setText(R.string.complete_profile_subtitle);
            btnSaveProfile.setText(R.string.save_and_continue);
            btnEmailProfile.setVisibility(View.GONE);
            btnChangePassword.setVisibility(View.GONE);
        }
    }

    // PART 4: LOAD EXISTING PROFILE DATA
    private void loadUserProfile(User user) {
        etPhone.setText(user.getPhone());
        etBirthDate.setText(user.getBirthDate());
        etCity.setText(user.getCity());
        etInstitution.setText(user.getInstitution());
        etMajor.setText(user.getMajor());

        // Parse birth date yyyy-MM-dd
        String birthDateStr = user.getBirthDate();
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = sdf.parse(birthDateStr);
                if (date != null) {
                    selectedBirthDate = Calendar.getInstance();
                    selectedBirthDate.setTime(date);
                }
            } catch (Exception ignored) {
                // Keep displayed text, do not crash
            }
        }

        // Select Spinner item
        String eduLevel = user.getEducationLevel();
        if (eduLevel != null) {
            spEducationLevel.setSelection(findSpinnerPosition(spEducationLevel, eduLevel));
        }

        // Interests Checkboxes
        String interests = user.getInterests();
        if (interests != null) {
            cbScholarships.setChecked(interests.contains("Scholarships"));
            cbInternships.setChecked(interests.contains("Internships"));
            cbTraining.setChecked(interests.contains("Training"));
            cbVolunteering.setChecked(interests.contains("Volunteering"));
            cbCompetitions.setChecked(interests.contains("Competitions"));
        }

        // Profile image
        String profileImage = user.getProfileImage();
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                selectedImageUri = Uri.parse(profileImage);
                ivProfileImage.setImageURI(selectedImageUri);
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

    private void showBirthDatePicker() {
        Calendar initialDate = Calendar.getInstance();
        initialDate.add(Calendar.YEAR, -18);

        if (selectedBirthDate != null) {
            initialDate = selectedBirthDate;
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (datePicker, year, month, day) -> {
                    selectedBirthDate = Calendar.getInstance();
                    selectedBirthDate.set(year, month, day);

                    String formattedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );

                    etBirthDate.setText(formattedDate);
                    etBirthDate.setError(null);
                    Toast.makeText(CompleteProfileActivity.this, "Birth date selected: " + formattedDate, Toast.LENGTH_SHORT).show();
                },
                initialDate.get(Calendar.YEAR),
                initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMaxDate(
                System.currentTimeMillis()
        );

        dialog.show();
    }

    private void validateAndSaveProfile() {
        clearErrors();

        String phone = etPhone.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String institution = etInstitution.getText().toString().trim();
        String major = etMajor.getText().toString().trim();

        if (!phone.isEmpty() &&
                !phone.matches("\\+?[0-9]{7,15}")) {

            showError(
                    etPhone,
                    "Enter a valid phone number"
            );
            return;
        }

        if (birthDate.isEmpty() || selectedBirthDate == null) {
            showError(
                    etBirthDate,
                    "Birth date is required"
            );
            return;
        }

        if (calculateAge(selectedBirthDate) < 15) {
            showError(
                    etBirthDate,
                    "You must be at least 15 years old"
            );
            return;
        }

        if (city.isEmpty()) {
            showError(etCity, "City is required");
            return;
        }

        if (!city.matches("[\\p{L} .'-]{2,50}")) {
            showError(
                    etCity,
                    "Enter a valid city name"
            );
            return;
        }

        if (institution.isEmpty()) {
            showError(
                    etInstitution,
                    "University or college is required"
            );
            return;
        }

        if (institution.length() < 2) {
            showError(
                    etInstitution,
                    "Enter a valid institution name"
            );
            return;
        }

        if (major.isEmpty()) {
            showError(etMajor, "Major is required");
            return;
        }

        if (major.length() < 2) {
            showError(etMajor, "Enter a valid major");
            return;
        }

        if (spEducationLevel.getSelectedItemPosition() == 0) {
            Toast.makeText(
                    this,
                    "Select your education level",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        List<String> selectedInterests = getSelectedInterests();

        if (selectedInterests.isEmpty()) {
            Toast.makeText(
                    this,
                    "Select at least one preferred opportunity",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String educationLevel =
                spEducationLevel.getSelectedItem().toString();

        String interests = TextUtils.join(
                ",",
                selectedInterests
        );

        String profileImage = selectedImageUri == null
                ? null
                : selectedImageUri.toString();

        boolean updated = databaseHelper.updateUserProfile(
                userId,
                phone,
                birthDate,
                city,
                institution,
                major,
                educationLevel,
                interests,
                profileImage
        );

        if (updated) {
            if (isEditMode) {
                Toast.makeText(
                        this,
                        R.string.msg_profile_update_success_p8,
                        Toast.LENGTH_SHORT
                ).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(
                        this,
                        "Profile completed successfully",
                        Toast.LENGTH_SHORT
                ).show();

                Intent intent = new Intent(
                        CompleteProfileActivity.this,
                        LoginActivity.class
                );

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                );

                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(
                    this,
                    isEditMode ? getString(R.string.msg_profile_update_failed_p8) : "Could not save the profile",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private List<String> getSelectedInterests() {
        List<String> interests = new ArrayList<>();

        if (cbScholarships.isChecked()) {
            interests.add("Scholarships");
        }

        if (cbInternships.isChecked()) {
            interests.add("Internships");
        }

        if (cbTraining.isChecked()) {
            interests.add("Training");
        }

        if (cbVolunteering.isChecked()) {
            interests.add("Volunteering");
        }

        if (cbCompetitions.isChecked()) {
            interests.add("Competitions");
        }

        return interests;
    }

    private int calculateAge(Calendar birthDate) {
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) -
                birthDate.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) <
                birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    // PART 7: IMPLICIT EMAIL INTENT
    private void sendProfileEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + currentUser.getEmail()));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.msg_no_email_app, Toast.LENGTH_SHORT).show();
        }
    }

    // PART 10: CHANGE PASSWORD DIALOG LOGIC
    private void showChangePasswordDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Change Password")
                .setView(dialogView)
                .setPositiveButton(R.string.btn_update_password, null) // Set null initially to prevent auto-close
                .setNegativeButton(R.string.btn_cancel_p8, null)
                .create();

        dialog.show();

        // Overriding the PositiveButton ClickListener to prevent automatic closing
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmNewPassword = etConfirmNewPassword.getText().toString();

            if (currentPassword.isEmpty()) {
                etCurrentPassword.setError("Current password is required");
                etCurrentPassword.requestFocus();
                return;
            }

            // Validate new password requirements: >= 8 length, uppercase, lowercase, digit
            if (newPassword.length() < 8) {
                etNewPassword.setError("Password must contain at least 8 characters");
                etNewPassword.requestFocus();
                return;
            }

            boolean hasUpper = false;
            boolean hasLower = false;
            boolean hasDigit = false;
            for (char c : newPassword.toCharArray()) {
                if (Character.isUpperCase(c)) hasUpper = true;
                else if (Character.isLowerCase(c)) hasLower = true;
                else if (Character.isDigit(c)) hasDigit = true;
            }

            if (!hasUpper || !hasLower || !hasDigit) {
                etNewPassword.setError("Password must contain uppercase, lowercase, and a number");
                etNewPassword.requestFocus();
                return;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                etConfirmNewPassword.setError("Passwords do not match");
                etConfirmNewPassword.requestFocus();
                return;
            }

            if (newPassword.equals(currentPassword)) {
                etNewPassword.setError(getString(R.string.err_new_password_different));
                etNewPassword.requestFocus();
                return;
            }

            // Verify current password hash
            boolean isPasswordCorrect = PasswordUtils.verifyPassword(currentPassword, currentUser.getPasswordHash());
            if (!isPasswordCorrect) {
                etCurrentPassword.setError(getString(R.string.err_current_password_incorrect));
                etCurrentPassword.requestFocus();
                return;
            }

            // Hash and update
            String newPasswordHash = PasswordUtils.hashPassword(newPassword);
            boolean isUpdated = databaseHelper.updateUserPassword(userId, newPasswordHash);
            if (isUpdated) {
                Toast.makeText(CompleteProfileActivity.this, R.string.msg_password_update_success, Toast.LENGTH_SHORT).show();
                // Reload user from SQLite
                currentUser = databaseHelper.getUserById(userId);
                dialog.dismiss();
            } else {
                Toast.makeText(CompleteProfileActivity.this, R.string.msg_password_update_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showError(EditText field, String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        field.setError(message);
        field.requestFocus();
    }

    private void clearErrors() {
        etPhone.setError(null);
        etBirthDate.setError(null);
        etCity.setError(null);
        etInstitution.setError(null);
        etMajor.setError(null);
    }
}