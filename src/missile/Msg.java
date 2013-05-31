package missile;

import java.awt.*;
import vgpackage.*;
import mytools.*;

public class Msg implements IAnimScript {

    public Msg(Missile parent, SpriteFinder sf) {
    //, int gunIndex, int cityIndex) {
        this.parent = parent;
        charSet = parent.charSet1;
        ammoSprite = sf.find(Missile.S_GUN + 2);
        ammo10Sprite = sf.find(Missile.S_GUN + 4);
        //gunIndex+2);
        citySprite = sf.find(Missile.S_CITY);//cityIndex);
        title0Sprite = sf.find(Missile.S_TITLE);
        title1Sprite = sf.find(Missile.S_TITLE+1);
    }

    private Sprite ammoSprite, citySprite, title0Sprite,title1Sprite, ammo10Sprite;

	// ===================================
	// IAnimScript interface
	// ===================================
    private static final int S_LOADING = 0;
    private static final int S_GAMEOVER = 1;
    private static final int S_LEVELINTRO = 2;
    private static final int S_DEMOMSG0 = 3;
    private static final int S_DEMOMSG1 = 4;
    private static final int S_DEMOMSG2 = 5;
    private static final int S_DEMOMSG3 = 6;
    private static final int S_DEMOMSG4 = 7;
    private static final int S_DEMOMSG5 = 8;
    private static final int S_DEMOMSG6 = 9;
    private static final int S_DEMOMSG7 = 10;
    private static final int S_DEMOMSG8 = 11;
    private static final int S_DEMOMSG9 = 12;
    private static final int S_LEVELDONE = 13;
    private static final int S_BONUS0 = 14;
    private static final int S_PAUSED = 15;
    private static final int S_CITYBONUS = 16;
    private static final int S_AMMOBONUS = 17;
    private static final int S_AMMOSPRITE = 18;
    private static final int S_CITYSPRITE = 19;
    private static final int S_ROUNDNUMBER = 20;
    private static final int S_TITLE0 = 21;
    private static final int S_TITLE1 = 22;
    private static final int S_AMMO10SPRITE = 23;

    private static final Object[] strings = {
            "LOADING...",
            "GAME OVER",
            "GET READY",
            "PRESS MOUSE BUTTON",
            "TO START",
            "CONTROLS",
            "MOUSE : MOVES TARGET",
            "BUTTON : LAUNCHES MISSILE",
            "EXTRA CITIES AWARDED AT",
            "25,000 POINTS",
            "50,000 POINTS",
            "100,000 POINTS",
            "200,000 POINTS",

            "ROUND COMPLETED",
            "BONUS",
            "GAME PAUSED",
    };
    public Object getObject(AnimScript script, int id) {
        Object obj = null;
        switch (id) {
        case S_CITYBONUS:
            obj = cityBonus + " X";
            break;

        case S_AMMOBONUS:
            obj = ammoBonus + " X";
            break;

        case S_ROUNDNUMBER:
            obj = "ROUND " + (VidGame.getLevel() + 1);
            break;

        case S_CITYSPRITE:
            obj = citySprite;
            break;

        case S_AMMOSPRITE:
            obj = ammoSprite;
            break;
        case S_AMMO10SPRITE:
            obj = ammo10Sprite;
            break;
        case S_TITLE0:
           obj = title0Sprite;
           break;
        case S_TITLE1:
            obj = title1Sprite;
            break;
        }
        if (obj == null)
            obj = strings[id];
        return obj;
    }

    private static short[][] scripts = {
        {CSTRING,S_LOADING,Y,150,END},

        {SPRITE,S_TITLE0,LOC,314,160,
         SPRITE,S_TITLE1,LOC,314,220,START,80,END},

        {CSTRING,S_GAMEOVER,Y,150,END},

        {CSTRING,S_LEVELINTRO,Y,150,START,60,
         CSTRING,S_ROUNDNUMBER,Y,175,START,120,END},

        {CSTRING,S_DEMOMSG0,Y,170,
         CSTRING,S_DEMOMSG1,Y,190,END},

        {CSTRING,S_DEMOMSG2,Y,150,
         CSTRING,S_DEMOMSG3,Y,175,START,100,
         CSTRING,S_DEMOMSG4,Y,195,START,200, END},

        {CSTRING,S_DEMOMSG5,Y,140,
         CSTRING,S_DEMOMSG6,Y,160,START,100,
         CSTRING,S_DEMOMSG7,Y,180,START,200,
         CSTRING,S_DEMOMSG8,Y,200,START,300,
         CSTRING,S_DEMOMSG9,Y,220,START,400, END},

    };

    private int ammoBonus, cityBonus;
    private int completeLevelTime;

    private short[] getMsgScript(int msg) {
        if (msg < MSG_LEVELCOMPLETE)
            return scripts[msg];

        final int[] cityBonuses = {
            200,300,500,750,1000,1200,1500,2000,2500,3000};

        ammoBonus = Math.min(VidGame.getLevel()+1,10) * 10;
        cityBonus = cityBonuses[Math.min(VidGame.getLevel(),10-1)];

        // Construct a script for MSG_LEVELCOMPLETE.
        // It should include the appropriate strings for the bonus
        // points, and the correct number of sprites.

        AnimScriptBuilder ab = new AnimScriptBuilder();

        final short[][] scripts = {
            {CSTRING,S_LEVELDONE,Y,140},
            {CSTRING,S_BONUS0,Y,160},
            {STRING,S_AMMOBONUS,LOC,140,200},
            {STRING,S_CITYBONUS,LOC,120,260},
        };

        int time = 0;
        ab.add(scripts[0],time);
        time += 100;

        ab.add(scripts[1],time);
        time += 50;

        int bonusBullets = Gun.getAmmoTotal();
//        db.pr("bonusBullets is "+bonusBullets);
        int time2 = time;
        if (bonusBullets > 0) {
            time2 += 30;
            ab.add(scripts[2],time);
            time += 30;

            short[] ammo = new short[7];
            ammo[0] = SPRITE;
            ammo[2] = LOC;
            ammo[4] = 200;
            ammo[5] = SIGNAL;
//            ammo[6] = S_AMMOSPRITE;

            int x = 250;
            while (bonusBullets > 0) {
                ammo[3] = (short)x;
                if (bonusBullets >= 10) {
                    bonusBullets -= 10;
                    ammo[1] = S_AMMO10SPRITE;
                    ammo[6] = S_AMMO10SPRITE;
                    x += 12;
                    ab.add(ammo, time);
                } else {
                    bonusBullets -= 1;
                    ammo[1] = S_AMMOSPRITE;
                    ammo[6] = S_AMMOSPRITE;
                    x += 6;
                    ab.add(ammo, time);
                }
                time += 20;
            }
            time += 50;
        }

        int bonusCities = Base.getActiveCount(City.TYPE);
        {
            ab.add(scripts[3],time2);
            time2 += 30;

            short[] scr = new short[7];
            scr[0] = SPRITE;
            scr[1] = S_CITYSPRITE;
            scr[2] = LOC;
            scr[4] = 260;
            scr[5] = SIGNAL;
            scr[6] = S_CITYSPRITE;

            for (int i = 0; i < bonusCities; i++) {
                scr[3] = (short)(250 + i * 78);
                ab.add(scr, time2);
                time2 += 30;
            }
            time2 += 50;
        }
        if (time2 > time)
            time = time2;
        completeLevelTime = (time * 10) + 2000;

        return ab.getScript();
    }

    private static final int MSG_LOADING = 0;
    private static final int MSG_TITLE = 1;
    private static final int MSG_GAMEOVER = 2;
    private static final int MSG_LEVELINTRO = 3;
    private static final int MSG_TOSTART = 4;
    private static final int MSG_CONTROLS = 5;
    private static final int MSG_BONUSCITIES = 6;
    private static final int MSG_LEVELCOMPLETE = 7;

    public void update(/*Graphics g, boolean valid*/) {
        boolean valid = BEngine.layerValid();
        Graphics g = BEngine.getGraphics();

        if (!valid) {
            prevScript = -1;
            animScript = null;
            pauseScript = null;
        }

        if (VidGame.paused()) {
            final short[] scr = {
                CSTRING,S_PAUSED,END
            };
            if (pauseScript == null)
                pauseScript = new AnimScript(scr,charSet,this);
            pauseScript.update();//g,valid);
            return;
        } else if (pauseScript != null) {
            pauseScript.stop();//g,valid);
            pauseScript = null;
        }

        int msg = -1;

        switch (VidGame.getMode()) {
        case VidGame.MODE_GAMEOVER:
            msg = MSG_GAMEOVER;
            loadedTime = (int)VidGame.getSystemTime(); // So we jump to title.
            break;

        case VidGame.MODE_PREGAME:
            msg = MSG_TITLE;
			int time = (((int)VidGame.getSystemTime()) - loadedTime) % 18000;
            if (time > 4000) {
                msg = MSG_TOSTART;
                if (time > 7000)
                    msg = MSG_CONTROLS;
                if (time > 11500)
                    msg = MSG_BONUSCITIES;
            }
            if (VidGame.loading()) {
                loadedTime = (int)VidGame.getSystemTime();
                msg = MSG_LOADING;
            }
            break;

        case VidGame.MODE_PLAYING:
            switch (VidGame.getStage()) {
            case Missile.GS_ROUNDINTRO:
                msg = MSG_LEVELINTRO;
                break;

            case Missile.GS_ROUNDCOMPLETE:
                msg = MSG_LEVELCOMPLETE;
                break;
            }
            break;
        }

//        if (VidGame.DEBUG && msg < 0) msg = MSG_LEVELCOMPLETE;

        if (msg != prevScript) {
            completeLevelTime = 0;

            if (animScript != null) {
                animScript.stop();//g,valid);
                animScript = null;
                prevScript = -1;
            }

            if (msg >= 0) {
                prevScript = msg;
                animScript = new AnimScript(
                    getMsgScript(msg),charSet, this);

            }
        }

        if (animScript != null) {
            animScript.update();//g, valid);

            // Process any signals.

            while (true) {
                int signal = animScript.readSignal();
                if (signal == 0) break;
                switch (signal) {
                case S_AMMOSPRITE:
                    VidGame.adjScore(ammoBonus);
                    break;
                case S_AMMO10SPRITE:
                    VidGame.adjScore(ammoBonus*10);
                    break;
                case S_CITYSPRITE:
                    VidGame.adjScore(cityBonus);
                    Sfx.play(Missile.SFX_BONUS);
                    break;
                }
            }

        }
      }

    public boolean roundCompleteFinished() {
        return (completeLevelTime != 0
         && VidGame.getStageTime() >= completeLevelTime);

    }

    private Missile parent;
    private AnimScript animScript;
    private AnimScript pauseScript;
    private int prevScript = -1;
    private int loadedTime;
    private CharSet charSet;
}