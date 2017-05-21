package app.matolaypal.com.rssreader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Responsible for whole UI.
 * Initialize fields, manage the activity lifecycle, show feeds and let them manageable.
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName() + " <#> ";

    private SwipeRefreshLayout mSwipeLayout;
    private RecyclerView mRecyclerView;
    private EditText mEditText;
    private Button mFetchFeedButton;
    private TextView mFeedTitleTextView;
    private TextView mFeedLinkTextView;
    private TextView mFeedDescriptionTextView;

    private RssFeedListAdapter listAdapter;
    private List<RssFeedModel> mFeedModelList;
    private String mFeedTitle;
    private String mFeedLink;
    private String mFeedDescription;

    /**
     * Create activity, find views, set click listener for button and refresh listener for layout.
     * {@link ItemTouchHelper} allow that the feeds is sortable and deletable by touch.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mEditText = (EditText) findViewById(R.id.rssFeedEditText);
        mFetchFeedButton = (Button) findViewById(R.id.fetchFeedButton);
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mFeedTitleTextView = (TextView) findViewById(R.id.feedTitle);
        mFeedDescriptionTextView = (TextView) findViewById(R.id.feedDescription);
        mFeedLinkTextView = (TextView) findViewById(R.id.feedLink);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mFetchFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FetchFeedTask().execute((Void) null);
            }
        });
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchFeedTask().execute((Void) null);
            }
        });

        // Create a new ItemTouchHelper and pass every motion as parameter, what is necessary.
        ItemTouchHelper.SimpleCallback simpleCallbackItemTouchHelper =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            // Responsible for reordering, when the user "grab" a feed and move up or down.
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                final int fromPosition = viewHolder.getAdapterPosition();
                final int toPosition = target.getAdapterPosition();
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(mFeedModelList, i, i + 1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(mFeedModelList, i, i - 1);
                    }
                }
                listAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }
            // Responsible for deleting when the user move feed left or right.
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                mFeedModelList.remove(position);
                listAdapter.notifyDataSetChanged();
            }
        };
        // Attach ItemTouchHelper object to RecyclerView object
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallbackItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * {@link FacebookController} call this method when finished with sharing.
     * {@link FacebookController#getCallbackManager()} give back the current
     * {@link com.facebook.CallbackManager} object.
     * Notify user that everything is awesome!
     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);
        FacebookController.getCallbackManager().onActivityResult(requestCode, responseCode, data);
        Toast.makeText(this, "Sharing completed!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Responsible for AsyncTask, what get data from internet (based on feed link).
     */
    private class FetchFeedTask extends AsyncTask<Void, Void, Boolean> {

        private String urlLink;

        /**
         * Initialize necessary variables.
         */
        @Override
        protected void onPreExecute() {
            mSwipeLayout.setRefreshing(true);
            mFeedTitle = null;
            mFeedLink = null;
            mFeedDescription = null;
            mFeedTitleTextView.setText("Feed Title: " + mFeedTitle);
            mFeedDescriptionTextView.setText("Feed Description: " + mFeedDescription);
            mFeedLinkTextView.setText("Feed Link: " + mFeedLink);
            urlLink = mEditText.getText().toString();
        }

        /**
         * Check the given url link. (Correct it, if it is necessary.)
         * Get {@link InputStream} object and passed to {@link #parseFeed(InputStream)} as argument.
         * Fill {@link #mFeedModelList} with the acquired data.
         * Catch exceptions, if something went wrong (and log it).
         * @param voids argument
         * @return Boolean about result
         */
        @Override
        protected Boolean doInBackground(Void... voids) {
            if (isEmpty(urlLink))
                return false;

            try {
                if(!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (XmlPullParserException | IOException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        /**
         * Refresh the {@link #mSwipeLayout}.
         * If success: Update the feed views, create new {@link RssFeedListAdapter} and
         * passed then context and {@link #mFeedModelList} as argument.
         * Otherwise check the network availability and notify user what went wrong.
         * @param success Boolean argument
         */
        @Override
        protected void onPostExecute(Boolean success) {
            mSwipeLayout.setRefreshing(false);
            if (success) {
                mFeedTitleTextView.setText("Feed Title: " + mFeedTitle);
                mFeedDescriptionTextView.setText("Feed Description: " + mFeedDescription);
                mFeedLinkTextView.setText("Feed Link: " + mFeedLink);
                listAdapter = new RssFeedListAdapter(MainActivity.this, mFeedModelList);
                mRecyclerView.setAdapter(listAdapter);
            } else {
                if (!isNetworkAvailable()) {
                    Toast.makeText(MainActivity.this,
                            "Internet is not available!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Enter a valid Rss feed url!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    /**
     * Responsible for XML "unpacking", create {@link RssFeedModel} from them and fill a List.
     * @param inputStream object
     * @return a list with {@link RssFeedModel}
     * @throws XmlPullParserException if parser get an error
     * @throws IOException if failed or interrupted I/O operations
     * (Every case close the inputSteam.)
     */
    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if(name == null)
                    continue;

                if(eventType == XmlPullParser.END_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if(name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                Log.d(TAG, "Parsing name: " + name);
                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title")) {
                    title = result;
                } else if (name.equalsIgnoreCase("link")) {
                    link = result;
                } else if (name.equalsIgnoreCase("description")) {
                    description = result;
                }

                if (title != null && link != null && description != null) {
                    if(isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, description);
                        items.add(item);
                    }
                    else {
                        mFeedTitle = title;
                        mFeedLink = link;
                        mFeedDescription = description;
                    }

                    title = null;
                    link = null;
                    description = null;
                    isItem = false;
                }
            }

            return items;
        } finally {
            inputStream.close();
        }
    }

    /**
     * Check the network connection with the help of the system.
     * @return Boolean about result
     */
    private Boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
