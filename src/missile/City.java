package missile;
import java.awt.*;
import vgpackage.*;
import mytools.*;

public class City extends Base {

    public static final int TYPE = 0;

    private static final int SPRITE_CITY = 0;
    private static final int SPRITE_DESTROYED = 1;
    private static final int SPRITE_TOTAL = 2;

    private City(int baseNumber) {

        super(baseNumber);
        setType(TYPE);
        height = 40 << VidGame.FRACBITS;
        width = 32 << VidGame.FRACBITS;

        Base.add(baseNumber,this);
//        Missile.baseClass.add(baseNumber, this);
        restore();
    }

    private void restore() {
        setStatus(S_ACTIVE);
    }

//    protected void moveOne() {
/*        if (VidGame.initFlag())
            restore(); */
//    }

    public void drawOne() {//Graphics g, boolean valid) {
        boolean valid = BEngine.layerValid();
//        Graphics g
        int newStatus = getStatus();
        if (drawnStatus != newStatus)
            valid = false;

        if (!valid || redrawFlag) {
            if (!valid)
                clearBgnd();
            redrawFlag = false;
//            Pt p = getLoc();
            drawnStatus = newStatus;
            BEngine.drawSpriteWorld(
                sprites[(newStatus == S_ACTIVE) ? SPRITE_CITY : SPRITE_DESTROYED],
                getLoc() );
        }

    }

    public static void init(SpriteFinder sf, int sIndex) {// combined) {
/*        final short d[] = {
            3,157,76,53,38,50,
            82,157,76,53,38,50,
        };
        */
        sprites = new Sprite[SPRITE_TOTAL];
        for (int i = 0; i < SPRITE_TOTAL; i++) {
            sprites[i] = sf.find(sIndex+i);
/*            int j = i * 6;
            sprites[i] = new Sprite(combined, d[j+0],d[j+1],d[j+2],
                d[j+3],d[j+4],d[j+5]);
            */
        }
        // Place four cities, between the guns.
        for (int i = 1; i <= 5; i ++) {
            if (i != 3) {
                City c = new City(i);
            }
        }
    }

    private static Sprite[] sprites;

    public static void prepare(int type) {
        switch (type) {
        case GAME:
            {
                for (int i = 1; i <= 5; i++) {
                    if (i != 3) {
                        City c = (City)iter.get(i);
                        c.restore();
                    }
                }
                bonusIndex = 0;
            }
            break;

        case LEVEL:
            checkBonusCity();
            break;
        }
    }

    protected void explode() {
        destroy(sprites[SPRITE_CITY]);
    }

    private static final int[] bonusScores = {
        25000,50000,100000,200000
    };

    private static void checkBonusCity() {
        if (bonusIndex == bonusScores.length) return;
        if (VidGame.getScore() < bonusScores[bonusIndex])
            return;
        bonusIndex++;
        if (getActiveCount(TYPE) == 4) return;

        // Find a city to restore.

        final int[] cityNumbers = {1,2,4,5};

        while (true) {
            int i = MyMath.rnd(4);
            City c = (City)iter.get(cityNumbers[i]);
            if (c.getStatus() == S_DESTROYED) {
                c.restore();
                break;
            }
        }
    }
    private static int bonusIndex;

}