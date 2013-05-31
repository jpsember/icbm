package missile;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Bullet extends GameObj {

    private static final int SPEED = VidGame.TICK * 16 * 300;

    private static final int SPR_BULLETS = 13;

    private static final int SPR_AIM = 0;
    private static final int SPR_BULLET = 1;
    private static final int SPR_TOTAL = SPR_BULLET+SPR_BULLETS;

    public static void move() {
        move(iter);
    }
    public static void draw() {//Graphics g, boolean valid) {
        draw(iter);
    }

    /**
     *  Attempts to launch a bullet to a location.
     *  @param dest the location, in world space, to shoot at
     */
    public static void shoot(Pt dest) {
        Bullet b = (Bullet)findVacant(iter);
        if (b == null) return;
        Gun gun = Gun.findLoaded(dest);
        if (gun == null) {
            Sfx.play(Missile.SFX_EMPTY);
            return;
        }
        b.shoot(gun, dest);
    }

    private void shoot(Gun gun, Pt aim) {
        gun.calcBarrelLoc(loc);
        aim.copyTo(dest);

        cycles = 0;

        setStatus(S_MOVING);
        Sfx.play(Missile.SFX_GUN);
    }

    private void calcVelocity() {
        Angle2 a = new Angle2();
        Pt delta = new Pt(dest.x-loc.x, dest.y - loc.y);
        a.calc(delta.x, delta.y);

        Angle2.createRay(SPEED, a, vel);
        // Determine which sprite to plot from the shoot angle.
        int spr = ((a.get() - (149 << Angle2.TRIGBITS)) * 12) /
            (86 << Angle2.TRIGBITS);
        spr = MyMath.clamp(spr, 0, SPR_BULLETS-1);
        spriteIndex = spr;

        cycles = Math.min(VidGame.FPS, delta.magnitude() / SPEED);

    }
    private static GameObjIterator iter;

    public static void init(SpriteFinder sf, int sIndex) {// combined) {

        iter = new GameObjIterator(10);
        while (iter.isRoom())
            iter.store(new Bullet());
        sprites = new Sprite[SPR_TOTAL];
        for (int i = 0; i < SPR_TOTAL; i++) {
            sprites[i] = sf.find(i + sIndex);
        }
    }

    protected void moveOne() {
        if (cycles == 0) {
            calcVelocity();
            if (cycles == 0) {
                setStatus(S_VACANT);
                Blast.add(loc);
                return;
            }
        }
        vel.addTo(loc);
        /*
        if (Blast.testCollision(loc)) {
            status = S_VACANT;
            return;
        }*/
        cycles--;
    }

    protected void drawOne() {//Graphics g, boolean valid) {
        BEngine.drawSpriteWorld( sprites[SPR_BULLET+spriteIndex], loc);
        BEngine.drawSpriteWorld( sprites[SPR_AIM], dest);
    }

    private Pt loc = new Pt();
    private Pt dest = new Pt();
    private Pt vel = new Pt();
    private int cycles = 0;
    private int spriteIndex;

    private static final int S_VACANT = 0;
    private static final int S_MOVING = 1;
    private static Sprite[] sprites;
}