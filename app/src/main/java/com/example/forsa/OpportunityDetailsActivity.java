package com.example.forsa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.forsa.database.DatabaseHelper;
import com.example.forsa.models.Opportunity;
import com.example.forsa.utils.SessionManager;

public class OpportunityDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_OPPORTUNITY = "EXTRA_OPPORTUNITY";

    private final ActivityResultLauncher<Intent> editOpportunityLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    }
            );

    private ImageView ivDetailsImage;
    private TextView tvDetailsTitle;
    private TextView tvDetailsOrganization;
    private TextView tvDetailsCategory;
    private TextView tvDetailsDescription;
    private TextView tvDetailsDeadline;
    private TextView tvDetailsLocation;
    private TextView tvDetailsLink;
    private TextView tvDetailsEmail;
    private TextView tvDetailsStatus;
    private TextView tvDetailsPriority;
    private TextView tvDetailsNotes;

    private Button btnOpenApplicationLink;
    private Button btnEmailOrganization;
    private Button btnEditOpportunityDetails;
    private Button btnDeleteOpportunityDetails;
    private Button btnBackFromDetails;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private long userId;
    private Opportunity opportunity;

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

        setContentView(R.layout.activity_opportunity_details);
        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        populateFields();
        setupListeners();
    }

    private void initializeViews() {
        ivDetailsImage = findViewById(R.id.ivDetailsImage);
        tvDetailsTitle = findViewById(R.id.tvDetailsTitle);
        tvDetailsOrganization = findViewById(R.id.tvDetailsOrganization);
        tvDetailsCategory = findViewById(R.id.tvDetailsCategory);
        tvDetailsDescription = findViewById(R.id.tvDetailsDescription);
        tvDetailsDeadline = findViewById(R.id.tvDetailsDeadline);
        tvDetailsLocation = findViewById(R.id.tvDetailsLocation);
        tvDetailsLink = findViewById(R.id.tvDetailsLink);
        tvDetailsEmail = findViewById(R.id.tvDetailsEmail);
        tvDetailsStatus = findViewById(R.id.tvDetailsStatus);
        tvDetailsPriority = findViewById(R.id.tvDetailsPriority);
        tvDetailsNotes = findViewById(R.id.tvDetailsNotes);

        btnOpenApplicationLink = findViewById(R.id.btnOpenApplicationLink);
        btnEmailOrganization = findViewById(R.id.btnEmailOrganization);
        btnEditOpportunityDetails = findViewById(R.id.btnEditOpportunityDetails);
        btnDeleteOpportunityDetails = findViewById(R.id.btnDeleteOpportunityDetails);
        btnBackFromDetails = findViewById(R.id.btnBackFromDetails);
    }

    private void populateFields() {
        // Required fields
        tvDetailsTitle.setText(opportunity.getTitle());
        tvDetailsOrganization.setText(opportunity.getOrganization());
        tvDetailsCategory.setText(opportunity.getCategory());
        tvDetailsDescription.setText(opportunity.getDescription());
        tvDetailsDeadline.setText(opportunity.getDeadline());
        tvDetailsStatus.setText(opportunity.getStatus());
        tvDetailsPriority.setText(opportunity.getPriority());

        applyStatusStyle(tvDetailsStatus, opportunity.getStatus());
        applyPriorityStyle(tvDetailsPriority, opportunity.getPriority());

        // Optional fields
        String location = opportunity.getLocation();
        tvDetailsLocation.setText((location == null || location.isEmpty()) ? getString(R.string.lbl_not_provided) : location);

        String appLink = opportunity.getApplicationLink();
        tvDetailsLink.setText((appLink == null || appLink.isEmpty()) ? getString(R.string.lbl_not_provided) : appLink);

        String contactEmail = opportunity.getContactEmail();
        tvDetailsEmail.setText((contactEmail == null || contactEmail.isEmpty()) ? getString(R.string.lbl_not_provided) : contactEmail);

        String notes = opportunity.getNotes();
        tvDetailsNotes.setText((notes == null || notes.isEmpty()) ? getString(R.string.lbl_not_provided) : notes);

        // Conditional Image Loading
        String imageUriStr = opportunity.getImageUri();
        if (imageUriStr != null && !imageUriStr.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imageUriStr);
                ivDetailsImage.setImageURI(imageUri);
                ivDetailsImage.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivDetailsImage.setVisibility(View.GONE);
            }
        } else {
            ivDetailsImage.setVisibility(View.GONE);
        }

        // Disable link button if empty
        if (appLink == null || appLink.isEmpty()) {
            btnOpenApplicationLink.setEnabled(false);
            btnOpenApplicationLink.setAlpha(0.5f);
        }

        // Disable email button if empty
        if (contactEmail == null || contactEmail.isEmpty()) {
            btnEmailOrganization.setEnabled(false);
            btnEmailOrganization.setAlpha(0.5f);
        }
    }

    private void setupListeners() {
        btnOpenApplicationLink.setOnClickListener(v -> openApplicationLink());
        btnEmailOrganization.setOnClickListener(v -> emailOrganization());
        btnEditOpportunityDetails.setOnClickListener(v -> {
            Toast.makeText(OpportunityDetailsActivity.this, "Opening edit screen...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(OpportunityDetailsActivity.this, EditOpportunityActivity.class);
            intent.putExtra(EditOpportunityActivity.EXTRA_OPPORTUNITY, opportunity);
            editOpportunityLauncher.launch(intent);
        });
        btnDeleteOpportunityDetails.setOnClickListener(v -> {
            Toast.makeText(OpportunityDetailsActivity.this, "Showing delete confirmation...", Toast.LENGTH_SHORT).show();
            showDeleteConfirmationDialog();
        });
        btnBackFromDetails.setOnClickListener(v -> finish());
    }

    private void openApplicationLink() {
        Toast.makeText(this, "Opening application link...", Toast.LENGTH_SHORT).show();
        String appLink = opportunity.getApplicationLink();
        if (appLink == null || appLink.isEmpty()) {
            Toast.makeText(this, R.string.msg_link_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        String url = appLink.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, R.string.msg_no_browser, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.msg_no_browser, Toast.LENGTH_SHORT).show();
        }
    }

    private void emailOrganization() {
        Toast.makeText(this, "Opening email client...", Toast.LENGTH_SHORT).show();
        String contactEmail = opportunity.getContactEmail();
        if (contactEmail == null || contactEmail.isEmpty()) {
            Toast.makeText(this, R.string.msg_email_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri mailUri = Uri.fromParts("mailto", contactEmail, null);
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, mailUri);
            String subject = getString(R.string.email_subject_template, opportunity.getTitle());
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                Toast.makeText(this, R.string.msg_no_email_client, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.msg_no_email_client, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.btn_delete_details, (dialog, which) -> {
                    boolean deleted = databaseHelper.deleteOpportunity(opportunity.getId(), userId);
                    if (deleted) {
                        Toast.makeText(this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, R.string.msg_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void applyStatusStyle(TextView view, String status) {
        if (status == null) {
            view.setBackgroundResource(R.drawable.bg_badge_slate);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.text_secondary));
            return;
        }

        switch (status) {
            case "Saved":
                view.setBackgroundResource(R.drawable.bg_badge_indigo);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.indigo_badge_text));
                break;
            case "Applied":
                view.setBackgroundResource(R.drawable.bg_badge_amber);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.amber_badge_text));
                break;
            case "Accepted":
                view.setBackgroundResource(R.drawable.bg_badge_green);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.green_badge_text));
                break;
            case "Rejected":
                view.setBackgroundResource(R.drawable.bg_badge_red);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.red_badge_text));
                break;
            case "Expired":
            default:
                view.setBackgroundResource(R.drawable.bg_badge_slate);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.text_secondary));
                break;
        }
    }

    private void applyPriorityStyle(TextView view, String priority) {
        if (priority == null) {
            view.setBackgroundResource(R.drawable.bg_badge_slate);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.text_secondary));
            return;
        }

        switch (priority) {
            case "Low":
                view.setBackgroundResource(R.drawable.bg_badge_green);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.green_badge_text));
                break;
            case "Medium":
                view.setBackgroundResource(R.drawable.bg_badge_amber);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.amber_badge_text));
                break;
            case "High":
                view.setBackgroundResource(R.drawable.bg_badge_red);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.red_badge_text));
                break;
            default:
                view.setBackgroundResource(R.drawable.bg_badge_slate);
                view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.text_secondary));
                break;
        }
    }
}
