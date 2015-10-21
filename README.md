# AutoPagerRecyclerManager
Provide auto pager support for a RecycleView, loading data from a multi-page source has never been so easy!

## When to use it?
When you want to use a `RecyclerView` to display data from a multi-page source. 

For example, you are displaying content from a multi-page forum, like www.forum.com/board.json?page=1, www.forum.com/board.json?page=2, www.forum.com/board.json?page=3, and so on. The `AutoPagerRecyclerManager` can handle the async process of loading data of different pages automatically. The data form `page=2` `page=3` or any other pages could be loaded easily on scroll or other custom actions.

## Features
1. Load following pages automatically until data items fill the screen height.
2. Load following pages automatically on scroll.
3. Provide an empty view and a simple crossfade animation by default.
4. Provide an interface to load any specified page you want.
5. **Provide a Loader-based Fragment implementation which work harmoniously with the  Activity lifecycle.**


## How to use it?
It's very easy to use `LoaderAutoPagerRefreshableRecyclerFragment` to load data from a multi-page source asynchronously. This fragment is very easy to extend. You only need to implement `onCreateLoader(int, Bundle)` and `onCreateAdapter(AutoPagerRecyclerViewManager)` to make it work. 

1. Wrapped your content in two custom classes, for example `Group` and `Element`. The `Group` class must implement the `ElementGroup` interface. The `Group` means the data you get for a certain page, which contains a list of `Element`s. For example, the `Group` data is the json you get from www.forum.com/board.json?page=1. It contains a `JsonArray` which stores all its items. These items could be extracted as a list of `Elements`. If there is no parsing work needed, the `Element` could even be a `String`.

		public interface ElementGroup<E> {

			// retrieve all the items
		    List<E> getElements();

			// an ElementGroup must know its current page index.
		    int getPageCurrentCount();

			// an ElementGroup must know the all page count. If it's endless just set a large number.
		    int getPageAllCount();
		}


2. Create a `Fragment` extending `LoaderAutoPagerRefreshableRecyclerFragment`.
3. Implement `onCreateLoader()` with an `AutoPagerLoader`. You only need to tell it how to get data for a certain page in `newGroup` method, which will be executed at background.

		@Override
		public Loader<List<Group>> onCreateLoader(int id, Bundle args) {
		    return new AutoPagerLoader<Group>(getActivity()) {
		        @Override
		        protected Group newGroup(int page) throws Exception {
					String response = getDataFromServer(page);
					Group group = parseString(response);
		            return group;
		        }
		    };
		} 

4. Implement `onCreateAdapter()` with an `AutoPagerRecyclerAdapter`. It's just like implementing a normal `RecyclerView.Adapter` to provide an adapter from data to view.

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

5. Add your fragment to an `Activity`. 
6. All the work is done. Enjoy it!

## How to include it in your project?
For gradle users, you only need to modify these configuration files:

1. Modify `setting.gradle` in root directory.

		include ':app', ':AutoPagerRecyclerManager'
		project(':AutoPagerRecyclerManager').projectDir = new File("...YOUR DIRECTORY PATH.../AutoPagerRecyclerManager/lib")

2. Modify `build.gradle` in `src` directory.

		dependencies {
		    compile fileTree(dir: 'libs', include: ['*.jar'])
		    compile project(':AutoPagerRecyclerManager')
		}


## The hierarchy
The package provides 3 components of different hierarchies to support the auto pager function.
#### AutoPagerRecyclerViewManager
This manager wraps a `RecyclerView` and its corresponding `Adapter`. It's the main part of the package, providing all the auto pager function.
#### AutoPagerRefreshableRecyclerFragment
This fragment is a default implementation of the `AutoPagerRecyclerViewManager`. It also provide PullToRefresh functions. Using this fragment is very easy, and you only need to implement how to get the data from source whether sync or async.
#### LoaderAutoPagerRefreshableRecyclerFragment
**Loading data from a multi-page source has never been so easy** using the fragment. This fragment is based on loader pattern of Android, so **what you need to do is** just providing the parsing function to convert the address of the source to a `ElementGroup` implementation, and an adapter specifying how to display the items. **Then all the paging and refreshing work will all be handled automatically, even across a configuration change!**

## Help me make it better
This is the first time I release a package, so there may be some problems or bugs. Feel free to tell me what could be done better even for a typo!
