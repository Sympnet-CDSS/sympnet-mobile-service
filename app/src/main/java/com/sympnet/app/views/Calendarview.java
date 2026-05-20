package com.sympnet.app.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sympnet.app.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class Calendarview extends LinearLayout {

    public interface OnDaySelectedListener {
        /** @param dateMillis midnight (UTC) of the selected day */
        void onDaySelected(long dateMillis);
    }

    // State
    private final Calendar displayedMonth = Calendar.getInstance();
    private final Calendar today          = Calendar.getInstance();
    private long selectedDateMillis = -1;

    // Views
    private TextView   tvMonth;
    private GridLayout grid;

    // Callback
    private OnDaySelectedListener listener;


    public Calendarview(Context ctx) { this(ctx, null); }
    public Calendarview(Context ctx, @Nullable AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx);
    }

    public void setOnDaySelectedListener(OnDaySelectedListener l) {
        this.listener = l;
    }

    // Build skeleton layout 
    private void init(Context ctx) {
        setOrientation(VERTICAL);
        setPadding(0, 8, 0, 8);

        //  Header row: < MONTH YEAR > 
        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView btnPrev = makeNavButton(ctx, "‹");
        tvMonth          = new TextView(ctx);
        TextView btnNext = makeNavButton(ctx, "›");

        tvMonth.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        tvMonth.setGravity(Gravity.CENTER);
        tvMonth.setTextSize(16f);
        tvMonth.setTextColor(Color.parseColor("#009688"));
        tvMonth.setTypeface(null, android.graphics.Typeface.BOLD);

        header.addView(btnPrev);
        header.addView(tvMonth);
        header.addView(btnNext);
        addView(header);

        btnPrev.setOnClickListener(v -> shiftMonth(-1));
        btnNext.setOnClickListener(v -> shiftMonth(+1));

        //  Weekday labels row 
        String[] days = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        LinearLayout dayRow = new LinearLayout(ctx);
        dayRow.setOrientation(HORIZONTAL);
        for (String d : days) {
            TextView tv = new TextView(ctx);
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            tv.setGravity(Gravity.CENTER);
            tv.setText(d);
            tv.setTextSize(12f);
            tv.setTextColor(Color.parseColor("#00796B"));
            tv.setPadding(0, 8, 0, 4);
            dayRow.addView(tv);
        }
        addView(dayRow);

        //  Day grid 
        grid = new GridLayout(ctx);
        grid.setColumnCount(7);
        grid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(grid);

        renderMonth();
    }

    //  Render the current displayed month 
    private void renderMonth() {
        Context ctx = getContext();
        grid.removeAllViews();

        // Header label
        SimpleDateFormat fmt = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonth.setText(fmt.format(displayedMonth.getTime()));

        // Clone to find 1st of month
        Calendar cal = (Calendar) displayedMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        // Monday-based offset (Mon=0 --> Sun=6)
        int firstDow = cal.get(Calendar.DAY_OF_WEEK); 
        int offset   = (firstDow == Calendar.SUNDAY) ? 6 : (firstDow - Calendar.MONDAY);

        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Blank cells before day 1
        for (int i = 0; i < offset; i++) addEmptyCell();

        for (int day = 1; day <= daysInMonth; day++) {
            cal.set(Calendar.DAY_OF_MONTH, day);
            long cellMillis = cal.getTimeInMillis();

            boolean isPast     = isBefore(cal, today);
            boolean isToday    = isSameDay(cal, today);
            boolean isSelected = (selectedDateMillis != -1)
                    && isSameDay(cal, millisToCalendar(selectedDateMillis));

            TextView tv = new TextView(ctx);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                    GridLayout.spec(GridLayout.UNDEFINED, 1f),
                    GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width  = 0;
            lp.height = dpToPx(40);
            tv.setLayoutParams(lp);
            tv.setGravity(Gravity.CENTER);
            tv.setText(String.valueOf(day));
            tv.setTextSize(13f);

            if (isSelected) {
                tv.setBackgroundResource(R.drawable.circle_background);
                tv.getBackground().setTint(Color.parseColor("#009688"));
                tv.setTextColor(Color.WHITE);
            } else if (isToday) {
                tv.setBackgroundResource(R.drawable.circle_background);
                tv.getBackground().setTint(Color.parseColor("#B2DFDB"));
                tv.setTextColor(Color.parseColor("#00796B"));
            } else if (isPast) {
                tv.setTextColor(Color.parseColor("#BDBDBD"));
                tv.setAlpha(0.5f);
            } else {
                tv.setTextColor(Color.parseColor("#212121"));
            }

            if (!isPast) {
                final long ms = cellMillis;
                tv.setOnClickListener(v -> {
                    selectedDateMillis = ms;
                    renderMonth(); // redraw to reflect selection
                    if (listener != null) listener.onDaySelected(ms);
                });
            }

            grid.addView(tv);
        }
    }

    //  Helpers
    private void shiftMonth(int delta) {
        displayedMonth.add(Calendar.MONTH, delta);
        if (displayedMonth.before(todayMonthStart())) {
            displayedMonth.add(Calendar.MONTH, -delta);
            return;
        }
        renderMonth();
    }

    private void addEmptyCell() {
        View v = new View(getContext());
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width  = 0;
        lp.height = dpToPx(40);
        v.setLayoutParams(lp);
        grid.addView(v);
    }

    private Calendar todayMonthStart() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private boolean isBefore(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR)  < b.get(Calendar.YEAR)  ||
                (a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
                        a.get(Calendar.DAY_OF_YEAR) < b.get(Calendar.DAY_OF_YEAR));
    }

    private boolean isSameDay(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR)       == b.get(Calendar.YEAR) &&
                a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    private Calendar millisToCalendar(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        return c;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private TextView makeNavButton(Context ctx, String label) {
        TextView btn = new TextView(ctx);
        btn.setText(label);
        btn.setBackgroundColor(Color.TRANSPARENT);
        btn.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
        btn.setTextSize(20f);
        btn.setTextColor(Color.parseColor("#009688"));
        btn.setFocusable(true);
        btn.setClickable(true);
        return btn;
    }
}
