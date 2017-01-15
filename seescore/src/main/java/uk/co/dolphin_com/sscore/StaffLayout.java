package uk.co.dolphin_com.sscore;

import java.util.List;

/**
 * Staff layout info returned from getStaffLayout
 */
public class StaffLayout {

    /**
     * info about a single staff
     */
    public static class Staff {

        /**
         * rectangle enclosing a single staff in a system
         */
        public final android.graphics.RectF staffRect;

        /**
         * the number of lines in the staff
         */
        public final int numLines;

        Staff(android.graphics.RectF r, int n) {
            staffRect = r;
            numLines = n;
        }
    }

    /**
     * the part index
     */
    public final int partIndex;

    /**
     * one tenth of the staff line separation in CG units
     */
    public final float tenthSize;

    /**
     * @return the array of staff info for this part (expect normally 1 or 2 in array)
     */
    public final List<Staff> staves;

    StaffLayout(int pindex, float tsize, List<Staff> staves) {
        this.partIndex = pindex;
        this.tenthSize = tsize;
        this.staves = staves;
    }
}
