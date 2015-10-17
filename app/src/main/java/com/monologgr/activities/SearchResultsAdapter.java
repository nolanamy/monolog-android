package com.monologgr.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.monologgr.R;
import com.monologgr.network.UploaderClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class SearchResultsAdapter extends BaseAdapter {
    private static final String TAG = "SearchResultsAdapter";

    private List<SearchResult> results;

    private LayoutInflater layoutInflater;

    private AsyncTask<String, Void, List<SearchResult>> asyncTask;

    private String playingFileName = "";

    public SearchResultsAdapter(Context activityContext) {
        super();
        layoutInflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return results == null ? 0 : results.size();
    }

    @Override
    public SearchResult getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_item_search, parent, false);
        }

        SearchResult searchResult = getItem(position);

        TextView textView = (TextView)view.findViewById(R.id.search_list_item_text_view);
        textView.setText(searchResult.text);

        TextView dateView = (TextView)view.findViewById(R.id.search_list_item_date_text_view);
        dateView.setText(prettyDate(searchResult.timestamp));

        view.setBackgroundColor(Color.TRANSPARENT);
        if (searchResult.fileName.equals(playingFileName)) {
            view.setBackgroundColor(Color.argb(255, 0, 176, 240));
        }

        return view;
    }

    class SearchResult {
        String beforeText;
        String text;
        String afterText;
        String fileName;
        Date   timestamp;

        public SearchResult(String beforeText, String text, String afterText, String fileName) {
            this.beforeText = beforeText;
            this.text = text;
            this.afterText = afterText;
            this.fileName = fileName;
            String timeLongString = fileName.substring(0, fileName.length() - 4);
            timestamp = new Date(Long.parseLong(timeLongString));
        }
    }

    public String getPlayingFileName() {
        return playingFileName;
    }

    public void setPlayingFileName(String fileName) {
        playingFileName = fileName;
        notifyDataSetChanged();
    }

    public void search(String search) {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        asyncTask = new AsyncTask<String, Void, List<SearchResult>>() {
            @Override
            protected List<SearchResult> doInBackground(String... params) {
                String string = UploaderClient.getInstance().search(params[0]);

                if (string.length() == 0) {
                    return null;
                }

                try {
                    JSONObject jsonObject = new JSONObject(string);
                    JSONArray jsonArray = jsonObject.getJSONArray("results");

                    ArrayList<SearchResult> list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject resultJSONObject = jsonArray.getJSONObject(i);
                        list.add(new SearchResult(resultJSONObject.getString("beforeText"), resultJSONObject.getString("text"), resultJSONObject.getString("afterText"), resultJSONObject.getString("filename")));
                    }

                    return list;
                } catch (JSONException e) {
                    // Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<SearchResult> results) {
                if (results != null) {
                    SearchResultsAdapter.this.results = results;
                    SearchResultsAdapter.this.notifyDataSetChanged();
                }
            }
        };
        asyncTask.execute(search);
    }

    private static String getDayDiffString(Date now, Date before)
    {
        if (before == null || now == null || before.after(now))
        {
            // TODO handle future dates
            return "ERROR: before=" + before;
        }
        else
        {
            String time = new SimpleDateFormat("h:mm aa", Locale.US).format(before);

            if (now.getTime() - before.getTime() < 1000 * 60 * 60 * 24 * 5)
            {
                Calendar nowCalendar = new GregorianCalendar();
                nowCalendar.setTime(now);
                Calendar beforeCalendar = new GregorianCalendar();
                beforeCalendar.setTime(before);

                int dayDiff = Math.abs(nowCalendar.get(Calendar.DAY_OF_WEEK) - beforeCalendar.get(Calendar.DAY_OF_WEEK));
                if (dayDiff == 0)
                {
                    return "Today " + time;
                }
                else if (dayDiff == 1)
                {
                    return "Yesterday " + time;
                }
                else
                {
                    return beforeCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + " " + time;
                }
            }
            else
            {
                return new SimpleDateFormat("MM/dd/yy", Locale.US).format(before) + " " + time;
            }
        }
    };

    public static String prettyDate(Date date)
    {
        if (date == null)
        {
            return "";
        }
        Date now = new Date();

        return getDayDiffString(now, date);
    }
}
