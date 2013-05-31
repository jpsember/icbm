package missile;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Blast extends GameObj {

    private static GameObjIterator iter;
    private static boolean levelCompleteFlag;

    // Determine if all bombs have stopped being detonated.
    public static boolean levelComplete() {
        return levelCompleteFlag;
    }

    public static void init(SpriteFinder sf, int sIndex) {// combined) {
        sprites = new Sprite[SPRITE_TOTAL];

        iter = new GameObjIterator(20);
        while (iter.isRoom())
            iter.store(new Blast());

        for (int i = 0; i < SPRITE_TOTAL; i++) {
            sprites[i] = sf.find(i + sIndex);
        }
    }

    protected void drawOne() {//Graphics g, boolean valid) {
        // If radius is undefined, we haven't moved this one yet;
        // leave it until the next cycle.
        if (radius == 0) return;
        int s = ((radius >> VidGame.FRACBITS) - MIN_RADIUS);
        Graphics g = BEngine.getGraphics();
        if (color == 1)
            g.setXORMode(Color.black);
        BEngine.drawSpriteWorld( sprites[s], loc);
        if (color == 1)
            g.setPaintMode();
    }
//    public static void plotAll(Graphics g, boolean valid) {}
//    protected void plot(Graphics g, boolean valid){}

    /**
     * Test if a point has intersected any blasts.
     * @param x world x coordinate
     * @param y world y coordinate
     * @param blastRadius the minimum radius of the blast required before
     *  any explosions register
     */
    public static boolean testCollision(int x, int y, int blastRadius) {
        iter.toFirst();
        while (iter.isNext()) {
            Blast b = (Blast)iter.getNext();
            if (b.getStatus() == S_VACANT) continue;
            if (b.radius < blastRadius) continue;
            if (!b.bounds.contains(x,y)) continue;
//            db.pr("testCollision loc="+db.ptStringScaled(loc)+", b.loc="+db.ptStringScaled(b.loc)
//            +", bounds="+db.rStringScaled(b.bounds));
            int dx = ((x - b.loc.x) >> VidGame.FRACBITS);
            int dy = ((y - b.loc.y) >> VidGame.FRACBITS);
            int testRadius = dx * dx + dy * dy;

//            db.pr(" testRadius ="+testRadius+", b.rSquared="+b.rSquared);
            if (testRadius > b.rSquared) continue;
//            b.status = S_VACANT;
            return true;
        }
        return false;
    }

    public static void add(Pt loc) {
        final int[] sfx = {
            Missile.SFX_BLAST,Missile.SFX_BLAST2 };

        Blast bNew = (Blast)findVacant(iter);

        if (bNew == null) {
            Blast bOldest = null;
            // Find the oldest one to replace.
            iter.toFirst();
            while (iter.isNext()) {
                Blast b = (Blast)iter.getNext();
                if (bOldest == null || b.age > bOldest.age)
                    bOldest = b;
            }
            bNew = bOldest;

        }

        bNew.setStatus(S_ACTIVE);
        bNew.radius = 0;    // So we are sure to recalculate bounding rect
        loc.copyTo(bNew.loc);
        bNew.age = 0;
        bNew.ageSpeed = VidGame.CYCLE + MyMath.rndCtr(VidGame.CYCLE >> 1);
        bNew.color = (byte)(MyMath.rnd(3) == 0 ? 1 : 0);

        // Avoid playing the same blast effect < .2 seconds apart.
        Sfx.play(sfx[MyMath.rnd(sfx.length)],0,200);
    }

    public static void move() {
        levelCompleteFlag = true;
        move(iter);
    }
    public static void draw() {//Graphics g, boolean valid) {
        draw(iter);//, g, valid);
    }

    protected void moveOne() {
        if (
            loc.y > (Missile.MAIN_YM - (50 << VidGame.FRACBITS))
         && age < (AGE_MAX * 3 / 4))
            levelCompleteFlag = false;

        int s = (age * ANIM_FRAMES) / AGE_MAX;
        if (s >= SPRITE_TOTAL)
            s = (SPRITE_TOTAL * 2 - 2) - s;
        if (s < 0) {
            setStatus(S_VACANT);
            return;
        }

        int newRadius = ((MIN_RADIUS + s) << VidGame.FRACBITS) + (VidGame.ONE >> 1);
        if (radius != newRadius) {
            radius = newRadius;
            int r2 = (radius >> VidGame.FRACBITS);
            rSquared = r2 * r2;
            bounds.setBounds(loc.x - radius, loc.y - radius, radius << 1, radius << 1);
        }

        age += ageSpeed; //VidGame.CYCLE; //+;
    }

    public static void prepare(int type) {
        if (type == GAME) {
            iter.toFirst();
            while (iter.isNext()) {
                Blast b = (Blast)iter.getNext();
                b.setStatus(S_VACANT);
            }
        }
    }

    // Class variables
    private static final int SPRITE_TOTAL = 28;

    // Instance variables
    private int age;
    private int ageSpeed;
    private int radius;
    private Rectangle bounds = new Rectangle();   // rectangular bounds of blast
    private int rSquared;       // square of blast radius
    private byte color;         // 0: normal 1: XOR
    private Pt loc = new Pt();

    private static final int S_ACTIVE = 1;

    private static final int MIN_DIAMETER = 3;
    private static final int MIN_RADIUS = 1;
    private static final int ANIM_FRAMES = SPRITE_TOTAL * 2 - 1;
    private static final int AGE_MAX = 3500; //6000;
    private static Sprite[] sprites;
}