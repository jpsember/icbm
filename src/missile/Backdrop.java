package missile;
import vgpackage.*;
import mytools.*;
import java.awt.*;

public class Backdrop {

    public static void init(SpriteFinder sf, int sIndex) {
        starsPrepared = false;
        MyColors.init(1);
	    MyColors.add(0, 60,100,240);

        sprites = new Sprite[S_TOTAL];
        for (int i = 0; i < S_TOTAL; i++)
            sprites[i] = sf.find(sIndex+i);
    }

    private static Sprite[] sprites;

    // Plot into background layer
    private static int starIndex;
    public static void draw() {//(Graphics g, boolean valid) {
        Graphics g = BEngine.getGraphics();
        boolean valid = BEngine.layerValid();
        int iMinBright = 13;//9;
        if (!starsPrepared)
            prepareStars();
		int prevBright = -1;
        int brightness = 0;

		int j = starIndex;
        int i = Math.min(40, STARS_TOTAL);
        if (!valid)
            i = STARS_TOTAL;

        while (i-- > 0) {
            j -= 3;
            if (j < 0)
                j += (STARS_TOTAL * 3);

			// Only the last 1/3 of the stars twinkle.
            brightness = iMinBright; //31;//iMinBright;
            if ((j & 3) == 0) { //j > (STARS_TOTAL * 2)) {
				int phase = MyMath.mod(cycle - starLocs[j+2], 2500);
				brightness = 25 - Math.abs(phase - 500) / 32;
				if (brightness < iMinBright)
					brightness = iMinBright;
			}
            if (brightness != prevBright) {
                MyColors.set(g, 0, brightness);
                prevBright = brightness;
            }
            int x = starLocs[j+0];
            int y = starLocs[j+1];
            g.drawLine(x,y,x,y);
		}
        starIndex = j;

        if (!valid)
            plotGround();
    }

    private static void plotGround() {
        Graphics g = BEngine.getGraphics();

        final byte bg[] = {
            2,3,4,3,4,5,
            0,1,0,1,0,1,0,1,0,1,0,
            2,3,4,3,4,5,
            0,1,0,1,0,1,0,1,0,1,0,
            2,3,4,3,4,5 };

        int y = (Missile.MAIN_YM >> VidGame.FRACBITS) - 1;
//        y -= 40;
        for (int i = 0; i < 40; i++) {
            int x = i * 16;
            BEngine.drawSprite(sprites[bg[i]], x, y);
        }
    }

    private static final int STARS_TOTAL = 400;
    private static short[] starLocs;

    private static void prepareStars() {
        starLocs = new short[STARS_TOTAL * 3];
        int j = 0;
        for (int i = 0; i < STARS_TOTAL; i++) {
            starLocs[j+0] = (short)
             (MyMath.rnd(Missile.MAIN_XM >> VidGame.FRACBITS) + BEngine.viewR.x);
            starLocs[j+1] = (short)
             (MyMath.rnd((Missile.MAIN_YM >> VidGame.FRACBITS) - 20) + BEngine.viewR.y);
			starLocs[j+2] = (short)MyMath.rnd(5000);
            j += 3;
        }
        starsPrepared = true;
    }

	public static void move() {
		cycle += (1000 / VidGame.FPS);
	}

    private static boolean starsPrepared;
	protected static int cycle;

    private static final int S_TOTAL = 6;
}