package app.matolaypal.com.rssreader;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Responsible for list view with feeds.
 */
class RssFeedListAdapter
        extends RecyclerView.Adapter<RssFeedListAdapter.FeedModelViewHolder> {

    private List<RssFeedModel> mRssFeedModels;
    private Context context;

    static class FeedModelViewHolder extends RecyclerView.ViewHolder {
        private View rssFeedView;

        FeedModelViewHolder(View v) {
            super(v);
            rssFeedView = v;
        }
    }

    /**
     * Adapter needed:
     * @param context from {@link MainActivity}
     * @param rssFeedModels as list with {@link RssFeedModel}
     */
    RssFeedListAdapter(Context context, List<RssFeedModel> rssFeedModels) {
        this.context = context;
        mRssFeedModels = rssFeedModels;
    }

    /**
     * Extend UI with item_rss_feed layout and create new holder.
     */
    @Override
    public FeedModelViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rss_feed, parent, false);
        return new FeedModelViewHolder(view);
    }

    /**
     * Update every item (view) with content and own popup menu.
     * Every item deletable or sharable (with {@link FacebookController}).
     * @param holder as FeedModelViewHolder
     * @param position as int
     */
    @Override
    public void onBindViewHolder(final FeedModelViewHolder holder, int position) {
        final RssFeedModel rssFeedModel = mRssFeedModels.get(position);
        ((TextView)holder.rssFeedView.findViewById(R.id.titleText))
                .setText(rssFeedModel.title);
        ((TextView)holder.rssFeedView.findViewById(R.id.descriptionText))
                .setText(rssFeedModel.description);
        ((TextView)holder.rssFeedView.findViewById(R.id.linkText))
                .setText(rssFeedModel.link);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(context, view);
                popup.inflate(R.menu.popup_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_share:
                                FacebookController fbc = new FacebookController(context);
                                fbc.share(rssFeedModel);
                                break;
                            case R.id.item_delete:
                                mRssFeedModels.remove(holder.getAdapterPosition());
                                notifyItemRemoved(holder.getAdapterPosition());
                                notifyItemRangeChanged(holder.getAdapterPosition(),
                                        mRssFeedModels.size());
                                break;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    /**
     * @return {@link #mRssFeedModels} size
     */
    @Override
    public int getItemCount() {
        return mRssFeedModels.size();
    }


}
