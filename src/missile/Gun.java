package missile;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Gun extends Base {

    public static final int TYPE = 1;
    private static final int AMMO_MAX = 25;

    private static final int SPRITE_GUN = 0;
    private static final int SPRITE_DESTROYED = 1;
    private static final int SPRITE_AMMO = 2;
    private static final int SPRITE_EMPTY = 3;
    private static final int SPRITE_TOTAL = 4;

    public static void move() {
        updateReload();
    }

//    private Gun() {
//    }

    private Gun(int baseNumber) {

        super(baseNumber);
//        loc.y -= 12 << VidGame.FRACBITS;
        drawnAmmo = -1;
        setType(TYPE);
//        height = 33 << VidGame.FRACBITS;
        height = 38 << VidGame.FRACBITS;
        width = 32 << VidGame.FRACBITS;
//        if (VidGame.DEBUG)
//            height = 44 << VidGame.FRACBITS;

        Base.add(baseNumber, this);

        restore();
    }

    private void restore() {
        setAmmo(AMMO_MAX);
        setStatus(S_ACTIVE);
    }
/*  This shouldn't be necessary.
    protected void moveOne() {
        if (VidGame.initFlag())
            restore();
    }
*/
    private static int reloadAccum;
    private static int reloadGun;
    private static void updateReload() {
        if (reloadCount > 0) {
            reloadAccum += VidGame.CYCLE * 8;
            if (reloadAccum > 1000) {
                reloadAccum -= 1000;
                reloadCount--;
                reloadGun = (reloadGun + 3);
                if (reloadGun > 6)
                    reloadGun = 0;
                int rFlag = 1 << reloadGun;

                if ((reloadFullFlags & rFlag) != 0) return;

                Gun g = (Gun)Base.iter.get(reloadGun);
                if (g.getStatus() == S_ACTIVE
                 && g.ammo < AMMO_MAX) {
                    g.ammo++;
                } else
                    reloadFullFlags |= rFlag;
            }
        }
    }

    public static int getAmmoTotal() {
        int total = 0;
        for (int i = 0; i <= 6; i += 3) {
            Gun g = (Gun)Base.iter.get(i);
            //)Base.iter.get(i);
            if (g.getStatus() == S_ACTIVE)
                total += g.ammo;
        }
        return total;
    }

    private void setAmmo(int n) {
        ammo = n;
    }

    protected void drawOne() {//Graphics g, boolean valid) {
        int newStatus = getStatus();
        boolean valid = BEngine.layerValid();
        if (drawnStatus != newStatus)
            valid = false;

        if (!valid || redrawFlag) {

            if (!valid)
                clearBgnd();

            redrawFlag = false;
            Pt p = getLoc();
            drawnStatus = newStatus;
            BEngine.drawSpriteWorld(
                sprites[(newStatus == S_ACTIVE) ? SPRITE_GUN : SPRITE_DESTROYED],
                p);
            drawnAmmo = -1;
        }

        if (newStatus == S_ACTIVE && drawnAmmo != ammo) {
            int a0 = 0;
            int a1 = AMMO_MAX;
            if (drawnAmmo >= 0) {
                a0 = Math.min(drawnAmmo, ammo);
                a1 = Math.max(drawnAmmo, ammo);
            }
            Pt p = getLoc();

            int x = 0;
            int y = -1;
            int rowSize = -1;
            int rowIndex = -1;
            for (int i = 0; i < a1; i++) {
                x++;
                if (rowIndex == rowSize) {
                    rowIndex = 0;
                    rowSize += 2;
                    y++;
                    x = -rowSize / 2;
                }
                rowIndex++;

                if (i < a0) continue;
//                Pt plotLoc = new Pt(p.x +
//                    x * (VidGame.ONE * 5),
//                    p.y - VidGame.ONE * 26 + y * (VidGame.ONE*5));

                BEngine.drawSpriteWorld( sprites[
                    (i < ammo) ? SPRITE_AMMO : SPRITE_EMPTY],
                    p.x + x * (VidGame.ONE * 5),
                    p.y - VidGame.ONE * 26 + y * (VidGame.ONE*5) );
            }
            drawnAmmo = ammo;
        }

    }

    public static void init(SpriteFinder sf, int sprIndex) {// combined) {
/*        final short d[] = {
            1,1,76,43,38,37,
            21,105,76,43,38,37,
            78,33,7,8,3,4,
            86,33,7,8,3,4,
        };
    */
        sprites = new Sprite[SPRITE_TOTAL];
        for (int i = 0; i < SPRITE_TOTAL; i++) {
            sprites[i] = sf.find(i + sprIndex);
/*            int j = i * 6;
            sprites[i] = new Sprite(combined, d[j+0],d[j+1],d[j+2],
                d[j+3],d[j+4],d[j+5]);
            */
        }
        // Place three guns.
        for (int i = 0; i <= 6; i += 3) {
            Gun g = new Gun(i);
        }
    }

    private static Sprite[] sprites;

    public static void prepare(int type) {
        switch (type) {
        case GAME:
            for (int i = 0; i <= 6; i += 3) {
                Gun g = (Gun)Base.iter.get(i);
                g.restore();
            }
//            newCityScoreIndex = 0;
            break;
        case LEVEL:
            startReload();

//                Gun.startReload();
            break;
        }
    }

    /**
     * Finds the closest loaded gun to a target.  Subtracts one bullet
     * from the gun's ammo store.
     * @param aim location being aimed at in world coordinates
     */
    public static Gun findLoaded(Pt aim) {
        int smallestDistance = 0;
        Gun closestGun = null;
//        BaseClass bc = Missile.baseClass;
        Base.iter.toFirst();
        while (Base.iter.isNext()) {
            Base b = (Base)Base.iter.getNext();
            //))iter.getNext();
            if (b == null) continue;
            if (b.getType() != TYPE) continue;
            Gun g = (Gun)b;
            if (g.getStatus() != S_ACTIVE || g.ammo == 0) continue;

            Pt pt = g.getLoc();
            int dist = Pt.magnitude(aim.x - pt.x, aim.y - pt.y);
            if (closestGun == null || smallestDistance > dist) {
                smallestDistance = dist;
                closestGun = g;
            }
        }
        if (closestGun != null) {
            closestGun.ammo--;
         }
        return closestGun;
    }

    protected void explode() {
        destroy(sprites[SPRITE_GUN]);
   }

    /** Calculates the location of the gun barrel.  This is where a bullet
     *  first appears.
     *  @param pt world coordinates stored here
     */
    public void calcBarrelLoc(Pt pt) {
        Pt p = getLoc();
        pt.set(p.x, p.y - VidGame.ONE * 30);
    }

    private static int reloadCount;
    private static int reloadFullFlags;
    public static void startReload() {
        reloadCount = Math.max(50, (3*AMMO_MAX) - VidGame.getLevel() * 2);
        reloadFullFlags = 0;

        // See about restoring some guns.

        int guns = getActiveCount(TYPE);
        int cities = Math.min(3,getActiveCount(City.TYPE));

//        db.pr("guns="+guns+", cities="+cities);

        while (guns < cities) {
            // Find a destroyed gun that is beside an active city.
            // Look for an active city.
            for (int i = 1; i <= 5; i++) {
                if (i == 3) continue;
                Base b = (Base)iter.get(i);
                if (b.getStatus() == S_DESTROYED) continue;
//                db.pr(" looking for destroyed gun next to city "+i);

                Gun bestG = null;

                for (int j = 0; j <= 6; j += 3) {

//                for (int j = i-1; j <= i+1; j+=2) {
                    Gun g = (Gun)Base.iter.get(j);
                    if (g.getStatus() != S_DESTROYED) continue;
//                    if (b2.getType() != TYPE) continue;
//                    Gun g = (Gun)b2;

                    if (bestG == null)
                        bestG = g;
                    if (Math.abs(j-i) <= 1) {
                        bestG = g;
                        break;
                    }
                }
                bestG.restore();
                bestG.ammo = 0;
                guns++;
                break;
            }
        }
    }

    private int drawnAmmo;
    private int ammo;

}