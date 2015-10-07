# AutoPagerRecyclerManager
Provide auto pager support for a RecycleView, loading data from a multi-page source has never been so easy!

## When to use it?
When you are building a `RecyclerView` based on data from a multi-page source. 

For example, you are displaying content from a forum, like www.forum.com/board.json?page=1. The `AutoPagerRecyclerManager` can handle the async process of loading a different page. The data form `page=2` `page=3` or any other pages could be loaded easily on scroll or other custom actions.

## How to use it?
It's very easy to use `LoaderAutoPagerRefreshableRecyclerFragment` to load multi-page data source async. This class is very easy to use, you only need to implement `onCreateLoader(int, Bundle)` and `onCreateAdapter(AutoPagerRecyclerViewManager)` to make it work. 

1. Wrapped your content in two custom classes `Group` `Element`. `Group` must implement the `ElementGroup` interface. The `Group` means the data you get for a certain page, which contains a list of `Element`s. If there is no parsing work needed, the `Element` could even be a `String`.

		public interface ElementGroup<E> {
			// retrieve all the items
		    List<E> getElements();
			// an ElementGroup must know its current page index.
		    int getPageCurrentCount();
			// an ElementGroup must know the all page count. If it's endless just set a large number.
		    int getPageAllCount();
		}


2. Create a `Fragment` extending `LoaderAutoPagerRefreshableRecyclerFragment`.
3. Implement `onCreateLoader()` with a `AutoPagerLoader`. You only need to tell it how to get data for a certain page.

		@Override
		public Loader<List<Group>> onCreateLoader(int id, Bundle args) {
		    return new AutoPagerLoader<Group>(getActivity()) {
		        @Override
		        protected Group newGroup(int page) throws Exception {
					String response = getDataFromServer(page)
					Group group = parseString(response);
		            return group;
		        }
		    };
		} 

4. Implement `onCreateAdapter()` with a `AutoPagerRecyclerAdapter`. It's just like implementing a normal `Adapter()` to provide conversion from data to view.

		private class GroupAdapter extends AutoPagerRecyclerAdapter<Group, String> {
		
		    public GroupAdapter(AutoPagerRecyclerViewManager<Group, String> autoPagerRecyclerViewManager) {
		        super(autoPagerRecyclerViewManager);
		    }
		
		    @Override
		    protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int viewType) {
		        return new ItemViewHolder(new TextView(getActivity()));
		    }
		
		    @Override
		    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		        ((ItemViewHolder) viewHolder).mTextView.setText(getItemWithOffset(position));
		    }
		}

		@Override
		protected AutoPagerRecyclerAdapter<Group, String> onCreateAdapter(AutoPagerRecyclerViewManager<Group, String> viewManager) {
		    return new GroupAdapter(viewManager);
		}

5. All the work is done. Enjoy it!


## The hierachy
The pacakage provides 3 components of diifferent hierachies to support the auto pager function.
### AutoPagerRecyclerViewManager
This manager wraps a `RecyclerView` and its corresponding `Adapter`. It's the main part of the package, providing all the auto pager function.
### AutoPagerRefreshableRecyclerFragment
This fragment is a default implementation of the `AutoPagerRecyclerViewManager`. It also provide PullToRefresh functions. Using this fragment is very easy, and you only need to implement how to get the data from source whether sync or async.
### LoaderAutoPagerRefreshableRecyclerFragment
**Loading data from a multi-page source has never been so easy** using the fragment. This fragment is based on loader pattern of Android, so **what you need to do is** just providing the parsing function to convert the address of the source to a `ElementGroup` implementation, and an adapter specifying how to display the items. **Then all the paging and refreshing work will all be handled automatically, even across a configuration change!**

## Feel free to pull a request
This is the first time I release a package, so there may be some problems or bugs. Feel free to tell me what could be done better even a typo!
