package tower.sphia.auto_pager_recycler.lib;

import android.support.annotation.NonNull;
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
    public static final int ITEM = 1;
    public static final int DIVIDER = 2;
    public static final int FOOTER = 3;
    public static final int END = 4;
    private static final String TAG = "AutoPagerAdapter";
    public static boolean DEBUG = false;
    private List<ItemWrapper<E>> mItems = new ArrayList<>();
    private int mFooterRes;
    private int mEnderRes;
    private int mLoaderRes;
    private boolean mInLastPage = false;
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

    public AdapterCallbacks getCallbacks() {
        return mCallbacks;
    }

    public void setCallbacks(AdapterCallbacks callbacks) {
        mCallbacks = callbacks;
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
        if (DEBUG) Log.d(TAG, "setItems() called with " + "pages.size() = [" + pages.size() + "]");
        mItems.clear();
        int prev = -1;

        for (Map.Entry<Integer, P> entry : pages.entrySet()) {
            Page<E> page = entry.getValue();
            int index = page.index();

            if (prev != -1) {
                if (index != prev + 1) {
                    mItems.add(ItemWrapper.<E>newDivider(index - 1));
                }
            }
            prev = index;
            for (E e : page) {
                mItems.add(ItemWrapper.newItem(e));
            }
        }
        if (mInLastPage) {
            mItems.add(ItemWrapper.<E>newEnd());
        } else {
            mItems.add(ItemWrapper.<E>newFooter());
        }
        if (DEBUG) Log.i(TAG, "setItems " + mItems.size());
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }

    protected abstract RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int viewType);

    public E getItem(int i) {
        return mItems.get(i).getItem();
    }

    public ItemDivider getDivider(int i) {
        return (ItemDivider) mItems.get(i);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (DEBUG) Log.d("LoadMore", "RecyclerWithFooterAdapter.onCreateViewHolder");
        switch (viewType) {
            case ITEM:
                return onCreateItemViewHolder(viewGroup, viewType);
            case FOOTER:
                View footer = LayoutInflater.from(viewGroup.getContext()).inflate(mFooterRes, viewGroup, false);
                footer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallbacks.onClickFooter(v);
                    }
                });
                return new FooterViewHolder(footer);
            case END:
                View ending = LayoutInflater.from(viewGroup.getContext()).inflate(mEnderRes, viewGroup, false);
                ending.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallbacks.onClickEnding(v);
                    }
                });
                return new EndViewHolder(ending);
            case DIVIDER:
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
        if (DEBUG)
            Log.i("LoadMore", "RecyclerWithFooterAdapter.onBindViewHolder " + position);
        if (viewHolder instanceof FooterViewHolder) {
//            FooterViewHolder holder = ((FooterViewHolder) viewHolder);
//            if (mItems.size() == 0 || mInLastPage) {
//                holder.linearLayout.setVisibility(View.GONE);
//                // todo maybe it's useless
//            }
        } else if (viewHolder instanceof PlaceHolderViewHolder) {
            if (DEBUG) Log.v("LoadMore", "" + position);
            ((PlaceHolderViewHolder) viewHolder).textView.setText("Click me to load more");
        } else if (viewHolder instanceof EndViewHolder) {
            if (DEBUG) Log.v("End", "" + position);
        } else {
            onBindItemViewHolder(viewHolder, position);
        }
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setInLastPage(boolean inLastPage) {
        this.mInLastPage = inLastPage;
    }

    /**
     * The callback listeners for the special items
     */
    public interface AdapterCallbacks {
        void onClickFooter(View view);

        void onClickLoadMore(View view);

        void onClickEnding(View view);
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

    /**
     * Created by Voyager on 2/14/2016.
     */

    static class ItemWrapper<E> {
        private final E mItem;
        private final int mType;

        private ItemWrapper(E item) {
            this.mItem = item;
            this.mType = ITEM;
        }

        protected ItemWrapper(int type) {
            this.mType = type;
            this.mItem = null;
        }

        public static <E> ItemDivider<E> newDivider(int lastPage) {
            return new ItemDivider<>(DIVIDER, lastPage);
        }

        public static <E> ItemWrapper<E> newEnd() {
            return new ItemWrapper<>(END);
        }

        public static <E> ItemWrapper<E> newFooter() {
            return new ItemWrapper<>(FOOTER);
        }

        public static <E> ItemWrapper<E> newItem(@NonNull E item) {
            return new ItemWrapper<>(item);
        }

        public int getType() {
            return mType;
        }

        public E getItem() {
            if (mItem == null) {
                throw new NullPointerException("this wrapper doesn't wrap a item");
            }
            return mItem;
        }

    }

    /**
     * Created by Voyager on 2/14/2016.
     */
    static class ItemDivider<E> extends ItemWrapper<E> {
        private int mLastPage;

        public ItemDivider(int type, int lastPage) {
            super(type);
            this.mLastPage = lastPage;
        }

        public int getLastPage() {
            return mLastPage;
        }

    }
}
