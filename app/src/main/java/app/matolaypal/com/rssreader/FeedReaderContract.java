package app.matolaypal.com.rssreader;

import android.provider.BaseColumns;

class FeedReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FeedReaderContract() {}

    /* Inner class that defines the table contents */
    static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "entry";
        static final String COLUMN_NAME_TITLE = "title";
    }
}
