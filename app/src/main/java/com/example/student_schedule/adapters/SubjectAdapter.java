package com.example.student_schedule.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_schedule.R;
import com.example.student_schedule.databinding.ItemSubjectBinding;
import com.example.student_schedule.models.Subject;

import java.util.Objects;

/**
 * List adapter for the subject management screen.
 */
public class SubjectAdapter extends ListAdapter<Subject, SubjectAdapter.SubjectViewHolder> {

    public interface OnSubjectActionListener {
        void onEdit(Subject subject);

        void onDelete(Subject subject);
    }

    private static final DiffUtil.ItemCallback<Subject> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Subject>() {
                @Override
                public boolean areItemsTheSame(@NonNull Subject oldItem, @NonNull Subject newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Subject oldItem, @NonNull Subject newItem) {
                    return oldItem.getCredits() == newItem.getCredits()
                            && Objects.equals(oldItem.getSubjectCode(), newItem.getSubjectCode())
                            && Objects.equals(oldItem.getSubjectName(), newItem.getSubjectName())
                            && Objects.equals(oldItem.getDescription(), newItem.getDescription());
                }
            };

    private final OnSubjectActionListener listener;

    public SubjectAdapter(OnSubjectActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubjectBinding binding = ItemSubjectBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new SubjectViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {

        private final ItemSubjectBinding binding;

        SubjectViewHolder(ItemSubjectBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Subject subject, OnSubjectActionListener listener) {
            binding.txtSubjectCode.setText(subject.getSubjectCode());
            binding.txtSubjectName.setText(subject.getSubjectName());
            binding.txtCredits.setText(binding.getRoot().getContext()
                    .getString(R.string.credits_label, subject.getCredits()));

            String description = subject.getDescription() == null
                    ? "" : subject.getDescription().trim();
            binding.txtDescription.setText(description);
            binding.txtDescription.setVisibility(description.isEmpty() ? View.GONE : View.VISIBLE);

            binding.btnEdit.setOnClickListener(view -> listener.onEdit(subject));
            binding.btnDelete.setOnClickListener(view -> listener.onDelete(subject));
        }
    }
}
