package tower.sphia.auto_pager_recycler.lib;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Voyager on 8/22/2015.
 */
 public abstract class AutoPagerLoader<D extends ElementGroup<?>> extends AsyncTaskLoaderImpl<List<D>> {
    private int mTargetPage = 1;

    public AutoPagerLoader(Context ctx) {
        super(ctx);
    }

    @Override
    protected void releaseResources(List<D> data) {

    }

    public AutoPagerLoader<D> setTargetPage(int page) {
        mTargetPage = page;
        return this;
    }


    /**
     * create group instance from string,
     *
     * @param page the page to be loaded
     * @return the object instance for the page
     */
    protected abstract D newGroup(int page) throws Exception;

    @Override
    public List<D> loadInBackground() {
        // This method is called on a background thread and should generate a
        // new set of data to be delivered back to the client.
        List<D> data = new ArrayList<>();
        // TODO: Perform the query here and add the results to 'data'.
        if (mTargetPage == 1) {
            try {
                data.add(newGroup(1));
            } catch (Exception e) {
                e.printStackTrace();
                return data;
            }
            return data;
        } else {
            data.addAll(getData());
            try {
                D newGroup = newGroup(mTargetPage);
                for (int i = data.size() - 1; i >= 0; i--) {
                    if (newGroup.getPageCurrentCount() > data.get(i).getPageCurrentCount()) {
                        data.add(i + 1, newGroup);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return data;
            }
            return data;
        }
    }

    public int getTargetPage() {
        return mTargetPage;
    }
}
