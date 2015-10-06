# AutoPagerRecyclerManager
Provide auto pager support for a RecycleView, loading data from a multi-page source has never been so easy!

## When to use it?
When you are building a `RecyclerView` based on data from a multi-page source. 

For example, you are displaying content from a forum, like www.forum.com/board.json?page=1. The `AutoPagerRecyclerManager` can handle the async process of loading a different page. The data form `page=2` `page=3` or any other pages could be loaded easily on scroll or other custom actions.

## The hierachy
The pacakage provides 3 components of diifferent hierachies to support the auto pager function.
### AutoPagerRecyclerViewManager
This manager wraps a `RecyclerView` and its corresponding `Adapter`. It's the main part of the package, providing all the auto pager function.
### AutoPagerRefreshableRecyclerFragment
This fragment is a default implementation of the `AutoPagerRecyclerViewManager`. It also provide PullToRefresh functions. Using this fragment is very easy, and you only need to implement how to get the data from source whether sync or async.
### LoaderAutoPagerRefreshableRecyclerFragment
**Loading data from a multi-page source has never been so easy** using the fragment. This fragment is based on loader pattern of Android, so **what you need to do is** just providing the parsing function to convert the address of the source to a `ElementGroup` implementation, and an adapter specifying how to display the items. **Then all the paging and refreshing work will all be handled automatically, even across a configuration change!**
## Feel free to pull a request
This is the first time I release a personal package, so there may be some problems or bugs. Feel free to tell me what could be done better even a typo or any other probelms!