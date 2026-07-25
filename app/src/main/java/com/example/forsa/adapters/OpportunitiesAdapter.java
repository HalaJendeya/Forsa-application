package com.example.forsa.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.forsa.R;
import com.example.forsa.models.Opportunity;

import java.util.ArrayList;

public class OpportunitiesAdapter extends RecyclerView.Adapter<OpportunitiesAdapter.OpportunityViewHolder> {

    public interface OnOpportunityActionListener {
        void onOpportunityClick(Opportunity opportunity);
        void onEditClick(Opportunity opportunity);
        void onDeleteClick(Opportunity opportunity);
    }

    private ArrayList<Opportunity> opportunities;
    private final OnOpportunityActionListener listener;

    public OpportunitiesAdapter(ArrayList<Opportunity> opportunities, OnOpportunityActionListener listener) {
        this.opportunities = opportunities;
        this.listener = listener;
    }

    public void setOpportunities(ArrayList<Opportunity> opportunities) {
        this.opportunities = opportunities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OpportunityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_opportunity, parent, false);
        return new OpportunityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OpportunityViewHolder holder, int position) {
        Opportunity opportunity = opportunities.get(position);
        holder.bind(opportunity, listener);
    }

    @Override
    public int getItemCount() {
        return opportunities != null ? opportunities.size() : 0;
    }

    static class OpportunityViewHolder extends RecyclerView.ViewHolder {

        private final View opportunityItemRoot;
        private final TextView tvOpportunityTitle;
        private final TextView tvOpportunityOrganization;
        private final TextView tvOpportunityCategory;
        private final TextView tvOpportunityDeadline;
        private final TextView tvOpportunityStatus;
        private final TextView tvOpportunityPriority;
        private final Button btnEditOpportunity;
        private final Button btnDeleteOpportunity;

        public OpportunityViewHolder(@NonNull View itemView) {
            super(itemView);
            opportunityItemRoot = itemView.findViewById(R.id.opportunityItemRoot);
            tvOpportunityTitle = itemView.findViewById(R.id.tvOpportunityTitle);
            tvOpportunityOrganization = itemView.findViewById(R.id.tvOpportunityOrganization);
            tvOpportunityCategory = itemView.findViewById(R.id.tvOpportunityCategory);
            tvOpportunityDeadline = itemView.findViewById(R.id.tvOpportunityDeadline);
            tvOpportunityStatus = itemView.findViewById(R.id.tvOpportunityStatus);
            tvOpportunityPriority = itemView.findViewById(R.id.tvOpportunityPriority);
            btnEditOpportunity = itemView.findViewById(R.id.btnEditOpportunity);
            btnDeleteOpportunity = itemView.findViewById(R.id.btnDeleteOpportunity);
        }

        public void bind(final Opportunity opportunity, final OnOpportunityActionListener listener) {
            tvOpportunityTitle.setText(opportunity.getTitle());
            tvOpportunityOrganization.setText(opportunity.getOrganization());
            tvOpportunityCategory.setText(opportunity.getCategory());
            tvOpportunityDeadline.setText(opportunity.getDeadline());
            tvOpportunityStatus.setText(opportunity.getStatus());
            tvOpportunityPriority.setText(opportunity.getPriority());

            applyStatusStyle(tvOpportunityStatus, opportunity.getStatus());
            applyPriorityStyle(tvOpportunityPriority, opportunity.getPriority());

            if (listener != null) {
                opportunityItemRoot.setOnClickListener(v -> listener.onOpportunityClick(opportunity));
                btnEditOpportunity.setOnClickListener(v -> listener.onEditClick(opportunity));
                btnDeleteOpportunity.setOnClickListener(v -> listener.onDeleteClick(opportunity));
            }
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
}
