package com.example.student_schedule.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_schedule.R;
import com.example.student_schedule.databinding.ItemScheduleBinding;
import com.example.student_schedule.models.StudySchedule;
import com.example.student_schedule.utils.DateTimeUtils;
import com.example.student_schedule.utils.StatusUtils;

import java.util.Objects;

/**
 * List adapter shared by the schedule list and the calendar day list.
 */
public class ScheduleAdapter
        extends ListAdapter<StudySchedule, ScheduleAdapter.ScheduleViewHolder> {

    public interface OnScheduleActionListener {
        void onEdit(StudySchedule schedule);

        void onDelete(StudySchedule schedule);

        void onStatusClick(StudySchedule schedule);
    }

    private static final int COLOR_PENDING_BACKGROUND = 0xFFFFE0B2;
    private static final int COLOR_PENDING_TEXT = 0xFF7A4A00;
    private static final int COLOR_CONFIRMED_BACKGROUND = 0xFFC8E6C9;
    private static final int COLOR_CONFIRMED_TEXT = 0xFF1B5E20;
    private static final int COLOR_CANCELLED_BACKGROUND = 0xFFE0E0E0;
    private static final int COLOR_CANCELLED_TEXT = 0xFFB3261E;

    private static final DiffUtil.ItemCallback<StudySchedule> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<StudySchedule>() {
                @Override
                public boolean areItemsTheSame(@NonNull StudySchedule oldItem,
                                               @NonNull StudySchedule newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull StudySchedule oldItem,
                                                  @NonNull StudySchedule newItem) {
                    return oldItem.getStudentId() == newItem.getStudentId()
                            && oldItem.getSubjectId() == newItem.getSubjectId()
                            && Objects.equals(oldItem.getSubjectName(), newItem.getSubjectName())
                            && Objects.equals(oldItem.getStudyDate(), newItem.getStudyDate())
                            && Objects.equals(oldItem.getStartTime(), newItem.getStartTime())
                            && Objects.equals(oldItem.getEndTime(), newItem.getEndTime())
                            && Objects.equals(oldItem.getLocation(), newItem.getLocation())
                            && Objects.equals(oldItem.getStatus(), newItem.getStatus());
                }
            };

    private final OnScheduleActionListener listener;

    public ScheduleAdapter(OnScheduleActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemScheduleBinding binding = ItemScheduleBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ScheduleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        private final ItemScheduleBinding binding;

        ScheduleViewHolder(ItemScheduleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(StudySchedule schedule, OnScheduleActionListener listener) {
            Context context = binding.getRoot().getContext();

            String subjectName = schedule.getSubjectName();
            if (subjectName == null || subjectName.trim().isEmpty()) {
                subjectName = context.getString(R.string.subject_fallback,
                        schedule.getSubjectId());
            }
            binding.txtSubject.setText(subjectName);

            binding.txtDate.setText(DateTimeUtils.formatLocalDate(
                    schedule.getStudyDate(), DateTimeUtils.PATTERN_DISPLAY_DATE));
            binding.txtTime.setText(context.getString(R.string.schedule_time_range,
                    safe(schedule.getStartTime()), safe(schedule.getEndTime())));

            String location = safe(schedule.getLocation());
            binding.txtLocation.setText(location);
            binding.txtLocation.setVisibility(location.isEmpty() ? View.GONE : View.VISIBLE);

            bindStatusChip(schedule.getStatus());

            binding.chipStatus.setOnClickListener(view -> listener.onStatusClick(schedule));
            binding.btnEdit.setOnClickListener(view -> listener.onEdit(schedule));
            binding.btnDelete.setOnClickListener(view -> listener.onDelete(schedule));
        }

        private void bindStatusChip(String status) {
            String normalized = status == null ? StudySchedule.STATUS_PENDING : status;
            int background;
            int textColor;
            if (StudySchedule.STATUS_CONFIRMED.equalsIgnoreCase(normalized)) {
                background = COLOR_CONFIRMED_BACKGROUND;
                textColor = COLOR_CONFIRMED_TEXT;
            } else if (StudySchedule.STATUS_CANCELLED.equalsIgnoreCase(normalized)) {
                background = COLOR_CANCELLED_BACKGROUND;
                textColor = COLOR_CANCELLED_TEXT;
            } else {
                background = COLOR_PENDING_BACKGROUND;
                textColor = COLOR_PENDING_TEXT;
            }
            binding.chipStatus.setText(StatusUtils.label(binding.getRoot().getContext(),
                    normalized));
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(background));
            binding.chipStatus.setTextColor(textColor);
        }

        private static String safe(String value) {
            return value == null ? "" : value;
        }
    }
}
