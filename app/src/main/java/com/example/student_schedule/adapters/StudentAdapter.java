package com.example.student_schedule.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_schedule.databinding.ItemStudentBinding;
import com.example.student_schedule.models.Student;

import java.util.Objects;

/**
 * List adapter for the student management screen.
 */
public class StudentAdapter extends ListAdapter<Student, StudentAdapter.StudentViewHolder> {

    public interface OnStudentActionListener {
        void onEdit(Student student);

        void onDelete(Student student);
    }

    private static final DiffUtil.ItemCallback<Student> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Student>() {
                @Override
                public boolean areItemsTheSame(@NonNull Student oldItem, @NonNull Student newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Student oldItem, @NonNull Student newItem) {
                    return oldItem.getAccountId() == newItem.getAccountId()
                            && Objects.equals(oldItem.getStudentCode(), newItem.getStudentCode())
                            && Objects.equals(oldItem.getFullname(), newItem.getFullname())
                            && Objects.equals(oldItem.getEmail(), newItem.getEmail())
                            && Objects.equals(oldItem.getPhone(), newItem.getPhone());
                }
            };

    private final OnStudentActionListener listener;

    public StudentAdapter(OnStudentActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStudentBinding binding = ItemStudentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {

        private final ItemStudentBinding binding;

        StudentViewHolder(ItemStudentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Student student, OnStudentActionListener listener) {
            binding.txtStudentCode.setText(student.getStudentCode());
            binding.txtFullname.setText(student.getFullname());

            String email = student.getEmail() == null ? "" : student.getEmail().trim();
            String phone = student.getPhone() == null ? "" : student.getPhone().trim();
            String contact;
            if (!email.isEmpty() && !phone.isEmpty()) {
                contact = email + " · " + phone;
            } else if (!email.isEmpty()) {
                contact = email;
            } else {
                contact = phone;
            }
            binding.txtContact.setText(contact);
            binding.txtContact.setVisibility(contact.isEmpty() ? View.GONE : View.VISIBLE);

            binding.btnEdit.setOnClickListener(view -> listener.onEdit(student));
            binding.btnDelete.setOnClickListener(view -> listener.onDelete(student));
        }
    }
}
