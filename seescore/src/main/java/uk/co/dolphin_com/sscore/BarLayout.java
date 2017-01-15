package uk.co.dolphin_com.sscore;

import java.util.List;

/**
 * info about barlines in a system/part returned from getBarLayout()
 */
public class BarLayout {

    public static enum BarlineLoc { left, middle, right}

    /**
     * info for a single barline
     */
    public static class Barline {

        /**
         * the bar index
         */
        public final int barIndex;

        /**
         * right/left barline
         */
        public final BarlineLoc loc;

        /**
         * a rectangle completely enclosing the barline and any repeat dots (ie wider for double barlines)
         */
        public final android.graphics.RectF rect;

        Barline(int bindex, BarlineLoc l, android.graphics.RectF r )
        {
            barIndex = bindex;
            loc = l;
            rect = r;
        }
    }

    /**
     * the part index
     */
    public final int partIndex;


    /**
     * list of Barline for the system/part
     */
    public final List<Barline> barlines;

    BarLayout(int pindex, List<Barline> blines)
    {
        partIndex = pindex;
        barlines = blines;
    }
}

