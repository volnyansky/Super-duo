package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.scoresAdapter;

/**
 * Created by Stas on 22.11.15.
 */
public class WidgetViewFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private int appWidgetId;
    private Cursor cursor;
    private ArrayList<Match> data = new ArrayList<>();

    public WidgetViewFactory(int appWidgetId, Context context) {
        this.appWidgetId = appWidgetId;
        this.context = context;
    }

    @Override
    public void onCreate() {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date fragmentdate = cal.getTime();

        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");

        cursor = context.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, new String[]{mformat.format(fragmentdate)}, null);

        saveCursor(context.getString(R.string.yesterday), cursor);
        cursor.close();

        cal = Calendar.getInstance();
        fragmentdate = cal.getTime();
        cursor = context.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, new String[]{mformat.format(fragmentdate)}, null);
        saveCursor(context.getString(R.string.today), cursor);

        cursor.close();

        cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        fragmentdate = cal.getTime();
        cursor = context.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, new String[]{mformat.format(fragmentdate)}, null);
        saveCursor(context.getString(R.string.tomorrow), cursor);

        cursor.close();
    }

    private void saveCursor(String title, Cursor cursor) {
        Match m = new Match();
        m.setTitle(title);
        data.add(m);
        while (cursor.moveToNext()) {

            m = new Match();
            m.setHome(cursor.getString(scoresAdapter.COL_HOME));
            m.setHomeCrest(Utilies.getTeamCrestByTeamName(
                    cursor.getString(scoresAdapter.COL_HOME)));

            m.setAway(cursor.getString(scoresAdapter.COL_AWAY));
            m.setAwayCrest(Utilies.getTeamCrestByTeamName(
                    cursor.getString(scoresAdapter.COL_AWAY)));
            m.setScore(
                    Utilies.getScores(cursor.getInt(scoresAdapter.COL_HOME_GOALS), cursor.getInt(scoresAdapter.COL_AWAY_GOALS))
            );
            m.setMacthTime(cursor.getString(scoresAdapter.COL_MATCHTIME));
            data.add(m);
        }
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row;
        Match match=data.get(position);
        if (match.getTitle() != null) {
            row = new RemoteViews(context.getPackageName(),
                    R.layout.widget_scores_section);
            row.setTextViewText(R.id.title,match.getTitle());
        } else {
            row = new RemoteViews(context.getPackageName(),
                    R.layout.widget_scores_list_item);

            row.setTextViewText(R.id.home_name, match.getHome());
            row.setTextViewText(R.id.away_name, match.getAway());

            row.setImageViewResource(R.id.home_crest, match.getHomeCrest());

            row.setImageViewResource(R.id.away_crest, match.getAwayCrest());

            row.setTextViewText(R.id.data_textview, match.getMacthTime());
            row.setTextViewText(R.id.score_textview, match.getScore());
        }
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("ROW_NUMBER", position);
        row.setOnClickFillInIntent(R.id.row, fillInIntent);

        ;
        return row;


    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }


    private class Match {
        String title;
        String home;
        String away;
        int homeCrest;
        int awayCrest;
        String macthTime;
        String score;

        public String getAway() {
            return away;
        }

        public void setAway(String away) {
            this.away = away;
        }

        public int getAwayCrest() {
            return awayCrest;
        }

        public void setAwayCrest(int away_crest) {
            this.awayCrest = away_crest;
        }

        public String getHome() {
            return home;
        }

        public void setHome(String home) {
            this.home = home;
        }

        public int getHomeCrest() {
            return homeCrest;
        }

        public void setHomeCrest(int home_crest) {
            this.homeCrest = home_crest;
        }

        public String getMacthTime() {
            return macthTime;
        }

        public void setMacthTime(String macthTime) {
            this.macthTime = macthTime;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

}
