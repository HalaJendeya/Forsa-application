package com.example.forsa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forsa.adapters.OpportunitiesAdapter;
import com.example.forsa.database.DatabaseHelper;
import com.example.forsa.models.Opportunity;
import com.example.forsa.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class OpportunitiesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOpportunities;
    private TextView tvEmptyOpportunities;
    private TextView tvOpportunitiesCount;
    private View emptyStateContainer;
    private FloatingActionButton fabAddOpportunity;

    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private OpportunitiesAdapter adapter;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        userId = sessionManager.getUserId();

        // Redirect to Login if not authenticated
        if (userId == -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_opportunities);

        databaseHelper = new DatabaseHelper(this);

        recyclerViewOpportunities = findViewById(R.id.recyclerViewOpportunities);
        tvEmptyOpportunities = findViewById(R.id.tvEmptyOpportunities);
        tvOpportunitiesCount = findViewById(R.id.tvOpportunitiesCount);
        emptyStateContainer = findViewById(R.id.emptyStateContainer);
        fabAddOpportunity = findViewById(R.id.fabAddOpportunity);

        recyclerViewOpportunities.setLayoutManager(new LinearLayoutManager(this));

        // Initialize empty list and adapter
        adapter = new OpportunitiesAdapter(new ArrayList<>(), new OpportunitiesAdapter.OnOpportunityActionListener() {
            @Override
            public void onOpportunityClick(Opportunity opportunity) {
                Intent intent = new Intent(OpportunitiesActivity.this, OpportunityDetailsActivity.class);
                intent.putExtra(OpportunityDetailsActivity.EXTRA_OPPORTUNITY, opportunity);
                startActivity(intent);
            }

            @Override
            public void onEditClick(Opportunity opportunity) {
                Intent intent = new Intent(OpportunitiesActivity.this, EditOpportunityActivity.class);
                intent.putExtra(EditOpportunityActivity.EXTRA_OPPORTUNITY, opportunity);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Opportunity opportunity) {
                new AlertDialog.Builder(OpportunitiesActivity.this)
                        .setTitle(R.string.dialog_delete_title)
                        .setMessage(R.string.dialog_delete_message)
                        .setPositiveButton(R.string.btn_delete_details, (dialog, which) -> {
                            boolean deleted = databaseHelper.deleteOpportunity(opportunity.getId(), userId);
                            if (deleted) {
                                Toast.makeText(OpportunitiesActivity.this, R.string.msg_delete_success, Toast.LENGTH_SHORT).show();
                                loadOpportunities();
                            } else {
                                Toast.makeText(OpportunitiesActivity.this, R.string.msg_delete_failed, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
            }
        });

        recyclerViewOpportunities.setAdapter(adapter);

        fabAddOpportunity.setOnClickListener(v -> {
            Intent intent = new Intent(OpportunitiesActivity.this, AddOpportunityActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOpportunities();
    }

    private void loadOpportunities() {
        ArrayList<Opportunity> list = databaseHelper.getOpportunitiesForUser(userId);

        int count = (list != null) ? list.size() : 0;
        String countText = getString(R.string.lbl_opportunities_count_plural, count);
        tvOpportunitiesCount.setText(countText);

        if (list == null || list.isEmpty()) {
            recyclerViewOpportunities.setVisibility(View.GONE);
            tvEmptyOpportunities.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        } else {
            recyclerViewOpportunities.setVisibility(View.VISIBLE);
            tvEmptyOpportunities.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.GONE);
            adapter.setOpportunities(list);
        }
    }
}
