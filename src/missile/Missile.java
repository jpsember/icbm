package missile;
import java.applet.Applet;
import vgpackage.*;
import mytools.*;
import java.awt.*;

public class Missile extends Applet
    implements Runnable, VidGameInt {

    public static final int SCORE_PIX_YM = 34;

    public static final int MAIN_XM = 628 * VidGame.ONE;
    public static final int MAIN_YM = 400 * VidGame.ONE;

	public static final int VIEW_SCORE = 1;
	public static final int VIEW_MAIN = 2;
	public static final int VIEW_STATUS = 3;

	public static final int GS_ROUNDINTRO = 0;
	public static final int GS_PLAYING = 1;
	public static final int GS_ROUNDCOMPLETE = 2;
    public static final int GS_DEAD = 3;

	// ===================================
	// Applet interface
	// ===================================
	public void init() {
		VidGame.doInit(this);
		VidGame.setHighScore(5000);
        VidGame.setCursorType(Cursor.CROSSHAIR_CURSOR);
        BEngine.open();
        {
            Pt p = new Pt(MAIN_XM / VidGame.ONE, MAIN_YM / VidGame.ONE);
			BEngine.defineView(VIEW_MAIN, 0, SCORE_PIX_YM, p.x, p.y);
			BEngine.defineView(VIEW_SCORE, 0, 0, p.x, SCORE_PIX_YM);
		}

        SpriteFinder sf = new SpriteFinder(new Sprite("combined"));

        Base.init();

        Gun.init(sf,S_GUN);
        City.init(sf,S_CITY);
        Target.init(this,sf,S_TARGET);
        Bullet.init(sf,S_BULLET);
        Bomb.init(sf,S_BOMB);
        Blast.init(sf,S_BLAST);
        Bomber.init(this, sf, S_BOMBER);
        Backdrop.init(sf,S_BACKDROP);
		SpriteExp.init();

		charSet1 = new CharSet(sf.find(S_CHARSET),
            15,17,1,1,
			"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ.,!<>^~:%()?");
		charSet1.setSpacingX(-3);

        final int[] scoreLocs = {8,2, (MAIN_XM >> VidGame.FRACBITS)>>1,2};

        scoreboard = new Scoreboard(scoreLocs, charSet1);

        msg = new Msg(this, sf);
	}

	public void start() {
		Sfx.open(sfxNames);

		VidGame.doStart();
	}

	public void run() {
		VidGame.doRun();
	}

	public void stop() {
      VidGame.doStop();
      Sfx.close();
	}

	public void destroy() {
        BEngine.close();
		VidGame.doDestroy();
	}

	// ===================================
	// VidGameInt interface
	// ===================================
	public void processLogic() {
		updateStage();
        Target.move();
        Bullet.move();
        Bomb.move();
        Base.move();
        Blast.move();
        Bomber.move();
        Gun.move();
        City.move();
		SpriteExp.move();
        Backdrop.move();
	}
	// ===================================
	private void updateStage() {
        //db.pr("updateStage, stage="+stage+", time="
        // +stageTime+" VGMode="+VidGame.getMode()+", time="
        // +VidGame.getTime());

		if (VidGame.initFlag()) {
            VidGame.initStage(GS_ROUNDINTRO);
            if (false && VidGame.DEBUG)
                VidGame.setLevel(20);
            Bomb.prepare(GameObj.GAME);
            Bomber.prepare(GameObj.GAME);
            Gun.prepare(GameObj.GAME);
            City.prepare(GameObj.GAME);
            Blast.prepare(GameObj.GAME);
		}

        int stage = VidGame.getStage();
        int stageTime = VidGame.getStageTime();

		switch (stage) {
         case GS_ROUNDINTRO:
		 	if (stageTime == 0) {
                if (VidGame.getMode() == VidGame.MODE_PREGAME)
                    VidGame.setLevel(MyMath.rnd(4));

                Bomber.prepare(GameObj.LEVEL);  // Must be called before Bomb.prepare
                Bomb.prepare(GameObj.LEVEL);
                Sfx.play(SFX_LEVEL);
                Gun.prepare(GameObj.LEVEL);
                City.prepare(GameObj.LEVEL);
			}

		 	if (stageTime > 2500 || (stageTime > 100 && VidGame.getMode() == VidGame.MODE_PREGAME))
				VidGame.setStage(GS_PLAYING);
            break;

         case GS_PLAYING:
			if (
				VidGame.getMode() == VidGame.MODE_PREGAME
			 && stageTime > 20000
			) {
				VidGame.setStage(GS_ROUNDINTRO);
			}

            if (VidGame.getMode() == VidGame.MODE_PLAYING) {
                if (Bomb.levelComplete()
                 && Blast.levelComplete()
                 && Bomber.levelComplete()
                ) {
                    VidGame.setStage(GS_ROUNDCOMPLETE);
                }
                if (Base.getActiveCount(City.TYPE) == 0)
                    VidGame.setStage(GS_DEAD);
            }
            break;

         case GS_ROUNDCOMPLETE:
            if (VidGame.getMode() == VidGame.MODE_PREGAME) {
                VidGame.setStage(GS_ROUNDINTRO);
                break;
            }
            if (msg.roundCompleteFinished()) {
				VidGame.adjLevel(1);
				VidGame.setStage(GS_ROUNDINTRO);
            }
            break;

        case GS_DEAD:
            if (stageTime > 3000 &&
             VidGame.getMode() == VidGame.MODE_PLAYING) {
                VidGame.setMode(VidGame.MODE_GAMEOVER);
            }
		 	if (VidGame.getMode() == VidGame.MODE_PREGAME) {
				VidGame.setStage(GS_PLAYING);
            }
            break;
        }
        VidGame.updateStage();
    }

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
        if (!VidGame.beginPaint()) return;

		// Prepare for update.  Constructs offscreen buffers if required.
		BEngine.prepareUpdate();

		// Process bg layer
        BEngine.openLayer(BEngine.L_BGND);

      // !!! Testing website problems...
      if (false) ; else

		plotBgnd();
        BEngine.closeLayer();

		// Process sprite layer
        BEngine.openLayer(BEngine.L_SPRITE);
        BEngine.erase();
		plotSprites();
		BEngine.closeLayer();
        BEngine.updateScreen(g);
        VidGame.endPaint();
	}

	private void plotBgnd() {
		BEngine.selectView(VIEW_SCORE);
        scoreboard.plotChanges();
		BEngine.selectView(VIEW_MAIN);

        // We update the entire background every frame so the stars
        // will blink properly.  This is not much of a performance hit
        // if it is done only with the background layer.
        BEngine.updateRect(0,0,BEngine.viewR.width,BEngine.viewR.height);
        BEngine.disableUpdate(1);

        if (!BEngine.layerValid())
            BEngine.clearView();

        Backdrop.draw();
        Base.draw();
        Bomb.draw();

        BEngine.disableUpdate(-1);
	}

	private void plotSprites() {
		BEngine.selectView(VIEW_MAIN);

        Target.draw();
        Bullet.draw();
        Bomb.draw();
        Bomber.draw();

        Blast.draw();
		SpriteExp.draw();
        msg.update();
	}

	private final static int SCORE_HEIGHT = 24;
	private final static int STATUS_HEIGHT = 16;
	private final static String sfxNames[] = {
        "blast","blast2","gun","citygone","level","bonus","empty"
	};
    public static final int SFX_BLAST = 0;
    public static final int SFX_BLAST2 = 1;
    public static final int SFX_GUN = 2;
    public static final int SFX_CITYGONE = 3;
    public static final int SFX_LEVEL = 4;
    public static final int SFX_BONUS = 5;
    public static final int SFX_EMPTY = 6;

    public static CharSet charSet0, charSet1;
    private static Scoreboard scoreboard;
    private static Msg msg;

    public static final int S_CHARSET = 0;
    public static final int S_TITLE = S_CHARSET + 1;
    public static final int S_TARGET = S_TITLE + 2;
    public static final int S_BULLET = S_TARGET + 1;
    public static final int S_BOMB = S_BULLET + 14;
    public static final int S_BLAST = S_BOMB + 3;
    public static final int S_GUN = S_BLAST + 28;
    public static final int S_CITY = S_GUN + 5;
    public static final int S_BOMBER = S_CITY + 2;
    public static final int S_BACKDROP = S_BOMBER + 8;
    public static final int S_TOTAL = S_BACKDROP + 6;
}