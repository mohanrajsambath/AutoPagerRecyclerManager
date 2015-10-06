package tower.sphia.auto_pager_recycler.lib;

import java.util.List;

/**
 * Created by Rye on 6/18/2015.
 *
 * The ElementGroup interface represents the data for a certain page. For example, when you get a json
 * from server, like www.autopager.com/sample.json?page=1, the json you get is for page 1, and it contains
 * many sub items as @param <E> represents.
 *
 * The concrete class for the data of a certain page must implement this interface to provide necessary information.
 * 
 * @param <E> the type of element, if there's no further parsing work needed, just use {@link String} is okay.
 */
public interface ElementGroup<E> {
    List<E> getElements();

    int getPageCurrentCount();

    int getPageAllCount();
}
