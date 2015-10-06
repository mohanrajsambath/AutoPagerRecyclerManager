package tower.sphia.auto_pager_recycler.lib;

import android.content.Context;
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
 * An adapter for RecyclerView which enables and auto-handles footer view.
 * Concrete Implementation of ProgressBar is used here.
 */
public abstract class AutoPagerRecyclerAdapter<G extends ElementGroup<E>, E> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final int TYPE_ITEM = 1;
    protected static final int TYPE_FOOTER = 2;

    protected static final int TYPE_PLACEHOLDER_UP = 3;
    protected static final int TYPE_END = 4;

    private AutoPagerRecyclerViewManager<G, E> mAutoPagerRecyclerViewManager;
    public View.OnClickListener mOnEndClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mAutoPagerRecyclerViewManager.getRecyclerView().scrollToPosition(0);
        }
    };
    private List<E> mItems = new ArrayList<>();
    private Map<Integer, Integer> mLoadMorePositionPageMap = new TreeMap<>();
    public View.OnClickListener mOnLoadMoreClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int childLayoutPosition = mAutoPagerRecyclerViewManager.getRecyclerView().getChildLayoutPosition(v);

            int page = mLoadMorePositionPageMap.get(childLayoutPosition);
            mAutoPagerRecyclerViewManager.loadPage(page);
        }
    };
    private Context mContext;
    private int mFooterRes;
    private int mEnderRes;
    private int mLoaderRes;

    public AutoPagerRecyclerAdapter(AutoPagerRecyclerViewManager<G, E> autoPagerRecyclerViewManager) {
        mAutoPagerRecyclerViewManager = autoPagerRecyclerViewManager;
        mFooterRes = R.layout.item_footer;
        mEnderRes = R.layout.item_end;
        mLoaderRes = R.layout.item_load_more;
    }

    public void setOnLoadMoreClickListener(View.OnClickListener onLoadMoreClickListener) {
        mOnLoadMoreClickListener = onLoadMoreClickListener;
    }

    public void setOnEndClickListener(View.OnClickListener onEndClickListener) {
        mOnEndClickListener = onEndClickListener;
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

    public void setData(List<G> groups) {
        mItems.clear();
        mLoadMorePositionPageMap.clear();
        int page = 0;
        for (G group : groups) {
            int pageCurrentCount = group.getPageCurrentCount();
            if (pageCurrentCount != page + 1) {
                mLoadMorePositionPageMap.put(mItems.size(), pageCurrentCount - 1);
            }
            page = pageCurrentCount;
            mItems.addAll(group.getElements());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int itemCount = getItemCount();

        if (position == itemCount - 1) {
            if (mItems.size() != 0 && mAutoPagerRecyclerViewManager.inLastPage()) {
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

    protected E getItemWithOffset(int i) {
        return mItems.get(i - getOffset(i));
    }

    protected E findItemByView(View view) {
        int childLayoutPosition = mAutoPagerRecyclerViewManager.getRecyclerView().getChildLayoutPosition(view);
        return getItemWithOffset(childLayoutPosition);
    }

    private Context getContext() {
        if (mContext == null) {
            mContext = mAutoPagerRecyclerViewManager.getRecyclerView().getContext();
        }
        return mContext;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (AutoPagerRecyclerViewManager.DEBUG) Log.i("LoadMore", "RecyclerWithFooterAdapter.onCreateViewHolder");
        switch (viewType) {
            case TYPE_ITEM:
                return onCreateItemViewHolder(viewGroup, viewType);
            case TYPE_FOOTER:
                View footer = LayoutInflater.from(getContext()).inflate(mFooterRes, viewGroup, false);
                return new FooterViewHolder(footer);
            case TYPE_END:
                View view2 = LayoutInflater.from(getContext()).inflate(mEnderRes, viewGroup, false);
                view2.setOnClickListener(mOnEndClickListener);
                return new EndViewHolder(view2);
            case TYPE_PLACEHOLDER_UP:
                View view = LayoutInflater.from(getContext()).inflate(mLoaderRes, viewGroup, false);
                view.setOnClickListener(mOnLoadMoreClickListener);
                return new PlaceHolderViewHolder(view);
            default:
                return onCreateItemViewHolder(viewGroup, viewType);
        }
    }

    protected abstract void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (AutoPagerRecyclerViewManager.DEBUG)
            Log.i("LoadMore", "RecyclerWithFooterAdapter.onBindViewHolder " + position);
        if (viewHolder instanceof FooterViewHolder) {
            FooterViewHolder holder = ((FooterViewHolder) viewHolder);
            if (mAutoPagerRecyclerViewManager.inLastPage() || mItems.size() == 0) {
                holder.linearLayout.setVisibility(View.GONE);
                // todo maybe it's useless
            }
        } else if (viewHolder instanceof PlaceHolderViewHolder) {
            if (AutoPagerRecyclerViewManager.DEBUG) Log.v("LoadMore", "" + position);
            ((PlaceHolderViewHolder) viewHolder).textView.setText("Click me to load more");
        } else if (viewHolder instanceof EndViewHolder) {
            if (AutoPagerRecyclerViewManager.DEBUG) Log.v("End", "" + position);
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
        TextView textView;

        public EndViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tvLoadMore);
        }
    }
}
