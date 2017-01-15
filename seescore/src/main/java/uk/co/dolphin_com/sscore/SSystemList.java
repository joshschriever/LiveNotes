/**
 * SeeScore Android API
 * Dolphin Computing http://www.dolphin-com.co.uk
 */

package uk.co.dolphin_com.sscore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.co.dolphin_com.sscore.SSystem.BarRange;

/**
 * A list of {@link SSystem}
 */
public class SSystemList  implements Iterable<SSystem>
{
	/**
	 * construct an empty SSystemList
	 */
	public SSystemList()
	{}

	/**
	 * Clear all {@link SSystem}s from the list
	 */
	public void clear()
	{
		list.clear();
	}
	
	/**
	 * Get the number of {@link SSystem} in this SystemList.
	 * 
	 * @return the number of SSystem
	 */
	public int getSize()
	{
		return list.size();
	}

	/**
	 * Add a {@link SSystem} to the end of this list.
	 * <p>Normally called from the layout callback.
	 * 
	 * @param sys the SSystem to add
	*/
	public void addSystem(SSystem sys)
	{
		list.add(sys);
	}

	/**
	 * Get a SSystem from the list by index.
	 *
	 * @param sysindex the index of the system (usually from the top of the score)
	 * @return the system in the systemlist indexed by sysindex (indexed in order of add)
	 */
	public SSystem getSystemAt(int sysindex)
	{
		return list.get(sysindex);
	}

	/**
	 * get the bounds of the systems
	 * 
	 * @param systemspacing the spacing between systems (0 = use default)
	 * @return the (width,height) of the score with the given systemspacing
	 */
	public Size getBounds(float systemspacing)
	{
		float width = 0;
		float height = 0;
		float gap = 0;
		for (SSystem sys : list)
		{
			Size bounds = sys.bounds();
			if (bounds.width > width)
				width = bounds.width;
			height += gap + bounds.height;
			gap = (systemspacing != 0) ? systemspacing : sys.getDefaultSpacing();
		}
		return new Size(width, height);
	}

	/**
	 * for a given bar return the index of the SSystem in this SSystemList which contains it
	 * 
	 * @param barindex the index of the bar
	 * @return return the system index for the bar index, or -1 if barindex is not contained in any SSystem
	 */
	public int getSystemIndexForBar(int barindex)
	{
		int sysindex = 0;
		for (SSystem sys : list)
		{
			BarRange br = sys.getBarRange();
			if(barindex >= br.startBarIndex && barindex < br.startBarIndex + br.numBars)
				return sysindex;
			++sysindex;
		}
		return -1;
	}

	List<SSystem> list = new ArrayList<SSystem>();

    @Override
    public Iterator<SSystem> iterator() {
        return new Iterator<SSystem>()
        {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < list.size();
            }

            @Override
            public SSystem next() {
                SSystem rval = list.get(index);
                ++index;
                return rval;
            }

            @Override
            public void remove() {

            }

        };
    }
}
