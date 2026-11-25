package edu.hitsz.aircraftwar.rank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import edu.hitsz.aircraftwar.R;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public final class RankAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private final Consumer<RankEntry> onDelete;
    private final NumberFormat scoreFormat = NumberFormat.getIntegerInstance();
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT,
            DateFormat.SHORT);
    private List<RankEntry> records = List.of();

    public RankAdapter(Context context, Consumer<RankEntry> onDelete) {
        inflater = LayoutInflater.from(context);
        this.onDelete = onDelete;
    }

    public void replaceRecords(List<RankEntry> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return records.size();
    }

    @Override
    public RankEntry getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.row_rank, parent, false);
            row.setTag(new RowViews(
                    row.findViewById(R.id.row_position), row.findViewById(R.id.row_name),
                    row.findViewById(R.id.row_time), row.findViewById(R.id.row_score),
                    row.findViewById(R.id.row_delete)));
        }

        RowViews views = (RowViews) row.getTag();
        RankEntry entry = getItem(position);
        Context context = row.getContext();
        boolean isFirst = position == 0;
        row.setBackgroundResource(isFirst ? R.drawable.rank_leader : R.drawable.panel);
        views.position().setText(scoreFormat.format(position + 1));
        views.position().setTextColor(context.getColor(isFirst ? R.color.leader : R.color.accent));
        views.name().setText(entry.name());
        views.time().setText(dateFormat.format(new Date(entry.playedAt())));
        views.score().setText(scoreFormat.format(entry.score()));
        views.delete().setContentDescription(context.getString(R.string.delete_rank_accessible,
                entry.name()));
        views.delete().setOnClickListener(view -> onDelete.accept(entry));
        return row;
    }

    private record RowViews(TextView position, TextView name, TextView time, TextView score,
                            Button delete) {
    }
}
