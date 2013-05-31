package missile;

import vgpackage.*;
import mytools.*;

import java.awt.*;
import java.awt.event.*;

public class BaseObj extends GameObj {
    private static final int MAX_TYPES = 2;

    protected void explode() {
    }

    protected static byte[] typeCounts = new byte[MAX_TYPES];
    public static final int WORLD_Y = (395 << VidGame.FRACBITS);
    protected static final int S_DESTROYED = 1;
    protected static final int S_ACTIVE = 2;

    public void setStatus(int s) {
        int cStatus = getStatus();
        if (cStatus != s) {
            if (cStatus == S_ACTIVE || s == S_ACTIVE)
                typeCounts[type] += (s == S_ACTIVE ? 1 : -1);
            super.setStatus(s);
        }
    }


    protected void destroy(Sprite s) {
        SpriteExp.add(s, loc,
                500, 0, 8,
                10, 500);
        setStatus(S_DESTROYED);
        Sfx.play(Missile.SFX_CITYGONE);
    }
/*
    // Class methods

    public static void init() {
        iter = init(MAX);
        iter.toFirst();

        int i = 0;
        while (iter.isNext()) {
            iter.storeNext(new Base(i++));
        }
    }

    public static void moveAll() {

        final int BLASTRADIUS = 3 << VidGame.FRACBITS;

        // Check a few of them for destroying each cycle.
        for (int i = 2; i > 0; i--) {
            Base b = (Base)iter.get(destroyIndex);

            if (++destroyIndex == MAX)
                destroyIndex = 0;

            if (b.getStatus() != S_ACTIVE) continue;

            boolean hit = false;

            int y = b.loc.y - b.height;
            hit = (Blast.testCollision(b.loc.x,y, BLASTRADIUS));
            int xOffset = (b.width * 3) >> 2; //32 << VidGame.FRACBITS;
            y = b.loc.y - (b.height >> 2);

            hit |= (Blast.testCollision(b.loc.x - xOffset,
                y, BLASTRADIUS));
            hit |= (Blast.testCollision(b.loc.x + xOffset,
                 y, BLASTRADIUS));

            if (hit)
                b.explode();
        }
    }

    private static int plotAccum;
    private static int plotIndex;

    public static void plotAll(Graphics g, boolean valid) {

        plotAccum -= VidGame.CYCLE; // * 4;
        if (plotAccum < 0) {
            plotAccum = 1000;
            plotIndex = (plotIndex + 1) % MAX;
            Base b = (Base)iter.get(plotIndex);
            b.redrawFlag = true;
        }
        plotAll(iter, g, valid);
    }
*/
/*
    public static Pt[] buildTargetList() {
        iter.toFirst();
        int total = 0;
        Pt[] list = new Pt[MAX];
        while (iter.isNext()) {
            Base b = (Base)iter.getNext();
            if (b.getStatus() != S_VACANT) {
                list[total++] = new Pt(b.loc);
            }
        }
        Pt[] outList = new Pt[total];
        while (total-- > 0)
            outList[total] = list[total];
        return outList;
    }

    public static void add(int index, Base b) {
        iter.toFirst();
        while (index-- > 0)
            iter.getNext();
        iter.storeNext(b);
    }
*/
    public BaseObj(int index) {
        final int[] xPos = {48,136,232,320,408,504,592};
        final int[] yPos = {21,13,13,21,13,13,21};
        loc = new Pt(xPos[index] << VidGame.FRACBITS,
            Missile.MAIN_YM - ((yPos[index] + 1) << VidGame.FRACBITS));
    }

    public BaseObj() {
    }

    public int getType() {
        return type;
    }

    public void setType(int t) {
        type = t;
    }

    public Pt getLoc() {
        return new Pt(loc);
    }

    public void clearBgnd() {//Graphics g) {
//        Graphics g = BEngine.getGraphics();
//        if (true) return;
        //BomberObj h= null;
//        g.setColor(Color.black);
        BEngine.setColor(Color.black);
//        MyColors.set(g,0,MyMath.rnd(32));

        final int PLOTWIDTH = 5 * 16; //76;
        final int PLOTHEIGHT = 40;
//        final int PLOTOFFSETY = 47;

        BEngine.fillRect( (loc.x >> VidGame.FRACBITS) - PLOTWIDTH/2,
         (loc.y >> VidGame.FRACBITS) - PLOTHEIGHT,
         PLOTWIDTH, PLOTHEIGHT  );
    }

    // Class variables

//    public static final int MAX = 7;
//    protected static GameObjIterator iter;

    // Instance variables
    protected boolean redrawFlag;
    private int type;
    protected int drawnStatus;
    protected int width;
    protected int height;
    private Pt loc = new Pt();
}