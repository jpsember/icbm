package missile;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Bomber extends GameObj {

    public static void draw() {
        draw(iter);
    }

    public static void init(Missile a, SpriteFinder sf, int sIndex) {
        parent = a;
        sprites = new Sprite[SPRITE_TOTAL];
        for (int i = 0; i < SPRITE_TOTAL; i++)
            sprites[i] = sf.find(i + sIndex);
        activeTotal = 0;

        iter = new GameObjIterator(3);
        while (iter.isRoom())
            iter.store(new Bomber());
    }

    public static void prepare(int type) {
        switch (type) {
        case GAME:
            iter.toFirst();
            while (iter.isNext()) {
                Bomber b = (Bomber)iter.getNext();
                if (b.getStatus() != S_VACANT) {
                    b.setStatus(S_VACANT);
                    activeTotal--;
                }
            }
            break;
        case LEVEL:
            prepareWave();
            break;
        }
    }

    public static boolean levelComplete() {
        return (activeTotal == 0 && (!bomberWave || bombersRemaining == 0));
    }

    public static void move() {
        if (VidGame.getStage() == Missile.GS_ROUNDCOMPLETE)
            bombersRemaining = 0;

        if (VidGame.getStage() == Missile.GS_PLAYING) {
            bringOnDelay -= VidGame.CYCLE;
            if (bringOnDelay <= 0) {
                bringOnDelay = 0;
                if (bombersRemaining > 0) {
                    Bomber b = (Bomber)findVacant(iter);
                    if (b != null) {
                        b.bringOn();
                        bombersRemaining--;
                        setBringOnDelay();
                    }
                }
            }
        }

        move(iter);
    }

    public static boolean isBomberWave() {
        return bomberWave;
    }

    public static void prepareWave() {
        bomberWave = false;
        if (VidGame.getMode() == VidGame.MODE_PREGAME) {
            bombersRemaining = 0;
            return;
        }
        int level = VidGame.getLevel();

        bombersRemaining = 0;
        bringOnDelay = 0;
        if (level >= 2) {
            if ((level + 1) % 5 == 0) {
                bomberWave = true;
                bombersRemaining = MyMath.clamp((level * 350) >> 8, 5, 15);
            } else
                bombersRemaining = MyMath.rnd(3) + 1;
            setBringOnDelay();
        }
    }

    private static void setBringOnDelay() {
        int level = VidGame.getLevel();
        int delay = Math.max(3000, 10000 - level * 1000);
        if (bomberWave)
            delay >>= 2;
        bringOnDelay = MyMath.rnd(delay) + delay;
        if (VidGame.DEBUG && false)
            bringOnDelay = 500;
    }

    private static int bombersRemaining;
    private static int bringOnDelay;
    private static GameObjIterator iter;
    private static Missile parent;

    protected static final int S_ACTIVE = 1;
    protected static int activeTotal;
    private static Sprite[] sprites;
    private static final int TYPES = 2;
    private static final int SPRITE_BOMBERS = 0;
    private static final int SPRITE_EXHAUST = (SPRITE_BOMBERS + TYPES*2);
    private static final int SPRITE_TOTAL = SPRITE_EXHAUST + (2*2);
    private static int[] xDir = {1,-1};
    private static final int SPEED = VidGame.TICK * 800;
    private static final int YRANGE = Missile.MAIN_YM / 3;
    private static final int YSTART = 10 << VidGame.FRACBITS;
    private static final int XMARGIN = 40 << VidGame.FRACBITS;
    private static int[] startX = {-XMARGIN, Missile.MAIN_XM + XMARGIN};

    // Instance variables
    private int type;
    private int direction;  // 0:to right 1:to left
    private Pt vel = new Pt();
    private int dropDelay;
    private int animFrame;

    protected void drawOne() {
        BEngine.drawSpriteWorld(sprites[SPRITE_BOMBERS + (type << 1) + direction], loc);
        int flame = (animFrame >> 7) & 1;
        BEngine.drawSpriteWorld(sprites[SPRITE_EXHAUST + (flame << 1) + direction], loc);
    }

    private static final int[] speeds = {
        VidGame.TICK * 800,
        VidGame.TICK * 1400};

    protected void bringOn() {
        int level = Math.min(VidGame.getLevel(), 10);

        direction = MyMath.rnd(2);
        loc.set(startX[direction],MyMath.rnd(YRANGE) + YSTART);
        setStatus(S_ACTIVE);
        activeTotal++;
        type = 0;
        if (MyMath.rnd(2 + level * 25) > 100)
            type = 1;
        int speed = speeds[type];
        if (bomberWave)
            speed += (speed / 3);
        speed = (MyMath.rnd(speed >> 2) + speed) * xDir[direction];

        vel.set(speed, MyMath.rndCtr(VidGame.TICK * 80));

        setDropDelay();
        dropDelay += 1000;

        // This sound effect is causing a lot of jerkiness in the
        // frame rate.  Something about how it was encoded?
//        Sfx.play(Missile.SFX_BOMBER);
    }

    private void setDropDelay() {
        int level = VidGame.getLevel();
        int delay = Math.max(1200, 2500 - level * 200);
        if (type == 1 && !bomberWave)
            delay >>= 1;

        dropDelay = MyMath.rnd(delay) + delay;
    }

    protected void moveOne() {
        final int COLOFFSET = (16 << VidGame.FRACBITS);
        animFrame += VidGame.CYCLE;
        vel.addTo(loc);
        if (loc.x < startX[0] || loc.x > startX[1]) {
            setStatus(S_VACANT);
            activeTotal--;
            return;
        }
        if (Blast.testCollision(loc.x - COLOFFSET, loc.y, 0)
         || Blast.testCollision(loc.x + COLOFFSET, loc.y, 0)
        ) {
            SpriteExp.add(
                sprites[(type << 1) + direction],
                loc,
                500, 0, 15,
                10, 25, vel);
            setStatus(S_VACANT);
            activeTotal--;
            final int[] scores = {250,1000};
            VidGame.adjScore(scores[type]);
            return;
        }

        dropDelay -= VidGame.CYCLE;
        if (dropDelay <= 0) {
            setDropDelay();
            Bomb.drop(new Pt(loc.x, loc.y + (VidGame.ONE * 6)), vel.x >> 3);
        }
    }

    private Pt loc = new Pt();
    private static boolean bomberWave;

}

