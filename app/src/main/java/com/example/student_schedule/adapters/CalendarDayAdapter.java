package com.example.student_schedule.adapters;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.student_schedule.databinding.ItemCalendarDayBinding;
import com.example.student_schedule.models.CalendarDay;

import java.util.Objects;

/**
 * Grid adapter for the month/week calendar cells.
 */
public class CalendarDayAdapter
        extends ListAdapter<CalendarDay, CalendarDayAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    private static final int COLOR_PRIMARY = 0xFF6750A4;
    private static final int COLOR_ON_PRIMARY = 0xFFFFFFFF;

    private static final DiffUtil.ItemCallback<CalendarDay> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CalendarDay>() {
                @Override
                public boolean areItemsTheSame(@NonNull CalendarDay oldItem,
                                               @NonNull CalendarDay newItem) {
                    return Objects.equals(oldItem.getDate(), newItem.getDate());
                }

                @Override
                public boolean areContentsTheSame(@NonNull CalendarDay oldItem,
                                                  @NonNull CalendarDay newItem) {
                    return oldItem.getDayOfMonth() == newItem.getDayOfMonth()
                            && oldItem.isInCurrentMonth() == newItem.isInCurrentMonth()
                            && oldItem.isSelected() == newItem.isSelected()
                            && oldItem.isToday() == newItem.isToday()
                            && oldItem.getSessionCount() == newItem.getSessionCount();
                }
            };

    private final OnDayClickListener listener;

    public CalendarDayAdapter(OnDayClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {

        private final ItemCalendarDayBinding binding;
        private final int defaultTextColor;

        DayViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.defaultTextColor = binding.txtDay.getCurrentTextColor();
        }

        void bind(CalendarDay day, OnDayClickListener listener) {
            View root = binding.getRoot();

            binding.txtDay.setText(String.valueOf(day.getDayOfMonth()));

            if (day.isSelected()) {
                binding.txtDay.setBackground(circle(COLOR_PRIMARY));
                binding.txtDay.setTextColor(COLOR_ON_PRIMARY);
            } else {
                binding.txtDay.setBackground(null);
                if (!day.isInCurrentMonth()) {
                    binding.txtDay.setTextColor(
                            ColorUtils.setAlphaComponent(defaultTextColor, 0x66));
                } else {
                    binding.txtDay.setTextColor(defaultTextColor);
                }
            }
            binding.txtDay.setTypeface(null,
                    day.isToday() || day.isSelected() ? Typeface.BOLD : Typeface.NORMAL);

            if (day.getSessionCount() > 0) {
                binding.txtCount.setVisibility(View.VISIBLE);
                binding.txtCount.setText(String.valueOf(day.getSessionCount()));
                binding.txtCount.setBackground(circle(day.isSelected()
                        ? COLOR_ON_PRIMARY
                        : COLOR_PRIMARY));
                binding.txtCount.setTextColor(day.isSelected()
                        ? COLOR_PRIMARY
                        : COLOR_ON_PRIMARY);
            } else {
                binding.txtCount.setVisibility(View.GONE);
            }

            root.setOnClickListener(view -> listener.onDayClick(day));
        }

        private static GradientDrawable circle(int color) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            return drawable;
        }
    }
}
