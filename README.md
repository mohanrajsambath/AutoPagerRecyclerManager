# AutoPagerRecyclerManager
Provide auto pager support for a RecycleView, loading data from a multi-page source has never been so easy!

## When to use it?
When you want to use a `RecyclerView` to display data from a multi-page source. 

For example, you are displaying content from a multi-page forum, like www.forum.com/board.json?page=1, www.forum.com/board.json?page=2, www.forum.com/board.json?page=3, and so on. The `AutoPagerManager` can handle the async process of loading data of different pages automatically. The data form `page=2` `page=3` or any other pages could be loaded easily on scroll or on other custom actions.

## Features
1. Load following pages automatically until data items fill the screen height.
2. Load following pages automatically on scroll.
3. Provide an empty view and a simple crossfade animation by default.
4. Provide an interface to load any specified page you want.
5. **Provide a Loader-based Fragment implementation which work harmoniously with the  Activity lifecycle.**
6. No need to save data or handle configuration changes, because all work is done by the Loader.


## How to use it?
It's very easy to use `AutoPagerFragment` or a refreshable version `AutoPagerRefreshableFragment` to load data from a multi-page source asynchronously. This fragment is very easy to extend. You only need to implement `onCreateLoader(int, Bundle)` and `onCreateAdapter()` to make it work. 

1. Enclosure your content in two custom classes, for example `ForumPage` and `Element`. The `ForumPage` class must implement the `Page<Element>` interface. The `Page` means the data you get for a certain page, which contains a iterable collection of `Element`s. For example, the `Page` data could be the json you get from www.forum.com/board.json?page=1. It contains a `JsonArray` which stores all its items, and they could be accessed by an iterator. These items could be contained in a list/map/set of `Elements`. If there is no parsing work needed, the `Element`  could just simply be a `String`.

	
		public interface Page<E> extends Iterable<E> {
		    
		    // return the index of the current Page instance.
            // note that the index of the first page should be 1.
		    int index();
		
		    
		    // return the index of the last page of the multi-page source. 
		    int last();
		}


2. Create a `Fragment` extending `AutoPagerFragment` or `AutoPagerRefreshableFragment`.
3. Implement `onCreateLoader()` with an `AutoPagerLoader`. You only need to tell it how to get data for a certain page in `newPage` method, which will be executed at background.

		@Override
		public Loader<TreeMap<Integer, ForumPage>> onCreateLoader(int id, Bundle args) {
		    return new AutoPagerLoader<ForumPage>(getActivity()) {
		        @Override
		        protected Page newPage(int page) throws DataNotLoadedException {
					String response = getDataFromServer(page);
					ForumPage page = parseString(response);
		            return page;
		        }
		    };
		} 

4. Implement `onCreateAdapter()` with an `AutoPagerAdapter`. It's just like implementing a normal `RecyclerView.Adapter` to provide an adapter from data to view.

		private class ForumPageAdapter extends AutoPagerAdapter<ForumPage, String> {
		
		    public ForumPageAdapter() {
		        super();
		    }
		
		    @Override
		    protected RecyclerView.ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int viewType) {
		        return new ItemViewHolder(new TextView(getActivity()));
		    }
		
		    @Override
		    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		        ((ItemViewHolder) viewHolder).mTextView.setText(getItem(position));
		    }
		}

		@Override
		protected AutoPagerRecyclerAdapter<ForumPage, String> onCreateAdapter() {
		    return new ForumPageAdapter();
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
The package provides 4 components of different hierarchies for you to custom the implementation.
#### AutoPagerManager
This manager wraps a `RecyclerView` and its corresponding `Adapter`. It's the main part of the package, providing all the auto pager function.
#### BaseAutoPagerFragment
This fragment is a default implementation of the `AutoPagerManager`. Using this fragment is very easy, and you only need to implement how to get the data from source whether sync or async.
#### AutoPagerFragment
**Loading data from a multi-page source has never been so easy** using the fragment. This fragment is based on loader pattern of Android, so **what you need to do is** just providing the parsing function to convert the address of the source to a `ElementGroup` implementation, and an adapter specifying how to display the items. **Then all the paging and refreshing work will all be handled automatically, even across a configuration change!**
#### AutoPagerRefreshableFragment
A refreshable version of `AutoPagerFragment`. You can substitute the implementation of `PullToRefresh` feature into whatever you like by extending `AutoPagerFragment`.

## Help me make it better
This is the first time I release a package, so there may be some problems or bugs. Feel free to tell me what could be done better even for a typo!
