package missile;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Target extends GameObj {

    public static void draw() {
        draw(iter);
    }
    private static final int SPEED = VidGame.TICK * 16 * 900;
    private static final int S_ACTIVE = 1;

    private Target() {
        loc = new Pt(Missile.MAIN_XM / 2, Missile.MAIN_YM / 2);
        seek = new Pt(loc);
        setStatus(S_ACTIVE);
    }

    private static GameObjIterator iter;

    private static Missile parent;

    public static void init(Missile p, SpriteFinder sf, int sprIndex) {
        parent = p;
        sprite = sf.find(sprIndex);
        iter = new GameObjIterator(1);
        iter.store(new Target());
    }

    public static void move() {
        move(iter);
    }

    protected void drawOne() {
        if (VidGame.getMode() != VidGame.MODE_PLAYING
         || VidGame.getStage() == Missile.GS_DEAD
         || Base.getActiveCount(Gun.TYPE) == 0
        ) return;
        BEngine.drawSpriteWorld(sprite,loc);
    }

    private static final int MARGIN = VidGame.ONE * 10;
    private static final int MARGIN_BOTTOM = VidGame.ONE * 90;

    protected void moveOne() {
        if (VidGame.getMode() != VidGame.MODE_PLAYING) return;

        // Get mouse position, in world coordinates, relative to the main
        // view.
        Point pt = BEngine.getMousePoint(Missile.VIEW_MAIN);

        // Within main window?

        Rectangle r = BEngine.getViewWorldRect(Missile.VIEW_MAIN);
        if (r.contains(pt)) {
            // Force into range of a bounding rectangle.
            seek.x = MyMath.clamp(pt.x, MARGIN, Missile.MAIN_XM - MARGIN);
            seek.y = MyMath.clamp(pt.y, MARGIN, Missile.MAIN_YM - MARGIN_BOTTOM);
        }

        // Make the target location approach this value.
        Pt diff = new Pt(seek.x - loc.x, seek.y - loc.y);
        diff.setMax(SPEED);
        diff.addTo(loc);

        // Was fire button pressed?
        if (VidGame.getJoystick().fireButtonClicked(0)
         && VidGame.getStage() == Missile.GS_PLAYING
        )
            Bullet.shoot(loc);
    }

    private Pt seek;
    private Pt loc = new Pt();
    private static Sprite sprite;
}
