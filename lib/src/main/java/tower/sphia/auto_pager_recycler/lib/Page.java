package tower.sphia.auto_pager_recycler.lib;

/**
 * Created by Rye on 6/18/2015.
 * <p>
 * The {@link Page} interface represents the data for a certain page. For example, when you get a json
 * from server, like www.autopager.com/sample.json?page=1, the json you get is for page 1, and it contains
 * many sub items as @param <E> represents.
 *
 * The first page is always 1. So you should add a offset if your page doesn't starts with 1.
 * <p>
 * The concrete class for the data of a certain page must implement this interface to provide necessary information.
 *
 * Implement {@link Iterable} to provide iterator for the elements/items of the current page.
 *
 * @param <E> the type of element, if there's no further parsing work needed, just use {@link String} is okay.
 */
public interface Page<E> extends Iterable<E> {

    /**
     * @return The index of the current {@link Page} instance.
     */
    int index();

    /**
     * @return The index of the last page of the multi-page source. The returned value may vary for different
     * {@link Page} instances, because the data source may be updated to get new trailing pages.
     */
    int last();
}
