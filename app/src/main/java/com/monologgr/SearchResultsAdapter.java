package com.monologgr;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchResultsAdapter extends BaseAdapter {
    private static final String TAG = "SearchResultsAdapter";

    private List<SearchResult> results;

    private LayoutInflater layoutInflater;

    private AsyncTask<String, Void, List<SearchResult>> asyncTask;

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

        return view;
    }

    class SearchResult {
        String beforeText;
        String text;
        String afterText;
        String fileName;
        File   file;

        public SearchResult(String beforeText, String text, String afterText, String fileName) {
            this.beforeText = beforeText;
            this.text = text;
            this.afterText = afterText;
            this.fileName = fileName;
//            file.
        }
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
}
