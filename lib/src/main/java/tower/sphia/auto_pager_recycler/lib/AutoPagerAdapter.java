package tower.sphia.auto_pager_recycler.lib;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An adapter for a {@link RecyclerView} which enables and helps handle special item views including foot views
 * the ending view and load-more indicators.
 */
public abstract class AutoPagerAdapter<P extends Page<E>, E> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int TYPE_ITEM = 1;
    protected static final int TYPE_FOOTER = 2;

    protected static final int TYPE_PLACEHOLDER_UP = 3;
    protected static final int TYPE_END = 4;
    private static final String TAG = "AutoPagerAdapter";
    private List<E> mItems = new ArrayList<>();
    private Map<Integer, Integer> mLoadMorePositionPageMap = new TreeMap<>();
    private int mFooterRes;
    private int mEnderRes;
    private int mLoaderRes;
    private boolean mInLastPage = false;

    public AdapterCallbacks getCallbacks() {
        return mCallbacks;
    }

    public void setCallbacks(AdapterCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private AdapterCallbacks mCallbacks;

    public AutoPagerAdapter(AdapterCallbacks callbacks) {
        this();
        mCallbacks = callbacks;
    }

    public AutoPagerAdapter() {
        mFooterRes = R.layout.item_footer;
        mEnderRes = R.layout.item_end;
        mLoaderRes = R.layout.item_load_more;
    }

    /**
     * The callback listeners for the special items
     */
    public interface AdapterCallbacks {
        void onClickFooter(View view);

        void onClickLoadMore(View view);

        void onClickEnding(View view);
    }


    public Map<Integer, Integer> getLoadMorePositionPageMap() {
        return mLoadMorePositionPageMap;
    }

    public void setFooterRes(int footerRes) {
        mFooterRes = footerRes;
    }

    public void setEnderRes(int enderRes) {
        mEnderRes = enderRes;
    }

    public void setLoaderRes(int loaderRes) {
        mLoaderRes = loaderRes;
    }

    /**
     * Converting the container of pages to a container of elements/items
     *
     * @param pages the data of all pages that have been loaded
     */
    public void setItems(TreeMap<Integer, P> pages) {
        if (AutoPagerManager.DEBUG) Log.d(TAG, "setItems() called with " + "pages.size() = [" + pages.size() + "]");
        mItems.clear();
        mLoadMorePositionPageMap.clear();
        int lastIndex = -1;
        for (Map.Entry<Integer, P> entry : pages.entrySet()) {
            Page<E> page = entry.getValue();
            int index = page.index();

            if (lastIndex != -1) {
                if (index != lastIndex + 1) {
                    mLoadMorePositionPageMap.put(mItems.size(), index - 1);
                }
            }
            lastIndex = index;
            for (E e : page) {
                mItems.add(e);
            }
        }
        if (AutoPagerManager.DEBUG) Log.i(TAG, "setItems " + mItems.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int itemCount = getItemCount();

        if (position == itemCount - 1) {
            if (mItems.size() != 0 && mInLastPage) {
                return TYPE_END;
            } else {
                return TYPE_FOOTER;
            }
        } else if (mLoadMorePositionPageMap.containsKey(position)) {
            return TYPE_PLACEHOLDER_UP;
        } else {
            return TYPE_ITEM;
        }
    }

    protected abstract RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int viewType);

    private E getItem(int i) {
        return mItems.get(i);
    }

    /**
     * @param i the position of item in {@link RecyclerView}
     * @return the index of data, which eliminates special items
     */
    protected E getItemWithOffset(int i) {
        return mItems.get(i - getOffset(i));
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (AutoPagerManager.DEBUG) Log.i("LoadMore", "RecyclerWithFooterAdapter.onCreateViewHolder");
        switch (viewType) {
            case TYPE_ITEM:
                return onCreateItemViewHolder(viewGroup, viewType);
            case TYPE_FOOTER:
                View footer = LayoutInflater.from(viewGroup.getContext()).inflate(mFooterRes, viewGroup, false);
                footer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallbacks.onClickFooter(v);
                    }
                });
                return new FooterViewHolder(footer);
            case TYPE_END:
                View ending = LayoutInflater.from(viewGroup.getContext()).inflate(mEnderRes, viewGroup, false);
                ending.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallbacks.onClickEnding(v);
                    }
                });
                return new EndViewHolder(ending);
            case TYPE_PLACEHOLDER_UP:
                View loadMore = LayoutInflater.from(viewGroup.getContext()).inflate(mLoaderRes, viewGroup, false);
                loadMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallbacks.onClickLoadMore(v);
                    }
                });
                return new PlaceHolderViewHolder(loadMore);
            default:
                return onCreateItemViewHolder(viewGroup, viewType);
        }
    }

    protected abstract void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (AutoPagerManager.DEBUG)
            Log.i("LoadMore", "RecyclerWithFooterAdapter.onBindViewHolder " + position);
        if (viewHolder instanceof FooterViewHolder) {
//            FooterViewHolder holder = ((FooterViewHolder) viewHolder);
//            if (mItems.size() == 0 || mInLastPage) {
//                holder.linearLayout.setVisibility(View.GONE);
//                // todo maybe it's useless
//            }
        } else if (viewHolder instanceof PlaceHolderViewHolder) {
            if (AutoPagerManager.DEBUG) Log.v("LoadMore", "" + position);
            ((PlaceHolderViewHolder) viewHolder).textView.setText("Click me to load more");
        } else if (viewHolder instanceof EndViewHolder) {
            if (AutoPagerManager.DEBUG) Log.v("End", "" + position);
        } else {
            onBindItemViewHolder(viewHolder, position);
        }
    }

    private int getOffset(int position) {
        int offset = 0;
        for (Integer placeHolder : mLoadMorePositionPageMap.keySet()) {
            if (placeHolder >= position) {
                break;
            }
            offset++;
        }
        return offset;
    }

    @Override
    public int getItemCount() {
        return mItems.size() + mLoadMorePositionPageMap.size() + 1;
    }

    public void setInLastPage(boolean inLastPage) {
        this.mInLastPage = inLastPage;
    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {
        View linearLayout;

        public FooterViewHolder(View itemView) {
            super(itemView);
            linearLayout = itemView;
        }
    }

    private static class PlaceHolderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public PlaceHolderViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tvLoadMore);
        }
    }

    private static class EndViewHolder extends RecyclerView.ViewHolder {
        View linearLayout;

        public EndViewHolder(View itemView) {
            super(itemView);
            linearLayout = itemView;
        }
    }
}
