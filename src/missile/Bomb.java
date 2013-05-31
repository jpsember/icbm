package missile;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Bomb extends GameObj {

    /**
     * Prepares for a new level.  Sets up a new series of attack waves.
     */
    public static void prepare(int type) {
        switch (type) {
        case GAME:
            removeAll();
            break;
        case LEVEL:
            {
            final int SPEED = VidGame.TICK * 16 * 20;
            final int SPEED_MAX = VidGame.TICK * 16 * 40;

            waveSpeed = Math.min(SPEED_MAX,
                (SPEED * (11 + VidGame.getLevel())) >> 3);

            if (Bomber.isBomberWave()) {
                wavesRemaining = 0;
                break;
            }
            final byte wavesPerLevel[] = {2,2,3,3,3,3,4,4};
            wavesRemaining = wavesPerLevel[Math.min(7,VidGame.getLevel())];
            setWaveDelay();
            if (VidGame.getMode() == VidGame.MODE_PREGAME)
                waveSpeed = SPEED_MAX * 3;
            }
            break;
        }
    }

    /**
     * Move all the bombs
     */
    public static void move() {

        if (VidGame.getStage() == Missile.GS_PLAYING && activeCount == 0) {
            waveDelay -= VidGame.CYCLE;
            if (waveDelay <= 0) {
                setWaveDelay();
                if (wavesRemaining > 0) {
                    wavesRemaining--;
                    prepareWave(null);
                }
            }
        }
        move(iter);
    }

    /**
     * Determines if the current level is complete.
     */
    public static boolean levelComplete() {
        return (wavesRemaining == 0 && activeCount == 0);
    }

    /**
     * Plot bombs to background layer
     * @param g Graphics object
     * @param valid if false, the entire buffer is invalid, and everything
     * needs redrawing
     */
    public static void draw() {
        draw(iter);
    }

    /**
     * Initialize the bomb class
     * @param sf SpriteFinder object containing bomb sprites
     * @param sIndex index of first bomb sprite in SpriteFinder
     */
    public static void init(SpriteFinder sf, int sIndex) {

        iter = new GameObjIterator(35);
        while (iter.isRoom())
            iter.store(new Bomb());
        sprites = new Sprite[SPR_TOTAL];
        for (int i = 0; i < SPR_TOTAL; i++)
            sprites[i] = sf.find(sIndex++);
    }

    /**
     * Drops a new bomb with a particular horizontal velocity
     * @param sourceLoc the starting location of the bomb
     * @param xVel the horizontal velocity
     */
    public static void drop(Pt sourceLoc, int xVel) {
        Bomb b = (Bomb)findVacant(iter);
        if (b == null) return;
        b.bringOn(sourceLoc, xVel, waveSpeed);
    }

    // Move a single bomb
    protected void moveOne() {
        switch (getStatus()) {
        case S_VACANT:
            return;

        case S_WAITING:
            delay -= VidGame.CYCLE;
            if (delay > 0) return;
            setStatus(S_MOVING);
            break;

        case S_SPLIT:
            if (activeChildren == 0) {
                setDying();
            }
            return;

        case S_DYING:
            if (lifeFrame != 0) {
                final int DEATH_SPEED = 500 / VidGame.FPS;
                lifeFrame = Math.max(lifeFrame - DEATH_SPEED, 0);
            }
            if (lifeFrame != 0) return;
            if (splitParent != null) {
                splitParent.activeChildren--;
                splitParent = null;
            }
            if (lifeFrameDraw == lifeFrame)
                setStatus(S_VACANT);
            return;
        }
        vel.addTo(loc);

        boolean destroy = Blast.testCollision(loc.x, loc.y, 0);
        if (destroy)
            VidGame.adjScore(100);
        if (!destroy && lifeFrame == lifeSpan) {
            if (!willSplit)
                destroy = true;
            else {
                // Split this one into a number of others.
                setStatus(S_SPLIT);
                prepareWave(this);
                return;
            }
        }

        if (destroy) {
            setDying();
            Blast.add(loc);
        } else {
            lifeFrame++;
            final int MARGIN = VidGame.ONE * 20;
            if (loc.x < -MARGIN || loc.x > Missile.MAIN_XM + MARGIN) {
                setDying();
            }
        }
    }

    /**
     * Removes all the bombs for the start of a new game.
     * Changes any S_MOVING bombs to S_DYING.
     */
    private static void removeAll() {
        iter.toFirst();
        while (iter.isNext()) {
            Bomb b = (Bomb)iter.getNext();
            int s = b.getStatus();
            if (s == S_WAITING)
                b.setStatus(S_VACANT);
            if (s == S_MOVING) {
                b.setDying();
            }
        }
    }

    // Draw or erase a bomb trail to the background layer
    // frameStart = frame to begin plotting
    // frameEnd = frame where plotting ends
    // erase = true if erasing the trail
    private void plotTrail(int frameStart, int frameEnd,
        boolean erase) {

        final int GRAIN_MASK = ~3;

        Debug.ASSERT(frameStart < frameEnd, "plotTrail, start="+frameStart+" end="+frameEnd+" erase="+erase);

        Graphics g = BEngine.getGraphics();
        Sprite s = sprites[SPR_TRAILS + (erase ? 1 : 0)];

        // Calculate the world coordinates where plotting begins
        final Pt pt = new Pt();    // This hopefully gets created at load time.
        pt.set(startLoc.x + vel.x * frameStart,
            startLoc.y + vel.y * frameStart);

        // Determine the view y-coordinate for this location.
//        int viewY = (pt.y >> VidGame.FRACBITS) & GRAIN_MASK;
        boolean firstTime = true;
        int newViewY = (pt.y >> VidGame.FRACBITS) & GRAIN_MASK;
//        int oldViewY = 0;
        while (frameStart < frameEnd) {
            frameStart++;
            int oldViewY = newViewY;
//            int oldViewY = (pt.y >> VidGame.FRACBITS) & GRAIN_MASK;
            vel.addTo(pt);
            newViewY = (pt.y >> VidGame.FRACBITS) & GRAIN_MASK;
//            boolean same = (newViewY == viewY);
//            viewY = newViewY;
            if (newViewY == oldViewY) continue;
            // Time to plot a new sprite.
            BEngine.drawSpriteWorld(s,pt);
        }

    }

    private void setDying() {
        setStatus(S_DYING);
    }

    // Plot a single bomb to the sprite layer
    protected void drawOne() {
        if (BEngine.isBgndLayer())
            drawOneBGND();
        else {
            if (getStatus() == S_MOVING)
                BEngine.drawSpriteWorld(sprites[SPR_BOMB],loc);
        }
    }

    // Set up a suitable delay until the next attack wave commences
    private static void setWaveDelay() {
        int level = Math.min(VidGame.getLevel(), 8-1);
        final short delays[] = {1500,1300,900,700,500,300,200,100};
        waveDelay = MyMath.rnd(delays[level]) + delays[level];
    }

    // Prepare a wave
    // splitSource: if not null, prepare wave splitting from this bomb
    private static void prepareWave(Bomb splitSource) {
        boolean splitFlag = (splitSource != null);

        Pt[] targets = null;

        if (!splitFlag) {
            targets = Base.buildTargetList();
            if (targets.length == 0) return;
        }

        // Choose # of bombs to bring in this wave.
        int bombTotal = 0;
        int vSum = 0;
        if (!splitFlag) {
            bombTotal = Math.min(MyMath.rnd(3) + 5, MAX_PER_WAVE);
        } else {
            bombTotal = MyMath.rnd(3)+2;
            vSum = splitSource.vel.x * bombTotal;
        }

        // Choose a starting point, target, and delay for each bomb.
        int nextDelay = 0;
        do {
            Bomb b = (Bomb)findVacant(iter);
            if (b == null) {
                if (VidGame.DEBUG)
                    Debug.print("unable to find vacant bomb, prepareWave");
                break;
            }

            if (!splitFlag) {
                int target = MyMath.rnd(targets.length);
                final Pt sPt = new Pt();
                sPt.set(
                    MyMath.rnd(Missile.MAIN_XM - (20 << VidGame.FRACBITS)) +
                        (10 << VidGame.FRACBITS),
                    -VidGame.ONE * 3);

                b.bringOn(sPt, targets[target], null);
                b.delay = nextDelay;
                nextDelay += MyMath.rnd(1500) + 400;
            } else {
                int vx = vSum;
                if (bombTotal > 1) {
                    vx = (vSum / bombTotal) + MyMath.rndCtr(waveSpeed >> 1);
                }
                vSum -= vx;

                int vy = splitSource.vel.y + MyMath.rndCtr(waveSpeed >> 2);

                // From the velocity, calculate the target location.

                int time = (BaseObj.WORLD_Y - splitSource.loc.y) / vy;
                Pt targ = new Pt(splitSource.loc.x + vx * time, BaseObj.WORLD_Y);

                b.bringOn(splitSource.loc, targ, splitSource);
            }
        } while (--bombTotal != 0);
    }

    private static int calcDestY() {
        final int DEST_Y = Missile.MAIN_YM - (VidGame.ONE * 45);

        int y = DEST_Y + MyMath.rndCtr(20 << VidGame.FRACBITS);
        if (VidGame.getMode() == VidGame.MODE_PREGAME)
            y = DEST_Y - (50 << VidGame.FRACBITS) - MyMath.rnd(60 << VidGame.FRACBITS);
        return y;
    }

    // Reset variables for bomb that is being brought on
    private void resetVars(Pt sL) {
        setStatus(S_WAITING);
        statusDraw = S_VACANT;
        lifeFrame = 0;
        splitParent = null;
        activeChildren = 0;
        sL.copyTo(startLoc);
        startLoc.copyTo(loc);
        willSplit = false;
    }

    // Bring on a new bomb that won't split, specifying
    // starting position and velocity
    private void bringOn(Pt sL, int xv, int yv) {
        resetVars(sL);
        vel.set(xv,yv);
        lifeSpan = (calcDestY() - startLoc.y) / yv;
    }

    // Bring on a new bomb that may split, that may have come from a
    // split bomb, and that is aimed at a target
    private void bringOn(Pt sL, Pt target, Bomb splitSource) {
        resetVars(sL);
        boolean splitFlag = (splitSource != null);

        willSplit = (!splitFlag && (MyMath.rnd(2) == 0));

        final Pt dest = new Pt();

        dest.x = target.x + MyMath.rndCtr(VidGame.ONE * 25);
        dest.y = calcDestY();

        calcVelocity(dest, vel);

        int span = (dest.y - startLoc.y) / vel.y;
        if (willSplit) {
            int range = Math.max(MyMath.rnd(span >> 1), span >> 2);
            span = (span >> 3) + MyMath.rnd(range);
        }

        lifeSpan = span;
        if (splitSource != null) {
            splitParent = splitSource;
            splitParent.activeChildren++;
        }
    }

    // Calculate the velocity for a bomb to reach a destination
    private void calcVelocity(Pt dest, Pt calcVel) {
        Angle2 a = new Angle2();
        Pt delta = new Pt(dest.x - loc.x, dest.y - loc.y);
        a.calc(delta.x, delta.y);
        Angle2.createRay(waveSpeed, a, calcVel);
    }

    // Change the status of the bomb
    public void setStatus(int s) {
        int oldStatus = getStatus();
        if (oldStatus != s) {
            if (s == S_VACANT)
                activeCount--;
            if (oldStatus == S_VACANT)
                activeCount++;
            super.setStatus(s);
        }
    }

    // Plot a single bomb trail to the background layer.
    protected void drawOneBGND() {
        // If new status does not equal last drawn status,
        // initialize variables for the draw state, based on the
        // old draw state.
        if (!BEngine.layerValid())
            statusDraw = S_VACANT;

        int s = getStatus();
        if (s != statusDraw) {
            switch (statusDraw) {
            case S_VACANT:
            case S_WAITING:
                lifeFrameDraw = 0;
                break;
            case S_MOVING:
            case S_SPLIT:
                break;
            }
        }

        do {
            // Plot growth trail if necessary.

            if (s < S_MOVING) break;
            if (lifeFrameDraw != lifeFrame) {
                if (lifeFrameDraw < lifeFrame)
                    plotTrail(lifeFrameDraw, lifeFrame, false);
                else
                    plotTrail(lifeFrame,lifeFrameDraw, true);
            }
        } while (false);

        // Now that we are sure the drawing has been made up to date,
        // copy the logic to draw variables.

        statusDraw = s;
        lifeFrameDraw = lifeFrame;
    }

    private static final int MAX_PER_WAVE = 28;
    private static final int SPR_BOMB = 0;
    private static final int SPR_TRAILS = 1;
    private static final int SPR_TOTAL = SPR_TRAILS + 2;

    private static final int S_WAITING = 1;     // waiting to appear
    private static final int S_MOVING = 2;      // bomb is on screen
    private static final int S_SPLIT = 3;       // trail left over from split
    private static final int S_DYING = 4;       // trail is erasing

    private static GameObjIterator iter;
    private static Sprite[] sprites;
    private static int activeCount;     // # active bombs

    private static int waveDelay;       // ms until new wave brought on
    private static int wavesRemaining;  // # waves remaining for this level
    private static int waveSpeed;

    // Instance variables

    private Pt loc = new Pt();
    private Pt vel = new Pt();
    private Pt startLoc = new Pt();
    private boolean willSplit;      // true if this bomb will or has split
    private int delay;
    private int lifeSpan;           // # cycles trail will grow
    private int lifeFrame;          // # cycles trail has been growing
    private Bomb splitParent;
    private byte activeChildren;    // # children this bomb path has alive
    private int statusDraw;         // last status drawn
    private int lifeFrameDraw;      // last lifeFrame plotted
}