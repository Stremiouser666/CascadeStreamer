package com.zeus.browser.arcade;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Lightweight, self-contained pixel-art cat-versus-dogs arcade game. */
public class ArcadeView extends View {
    // ---- screen / game modes ----
    private static final int MODE_TITLE = 0;
    private static final int MODE_DIFFICULTY = 1;
    private static final int MODE_PLAYING = 2;
    private static final int MODE_NAME_ENTRY = 3;
    private static final int MODE_GAME_OVER = 4;
    private static final int MODE_ALL_LIVES_LOST = 5;

    // ---- difficulty levels ----
    private static final int DIFF_EASY = 0;
    private static final int DIFF_MEDIUM = 1;
    private static final int DIFF_HARD = 2;
    private static final String[] DIFF_LABELS = {"Easy", "Medium", "Hard"};
    private static final String[] DIFF_UPPER = {"EASY", "MEDIUM", "HARD"};
    private static final String[] DIFF_DESCS = {
            "RELAXED DOGS  -  GENTLE PACE",
            "STANDARD SPEED  -  BALANCED PACE",
            "FASTER DOGS  -  BRISK PACE"
    };

    // ---- enemy movement patterns (Wave 4+) ----
    private static final int MOVE_CLASSIC = 0;
    private static final int MOVE_ALTERNATING_ROWS = 1;
    private static final int MOVE_SINE_WEAVE = 2;
    private static final int MOVE_ACCORDION = 3;
    private static final int MOVE_DIVE_BOMBERS = 4;
    private static final int MOVE_SWARM = 5;
    private static final int MOVE_PINCER = 6;
    private static final int MOVE_SPIRAL = 7;
    private static final int MOVE_FORMATION_BREAK = 8;
    private static final int MOVE_ZIGZAG = 9;
    private static final int MOVE_SPIRAL_DIVERS = 10;

    private static final int POWER_MISSILES = 0, POWER_TRIPLE = 1, POWER_PAW_BOMB = 2,
            POWER_FREEZE = 3, POWER_MAGNET = 4, POWER_GOLD_SHIELD = 5,
            POWER_LIGHTNING = 6, POWER_MEGA_LASER = 7, POWER_SCORE_FRENZY = 8,
            POWER_GOLDEN_FISH = 9;

    // ---- Alpha Hound Boss variants ----
    private static final int BOSS_ROTTWEILER = 0; // Boss #1: Spiked Rottweiler Warlord
    private static final int BOSS_BULLDOG = 1;    // Boss #2: Armored Mech-Bulldog
    private static final int BOSS_DOBERMAN = 2;   // Boss #3: Cyber-Doberman Dreadnought

    // ---- effect timings (ms) ----
    private static final long DOG_BLAST_MS = 380, CHARLIE_BLAST_MS = 950,
            MEGA_BLAST_MS = 680,
            CHARLIE_HIT_MS = 1500,      // Charlie cannot lose another life during this time
            CHARLIE_HIDE_MS = 500,      // Charlie is hidden inside the blast for this long
            GAME_OVER_ANIM_MS = 1100,   // length of the GAME OVER dramatic zoom-in
            GAME_OVER_LOCK_MS = 2400,   // cooldown before returning from game over
            ALL_LIVES_LOST_MS = 1900,   // pixelated zoom-in + beat before Top-10 / Game Over
            NAME_ENTRY_LOCK_MS = 450,   // guard against accidental taps as name entry opens
            WAVE_BANNER_MS = 1500,      // wave intro banner duration
            SHAKE_MS = 260,             // screen shake duration
            LB_ZOOM_HOLD_MS = 3000,     // high score zoom-out hold duration on start screen
            LB_ZOOM_TRANS_MS = 420,     // zoom-in / zoom-out transition speed
            SHIELD_DURATION_MS = 15000L,// 15-second shield duration
            OVERCHARGE_DURATION_MS = 12000L, // twin-stream yarn overcharge duration
            GOLDEN_FISH_DURATION_MS = 10000L,
            TRIPLE_PAW_DURATION_MS = 12000L,
            FREEZE_DURATION_MS = 4000L,
            MAGNET_MIN_DURATION_MS = 60000L,
            MAGNET_MAX_DURATION_MS = 90000L,
            REFLECT_DURATION_MS = 8000L,
            SCORE_FRENZY_DURATION_MS = 12000L,
            MEGA_LASER_DURATION_MS = 4000L,
            HIT_STOP_MS = 90L,          // heavy arcade impact freeze on mega blasts & boss hits
            CHEAT_BANNER_MS = 1600L;    // cheat confirmation flash/banner duration
    private static final int[][] TEST_ITEM_SECTIONS = {
            {0, 2, 4},                         // Points & Lives
            {1},                               // Shields
            {5, 7, 8, 9, 14},                  // Weapons
            {3, 6, 10, 11, 12, 13, 15, 16}    // Special Powers
    };
    private static final String[] TEST_ITEM_SECTION_NAMES = {
            "POINTS & LIVES", "SHIELDS", "WEAPONS", "SPECIAL POWERS"
    };
    private static final float BONE_TOP_Y = 0.135f;  // bonus bone flies across the top of the screen
    private static final float MOUSE_TOP_Y = 0.185f; // bonus mouse flies across upper sky

    // ---- cheat sequence: UP, UP, DOWN, DOWN, LEFT, RIGHT ----
    private static final int[] CHEAT_LIVES_SEQ = {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_DOWN,
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT
    };
    private static final int[] CHEAT_SHIELD_SEQ = {
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT
    };
    private static final int[] CHEAT_OVERCHARGE_SEQ = {
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_UP
    };

    // ---- sound (ToneGenerator on a tiny background thread; every failure is ignored) ----
    private static final int MSG_INIT = 1, MSG_TONE = 2, MSG_STOP = 3;
    private static final int PRIO_MARCH = 0, PRIO_FIRE = 1, PRIO_NOTE = 2, PRIO_HIT = 3,
            PRIO_LIFE = 4, PRIO_OVER = 5;
    private static final int TONE_VOLUME = 60; // 0-100
    private static final int[] SND_FIRE = {
            ToneGenerator.TONE_DTMF_3, 26, 28, ToneGenerator.TONE_DTMF_2, 26, 28,
            ToneGenerator.TONE_DTMF_1, 26, 28};
    private static final int[] SND_BOOM = {
            ToneGenerator.TONE_DTMF_P, 35, 35, ToneGenerator.TONE_DTMF_0, 35, 35,
            ToneGenerator.TONE_DTMF_S, 35, 35, ToneGenerator.TONE_DTMF_1, 50, 50};
    private static final int[] SND_MEGA_BOOM = {
            ToneGenerator.TONE_DTMF_P, 60, 65, ToneGenerator.TONE_DTMF_9, 55, 60,
            ToneGenerator.TONE_DTMF_5, 55, 60, ToneGenerator.TONE_DTMF_0, 80, 85};
    private static final int[] SND_ARMOR = {
            ToneGenerator.TONE_DTMF_6, 30, 35, ToneGenerator.TONE_DTMF_9, 30, 35};
    private static final int[] SND_BONE_APPEAR = {
            ToneGenerator.TONE_DTMF_4, 55, 70, ToneGenerator.TONE_DTMF_9, 75, 75};
    private static final int[] SND_BONE_HIT = {
            ToneGenerator.TONE_DTMF_8, 40, 45, ToneGenerator.TONE_DTMF_P, 65, 70};
    private static final int[] SND_BONE = {
            ToneGenerator.TONE_DTMF_1, 55, 65, ToneGenerator.TONE_DTMF_5, 55, 65,
            ToneGenerator.TONE_DTMF_9, 55, 65, ToneGenerator.TONE_DTMF_P, 140, 140};
    private static final int[] SND_SHIELD = {
            ToneGenerator.TONE_DTMF_2, 50, 55, ToneGenerator.TONE_DTMF_6, 50, 55,
            ToneGenerator.TONE_DTMF_A, 75, 80};
    private static final int[] SND_GOLDEN = {
            ToneGenerator.TONE_DTMF_2, 55, 45, ToneGenerator.TONE_DTMF_6, 55, 45,
            ToneGenerator.TONE_DTMF_9, 65, 45, ToneGenerator.TONE_DTMF_P, 170, 160};
    private static final int[] SND_CHEAT = {
            ToneGenerator.TONE_DTMF_1, 45, 50, ToneGenerator.TONE_DTMF_3, 45, 50,
            ToneGenerator.TONE_DTMF_5, 45, 50, ToneGenerator.TONE_DTMF_7, 45, 50,
            ToneGenerator.TONE_DTMF_9, 60, 65, ToneGenerator.TONE_DTMF_P, 150, 150};
    private static final int[] SND_WAVE = {
            ToneGenerator.TONE_DTMF_1, 55, 65, ToneGenerator.TONE_DTMF_3, 55, 65,
            ToneGenerator.TONE_DTMF_6, 85, 90};
    private static final int[] SND_LIFE = {
            ToneGenerator.TONE_DTMF_4, 90, 90, ToneGenerator.TONE_DTMF_1, 90, 90,
            ToneGenerator.TONE_PROP_NACK, 220, 220};
    private static final int[] SND_OVER = {
            ToneGenerator.TONE_DTMF_9, 130, 180, ToneGenerator.TONE_DTMF_6, 130, 180,
            ToneGenerator.TONE_DTMF_3, 130, 180, ToneGenerator.TONE_DTMF_1, 160, 210,
            ToneGenerator.TONE_PROP_NACK, 520, 520};
    private static final int[][] SND_MARCH = {
            {ToneGenerator.TONE_DTMF_S, 55, 60}, {ToneGenerator.TONE_DTMF_7, 55, 60},
            {ToneGenerator.TONE_DTMF_4, 55, 60}, {ToneGenerator.TONE_DTMF_1, 55, 60}};
    private static final String[] DIGITS = {"0", "1", "2", "3", "4", "5"};
    private static final String[] WAVE_NAMES = {
            "BACKYARD PATROL", "CHEVRON PACK", "ALLEY AMBUSH", "KENNEL GUARD"};
    private static final String PREF_HIGH_SCORE = "high_score";
    private static final String PREF_LEADERBOARD = "leaderboard_v1";
    private static final String PREF_SOUND_MUTED = "sound_muted";

    private final Paint p = new Paint(3);
    private final Path earPath = new Path();
    private final Random random = new Random();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Shot> shots = new ArrayList<>();
    private final ArrayList<Shot> dogShots = new ArrayList<>();
    private final ArrayList<Shield> shields = new ArrayList<>();
    private final ArrayList<Blast> blasts = new ArrayList<>();
    private final ArrayList<BoneDrop> boneDrops = new ArrayList<>();
    private final ArrayList<ShieldDrop> shieldDrops = new ArrayList<>();
    private final ArrayList<ParachuteCat> parachuteCats = new ArrayList<>();
    private final ArrayList<PowerDrop> powerDrops = new ArrayList<>();
    private final ArrayList<Popup> popups = new ArrayList<>();
    private final ArrayList<ScoreEntry> leaderboard = new ArrayList<>();
    private final ArrayList<Integer> ricochetedPairIds = new ArrayList<>();
    private int nextShotPairId = 1;
    private final SharedPreferences prefs;
    private final Typeface condensedBold = Typeface.create("sans-serif-condensed", Typeface.BOLD);

    private long lastFrame, lastShot, lastEnemyShot, lastBone, lastMouse, lastDiveAt,
            gameOverAt, allLivesLostAt, nameEntryAt, waveBannerAt, shakeAt, cheatBannerAt,
            hitStopUntil, muzzleFlashUntil, freezeUntil, magnetUntil, goldenFishUntil,
            reflectUntil, scoreFrenzyUntil, megaLaserUntil, nextGoldenShotAt, nextLaserTickAt,
            goldenFishEndFlashUntil;
    private Bitmap pixelBannerBmp;
    private Canvas pixelBannerCanvas;
    private final RectF pixelBannerDst = new RectF();

    private Bitmap lbZoomBmp;
    private Canvas lbZoomCanvas;
    private final RectF lbZoomDst = new RectF();

    // Start screen leaderboard zoom-out state
    private boolean lbZoomActive = false;
    private long lbZoomStartedAt = 0L;
    private long lbZoomClosingAt = 0L;
    private boolean lbZoomClosing = false;

    // TV Quality-of-Life: Pause & Sound Mute
    private boolean paused = false;
    private boolean soundMuted = false;

    // Phone-only touch controls and hidden wave-testing panel.
    private boolean touchMode = false, waveSelectorOpen = false, testRun = false;
    private int selectedTestWave = 1, selectedTestDifficulty = DIFF_MEDIUM;
    private int cornerTapCount = 0;
    private long lastCornerTap = 0L;
    private boolean touchLeft = false, touchRight = false, touchFire = false;
    private boolean testCollectablesEnabled = true, testItemMenuOpen = false;
    private int selectorFocus = 0, pauseCursor = 0, testItemCursor = 0;
    private int testItemSection = 0, testItemOption = 0;
    private int titleDownCount = 0;
    private long titleDownLastAt = 0L;

    // Latest top-10 entry highlight
    private String latestEntryInitials = null;
    private int latestEntryScore = -1;
    private String latestEntryDiff = null;

    private float shakePower = 0f;
    private boolean running = true, left, right, newRecord = false;
    private int mode = MODE_TITLE;
    private int difficulty = DIFF_MEDIUM;
    private int diffCursor = DIFF_MEDIUM;
    private int waveMoveMode = MOVE_CLASSIC;

    // 3-letter top-10 initials entry state
    private final char[] entryLetters = {'A', 'A', 'A'};
    private final boolean[] entryTouched = {false, false, false};
    private int entrySlot = 0;

    private float playerX = 0.5f, playerY, formationDir = 1f;
    private float demoTargetX = 0.5f;
    private long demoNextDecision = 0L;
    private int demoWave = 1;
    private int score = 0, lives = 3, wave = 1, highScore = 0, prevBest = 0;
    private float enemySpeed = 0.000055f, stepDown = 0.025f;
    private long enemyFireInterval = 850L;
    private float enemyShotSpeed = 0.00043f;

    // Active 15-second player shield & Bowtie Yarn Overcharge
    private long shieldUntil = 0L;
    private long overchargeUntil = 0L;
    private long rapidFireUntil = 0L, tripleShotUntil = 0L;
    private int killStreak = 0;
    private long comboUntil = 0L;
    private long formationBreakUntil = 0L;
    private long lastSpecialEnemyMoveAt = 0L;

    // Cheat code tracking (once per wave/level)
    private int cheatIndex = 0, shieldCheatIndex = 0, overchargeCheatIndex = 0;
    private boolean cheatUsedThisWave = false;
    private String cheatMessage = "CHEAT ACTIVATED!";

    // Top flying bonus bone state
    private float boneX = -1f, boneY = BONE_TOP_Y, boneDir = 1f, boneSpeed = 0.00022f, bonePhase = 0f;
    private int boneValue = 200;

    // Bonus mouse state (early on to start, then very rarely)
    private boolean spawnedInitialMouse = false;
    private float mouseX = -1f, mouseY = MOUSE_TOP_Y, mouseDir = 1f, mouseSpeed = 0.00024f, mousePhase = 0f;

    private long hitAt = -1000000L;      // when Charlie was last hit
    private boolean charlieGone = false; // true after the final life is lost to a bullet
    private long lastMarch;
    private int marchStep;
    private final int[] rowColours = {Color.rgb(255, 170, 90), Color.rgb(235, 205, 130),
            Color.rgb(180, 190, 210), Color.rgb(180, 145, 220)};
    private final int navy = Color.rgb(5, 9, 25), blue = Color.rgb(45, 170, 255),
            gold = Color.rgb(255, 205, 80), white = Color.rgb(235, 242, 255),
            coral = Color.rgb(255, 105, 95), mint = Color.rgb(110, 235, 175),
            catBlack = Color.rgb(22, 24, 32), catWhite = Color.rgb(245, 248, 255),
            outlineShadow = Color.rgb(120, 165, 235);

    // audio state (soundUntil/soundPrio are only used from the UI thread; tone only on the audio thread)
    private HandlerThread audioThread;
    private Handler audioHandler;
    private ToneGenerator tone;
    private long soundUntil;
    private int soundPrio;

    private final Handler.Callback audioCallback = new Handler.Callback() {
        @Override public boolean handleMessage(Message m) {
            try {
                if (m.what == MSG_INIT) {
                    if (tone == null) tone = new ToneGenerator(AudioManager.STREAM_MUSIC, TONE_VOLUME);
                } else if (tone != null) {
                    if (m.what == MSG_TONE) tone.startTone(m.arg1, m.arg2);
                    else if (m.what == MSG_STOP) tone.stopTone();
                }
            } catch (RuntimeException e) {
                // audio unavailable: the game simply carries on silently
            }
            return true;
        }
    };

    public ArcadeView(Context c) {
        super(c);
        setFocusable(true);
        prefs = c.getSharedPreferences("charlies_arcade", Context.MODE_PRIVATE);
        soundMuted = prefs.getBoolean(PREF_SOUND_MUTED, false);
        loadLeaderboard();
        highScore = prefs.getInt(PREF_HIGH_SCORE, 0);
        if (!leaderboard.isEmpty()) {
            highScore = Math.max(highScore, leaderboard.get(0).score);
        }
        setupTitleScreen();
    }

    private void loadLeaderboard() {
        leaderboard.clear();
        String raw = prefs.getString(PREF_LEADERBOARD, null);
        if (raw != null && !raw.isEmpty()) {
            String[] items = raw.split(";");
            for (String item : items) {
                String[] parts = item.split("\\|");
                if (parts.length >= 3) {
                    try {
                        int pts = Integer.parseInt(parts[1]);
                        String name = parts[0];
                        String diff = parts[2];
                        leaderboard.add(new ScoreEntry(name, pts, diff));
                    } catch (NumberFormatException ignored) {
                        // skip malformed entry
                    }
                }
            }
        }
        if (leaderboard.isEmpty()) {
            leaderboard.add(new ScoreEntry("C.A.T", 1200, "Medium"));
            leaderboard.add(new ScoreEntry("D.O.G", 900, "Easy"));
            leaderboard.add(new ScoreEntry("B.A.R", 650, "Hard"));
        }
        sortAndTrimLeaderboard();
    }

    private void saveLeaderboard() {
        sortAndTrimLeaderboard();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < leaderboard.size(); i++) {
            ScoreEntry e = leaderboard.get(i);
            if (i > 0) sb.append(';');
            sb.append(e.initials).append('|').append(e.score).append('|').append(e.difficulty);
        }
        prefs.edit()
                .putString(PREF_LEADERBOARD, sb.toString())
                .putInt(PREF_HIGH_SCORE, highScore)
                .apply();
    }

    private void sortAndTrimLeaderboard() {
        Collections.sort(leaderboard, new Comparator<ScoreEntry>() {
            @Override public int compare(ScoreEntry a, ScoreEntry b) {
                return Integer.compare(b.score, a.score);
            }
        });
        while (leaderboard.size() > 10) {
            leaderboard.remove(leaderboard.size() - 1);
        }
    }

    private boolean qualifiesForTop10(int finalScore) {
        if (finalScore <= 0) return false;
        if (leaderboard.size() < 10) return true;
        return finalScore > leaderboard.get(leaderboard.size() - 1).score;
    }

    private String formatInitials(char[] chars, boolean[] touched) {
        char c0 = (touched != null && touched[0]) ? chars[0] : ' ';
        char c1 = (touched != null && touched[1]) ? chars[1] : ' ';
        char c2 = (touched != null && touched[2]) ? chars[2] : ' ';
        return "" + c0 + "." + c1 + "." + c2;
    }

    private String formatOrdinal(int oneBasedRank) {
        if (oneBasedRank == 1) return "1st";
        if (oneBasedRank == 2) return "2nd";
        if (oneBasedRank == 3) return "3rd";
        return oneBasedRank + "th";
    }

    private void resetTransientEntities() {
        shots.clear(); dogShots.clear(); blasts.clear();
        boneDrops.clear(); shieldDrops.clear(); parachuteCats.clear(); powerDrops.clear(); popups.clear();
        ricochetedPairIds.clear();
        boneX = -1f; mouseX = -1f;
        shieldUntil = 0L;
        overchargeUntil = 0L;
        rapidFireUntil = 0L;
        tripleShotUntil = 0L;
        freezeUntil = magnetUntil = goldenFishUntil = reflectUntil = 0L;
        scoreFrenzyUntil = megaLaserUntil = nextGoldenShotAt = nextLaserTickAt = 0L;
        goldenFishEndFlashUntil = 0L;
        killStreak = 0; comboUntil = formationBreakUntil = lastSpecialEnemyMoveAt = 0L;
        hitStopUntil = 0L;
        muzzleFlashUntil = 0L;
        paused = false;
        cheatIndex = 0;
        shieldCheatIndex = 0;
        overchargeCheatIndex = 0;
        cheatUsedThisWave = false;
        cheatBannerAt = 0L;
        cheatMessage = "CHEAT ACTIVATED!";
        lbZoomActive = false;
        lbZoomClosing = false;
        lbZoomStartedAt = 0L;
        lbZoomClosingAt = 0L;
    }

    private void setupTitleScreen() {
        score = 0; lives = 3; wave = 1; demoWave = 1;
        mode = MODE_TITLE;
        newRecord = false;
        prevBest = highScore;
        left = false; right = false;
        playerX = .5f; demoTargetX = .5f; demoNextDecision = 0L;
        resetTransientEntities();
        hitAt = -1000000L; charlieGone = false; lastMarch = 0; marchStep = 0;
        gameOverAt = 0; allLivesLostAt = 0; nameEntryAt = 0; waveBannerAt = 0; shakeAt = 0;
        long now = SystemClock.uptimeMillis();
        lastBone = now - 5000L;
        lastMouse = now - 16000L;
        lastDiveAt = now;
        spawnedInitialMouse = false;
        stopSounds();
        makeWave(false);
    }

    private void startRunWithDifficulty(int chosenDiff) {
        difficulty = chosenDiff;
        testRun = false;
        testItemMenuOpen = false;
        pauseCursor = 0;
        score = 0; lives = startingLivesForDifficulty(chosenDiff); wave = 1;
        mode = MODE_PLAYING;
        newRecord = false;
        prevBest = highScore;
        left = false; right = false;
        playerX = .5f;
        resetTransientEntities();
        hitAt = -1000000L; charlieGone = false; lastMarch = 0; marchStep = 0;
        gameOverAt = 0; allLivesLostAt = 0; nameEntryAt = 0; shakeAt = 0;
        stopSounds();
        long now = SystemClock.uptimeMillis();
        lastBone = now - 3500L;
        lastMouse = now - 15000L;
        lastDiveAt = now;
        spawnedInitialMouse = false;
        makeWave(true);
        waveBannerAt = now;
        playSound(PRIO_NOTE, SND_WAVE);
    }

    // Starting lives give each difficulty a clear, beginner-friendly identity.
    private int startingLivesForDifficulty(int chosenDiff) {
        if (chosenDiff == DIFF_EASY) return 5;
        if (chosenDiff == DIFF_HARD) return 3;
        return 4;
    }

    // ~25-30% easier on Easy compared to prior tuning; Medium and Hard untouched
    private float diffSpeedMult() {
        if (difficulty == DIFF_EASY) return 0.52f;
        if (difficulty == DIFF_HARD) return 1.18f;
        return 1.0f;
    }

    private float diffFireIntervalMult() {
        if (difficulty == DIFF_EASY) return 1.62f;
        if (difficulty == DIFF_HARD) return 0.82f;
        return 1.0f;
    }

    private float diffShotSpeedMult() {
        if (difficulty == DIFF_EASY) return 0.58f;
        if (difficulty == DIFF_HARD) return 1.16f;
        return 1.0f;
    }

    private void makeWave(boolean announce) {
        enemies.clear();
        shields.clear();
        dogShots.clear();
        parachuteCats.clear();
        powerDrops.clear();
        lastSpecialEnemyMoveAt = SystemClock.uptimeMillis();
        formationBreakUntil = 0L;
        boneX = -1f;
        mouseX = -1f;
        cheatIndex = 0;
        shieldCheatIndex = 0;
        overchargeCheatIndex = 0;
        cheatUsedThisWave = false;

        if (wave >= 4 && wave % 5 != 0 && wave % 4 == 0) {
            // Every fourth non-boss wave is a clearly signposted special challenge.
            waveMoveMode = MOVE_DIVE_BOMBERS;
        } else if (wave >= 4) {
            int[] pool = {
                    MOVE_ALTERNATING_ROWS,
                    MOVE_SINE_WEAVE,
                    MOVE_ACCORDION,
                    MOVE_DIVE_BOMBERS,
                    MOVE_SWARM,
                    MOVE_PINCER,
                    MOVE_SPIRAL,
                    MOVE_FORMATION_BREAK,
                    MOVE_ZIGZAG,
                    MOVE_SPIRAL_DIVERS
            };
            waveMoveMode = pool[random.nextInt(pool.length)];
        } else {
            waveMoveMode = MOVE_CLASSIC;
        }

        int pattern = (wave - 1) % 4;
        int cols = 8;
        float startX = 0.13f, stepX = 0.105f;
        float baseY = 0.21f, stepY = 0.088f;

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < cols; col++) {
                boolean include = true;
                float yOffset = 0f;
                int hp = 1;

                if (pattern == 1) {
                    float distFromCenter = Math.abs(col - 3.5f);
                    yOffset = (3.5f - distFromCenter) * 0.016f;
                    if (row == 0 && (col == 0 || col == 7)) include = false;
                } else if (pattern == 2) {
                    if ((row + col) % 2 == 1 && row < 3) include = false;
                    if (row == 3 && (col == 2 || col == 5)) hp = 2;
                } else if (pattern == 3) {
                    if (col == 3 || col == 4) {
                        if (row == 0 || row == 2) include = false;
                    }
                    if (row == 3 && col % 2 == 0) hp = 2;
                }

                if (wave >= 5 && row == 3 && col % 3 == 1) hp = 2;

                if (include) {
                    float ex = startX + col * stepX;
                    float ey = baseY + row * stepY + yOffset;
                    Enemy en = new Enemy(ex, ey, row, col, hp, false);
                    en.baseX = ex;
                    en.baseY = ey;
                    en.rowDir = (waveMoveMode == MOVE_ALTERNATING_ROWS)
                            ? ((row % 2 == 0) ? 1f : -1f)
                            : ((wave % 2 == 1) ? 1f : -1f);
                    enemies.add(en);
                }
            }
        }

        // Special enemy roles are deliberately limited so early waves remain readable.
        // Waves 4-5 get at most one teleporter and one laser-charger; later waves
        // introduce additional specialists gradually.
        if (wave >= 4) {
            ArrayList<Enemy> candidates = new ArrayList<>();
            for (Enemy e : enemies) if (!e.isBoss) candidates.add(e);
            Collections.shuffle(candidates, random);
            int specialistCount = wave <= 5 ? 1 : wave <= 8 ? 2 : 3;
            for (int i = 0; i < candidates.size() && i < specialistCount; i++) {
                Enemy e = candidates.get(i);
                e.canTeleport = true;
                // Teleporters stay on their original side of the formation.
                e.teleportMinX = e.baseX < 0.5f ? 0.07f : 0.51f;
                e.teleportMaxX = e.baseX < 0.5f ? 0.49f : 0.93f;
            }
            int laserAssigned = 0;
            for (int i = specialistCount; i < candidates.size() && laserAssigned < specialistCount; i++) {
                candidates.get(i).canLaser = true;
                laserAssigned++;
            }
        }

        // Every 5th wave (5, 10, 15 single boss; 20, 25, ... two random bosses), Alpha Hound Bosses patrol above the pack
        if (mode == MODE_PLAYING && wave >= 5 && wave % 5 == 0) {
            int bossEncounter = wave / 5;
            int bossHp = 5 + bossEncounter * 2;
            long nowSpawn = SystemClock.uptimeMillis();
            if (bossEncounter <= 3) {
                int bType = (bossEncounter == 1) ? BOSS_ROTTWEILER
                        : (bossEncounter == 2) ? BOSS_BULLDOG
                        : BOSS_DOBERMAN;
                Enemy boss = new Enemy(0.5f, 0.155f, 3, 4, bossHp, true);
                boss.bossType = bType;
                boss.baseX = 0.5f;
                boss.baseY = 0.155f;
                boss.rowDir = random.nextBoolean() ? 1f : -1f;
                boss.nextBossFireAt = nowSpawn + 900L;
                enemies.add(boss);
            } else {
                // Boss #4+: Two distinct random bosses moving in random left/right directions
                int firstType = random.nextInt(3);
                int secondType = (firstType + 1 + random.nextInt(2)) % 3;
                int dualHp = Math.max(6, (int) (bossHp * 0.72f));

                Enemy boss1 = new Enemy(0.32f, 0.150f, 3, 2, dualHp, true);
                boss1.bossType = firstType;
                boss1.baseX = 0.32f;
                boss1.baseY = 0.150f;
                boss1.rowDir = random.nextBoolean() ? 1f : -1f;
                boss1.nextBossFireAt = nowSpawn + 850L;
                enemies.add(boss1);

                Enemy boss2 = new Enemy(0.68f, 0.165f, 3, 6, dualHp, true);
                boss2.bossType = secondType;
                boss2.baseX = 0.68f;
                boss2.baseY = 0.165f;
                boss2.rowDir = random.nextBoolean() ? 1f : -1f;
                boss2.nextBossFireAt = nowSpawn + 1450L; // Staggered cooldown
                enemies.add(boss2);
            }
        }

        int shieldCount = (pattern == 2) ? 3 : 4;
        int shieldHp = Math.max(3, 5 - (wave / 4));
        if (shieldCount == 3) {
            for (int i = 0; i < 3; i++) {
                Shield s = new Shield(0.22f + i * 0.28f, 0.79f);
                s.hp = shieldHp;
                shields.add(s);
            }
        } else {
            for (int i = 0; i < 4; i++) {
                Shield s = new Shield(0.17f + i * 0.22f, 0.79f);
                s.hp = shieldHp;
                shields.add(s);
            }
        }

        formationDir = (wave % 2 == 1) ? 1f : -1f;
        float baseEnemySpeed = Math.min(0.000145f, 0.000046f + wave * 0.0000095f);
        float openingSpeed = wave == 1 ? 0.76f : wave == 2 ? 0.84f : wave == 3 ? 0.92f : 1.0f;
        enemySpeed = baseEnemySpeed * diffSpeedMult() * openingSpeed;
        stepDown = Math.min(0.020f, 0.0105f + wave * 0.00042f) * (wave <= 3 ? 0.88f : 1.0f);
        long baseFire = Math.max(360L, 860L - wave * 48L);
        float openingFireDelay = wave == 1 ? 1.45f : wave == 2 ? 1.32f : wave == 3 ? 1.16f : 1.0f;
        enemyFireInterval = Math.max(260L, (long) (baseFire * diffFireIntervalMult() * openingFireDelay));
        float baseShotSpeed = Math.min(0.00072f, 0.00042f + wave * 0.000022f);
        float openingShotSpeed = wave == 1 ? 0.78f : wave == 2 ? 0.86f : wave == 3 ? 0.94f : 1.0f;
        enemyShotSpeed = baseShotSpeed * diffShotSpeedMult() * openingShotSpeed;

        if (announce && mode == MODE_PLAYING) {
            long now = SystemClock.uptimeMillis();
            waveBannerAt = now;
            lastDiveAt = now;
            playSound(PRIO_NOTE, SND_WAVE);
        }
    }

    public void pauseGame() { running = false; left = false; right = false; stopSounds(); }
    public void resumeGame() { running = true; lastFrame = SystemClock.uptimeMillis(); invalidate(); }

    public boolean isEnteringName() {
        return mode == MODE_NAME_ENTRY;
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAudio();
    }

    @Override protected void onDetachedFromWindow() {
        stopAudio();
        super.onDetachedFromWindow();
    }

    private void toggleSoundMute() {
        soundMuted = !soundMuted;
        prefs.edit().putBoolean(PREF_SOUND_MUTED, soundMuted).apply();
        if (soundMuted) {
            stopSounds();
        } else {
            playSound(PRIO_NOTE, SND_BONE_APPEAR);
        }
    }

    public boolean handleKey(int key, boolean down) {
        if (!down) {
            if (key == KeyEvent.KEYCODE_DPAD_LEFT) { left = false; return true; }
            if (key == KeyEvent.KEYCODE_DPAD_RIGHT) { right = false; return true; }
            return false;
        }

        if (waveSelectorOpen) {
            if (key == KeyEvent.KEYCODE_DPAD_UP) {
                selectedTestWave = Math.max(1, selectedTestWave - 1);
                invalidate(); return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                selectedTestWave = Math.min(20, selectedTestWave + 1);
                invalidate(); return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                int dir = key == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1;
                if (selectorFocus == 0) selectedTestDifficulty = (selectedTestDifficulty + dir + 3) % 3;
                else if (selectorFocus == 1) testCollectablesEnabled = !testCollectablesEnabled;
                else if (selectorFocus == 2) selectorFocus = 3;
                else if (selectorFocus == 3) selectorFocus = 4;
                else selectorFocus = 0;
                invalidate(); return true;
            }
            if (key == KeyEvent.KEYCODE_BACK) { waveSelectorOpen = false; invalidate(); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER || key == KeyEvent.KEYCODE_SPACE) {
                if (selectorFocus == 0) selectorFocus = 1;
                else if (selectorFocus == 1) selectorFocus = 2;
                else if (selectorFocus == 2) testCollectablesEnabled = !testCollectablesEnabled;
                else if (selectorFocus == 3) {
                    waveSelectorOpen = false;
                    startRunWithDifficulty(selectedTestDifficulty);
                    testRun = true; wave = selectedTestWave; makeWave(true); score = 0; prevBest = highScore;
                } else { waveSelectorOpen = false; }
                invalidate(); return true;
            }
            return true;
        }

        if (testItemMenuOpen) {
            if (key == KeyEvent.KEYCODE_DPAD_UP) {
                testItemSection = (testItemSection + TEST_ITEM_SECTIONS.length - 1) % TEST_ITEM_SECTIONS.length;
                testItemOption = 0;
                syncTestItemCursor();
            } else if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                testItemSection = (testItemSection + 1) % TEST_ITEM_SECTIONS.length;
                testItemOption = 0;
                syncTestItemCursor();
            } else if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                int count = TEST_ITEM_SECTIONS[testItemSection].length;
                int direction = key == KeyEvent.KEYCODE_DPAD_RIGHT ? 1 : -1;
                testItemOption = (testItemOption + direction + count) % count;
                syncTestItemCursor();
            } else if (key == KeyEvent.KEYCODE_BACK) {
                testItemMenuOpen = false;
            } else if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER || key == KeyEvent.KEYCODE_SPACE) {
                activateTestItem(testItemCursor);
            }
            invalidate(); return true;
        }

        if (mode == MODE_TITLE) {
            long now = SystemClock.uptimeMillis();
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (now - titleDownLastAt > 3500L) titleDownCount = 0;
                titleDownLastAt = now;
                titleDownCount++;
                if (titleDownCount >= 10) titleDownCount = 10;
                if (titleDownCount == 10) { invalidate(); }
                else if (titleDownCount > 10) titleDownCount = 10;
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_UP && titleDownCount == 10 && now - titleDownLastAt <= 3500L) {
                titleDownCount = 0; waveSelectorOpen = true; selectorFocus = 0; invalidate(); return true;
            }
            if (key != KeyEvent.KEYCODE_DPAD_UP && key != KeyEvent.KEYCODE_DPAD_DOWN) titleDownCount = 0;
            if (key == KeyEvent.KEYCODE_DPAD_UP) {
                lbZoomActive = true;
                lbZoomClosing = false;
                lbZoomStartedAt = now;
                playSound(PRIO_NOTE, SND_BONE_APPEAR);
                invalidate();
                return true;
            }
            if ((key == KeyEvent.KEYCODE_DPAD_DOWN || key == KeyEvent.KEYCODE_BACK) && lbZoomActive) {
                if (!lbZoomClosing) {
                    lbZoomClosing = true;
                    lbZoomClosingAt = now;
                }
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT
                    || key == KeyEvent.KEYCODE_VOLUME_MUTE) {
                toggleSoundMute();
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER
                    || key == KeyEvent.KEYCODE_SPACE) {
                lbZoomActive = false;
                lbZoomClosing = false;
                diffCursor = difficulty;
                mode = MODE_DIFFICULTY;
                playSound(PRIO_NOTE, SND_BONE_APPEAR);
                invalidate();
                return true;
            }
            return false;
        }

        if (mode == MODE_DIFFICULTY) {
            if (key == KeyEvent.KEYCODE_DPAD_UP) {
                diffCursor = (diffCursor + 2) % 3;
                playSound(PRIO_MARCH, SND_MARCH[0]);
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                diffCursor = (diffCursor + 1) % 3;
                playSound(PRIO_MARCH, SND_MARCH[1]);
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                toggleSoundMute();
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_BACK) {
                mode = MODE_TITLE;
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER
                    || key == KeyEvent.KEYCODE_SPACE) {
                startRunWithDifficulty(diffCursor);
                invalidate();
                return true;
            }
            return false;
        }

        if (mode == MODE_NAME_ENTRY) {
            if (SystemClock.uptimeMillis() - nameEntryAt < NAME_ENTRY_LOCK_MS) {
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_UP) {
                char cur = entryTouched[entrySlot] ? entryLetters[entrySlot] : 'A';
                entryLetters[entrySlot] = (cur == 'Z') ? 'A' : (char) (cur + 1);
                entryTouched[entrySlot] = true;
                playSound(PRIO_MARCH, SND_MARCH[0]);
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                char cur = entryTouched[entrySlot] ? entryLetters[entrySlot] : 'A';
                entryLetters[entrySlot] = (cur == 'A') ? 'Z' : (char) (cur - 1);
                entryTouched[entrySlot] = true;
                playSound(PRIO_MARCH, SND_MARCH[1]);
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_LEFT) {
                entrySlot = (entrySlot + 2) % 3;
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_RIGHT) {
                entrySlot = (entrySlot + 1) % 3;
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER
                    || key == KeyEvent.KEYCODE_SPACE) {
                if (!entryTouched[entrySlot]) {
                    entryLetters[entrySlot] = 'A';
                    entryTouched[entrySlot] = true;
                }
                playSound(PRIO_NOTE, SND_BONE_APPEAR);
                if (entrySlot < 2) {
                    entrySlot++;
                } else {
                    commitTop10EntryAndGameOver();
                }
                invalidate();
                return true;
            }
            if (key == KeyEvent.KEYCODE_BACK) {
                for (int i = entrySlot; i < 3; i++) {
                    if (!entryTouched[i]) {
                        entryLetters[i] = ' ';
                    }
                }
                commitTop10EntryAndGameOver();
                invalidate();
                return true;
            }
            return true;
        }

        if (mode == MODE_GAME_OVER) {
            if (testRun && key == KeyEvent.KEYCODE_BACK) { setupTitleScreen(); invalidate(); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER
                    || key == KeyEvent.KEYCODE_SPACE) {
                if (SystemClock.uptimeMillis() - gameOverAt >= GAME_OVER_LOCK_MS) {
                    setupTitleScreen();
                    invalidate();
                }
                return true;
            }
            return false;
        }

        // MODE_PLAYING: Quick Pause handling for TV play
        if (paused) {
            if (key == KeyEvent.KEYCODE_DPAD_UP) { pauseCursor = Math.max(0, pauseCursor - 1); invalidate(); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) { pauseCursor = Math.min(testRun ? 3 : 1, pauseCursor + 1); invalidate(); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) { toggleSoundMute(); invalidate(); return true; }
            if (key == KeyEvent.KEYCODE_BACK || key == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                paused = false; lastFrame = SystemClock.uptimeMillis(); invalidate(); return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER || key == KeyEvent.KEYCODE_SPACE) {
                if (pauseCursor == 0) { paused = false; lastFrame = SystemClock.uptimeMillis(); playSound(PRIO_NOTE, SND_BONE_APPEAR); }
                else if (testRun && pauseCursor == 1) { testItemMenuOpen = true; testItemSection = 0; testItemOption = 0; syncTestItemCursor(); }
                else if (testRun && pauseCursor == 2) { paused = false; wave = selectedTestWave; startRunWithDifficulty(difficulty); testRun = true; wave = selectedTestWave; makeWave(true); }
                else { paused = false; setupTitleScreen(); }
                invalidate(); return true;
            }
            return true;
        }

        if (key == KeyEvent.KEYCODE_BACK || key == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || key == KeyEvent.KEYCODE_MENU) {
            paused = true;
            pauseCursor = 0;
            left = false;
            right = false;
            stopSounds();
            invalidate();
            return true;
        }

        if (key == KeyEvent.KEYCODE_DPAD_UP || key == KeyEvent.KEYCODE_DPAD_DOWN
                || key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT) {
            processCheatKey(key);
        }

        if (key == KeyEvent.KEYCODE_DPAD_LEFT) { left = true; return true; }
        if (key == KeyEvent.KEYCODE_DPAD_RIGHT) { right = true; return true; }
        if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER
                || key == KeyEvent.KEYCODE_SPACE) {
            fire();
            return true;
        }
        return false;
    }

    private int advanceCheatSequence(int key, int[] sequence, int progress) {
        if (key == sequence[progress]) {
            progress++;
            return progress == sequence.length ? -1 : progress;
        }
        return key == sequence[0] ? 1 : 0;
    }

    private void processCheatKey(int key) {
        if (cheatUsedThisWave) return;

        cheatIndex = advanceCheatSequence(key, CHEAT_LIVES_SEQ, cheatIndex);
        shieldCheatIndex = advanceCheatSequence(key, CHEAT_SHIELD_SEQ, shieldCheatIndex);
        overchargeCheatIndex = advanceCheatSequence(key, CHEAT_OVERCHARGE_SEQ, overchargeCheatIndex);

        long now = SystemClock.uptimeMillis();
        if (cheatIndex == -1) {
            lives += 3;
            cheatMessage = "CHEAT! +3 LIVES";
        } else if (shieldCheatIndex == -1) {
            shieldUntil = now + SHIELD_DURATION_MS;
            cheatMessage = "CHEAT! CAT SHIELD";
        } else if (overchargeCheatIndex == -1) {
            overchargeUntil = now + OVERCHARGE_DURATION_MS;
            cheatMessage = "CHEAT! YARN OVERCHARGE";
        } else {
            return;
        }

        cheatIndex = 0;
        shieldCheatIndex = 0;
        overchargeCheatIndex = 0;
        cheatUsedThisWave = true;
        cheatBannerAt = now;
        triggerShake(now, 0.55f);
        addPopup(playerX, playerY - 0.06f, cheatMessage, mint);
        playSound(PRIO_LIFE, SND_CHEAT);
    }

    private void commitTop10EntryAndGameOver() {
        String formattedName = formatInitials(entryLetters, entryTouched);
        latestEntryInitials = formattedName;
        latestEntryScore = score;
        latestEntryDiff = DIFF_LABELS[difficulty];
        leaderboard.add(new ScoreEntry(formattedName, score, DIFF_LABELS[difficulty]));
        saveLeaderboard();
        gameOverAt = SystemClock.uptimeMillis();
        mode = MODE_GAME_OVER;
        playSound(PRIO_OVER, SND_OVER);
    }

    private void fire() {
        long now = SystemClock.uptimeMillis();
        long cooldown = now < rapidFireUntil ? 115L : (isOvercharged(now) ? 215L : 270L);
        if (now - lastShot < cooldown) return;
        if (charlieDown(now)) return;
        lastShot = now;
        muzzleFlashUntil = now + 135L;
        if (now < tripleShotUntil) {
            Shot leftPaw=new Shot(playerX - 0.050f, playerY - .038f, -0.00076f);leftPaw.playerKind=2;shots.add(leftPaw);
            Shot centrePaw=new Shot(playerX, playerY - .044f, -0.00098f);centrePaw.playerKind=1;shots.add(centrePaw);
            Shot rightPaw=new Shot(playerX + 0.050f, playerY - .038f, -0.00076f);rightPaw.playerKind=2;shots.add(rightPaw);
        } else if (isOvercharged(now)) {
            int pairId = nextShotPairId++;
            if (nextShotPairId > 100000) nextShotPairId = 1;
            shots.add(Shot.playerPaired(playerX - 0.017f, playerY - .038f, -0.00086f, pairId));
            shots.add(Shot.playerPaired(playerX + 0.017f, playerY - .038f, -0.00086f, pairId));
        } else {
            shots.add(new Shot(playerX, playerY - .038f, -0.00080f));
        }
        playSound(PRIO_FIRE, SND_FIRE);
    }

    private boolean isShieldActive(long now) {
        return now < shieldUntil;
    }

    private boolean isOvercharged(long now) {
        return now < overchargeUntil;
    }


    @Override public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        int w = getWidth(), h = getHeight();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchMode = true;
            if (waveSelectorOpen) {
                handleWaveSelectorTouch(x, y, w, h);
                invalidate();
                return true;
            }
            if (testItemMenuOpen) {
                if (x > w * .82f && y < h * .20f) {
                    testItemMenuOpen = false;
                } else if (x < w * .45f) {
                    int section = Math.max(0, Math.min(3, Math.round((y / (float) h - .30f) / .15f)));
                    testItemSection = section;
                    testItemOption = 0;
                    syncTestItemCursor();
                } else {
                    int option = Math.max(0, Math.min(TEST_ITEM_SECTIONS[testItemSection].length - 1,
                            (int) ((y / (float) h - .24f) / .075f)));
                    testItemOption = option;
                    syncTestItemCursor();
                    activateTestItem(testItemCursor);
                }
                invalidate(); return true;
            }
            long now = SystemClock.uptimeMillis();
            if (x < w * .16f && y < h * .18f) {
                if (now - lastCornerTap > 1400L) cornerTapCount = 0;
                lastCornerTap = now;
                if (++cornerTapCount >= 5) {
                    cornerTapCount = 0;
                    waveSelectorOpen = true;
                    touchLeft = touchRight = touchFire = false;
                }
                invalidate();
                return true;
            }
            if (mode == MODE_PLAYING && testRun && testCollectablesEnabled && !paused
                    && x > w * .89f && y > h * .18f && y < h * .66f) {
                int item = Math.max(0, Math.min(16, (int) ((y / (float) h - .12f) / .041f)));
                activateTestItem(item); invalidate(); return true;
            }
            if (mode == MODE_GAME_OVER && testRun && x > w*.80f && y < h*.13f) {
                setupTitleScreen(); invalidate(); return true;
            }
            if (mode == MODE_PLAYING && paused) {
                if (testRun && y > h * .38f && y < h * .78f) {
                    int row = Math.max(0, Math.min(3, Math.round((y / (float) h - .518f) / .047f)));
                    pauseCursor = row;
                    if (row == 0) paused = false;
                    else if (row == 1) { testItemMenuOpen = true; testItemSection = 0; testItemOption = 0; syncTestItemCursor(); }
                    else if (row == 2) { paused = false; startRunWithDifficulty(difficulty); testRun = true; wave = selectedTestWave; makeWave(true); }
                    else { paused = false; setupTitleScreen(); }
                } else if (y > h * .75f) { paused = false; }
                invalidate(); return true;
            }
            if (mode == MODE_PLAYING && !paused && testRun && x > w*.80f && y < h*.13f) {
                paused = true; pauseCursor = 0; left = right = false; stopSounds(); invalidate(); return true;
            }
            if (mode == MODE_PLAYING && !paused) {
                if (y > h * .68f) {
                    if (x < w * .22f) touchLeft = true;
                    else if (x < w * .43f) touchRight = true;
                    else touchFire = true;
                } else if (x < w * .42f) {
                    touchLeft = true;
                } else if (x > w * .58f) {
                    touchRight = true;
                } else {
                    fire();
                }
                left = touchLeft;
                right = touchRight;
                if (touchFire) fire();
                invalidate();
                return true;
            }
            if (mode == MODE_TITLE) {
                diffCursor = difficulty;
                mode = MODE_DIFFICULTY;
                invalidate();
                return true;
            } else if (mode == MODE_DIFFICULTY) {
                // Touch users select a difficulty first, then explicitly press START.
                // The back button returns to the title screen without starting a run.
                if (x > w * 0.77f && y < h * 0.20f) {
                    mode = MODE_TITLE;
                    invalidate();
                    return true;
                }
                float[] difficultyRows = {0.605f, 0.670f, 0.735f};
                for (int i = 0; i < difficultyRows.length; i++) {
                    float rowY = h * difficultyRows[i];
                    if (Math.abs(y - rowY) <= h * 0.035f
                            && x > w * 0.18f && x < w * 0.82f) {
                        diffCursor = i;
                        invalidate();
                        return true;
                    }
                }
                if (y > h * 0.79f && y < h * 0.91f
                        && x > w * 0.25f && x < w * 0.75f) {
                    startRunWithDifficulty(diffCursor);
                }
                invalidate();
                return true;
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            touchLeft = touchRight = touchFire = false;
            left = right = false;
            invalidate();
            return true;
        }
        return true;
    }

    private void drawTestItemStrip(Canvas c, int w, int h) {
        String[] icons = {"B", "S", "+", "Y", "L", "H", "M", "F", "3"};
        for (int i = 0; i < icons.length; i++) {
            float cy = h * (.175f + i * .052f);
            p.setColor(withAlpha(navy, 220)); c.drawRoundRect(w*.905f,cy-h*.024f,w*.975f,cy+h*.024f,7,7,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(gold);
            c.drawRoundRect(w*.905f,cy-h*.024f,w*.975f,cy+h*.024f,7,7,p); p.setStyle(Paint.Style.FILL);
            p.setColor(mint); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(h*.026f);
            c.drawText(icons[i],w*.94f,cy+h*.009f,p);
        }
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawTestBackButton(Canvas c, int w, int h) {
        p.setColor(withAlpha(Color.rgb(25,55,100),210));
        c.drawRoundRect(w*.82f,h*.035f,w*.97f,h*.105f,9,9,p);
        p.setColor(white); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(h*.024f);
        c.drawText(mode == MODE_GAME_OVER ? "OK / BACK" : (paused ? "OK / BACK" : "BACK / PAUSE"),w*.895f,h*.082f,p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawTouchControls(Canvas c, int w, int h) {
        if (mode != MODE_PLAYING || paused || waveSelectorOpen) return;
        float cy = h * .91f, r = Math.min(w, h) * .065f;
        p.setColor(withAlpha(Color.rgb(90, 135, 205), 115));
        c.drawCircle(w * .12f, cy, r, p);
        c.drawCircle(w * .29f, cy, r, p);
        p.setColor(withAlpha(Color.rgb(205, 85, 95), 145));
        c.drawCircle(w * .86f, cy, r * 1.18f, p);
        p.setColor(white);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(r * .78f);
        c.drawText("◀", w * .12f, cy + r * .28f, p);
        c.drawText("▶", w * .29f, cy + r * .28f, p);
        p.setTextSize(r * .48f);
        c.drawText("FIRE", w * .86f, cy + r * .16f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void activateTestItem(int item) {
        long now = SystemClock.uptimeMillis();
        switch (item) {
            case 0: score += 200; addPopup(playerX, playerY - .06f, "+200 BONUS", gold); playSound(PRIO_LIFE, SND_BONE); break;
            case 1: shieldUntil = now + SHIELD_DURATION_MS; addPopup(playerX, playerY - .06f, "CAT SHIELD!", mint); playSound(PRIO_LIFE, SND_SHIELD); break;
            case 2: lives++; addPopup(playerX, playerY - .06f, "+1 EXTRA LIFE", gold); playSound(PRIO_LIFE, SND_LIFE); break;
            case 3: overchargeUntil = now + OVERCHARGE_DURATION_MS; addPopup(playerX, playerY - .06f, "YARN OVERCHARGE!", mint); playSound(PRIO_LIFE, SND_CHEAT); break;
            case 4: lives += 2; addPopup(playerX, playerY - .06f, "+2 LIVES", gold); playSound(PRIO_LIFE, SND_LIFE); break;
            case 5: activatePower(POWER_MISSILES, now); break;
            case 6: shieldUntil = now + SHIELD_DURATION_MS; overchargeUntil = now + OVERCHARGE_DURATION_MS; addPopup(playerX, playerY - .06f, "MEGA POWER!", mint); playSound(PRIO_LIFE, SND_CHEAT); break;
            case 7: rapidFireUntil = now + 10000L; addPopup(playerX, playerY - .06f, "RAPID PAWS!", mint); playSound(PRIO_LIFE, SND_CHEAT); break;
            case 8: activatePower(POWER_TRIPLE, now); break;
            case 9: activatePower(POWER_PAW_BOMB, now); break;
            case 10: activatePower(POWER_FREEZE, now); break;
            case 11: activatePower(POWER_MAGNET, now); break;
            case 12: activatePower(POWER_GOLD_SHIELD, now); break;
            case 13: activatePower(POWER_LIGHTNING, now); break;
            case 14: activatePower(POWER_MEGA_LASER, now); break;
            case 15: activatePower(POWER_GOLDEN_FISH, now); break;
            case 16: activatePower(POWER_SCORE_FRENZY, now); break;
        }
        invalidate();
    }

    private void syncTestItemCursor() {
        int[] sectionItems = TEST_ITEM_SECTIONS[testItemSection];
        testItemOption = Math.max(0, Math.min(sectionItems.length - 1, testItemOption));
        testItemCursor = sectionItems[testItemOption];
    }

    private void drawTestItemMenu(Canvas c, int w, int h) {
        p.setColor(0xEE05091B); c.drawRect(0, 0, w, h, p);
        p.setColor(navy); c.drawRoundRect(w*.08f,h*.06f,w*.92f,h*.94f,18,18,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(3); p.setColor(gold);
        c.drawRoundRect(w*.08f,h*.06f,w*.92f,h*.94f,18,18,p); p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER); p.setColor(gold); p.setTextSize(h*.038f);
        c.drawText("TEST COLLECTABLES & WEAPONS",w*.5f,h*.12f,p);
        String[] labels={"BONUS BONE  +200 POINTS","CAT SHIELD  15 SECONDS","EXTRA LIFE  +1","YARN OVERCHARGE  12 SECONDS","LIFE BONUS  +2 LIVES","HEAT-SEEKING MISSILES","MEGA POWER  SHIELD + OVERCHARGE","RAPID PAWS  10 SECONDS","TRIPLE PAW  12 SECONDS","PAW BOMB","FREEZE PAW  4 SECONDS","YARN MAGNET  60-90 SECONDS (RANDOM)","GOLDEN PAW SHIELD  8 SECONDS","LIGHTNING STRIKE","MEGA LASER  4 SECONDS","GOLDEN FISH FRENZY  10 SECONDS","SCORE FRENZY  2X POINTS"};
        for (int section = 0; section < TEST_ITEM_SECTIONS.length; section++) {
            float yy = h * (.29f + section * .15f);
            boolean selected = section == testItemSection;
            p.setColor(selected ? withAlpha(gold, 75) : withAlpha(blue, 40));
            c.drawRoundRect(w*.12f, yy-h*.035f, w*.43f, yy+h*.035f, 9, 9, p);
            p.setColor(selected ? gold : white); p.setTextSize(h*.023f);
            c.drawText((selected ? "> " : "") + TEST_ITEM_SECTION_NAMES[section], w*.275f, yy+h*.008f, p);
        }
        int[] items = TEST_ITEM_SECTIONS[testItemSection];
        for (int option = 0; option < items.length; option++) {
            int item = items[option];
            float yy = h * (.235f + option * .078f);
            boolean selected = option == testItemOption;
            p.setColor(selected ? withAlpha(gold, 75) : withAlpha(blue, 35));
            c.drawRoundRect(w*.48f, yy-h*.027f, w*.88f, yy+h*.027f, 7, 7, p);
            p.setColor(selected ? gold : white); p.setTextSize(h*.020f);
            c.drawText((selected ? "> " : "") + labels[item], w*.68f, yy+h*.007f, p);
        }
        p.setColor(mint); p.setTextSize(h*.018f);
        c.drawText("UP / DOWN: SECTION   LEFT / RIGHT: OPTION   OK: ACTIVATE   BACK: RETURN",w*.5f,h*.915f,p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawWaveSelector(Canvas c, int w, int h) {
        p.setColor(withAlpha(Color.BLACK, 225));
        c.drawRoundRect(w * .08f, h * .08f, w * .92f, h * .92f, 18, 18, p);
        p.setColor(gold); p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(h * .045f);
        c.drawText("TEST WAVE SELECTOR", w * .5f, h * .16f, p);
        p.setColor(white); p.setTextSize(h * .027f);
        c.drawText("Choose an existing wave (boss waves marked)", w * .5f, h * .22f, p);
        for (int i = 1; i <= 20; i++) {
            int col = (i - 1) % 5, row = (i - 1) / 5;
            float cx = w * (.18f + col * .16f), cy = h * (.34f + row * .105f);
            p.setColor(i == selectedTestWave ? gold : Color.rgb(65, 85, 125));
            c.drawRoundRect(cx - w * .055f, cy - h * .032f, cx + w * .055f, cy + h * .032f, 9, 9, p);
            p.setColor(i == selectedTestWave ? navy : white);
            p.setTextSize(h * .026f);
            c.drawText(i % 5 == 0 ? i + " BOSS" : "WAVE " + i, cx, cy + h * .009f, p);
        }
        p.setColor(selectorFocus == 1 ? gold : white); p.setTextSize(h * .027f);
        c.drawText((selectorFocus == 1 ? "> " : "") + "Difficulty: " + DIFF_LABELS[selectedTestDifficulty], w * .5f, h * .755f, p);
        p.setColor(selectorFocus == 2 ? gold : mint);
        c.drawText((selectorFocus == 2 ? "> " : "") + "Collectables & Weapons: " + (testCollectablesEnabled ? "ON" : "OFF"), w * .5f, h * .805f, p);
        p.setColor(selectorFocus == 3 ? gold : mint); c.drawText((selectorFocus == 3 ? "> " : "") + "START TEST", w * .34f, h * .88f, p);
        p.setColor(selectorFocus == 4 ? gold : coral); c.drawText((selectorFocus == 4 ? "> " : "") + "CANCEL", w * .69f, h * .88f, p);
        p.setColor(white); p.setTextSize(h * .019f); c.drawText("UP / DOWN: WAVE  -  OK: NEXT OPTION / SELECT  -  BACK: CLOSE", w * .5f, h * .925f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void handleWaveSelectorTouch(float x, float y, int w, int h) {
        if (y > h * .725f && y < h * .775f) {
            selectedTestDifficulty = (selectedTestDifficulty + 1) % 3;
        } else if (y >= h * .775f && y < h * .835f) {
            testCollectablesEnabled = !testCollectablesEnabled;
        } else if (y >= h * .835f && y < h * .93f) {
            if (x < w * .52f) {
                waveSelectorOpen = false;
                startRunWithDifficulty(selectedTestDifficulty);
                testRun = true;
                wave = selectedTestWave;
                makeWave(true);
                score = 0;
                prevBest = highScore;
            } else {
                waveSelectorOpen = false;
            }
        } else if (y > h * .285f && y < h * .69f
                && x > w * .105f && x < w * .895f) {
            int col = Math.max(0, Math.min(4, Math.round((x / w - .18f) / .16f)));
            int row = Math.max(0, Math.min(3, Math.round((y / h - .34f) / .105f)));
            selectedTestWave = Math.max(1, Math.min(20, row * 5 + col + 1));
        }
    }

    @Override protected void onDraw(Canvas c) {
        super.onDraw(c);
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;
        playerY = .88f;

        long now = SystemClock.uptimeMillis();
        if (running) update(now, w, h);

        c.save();
        long shakeElapsed = now - shakeAt;
        if (shakeAt > 0 && shakeElapsed < SHAKE_MS) {
            float k = 1f - (shakeElapsed / (float) SHAKE_MS);
            float amp = shakePower * k * Math.min(w, h) * 0.016f;
            float dx = (float) Math.sin(now * 0.09f) * amp;
            float dy = (float) Math.cos(now * 0.13f) * amp;
            c.translate(dx, dy);
        }

        c.drawColor(navy);
        drawBackdrop(c, w, h, now);

        if (mode == MODE_TITLE || mode == MODE_DIFFICULTY) {
            drawDemoBackground(c, w, h, now);
            drawLeaderboardBehindMenu(c, w, h, now);
            if (mode == MODE_TITLE) {
                drawTitleCard(c, w, h);
            } else {
                drawDifficultyCard(c, w, h);
            }
        } else {
            drawHud(c, w, h, now);
            for (Shield s : shields) drawShield(c, s, w, h);
            for (Enemy e : enemies) drawDog(c, e, w, h, now);
            for (Shot s : shots) drawShot(c, s, w, h, isOvercharged(now) ? mint : gold, false);
            for (Shot s : dogShots) drawShot(c, s, w, h, coral, true);
            if (boneX >= 0) drawTopBone(c, boneX * w, boneY * h, now);
            if (mouseX >= 0) drawBonusMouse(c, mouseX * w, mouseY * h, now);
            for (BoneDrop d : boneDrops) drawBoneDrop(c, d, w, h, now);
            for (ShieldDrop sd : shieldDrops) drawShieldDrop(c, sd, w, h, now);
            for (ParachuteCat pc : parachuteCats) drawParachuteCat(c, pc, w, h, now);
            for (PowerDrop pd : powerDrops) drawPowerDrop(c, pd, w, h, now);
            if (charlieVisible(now)) drawCharlie(c, playerX * w, playerY * h, now);
            if (now < megaLaserUntil) { p.setColor(withAlpha(blue,95)); p.setStrokeWidth(Math.max(8f,h*.018f)); c.drawLine(playerX*w,playerY*h,w*playerX,h*.10f,p); p.setColor(withAlpha(white,220)); p.setStrokeWidth(Math.max(3f,h*.006f)); c.drawLine(playerX*w,playerY*h,w*playerX,h*.10f,p); }
            if (now < freezeUntil) { p.setColor(withAlpha(Color.rgb(100,200,255),35)); c.drawRect(0,h*.12f,w,h*.78f,p); }
            drawBlasts(c, w, h, now);
            drawPopups(c, w, h, now);
            if(now<goldenFishUntil || now<goldenFishEndFlashUntil) drawGoldenFishBanner(c,w,h,now);
            if (touchMode) drawTouchControls(c, w, h);
            if (testRun) {
                drawTestBackButton(c, w, h);
                if (testCollectablesEnabled && !paused) drawTestItemStrip(c, w, h);
            }

            if (paused) {
                drawPauseOverlay(c, w, h);
            } else if (mode == MODE_ALL_LIVES_LOST) {
                drawAllLivesLost(c, w, h, now);
            } else if (mode == MODE_PLAYING && cheatBannerAt > 0 && now - cheatBannerAt < CHEAT_BANNER_MS) {
                drawCheatBanner(c, w, h, now - cheatBannerAt);
            } else if (mode == MODE_PLAYING && waveBannerAt > 0 && now - waveBannerAt < WAVE_BANNER_MS) {
                drawWaveBanner(c, w, h, now - waveBannerAt);
            } else if (mode == MODE_NAME_ENTRY) {
                drawNameEntryScreen(c, w, h, now);
            } else if (mode == MODE_GAME_OVER) {
                drawGameOver(c, w, h, now);
            }
        }
        // The hidden selector must be drawn above title, difficulty and gameplay screens.
        if (waveSelectorOpen) drawWaveSelector(c, w, h);
        if (testItemMenuOpen) drawTestItemMenu(c, w, h);
        c.restore();

        if (running) postInvalidateDelayed(16);
    }

    private void update(long now, int w, int h) {
        if (lastFrame == 0) lastFrame = now;
        float dt = Math.min(40, now - lastFrame);
        lastFrame = now;

        if (paused) return;
        if (now < hitStopUntil) return;

        if (mode == MODE_TITLE || mode == MODE_DIFFICULTY) {
            updateLeaderboardZoom(now);
            updateDemo(now, dt, w, h);
            return;
        }
        if (mode == MODE_ALL_LIVES_LOST) {
            updatePopups(dt);
            if (now - allLivesLostAt >= ALL_LIVES_LOST_MS) {
                finishRun(now);
            }
            return;
        }
        if (mode != MODE_PLAYING) {
            updatePopups(dt);
            return;
        }

        if (goldenFishUntil > 0L && now >= goldenFishUntil) {
            goldenFishUntil = 0L; goldenFishEndFlashUntil = now + 520L;
            addBlast(playerX,playerY,gold,true,now); triggerShake(now,.48f);
            addPopup(playerX,playerY-.07f,"FRENZY OVER!",gold);
        }

        if (left) playerX -= dt * .00044f;
        if (right) playerX += dt * .00044f;
        playerX = Math.max(.045f, Math.min(.955f, playerX));

        updateEnemyFormation(now, dt, false);
        updateSpecialEnemyActions(now, dt);

        // Shield contact kills regular dogs (or damages bosses) while active
        if (isShieldActive(now) && !charlieDown(now)) {
            Iterator<Enemy> sei = enemies.iterator();
            while (sei.hasNext()) {
                Enemy e = sei.next();
                float hitScale=now<goldenFishUntil?2f:1f;
                float hitRx = (e.isBoss ? 0.068f : 0.048f)*hitScale;
                float hitRy = (e.isBoss ? 0.068f : 0.055f)*hitScale;
                if (Math.abs(e.x - playerX) < hitRx && Math.abs(e.y - playerY) < hitRy) {
                    if (e.isBoss && e.hp > 1) {
                        e.hp--;
                        e.flashUntil = now + 140L;
                        e.y = Math.max(0.155f, e.y - 0.14f);
                        addBlast(e.x, e.y, blue, false, now);
                        playSound(PRIO_NOTE, SND_ARMOR);
                    } else {
                        sei.remove();
                        int pts = e.isBoss ? (250 + wave * 40) : (4 - e.row) * 10 * e.maxHp;
                        awardEnemyPoints(pts, now);
                        addBlast(e.x, e.y, now<goldenFishUntil?gold:blue, e.isBoss || now<goldenFishUntil, now);
                        addPopup(e.x, e.y - 0.015f, "+" + pts, now<goldenFishUntil?gold:blue);
                        playSound(PRIO_HIT, SND_BOOM);
                    }
                }
            }
        }

        // Classic four-note march, faster as fewer dogs remain
        if (!enemies.isEmpty() && now - lastMarch >= 240 + enemies.size() * 14L) {
            lastMarch = now;
            playSound(PRIO_MARCH, SND_MARCH[marchStep]);
            marchStep = (marchStep + 1) & 3;
        }

        // Player shots
        Iterator<Shot> si = shots.iterator();
        while (si.hasNext()) {
            Shot s = si.next();
            if (s.missileStage == 3) {
                Enemy target = nearestEnemy(s.x, s.y);
                if (target != null) {
                    float dxTarget = target.x - s.x, dyTarget = target.y - s.y;
                    float len = Math.max(.0001f, (float) Math.hypot(dxTarget, dyTarget));
                    float speed = .00102f;
                    s.vx += (dxTarget / len * speed - s.vx) * .18f;
                    s.v += (dyTarget / len * speed - s.v) * .18f;
                }
            }
            s.x += s.vx * dt;
            s.y += s.v * dt;

            if (s.ricocheted) {
                if (s.x < 0.04f) {
                    s.x = 0.04f;
                    s.vx = Math.abs(s.vx);
                } else if (s.x > 0.96f) {
                    s.x = 0.96f;
                    s.vx = -Math.abs(s.vx);
                }
                if (s.y < 0.08f || s.y > 0.94f) {
                    si.remove();
                    continue;
                }
            } else if (s.y < 0.08f) {
                si.remove();
                continue;
            }

            if (hitShield(s)) {
                si.remove();
                continue;
            }

            // Shooting a primary Alpha Hound Boss Missile triggers a Mega Blast!
            boolean hitBossMissile = false;
            Iterator<Shot> bossMissileIter = dogShots.iterator();
            while (bossMissileIter.hasNext()) {
                Shot ds = bossMissileIter.next();
                if (ds.missileStage == 1
                        && Math.abs(s.x - ds.x) < 0.036f
                        && Math.abs(s.y - ds.y) < 0.042f) {
                    bossMissileIter.remove();
                    si.remove();
                    triggerMegaExplosion(ds.x, ds.y, now);
                    hitBossMissile = true;
                    break;
                }
            }
            if (hitBossMissile) continue;

            // Shooting a falling BoneDrop triggers 2x explosion & kills nearby enemies (NO points for token)
            boolean hitBoneDrop = false;
            Iterator<BoneDrop> dropHitIter = boneDrops.iterator();
            while (dropHitIter.hasNext()) {
                BoneDrop d = dropHitIter.next();
                if (Math.abs(s.x - d.x) < 0.034f && Math.abs(s.y - d.y) < 0.036f) {
                    dropHitIter.remove();
                    si.remove();
                    triggerMegaExplosion(d.x, d.y, now);
                    hitBoneDrop = true;
                    break;
                }
            }
            if (hitBoneDrop) continue;

            // Check top bonus bone hit! Drops bonus points, and randomly drops a 15s Shield
            if (boneX >= 0 && Math.abs(s.x - boneX) < 0.036f && Math.abs(s.y - boneY) < 0.032f) {
                si.remove();
                addBlast(boneX, boneY, gold, false, now);
                float roll = random.nextFloat();
                if (roll < 0.18f) {
                    shieldDrops.add(new ShieldDrop(boneX, boneY));
                    addPopup(boneX, boneY - 0.02f, "SHIELD DROP!", gold);
                } else if (roll < 0.88f) {
                    int power = random.nextInt(50) == 0 ? POWER_GOLDEN_FISH : random.nextInt(9);
                    powerDrops.add(new PowerDrop(boneX, boneY, power));
                    addPopup(boneX, boneY - 0.02f, powerName(power) + "!", power == POWER_GOLDEN_FISH ? gold : mint);
                } else {
                    boneDrops.add(new BoneDrop(boneX, boneY, boneValue));
                    addPopup(boneX, boneY - 0.02f, "CATCH +" + boneValue + "!", gold);
                }
                playSound(PRIO_HIT, SND_BONE_HIT);
                boneX = -1f;
                lastBone = now;
                continue;
            }

            // Check bonus mouse hit! Drops parachute cat (extra life)
            if (mouseX >= 0 && Math.abs(s.x - mouseX) < 0.038f && Math.abs(s.y - mouseY) < 0.034f) {
                si.remove();
                addBlast(mouseX, mouseY, mint, false, now);
                parachuteCats.add(new ParachuteCat(mouseX, mouseY));
                addPopup(mouseX, mouseY - 0.02f, "PARACHUTE CAT!", mint);
                playSound(PRIO_NOTE, SND_BONE_APPEAR);
                mouseX = -1f;
                lastMouse = now;
                continue;
            }

            boolean removeShot = false;
            Iterator<Enemy> ei = enemies.iterator();
            while (ei.hasNext()) {
                Enemy e = ei.next();
                if (s.ricocheted && e == s.lastHitEnemy) continue;
                float rx = (e.isBoss ? 0.056f : 0.027f) + (s.missileStage == 3 ? .012f : 0f);
                float ry = (e.isBoss ? 0.052f : 0.035f) + (s.missileStage == 3 ? .012f : 0f);
                if (Math.abs(s.x - e.x) < rx && Math.abs(s.y - e.y) < ry) {
                    e.hp--;
                    e.flashUntil = now + 130L;
                    if (e.hp <= 0) {
                        ei.remove();
                        int pts = e.isBoss ? (250 + wave * 40) : (4 - e.row) * 10 * e.maxHp;
                        awardEnemyPoints(pts, now);
                        int blastCol = (e.isBoss || now<goldenFishUntil) ? gold : rowColours[e.row];
                        addBlast(e.x, e.y, blastCol, e.isBoss || now<goldenFishUntil, now);
                        if (e.maxHp > 1) {
                            // The final hit bursts the inflated hound into a brighter fragment pop.
                            addBlast(e.x, e.y, white, true, now);
                            triggerShake(now, .18f);
                        }
                        if(now<goldenFishUntil)triggerShake(now,.22f);
                        addPopup(e.x, e.y - 0.015f, (e.isBoss ? "ALPHA HOUND! +" : "+") + pts, blastCol);
                        if (e.isBoss) {
                            triggerShake(now, 1.35f);
                            hitStopUntil = now + HIT_STOP_MS;
                            blasts.add(new Blast(e.x, e.y, white, false, now, random.nextFloat() * 6.2832f, false, true));
                            playSound(PRIO_HIT, SND_MEGA_BOOM);
                        } else {
                            playSound(PRIO_HIT, SND_BOOM);
                        }
                    } else {
                        if (e.isBoss) {
                            hitStopUntil = now + 38L;
                        }
                        playSound(PRIO_NOTE, SND_ARMOR);
                    }

                    if (s.missileStage == 3) {
                        addBlast(e.x, e.y, gold, true, now);
                        triggerShake(now, .48f);
                        Iterator<Enemy> splash=enemies.iterator();
                        while(splash.hasNext()){Enemy nearby=splash.next();if(nearby!=e&&Math.abs(nearby.x-e.x)<.105f&&Math.abs(nearby.y-e.y)<.10f){nearby.hp--;nearby.flashUntil=now+180L;if(nearby.hp<=0){awardEnemyPoints(nearby.isBoss?250+wave*40:(4-nearby.row)*10*nearby.maxHp,now);addBlast(nearby.x,nearby.y,gold,true,now);splash.remove();}}}
                    }

                    // Each twin-stream pair's first impact triggers one random 360-degree ricochet
                    if (s.missileStage != 3 && !s.ricocheted && s.pairId > 0 && !ricochetedPairIds.contains(s.pairId)) {
                        ricochetedPairIds.add(s.pairId);
                        if (ricochetedPairIds.size() > 64) {
                            ricochetedPairIds.remove(0);
                        }
                        s.ricocheted = true;
                        s.lastHitEnemy = e;
                        double angle = random.nextDouble() * Math.PI * 2.0;
                        float speed = 0.00095f;
                        s.vx = (float) Math.cos(angle) * speed;
                        s.v = (float) Math.sin(angle) * speed;
                        addBlast(s.x, s.y, mint, false, now);
                        removeShot = false;
                    } else {
                        removeShot = true;
                    }
                    break;
                }
            }
            if (removeShot) si.remove();
        }

        // Charlie's active bonuses: freeze the pack, unleash the Golden Fish barrage, or fire the Mega Laser.
        if (now < goldenFishUntil && now >= nextGoldenShotAt && !charlieDown(now)) {
            nextGoldenShotAt = now + 105L;
            Shot goldLeft=new Shot(playerX-.022f,playerY-.045f,-.00108f);goldLeft.playerKind=3;shots.add(goldLeft);
            Shot goldCentre=new Shot(playerX,playerY-.052f,-.00118f);goldCentre.playerKind=3;shots.add(goldCentre);
            Shot goldRight=new Shot(playerX+.022f,playerY-.045f,-.00108f);goldRight.playerKind=3;shots.add(goldRight);
        }
        if (now < megaLaserUntil && now >= nextLaserTickAt) {
            nextLaserTickAt = now + 260L;
            Iterator<Enemy> laserHits = enemies.iterator();
            while (laserHits.hasNext()) {
                Enemy target = laserHits.next();
                if (Math.abs(target.x - playerX) < .075f) {
                    target.hp -= target.isBoss ? 1 : target.hp;
                    target.flashUntil = now + 160L;
                    if (target.hp <= 0) {
                        int pts = target.isBoss ? 250 + wave * 40 : (4 - target.row) * 10 * target.maxHp;
                        awardEnemyPoints(pts, now);
                        addBlast(target.x, target.y, gold, target.isBoss, now);
                        laserHits.remove();
                    }
                }
            }
        }

        // Alpha Hound Bosses fire staggered splitting warhead missiles
        if (now >= freezeUntil) for (Enemy e : enemies) {
            if (e.isBoss && now >= e.nextBossFireAt) {
                long baseBossCd = Math.max(1750L, (long) ((2700L - wave * 25L) * diffFireIntervalMult()));
                float health=e.hp/(float)Math.max(1,e.maxHp);
                if(health<.34f) baseBossCd=(long)(baseBossCd*.68f);
                else if(health<.67f) baseBossCd=(long)(baseBossCd*.82f);
                e.nextBossFireAt = now + baseBossCd + random.nextInt(450);
                dogShots.add(Shot.bossMissile(e.x, e.y + 0.048f, 0f, enemyShotSpeed * 0.92f, 1));
                playSound(PRIO_NOTE, SND_BONE_APPEAR);
            }
        }

        // Regular pack firing
        if (now >= freezeUntil && now - lastEnemyShot > enemyFireInterval && !enemies.isEmpty()) {
            ArrayList<Enemy> regulars = new ArrayList<>();
            for (Enemy e : enemies) {
                if (!e.isBoss) regulars.add(e);
            }
            if (!regulars.isEmpty()) {
                Enemy e = regulars.get(random.nextInt(regulars.size()));
                dogShots.add(new Shot(e.x, e.y + .03f, enemyShotSpeed));
            }
            lastEnemyShot = now;
        }

        ArrayList<Shot> splitSpawns = new ArrayList<>();
        Iterator<Shot> di = dogShots.iterator();
        while (di.hasNext()) {
            Shot s = di.next();
            if (s.missileStage != 4) { s.x += s.vx * dt; s.y += s.v * dt; }

            if (s.missileStage == 4) {
                if (Math.abs(s.x-playerX)<.20f && Math.abs(s.y-playerY)<.035f && now>=goldenFishUntil && !isShieldActive(now) && now-hitAt>=CHARLIE_HIT_MS) loseLife(now);
                if (Math.abs(s.x-playerX)<.20f && (now-hitAt<CHARLIE_HIT_MS || isShieldActive(now) || now<goldenFishUntil)) { addBlast(playerX,s.y,blue,false,now); }
                di.remove(); continue;
            }

            // Primary boss missile splits into two smaller angled warheads
            if (s.missileStage == 1 && s.y >= 0.36f) {
                addBlast(s.x, s.y, gold, false, now);
                playSound(PRIO_NOTE, SND_ARMOR);
                float splitVy = s.v * 1.12f;
                float splitVx = splitVy * 0.54f;
                splitSpawns.add(Shot.bossMissile(s.x - 0.012f, s.y, -splitVx, splitVy, 2));
                splitSpawns.add(Shot.bossMissile(s.x + 0.012f, s.y, splitVx, splitVy, 2));
                di.remove();
                continue;
            }

            if (s.y > .96f || s.x < 0.02f || s.x > 0.98f) {
                di.remove();
                continue;
            }
            if (hitShield(s)) {
                di.remove();
                continue;
            }
            if(now<reflectUntil && Math.abs(s.x-playerX)<.10f && Math.abs(s.y-playerY)<.12f){
                Shot reflected=new Shot(s.x,s.y-.02f,-.00105f);shots.add(reflected);addBlast(s.x,s.y,gold,false,now);di.remove();continue;
            }
            float hitRx = (s.missileStage == 1) ? 0.042f : 0.034f;
            float hitRy = (s.missileStage == 1) ? 0.042f : 0.034f;
            if(now<goldenFishUntil){hitRx*=2f;hitRy*=2f;}
            if (Math.abs(s.x - playerX) < hitRx && Math.abs(s.y - playerY) < hitRy) {
                di.remove();
                if (!isShieldActive(now) && now >= goldenFishUntil && now - hitAt >= CHARLIE_HIT_MS) loseLife(now);
            }
        }
        dogShots.addAll(splitSpawns);

        // Top flying bonus bone
        if (boneX < 0 && boneDrops.isEmpty() && shieldDrops.isEmpty() && now - lastBone > 8500L) {
            boneDir = random.nextBoolean() ? 1f : -1f;
            boneX = (boneDir > 0) ? -0.05f : 1.05f;
            bonePhase = random.nextFloat() * 6.2832f;
            boneSpeed = Math.min(0.00038f, 0.00021f + wave * 0.000018f);
            boneValue = 150 + Math.min(350, wave * 50);
            lastBone = now;
            playSound(PRIO_NOTE, SND_BONE_APPEAR);
        }
        if (boneX >= 0) {
            boneX += boneDir * boneSpeed * dt;
            boneY = BONE_TOP_Y + (float) Math.sin(now * 0.0075f + bonePhase) * 0.022f;
            if ((boneDir > 0 && boneX > 1.06f) || (boneDir < 0 && boneX < -0.06f)) {
                boneX = -1f;
                lastBone = now;
            }
        }

        // Bonus mouse: spawns early on to start, then very rarely
        if (mouseX < 0 && parachuteCats.isEmpty()) {
            if (!spawnedInitialMouse && now - waveBannerAt > 2400L) {
                spawnBonusMouse(now);
                spawnedInitialMouse = true;
            } else if (spawnedInitialMouse && now - lastMouse > 28000L && random.nextFloat() < 0.025f) {
                spawnBonusMouse(now);
            }
        }
        if (mouseX >= 0) {
            mouseX += mouseDir * mouseSpeed * dt;
            mouseY = MOUSE_TOP_Y + (float) Math.sin(now * 0.011f + mousePhase) * 0.018f;
            if ((mouseDir > 0 && mouseX > 1.06f) || (mouseDir < 0 && mouseX < -0.06f)) {
                mouseX = -1f;
                lastMouse = now;
            }
        }

        // Falling bone points that Charlie can catch (or shoot for 2x explosion with 0 token points)
        Iterator<BoneDrop> bi = boneDrops.iterator();
        while (bi.hasNext()) {
            BoneDrop d = bi.next();
            d.vy = Math.min(0.00056f, d.vy + 0.00000055f * dt);
            d.y += d.vy * dt;
            d.x += (float) Math.sin(now * 0.008f + d.seed) * 0.00014f * dt;
            if(now<magnetUntil){d.x+=Math.signum(playerX-d.x)*.00042f*dt;d.y+=Math.signum(playerY-d.y)*.00008f*dt;}
            d.x = Math.max(0.06f, Math.min(0.94f, d.x));

            float sz = Math.min(w, h) * 0.033f;
            float catchScale=now<goldenFishUntil?2f:1f;
            if (!charlieDown(now)
                    && Math.abs(d.x - playerX) * w < sz * 1.9f * catchScale
                    && Math.abs(d.y - playerY) < 0.055f * catchScale) {
                score += d.points * (now < scoreFrenzyUntil ? 2 : 1);
                if (isShieldActive(now)) {
                    overchargeUntil = now + OVERCHARGE_DURATION_MS;
                    addPopup(playerX, playerY - 0.065f, "+" + d.points + " & BOWTIE OVERCHARGE!", mint);
                } else {
                    addPopup(playerX, playerY - 0.055f, "+" + d.points + "!", mint);
                }
                addBlast(d.x, d.y, mint, false, now);
                playSound(PRIO_HIT, SND_BONE);
                bi.remove();
                continue;
            }
            if (d.y > 0.96f) {
                addPopup(d.x, 0.92f, "MISSED!", coral);
                bi.remove();
            }
        }

        // Falling 15-second shield drops (catching while already shielded triggers Bowtie Overcharge)
        Iterator<ShieldDrop> sdi = shieldDrops.iterator();
        while (sdi.hasNext()) {
            ShieldDrop sd = sdi.next();
            sd.y += sd.vy * dt;
            sd.x += (float) Math.sin(now * 0.007f + sd.seed) * 0.00012f * dt;
            if(now<magnetUntil){sd.x+=Math.signum(playerX-sd.x)*.00042f*dt;sd.y+=Math.signum(playerY-sd.y)*.00008f*dt;}
            sd.x = Math.max(0.06f, Math.min(0.94f, sd.x));

            float sz = Math.min(w, h) * 0.033f;
            float catchScale=now<goldenFishUntil?2f:1f;
            if (!charlieDown(now)
                    && Math.abs(sd.x - playerX) * w < sz * 1.9f * catchScale
                    && Math.abs(sd.y - playerY) < 0.055f * catchScale) {
                boolean wasShielded = isShieldActive(now);
                shieldUntil = now + SHIELD_DURATION_MS;
                if (wasShielded) {
                    overchargeUntil = now + OVERCHARGE_DURATION_MS;
                    addPopup(playerX, playerY - 0.065f, "BOWTIE OVERCHARGE!", mint);
                } else {
                    addPopup(playerX, playerY - 0.055f, "GOLD SHIELD 15S!", gold);
                }
                addBlast(sd.x, sd.y, gold, false, now);
                playSound(PRIO_NOTE, SND_SHIELD);
                sdi.remove();
                continue;
            }
            if (sd.y > 0.96f) {
                sdi.remove();
            }
        }

        // Falling special bonuses. Yarn Magnet bends their paths towards Charlie.
        Iterator<PowerDrop> pdi = powerDrops.iterator();
        while (pdi.hasNext()) {
            PowerDrop pd = pdi.next();
            pd.y += pd.vy * dt;
            pd.x += (float) Math.sin(now * .008f + pd.seed) * .00012f * dt;
            if (now < magnetUntil) {
                pd.x += Math.signum(playerX - pd.x) * .00042f * dt;
                pd.y += Math.signum(playerY - pd.y) * .00008f * dt;
            }
            pd.x = Math.max(.045f, Math.min(.955f, pd.x));
            float catchScale=now<goldenFishUntil?2f:1f;
            if (!charlieDown(now) && Math.abs(pd.x - playerX) < .055f*catchScale && Math.abs(pd.y - playerY) < .065f*catchScale) {
                activatePower(pd.type, now);
                addBlast(pd.x, pd.y, pd.type == POWER_GOLDEN_FISH ? gold : mint, false, now);
                pdi.remove();
                continue;
            }
            if (pd.y > .96f) pdi.remove();
        }

        // Gliding parachute cats with gentle breeze drift (extra life only, collectible only)
        Iterator<ParachuteCat> pci = parachuteCats.iterator();
        while (pci.hasNext()) {
            ParachuteCat pc = pci.next();
            pc.y += pc.vy * dt;
            if(now<magnetUntil){pc.x+=Math.signum(playerX-pc.x)*.00042f*dt;pc.y+=Math.signum(playerY-pc.y)*.00008f*dt;}
            float breeze = (float) Math.sin(now * 0.0032f + pc.seed) * 0.00018f
                    + (float) Math.cos(now * 0.0075f + pc.seed * 0.5f) * 0.00009f;
            pc.x = Math.max(0.06f, Math.min(0.94f, pc.x + breeze * dt));

            float sz = Math.min(w, h) * 0.033f;
            float catchScale=now<goldenFishUntil?2f:1f;
            if (!charlieDown(now)
                    && Math.abs(pc.x - playerX) * w < sz * 2.0f * catchScale
                    && Math.abs(pc.y - playerY) < 0.06f * catchScale) {
                lives++;
                addPopup(playerX, playerY - 0.06f, "+1 LIFE", mint);
                addBlast(pc.x, pc.y, mint, false, now);
                playSound(PRIO_NOTE, SND_BONE);
                pci.remove();
                continue;
            }
            if (pc.y > 0.96f) {
                pci.remove();
            }
        }

        updatePopups(dt);
        if(now>comboUntil)killStreak=0;

        // Diving dog direct collision costs 1 life (and returns dog to formation) instead of wiping all lives
        Iterator<Enemy> diveHitIter = enemies.iterator();
        while (diveHitIter.hasNext()) {
            Enemy e = diveHitIter.next();
            if (!e.diving) continue;
            float playerHitScale=now<goldenFishUntil?2f:1f;
            if (Math.abs(e.x - playerX) < 0.044f*playerHitScale && Math.abs(e.y - playerY) < 0.048f*playerHitScale) {
                e.diving = false;
                if(now<goldenFishUntil){
                    int pts=e.isBoss?250+wave*40:(4-e.row)*10*e.maxHp;
                    awardEnemyPoints(pts,now);addBlast(e.x,e.y,gold,true,now);triggerShake(now,.35f);
                    diveHitIter.remove();
                } else {
                    e.x = e.baseX; e.y = e.baseY; e.rowDir = formationDir;
                    if (!isShieldActive(now) && now - hitAt >= CHARLIE_HIT_MS) loseLife(now);
                }
                break;
            }
        }

        for (Enemy e : enemies) {
            if (!e.diving && e.y > .81f && now < goldenFishUntil) {
                e.y = 0.24f; e.baseY = 0.24f;
            } else if (!e.diving && e.y > .81f && now >= goldenFishUntil) {
                if (isShieldActive(now)) {
                    e.y = 0.24f;
                } else {
                    lives = 0;
                    charlieGone = true;
                    addBlast(playerX, playerY, 0, true, now);
                    triggerShake(now, 1.2f);
                    finishRun(now);
                    break;
                }
            }
        }

        if (mode == MODE_PLAYING && enemies.isEmpty()) {
            wave++;
            makeWave(true);
        }

        if (!testRun && score > highScore) {
            highScore = score;
            if (score > prevBest && prevBest > 0) newRecord = true;
            prefs.edit().putInt(PREF_HIGH_SCORE, highScore).apply();
        }
    }

    private void spawnBonusMouse(long now) {
        mouseDir = random.nextBoolean() ? 1f : -1f;
        mouseX = (mouseDir > 0) ? -0.05f : 1.05f;
        mousePhase = random.nextFloat() * 6.2832f;
        mouseSpeed = 0.00024f;
        lastMouse = now;
        playSound(PRIO_NOTE, SND_BONE_APPEAR);
    }

    /** Large explosion (~2x Charlie's blast radius) that damages/kills enemies in range; 0 token points. */
    private void triggerMegaExplosion(float cx, float cy, long now) {
        blasts.add(new Blast(cx, cy, gold, false, now, random.nextFloat() * 6.2832f, true));
        blasts.add(new Blast(cx, cy, white, false, now, random.nextFloat() * 6.2832f, false, true));
        triggerShake(now, 1.55f);
        hitStopUntil = now + HIT_STOP_MS;
        addPopup(cx, cy - 0.025f, "MEGA BLAST!", coral);
        playSound(PRIO_HIT, SND_MEGA_BOOM);

        float radiusX = 0.145f;
        float radiusY = 0.165f;
        Iterator<Shot> enemyBulletClear=dogShots.iterator();
        while(enemyBulletClear.hasNext()){
            Shot bullet=enemyBulletClear.next();
            float bx=(bullet.x-cx)/radiusX,by=(bullet.y-cy)/radiusY;
            if(bx*bx+by*by<=1.35f)enemyBulletClear.remove();
        }
        Iterator<Enemy> ei = enemies.iterator();
        while (ei.hasNext()) {
            Enemy e = ei.next();
            float dx = (e.x - cx) / radiusX;
            float dy = (e.y - cy) / radiusY;
            if (dx * dx + dy * dy <= 1f) {
                if (e.isBoss && e.hp > 3) {
                    e.hp -= 3;
                    e.flashUntil = now + 180L;
                    addBlast(e.x, e.y, gold, false, now);
                } else {
                    ei.remove();
                    int pts = e.isBoss ? (250 + wave * 40) : (4 - e.row) * 10 * e.maxHp;
                    awardEnemyPoints(pts, now);
                    int col = e.isBoss ? gold : rowColours[e.row];
                    addBlast(e.x, e.y, col, e.isBoss, now);
                    addPopup(e.x, e.y - 0.015f, "+" + pts, col);
                }
            }
        }
    }

    private void updateEnemyFormation(long now, float dt, boolean demoMode) {
        if (enemies.isEmpty() || (!demoMode && now < freezeUntil)) return;
        for (Enemy trailEnemy : enemies) { trailEnemy.trailX = trailEnemy.x; trailEnemy.trailY = trailEnemy.y; }

        // Alpha Hound Bosses patrol across the upper sky and bounce off each other
        ArrayList<Enemy> bosses = new ArrayList<>();
        for (Enemy e : enemies) {
            if (e.isBoss) {
                bosses.add(e);
                float bossHealth=e.hp/(float)Math.max(1,e.maxHp);
                float phaseSpeed=bossHealth<.30f?2.35f:bossHealth<.60f?1.85f:1.45f;
                e.x += enemySpeed * phaseSpeed * dt * e.rowDir;
                if (e.x > 0.88f) {
                    e.x = 0.88f;
                    e.rowDir = -1f;
                } else if (e.x < 0.12f) {
                    e.x = 0.12f;
                    e.rowDir = 1f;
                }
                float bossSweepHealth=e.hp/(float)Math.max(1,e.maxHp);
                float sweep=bossSweepHealth<.34f?.034f:bossSweepHealth<.67f?.022f:.012f;
                e.y = e.baseY + (float) Math.sin(now * (bossSweepHealth<.34f?.009f:.004f) + e.bossType * 1.7f) * sweep;
            }
        }
        if (bosses.size() >= 2) {
            for (int i = 0; i < bosses.size(); i++) {
                for (int j = i + 1; j < bosses.size(); j++) {
                    Enemy b1 = bosses.get(i);
                    Enemy b2 = bosses.get(j);
                    float minDist = 0.125f;
                    float dxBoss = b2.x - b1.x;
                    if (Math.abs(dxBoss) < minDist) {
                        float overlap = (minDist - Math.abs(dxBoss)) * 0.5f;
                        if (dxBoss >= 0f) {
                            b1.x = Math.max(0.10f, b1.x - overlap);
                            b2.x = Math.min(0.90f, b2.x + overlap);
                            b1.rowDir = -1f;
                            b2.rowDir = 1f;
                        } else {
                            b1.x = Math.min(0.90f, b1.x + overlap);
                            b2.x = Math.max(0.10f, b2.x - overlap);
                            b1.rowDir = 1f;
                            b2.rowDir = -1f;
                        }
                    }
                }
            }
        }

        if (waveMoveMode == MOVE_ALTERNATING_ROWS) {
            float[] rowMin = {1f, 1f, 1f, 1f};
            float[] rowMax = {0f, 0f, 0f, 0f};
            for (Enemy e : enemies) {
                if (e.diving || e.isBoss) continue;
                int r = Math.max(0, Math.min(3, e.row));
                rowMin[r] = Math.min(rowMin[r], e.x);
                rowMax[r] = Math.max(rowMax[r], e.x);
            }
            boolean[] flipRow = new boolean[4];
            for (int r = 0; r < 4; r++) {
                float rDir = (r % 2 == 0) ? formationDir : -formationDir;
                float rDx = enemySpeed * 1.15f * dt * rDir;
                if (rowMax[r] > 0f && (rowMax[r] + rDx > 0.93f || rowMin[r] + rDx < 0.07f)) {
                    flipRow[r] = true;
                }
            }
            for (Enemy e : enemies) {
                if (e.diving || e.isBoss) continue;
                int r = Math.max(0, Math.min(3, e.row));
                float rDir = (r % 2 == 0) ? formationDir : -formationDir;
                e.rowDir = rDir;
                if (flipRow[r]) {
                    e.y += stepDown * (demoMode ? 0.55f : 0.72f);
                    e.x -= rDir * enemySpeed * 2.2f * dt;
                } else {
                    e.x += enemySpeed * 1.15f * dt * rDir;
                }
            }
            if (flipRow[0] || flipRow[1] || flipRow[2] || flipRow[3]) {
                formationDir *= -1f;
            }
            return;
        }

        float min = 0.08f, max = 0.92f;
        for (Enemy e : enemies) {
            if (e.diving || e.isBoss) continue;
            min = Math.min(min, e.x);
            max = Math.max(max, e.x);
        }
        float dx = enemySpeed * dt * formationDir;
        boolean hitEdge = (max + dx > 0.93f || min + dx < 0.07f);
        if (hitEdge) {
            formationDir *= -1f;
            float reversalStep = enemySpeed * dt * formationDir;
            for (Enemy e : enemies) {
                if (!e.diving && !e.isBoss) {
                    e.y += stepDown * (demoMode ? 0.65f : 1f);
                    e.baseY = e.y;
                    e.x = Math.max(0.07f, Math.min(0.93f, e.x + reversalStep));
                    e.baseX = Math.max(0.07f, Math.min(0.93f, e.baseX + reversalStep));
                    e.rowDir = formationDir;
                }
            }
        } else {
            for (Enemy e : enemies) {
                if (!e.diving && !e.isBoss) {
                    e.x += dx;
                    e.baseX += dx;
                    e.rowDir = formationDir;
                }
            }
        }

        if (waveMoveMode == MOVE_SINE_WEAVE) {
            for (Enemy e : enemies) {
                if (!e.diving && !e.isBoss) {
                    e.y = e.baseY + (float) Math.sin(now * 0.0042f + e.col * 0.65f + e.row * 0.4f) * 0.015f;
                    e.rowDir = formationDir;
                }
            }
        } else if (waveMoveMode == MOVE_ACCORDION) {
            float breath = (float) Math.sin(now * 0.0028f) * 0.022f;
            for (Enemy e : enemies) {
                if (!e.diving && !e.isBoss) {
                    float offsetFromMid = (e.col - 3.5f) / 3.5f;
                    float prevX = e.x;
                    e.x = Math.max(0.07f, Math.min(0.93f, e.baseX + offsetFromMid * breath));
                    if (Math.abs(e.x - prevX) > 0.00001f) {
                        e.rowDir = (e.x >= prevX) ? 1f : -1f;
                    }
                }
            }
        } else if (waveMoveMode == MOVE_SWARM) {
            float pulse=(float)Math.sin(now*.0032f)*.024f;
            for(Enemy e:enemies) if(!e.diving&&!e.isBoss){float targetX=.5f+(e.col-3.5f)*.095f+pulse*(e.row%2==0?1f:-1f);e.x+=(targetX-e.x)*.035f;e.y=e.baseY+(float)Math.sin(now*.005f+e.col*.8f+e.row)*.018f;}
        } else if (waveMoveMode == MOVE_PINCER) {
            for(Enemy e:enemies) if(!e.diving&&!e.isBoss){float side=e.col<4?-1f:1f;float targetX=.5f+side*(.12f+e.row*.055f)+(float)Math.sin(now*.002f+e.row)*.018f;e.x+=(targetX-e.x)*.025f;e.y=e.baseY+(float)Math.sin(now*.004f+e.col)*.012f;}
        } else if (waveMoveMode == MOVE_SPIRAL) {
            for(Enemy e:enemies) if(!e.diving&&!e.isBoss){float a=now*.0016f+e.col*.72f+e.row*.55f;float radius=.025f+e.row*.009f;e.x=e.baseX+(float)Math.cos(a)*radius;e.y=e.baseY+(float)Math.sin(a)*radius*.7f;}
        } else if (waveMoveMode == MOVE_ZIGZAG) {
            for(Enemy e:enemies)if(!e.diving&&!e.isBoss){float phase=(now*.0028f+e.col*.75f+e.row*.35f)%(float)(Math.PI*2);float triangle=(float)(2.0/Math.PI)* (float)Math.asin(Math.sin(phase));e.x=e.baseX+triangle*.045f;e.y=e.baseY+(float)Math.sin(phase*.5f)*.012f;}
        } else if (waveMoveMode == MOVE_FORMATION_BREAK) {
            if(now>lastSpecialEnemyMoveAt+6200L){lastSpecialEnemyMoveAt=now;formationBreakUntil=now+2100L;}
            if(now<formationBreakUntil){for(Enemy e:enemies)if(!e.diving&&!e.isBoss){float a=now*.003f+e.col*.9f;float tx=.5f+(float)Math.cos(a)*(.10f+e.row*.025f);float ty=.35f+(float)Math.sin(a)*.11f;e.x+=(tx-e.x)*.035f;e.y+=(ty-e.y)*.035f;}}
            else {for(Enemy e:enemies)if(!e.diving&&!e.isBoss){e.x+=(e.baseX-e.x)*.025f;e.y+=(e.baseY-e.y)*.025f;}}
        } else if ((waveMoveMode == MOVE_DIVE_BOMBERS || waveMoveMode == MOVE_SPIRAL_DIVERS) && !demoMode) {
            if (now - lastDiveAt > (waveMoveMode == MOVE_SPIRAL_DIVERS ? 2600L : 2200L)) {
                boolean alreadyDiving = false;
                for (Enemy active : enemies) if (active.diving) { alreadyDiving = true; break; }
                if (!alreadyDiving) {
                    lastDiveAt = now;
                    for (Enemy e : enemies) {
                        if (!e.diving && !e.isBoss) {
                            e.diving = true;
                            e.diveOriginX = e.x;
                            e.diveOriginY = e.y;
                            e.diveStyle = waveMoveMode == MOVE_SPIRAL_DIVERS ? 3 : 0;
                            e.divePhase = -0.45f;
                            e.flashUntil = now + 450L;
                            break;
                        }
                    }
                }
            }
            for (Enemy e : enemies) {
                if (e.diving) {
                    if (e.divePhase < 0f) {
                        e.divePhase += dt * 0.00078f;
                        if (e.divePhase > 0f) e.divePhase = 0f;
                        e.x = e.baseX + (float) Math.sin(now * 0.09f) * 0.006f;
                        e.y = e.baseY;
                        e.flashUntil = now + 40L;
                        continue;
                    }
                    float prevX = e.x;
                    e.divePhase += dt * 0.00052f; // slower, more readable dive and return
                    if (e.divePhase < 0.55f) {
                        float u = e.divePhase / 0.55f;
                        e.y = e.diveOriginY + u * 0.56f;
                        if(e.diveStyle>=3){float angle=u*(float)Math.PI*4f+e.col*.7f;float radius=.10f*u;e.x=e.diveOriginX+(float)Math.cos(angle)*radius;e.y=e.diveOriginY+u*.56f+(float)Math.sin(angle)*radius*.55f;}
                        else {float curve=(e.diveStyle==0?1f:e.diveStyle==1?-1f:1.65f);e.x=e.diveOriginX+(float)Math.sin(u*(float)Math.PI*(e.diveStyle==2?3f:2f))*.085f*curve;}
                    } else if (e.divePhase < 1.0f) {
                        float u = (e.divePhase - 0.55f) / 0.45f;
                        float divePeakY = e.diveOriginY + 0.56f;
                        e.y = divePeakY + (e.baseY - divePeakY) * u;
                        e.x = e.x + (e.baseX - e.x) * 0.14f;
                    } else {
                        e.diving = false;
                        e.x = e.baseX;
                        e.y = e.baseY;
                        e.rowDir = formationDir;
                    }
                    if (e.diving && Math.abs(e.x - prevX) > 0.00001f) {
                        e.rowDir = (e.x >= prevX) ? 1f : -1f;
                    }
                }
            }
        }
    }


    private void updateSpecialEnemyActions(long now,float dt) {
        if(wave<4 || now<freezeUntil)return;
        ArrayList<Shot> beams=new ArrayList<>();
        ArrayList<Enemy> reinforcements=new ArrayList<>();
        for(Enemy e:enemies){
            if(e.isBoss){
                float health=e.hp/(float)Math.max(1,e.maxHp);
                if(health<.42f){
                    if(e.nextReinforceAt==0L)e.nextReinforceAt=now+4200L;
                    if(now>=e.nextReinforceAt && enemies.size()<42){
                        for(int side=-1;side<=1;side+=2){Enemy minion=new Enemy(Math.max(.08f,Math.min(.92f,e.x+side*.055f)),e.y+.10f,2,side<0?2:5,1,false);minion.baseX=minion.x;minion.baseY=minion.y;minion.rowDir=side;reinforcements.add(minion);}
                        e.nextReinforceAt=now+8500L;addPopup(e.x,e.y-.07f,"REINFORCEMENTS!",coral);triggerShake(now,.25f);
                    }
                }
                continue;
            }
            if(e.diving)continue;
            if (e.canTeleport && e.y <= .53f) {
                if(e.nextTeleportAt==0L)e.nextTeleportAt=now+6500L+random.nextInt(3500);
                if(now>=e.nextTeleportAt){
                    float span = Math.max(.04f, e.teleportMaxX - e.teleportMinX);
                    float offset = span * (.22f + random.nextFloat() * .12f);
                    float direction = random.nextBoolean() ? 1f : -1f;
                    float oldX = e.x;
                    float destination = e.x + offset * direction;
                    if (destination < e.teleportMinX || destination > e.teleportMaxX) destination = e.x - offset * direction;
                    e.x=Math.max(e.teleportMinX,Math.min(e.teleportMaxX,destination));
                    e.baseX = Math.max(e.teleportMinX,Math.min(e.teleportMaxX,e.baseX + (e.x - oldX)));
                    e.trailX=e.x;e.trailY=e.y;e.flashUntil=now+260L;
                    e.nextTeleportAt=now+8500L+random.nextInt(4500);
                    addBlast(e.x,e.y,Color.rgb(175,90,255),false,now);
                }
            }
            if (e.canLaser) {
                if(e.laserFireAt==0L)e.laserFireAt=now+9000L+random.nextInt(5000);
                if(now>=e.laserFireAt-900L)e.laserCharging=true;
                if(now>=e.laserFireAt){e.laserCharging=false;e.laserFireAt=now+10500L+random.nextInt(4500);if(Math.abs(e.x-playerX)<.20f){Shot beam=new Shot(e.x,playerY,0f);beam.missileStage=4;beams.add(beam);addPopup(e.x,e.y+.07f,"LASER!",coral);triggerShake(now,.28f);}}
            }
        }
        dogShots.addAll(beams);
        enemies.addAll(reinforcements);
    }

    private void updateLeaderboardZoom(long now) {
        if (!lbZoomActive) return;
        if (lbZoomClosing) {
            if (now - lbZoomClosingAt >= LB_ZOOM_TRANS_MS) {
                lbZoomActive = false;
                lbZoomClosing = false;
            }
        } else {
            if (now - lbZoomStartedAt >= LB_ZOOM_HOLD_MS + LB_ZOOM_TRANS_MS * 2L) {
                lbZoomClosing = true;
                lbZoomClosingAt = now;
            }
        }
    }

    private float getLeaderboardZoomProgress(long now) {
        if (!lbZoomActive) return 0f;
        if (lbZoomClosing) {
            float t = Math.min(1f, Math.max(0f, (now - lbZoomClosingAt) / (float) LB_ZOOM_TRANS_MS));
            return 1f - smoothStep(t);
        }
        long elapsed = Math.max(0L, now - lbZoomStartedAt);
        if (elapsed < LB_ZOOM_TRANS_MS) {
            return smoothStep(elapsed / (float) LB_ZOOM_TRANS_MS);
        }
        if (elapsed < LB_ZOOM_TRANS_MS + LB_ZOOM_HOLD_MS) {
            return 1f;
        }
        long outElapsed = elapsed - (LB_ZOOM_TRANS_MS + LB_ZOOM_HOLD_MS);
        if (outElapsed < LB_ZOOM_TRANS_MS) {
            return 1f - smoothStep(outElapsed / (float) LB_ZOOM_TRANS_MS);
        }
        return 0f;
    }

    private static float smoothStep(float t) {
        float c = Math.max(0f, Math.min(1f, t));
        return c * c * (3f - 2f * c);
    }

    private void updatePopups(float dt) {
        Iterator<Popup> pi = popups.iterator();
        while (pi.hasNext()) {
            Popup pop = pi.next();
            pop.y -= 0.00009f * dt;
            pop.life -= dt;
            if (pop.life <= 0f) pi.remove();
        }
    }

    /** Autonomous attract-mode demo battle running silently behind the start/difficulty screens. */
    private void updateDemo(long now, float dt, int w, int h) {
        if (now >= demoNextDecision) {
            demoNextDecision = now + 180L;
            float desired = playerX;

            if (!parachuteCats.isEmpty()) {
                desired = parachuteCats.get(0).x;
            } else if (!shieldDrops.isEmpty()) {
                desired = shieldDrops.get(0).x;
            } else if (!boneDrops.isEmpty()) {
                desired = boneDrops.get(0).x;
            } else if (boneX > 0.12f && boneX < 0.88f) {
                desired = boneX + boneDir * 0.04f;
            } else if (!enemies.isEmpty()) {
                Enemy targetDog = enemies.get((int) ((now / 900L) % enemies.size()));
                desired = targetDog.x;
            }

            for (Shot ds : dogShots) {
                if (ds.y > 0.56f && ds.y < 0.91f && Math.abs(ds.x - desired) < 0.075f) {
                    desired = (ds.x < 0.5f) ? ds.x + 0.13f : ds.x - 0.13f;
                    break;
                }
            }
            demoTargetX = Math.max(0.06f, Math.min(0.94f, desired));
        }

        float diff = demoTargetX - playerX;
        float maxStep = dt * 0.00040f;
        if (Math.abs(diff) <= maxStep) {
            playerX = demoTargetX;
        } else {
            playerX += Math.signum(diff) * maxStep;
        }

        if (now - lastShot >= 320L && !charlieDown(now)) {
            lastShot = now;
            muzzleFlashUntil = now + 70L;
            shots.add(new Shot(playerX, playerY - 0.038f, -0.00080f));
        }

        updateEnemyFormation(now, dt, true);

        Iterator<Shot> si = shots.iterator();
        while (si.hasNext()) {
            Shot s = si.next();
            s.y += s.v * dt;
            if (s.y < 0.08f) {
                si.remove();
                continue;
            }
            if (hitShield(s)) {
                si.remove();
                continue;
            }
            if (boneX >= 0 && Math.abs(s.x - boneX) < 0.038f && Math.abs(s.y - boneY) < 0.034f) {
                si.remove();
                addBlast(boneX, boneY, gold, false, now);
                boneDrops.add(new BoneDrop(boneX, boneY, boneValue));
                boneX = -1f;
                lastBone = now;
                continue;
            }
            if (mouseX >= 0 && Math.abs(s.x - mouseX) < 0.038f && Math.abs(s.y - mouseY) < 0.034f) {
                si.remove();
                addBlast(mouseX, mouseY, mint, false, now);
                parachuteCats.add(new ParachuteCat(mouseX, mouseY));
                mouseX = -1f;
                lastMouse = now;
                continue;
            }
            boolean hit = false;
            Iterator<Enemy> ei = enemies.iterator();
            while (ei.hasNext()) {
                Enemy e = ei.next();
                if (Math.abs(s.x - e.x) < 0.027f && Math.abs(s.y - e.y) < 0.035f) {
                    hit = true;
                    e.hp--;
                    e.flashUntil = now + 110L;
                    if (e.hp <= 0) {
                        ei.remove();
                        addBlast(e.x, e.y, rowColours[e.row], false, now);
                    }
                    break;
                }
            }
            if (hit) si.remove();
        }

        if (now - lastEnemyShot > 950L && !enemies.isEmpty()) {
            Enemy e = enemies.get(random.nextInt(enemies.size()));
            dogShots.add(new Shot(e.x, e.y + 0.03f, enemyShotSpeed));
            lastEnemyShot = now;
        }
        Iterator<Shot> di = dogShots.iterator();
        while (di.hasNext()) {
            Shot s = di.next();
            s.y += s.v * dt;
            if (s.y > 0.96f) {
                di.remove();
                continue;
            }
            if (hitShield(s)) {
                di.remove();
                continue;
            }
            if (Math.abs(s.x - playerX) < 0.034f && Math.abs(s.y - playerY) < 0.034f) {
                di.remove();
                if (now - hitAt >= CHARLIE_HIT_MS) {
                    hitAt = now;
                    addBlast(playerX, playerY, 0, true, now);
                }
            }
        }

        if (boneX < 0 && boneDrops.isEmpty() && now - lastBone > 7500L) {
            boneDir = random.nextBoolean() ? 1f : -1f;
            boneX = (boneDir > 0) ? -0.05f : 1.05f;
            bonePhase = random.nextFloat() * 6.2832f;
            boneSpeed = 0.00022f;
            boneValue = 200;
            lastBone = now;
        }
        if (boneX >= 0) {
            boneX += boneDir * boneSpeed * dt;
            boneY = BONE_TOP_Y + (float) Math.sin(now * 0.0075f + bonePhase) * 0.022f;
            if ((boneDir > 0 && boneX > 1.06f) || (boneDir < 0 && boneX < -0.06f)) {
                boneX = -1f;
                lastBone = now;
            }
        }

        Iterator<BoneDrop> bi = boneDrops.iterator();
        while (bi.hasNext()) {
            BoneDrop d = bi.next();
            d.vy = Math.min(0.00056f, d.vy + 0.00000055f * dt);
            d.y += d.vy * dt;
            d.x += (float) Math.sin(now * 0.008f + d.seed) * 0.00014f * dt;
            d.x = Math.max(0.06f, Math.min(0.94f, d.x));
            float sz = Math.min(w, h) * 0.033f;
            if (!charlieDown(now)
                    && Math.abs(d.x - playerX) * w < sz * 1.9f
                    && Math.abs(d.y - playerY) < 0.055f) {
                addBlast(d.x, d.y, mint, false, now);
                bi.remove();
                continue;
            }
            if (d.y > 0.96f) bi.remove();
        }

        Iterator<ShieldDrop> sdi = shieldDrops.iterator();
        while (sdi.hasNext()) {
            ShieldDrop sd = sdi.next();
            sd.y += sd.vy * dt;
            if (sd.y > 0.96f) sdi.remove();
        }

        Iterator<ParachuteCat> pci = parachuteCats.iterator();
        while (pci.hasNext()) {
            ParachuteCat pc = pci.next();
            pc.y += pc.vy * dt;
            float sz = Math.min(w, h) * 0.033f;
            if (!charlieDown(now)
                    && Math.abs(pc.x - playerX) * w < sz * 2.0f
                    && Math.abs(pc.y - playerY) < 0.06f) {
                addBlast(pc.x, pc.y, mint, false, now);
                pci.remove();
                continue;
            }
            if (pc.y > 0.96f) pci.remove();
        }

        updatePopups(dt);

        boolean reachedLow = false;
        for (Enemy e : enemies) {
            if (e.y > 0.72f) {
                reachedLow = true;
                break;
            }
        }
        if (enemies.isEmpty() || reachedLow) {
            demoWave = (demoWave % 4) + 1;
            int savedWave = wave;
            wave = demoWave;
            makeWave(false);
            wave = savedWave;
        }
    }

    private void addPopup(float x, float y, String text, int colour) {
        popups.add(new Popup(x, y, text, colour, 850f));
    }

    private void triggerShake(long now, float power) {
        shakeAt = now;
        shakePower = power;
    }

    /** Damages the first shield the bullet touches; removes the shield at zero hit points. */
    private boolean hitShield(Shot s) {
        for (int i = 0; i < shields.size(); i++) {
            Shield shield = shields.get(i);
            if (shield.hp > 0 && Math.abs(s.x - shield.x) < shield.width * .52f
                    && s.y > shield.y && s.y < shield.y + shield.height) {
                shield.hp--;
                if (shield.hp <= 0) shields.remove(i);
                return true;
            }
        }
        return false;
    }

    private void loseLife(long now) {
        lives--;
        hitAt = now;
        overchargeUntil = 0L;
        addBlast(playerX, playerY, 0, true, now);
        triggerShake(now, 1.0f);
        if (lives <= 0) {
            lives = 0;
            charlieGone = true;
            allLivesLostAt = now;
            mode = MODE_ALL_LIVES_LOST;
            playSound(PRIO_OVER, SND_OVER);
        } else {
            playSound(PRIO_LIFE, SND_LIFE);
        }
    }

    /** Moves to 3-letter Top-10 entry if qualified, or directly to Game Over. */
    private void finishRun(long now) {
        if (mode != MODE_PLAYING && mode != MODE_ALL_LIVES_LOST) return;
        if (!testRun && qualifiesForTop10(score)) {
            entryLetters[0] = 'A';
            entryLetters[1] = 'A';
            entryLetters[2] = 'A';
            entryTouched[0] = false;
            entryTouched[1] = false;
            entryTouched[2] = false;
            entrySlot = 0;
            nameEntryAt = now;
            mode = MODE_NAME_ENTRY;
            playSound(PRIO_NOTE, SND_BONE);
        } else {
            gameOverAt = now;
            mode = MODE_GAME_OVER;
        }
    }

    /** True while Charlie is destroyed or hidden inside his explosion. */
    private boolean charlieDown(long now) {
        return charlieGone || now - hitAt < CHARLIE_HIDE_MS;
    }

    /** Charlie is hidden during the blast, then blinks until the hit animation ends. */
    private boolean charlieVisible(long now) {
        if (charlieGone) return false;
        long since = now - hitAt;
        if (since >= CHARLIE_HIT_MS) return true;
        return since >= CHARLIE_HIDE_MS && ((now / 90) & 1) == 0;
    }

    private void addBlast(float x, float y, int colour, boolean big, long now) {
        blasts.add(new Blast(x, y, colour, big, now, random.nextFloat() * 6.2832f, false));
    }

    // ------------------------------------------------------------------ audio

    private void startAudio() {
        if (audioThread != null) return;
        try {
            HandlerThread t = new HandlerThread("arcade-audio") {
                @Override public void run() {
                    try { super.run(); }
                    finally { releaseTone(); }
                }
            };
            t.start();
            Looper lp = t.getLooper();
            if (lp == null) { t.quit(); return; }
            audioHandler = new Handler(lp, audioCallback);
            audioThread = t;
            audioHandler.sendEmptyMessage(MSG_INIT);
        } catch (RuntimeException e) {
            audioThread = null; audioHandler = null;
        }
    }

    private void stopAudio() {
        Handler hd = audioHandler; HandlerThread th = audioThread;
        audioHandler = null; audioThread = null; soundUntil = 0;
        if (hd != null) hd.removeCallbacksAndMessages(null);
        if (th != null) th.quit();
    }

    private void releaseTone() {
        ToneGenerator t = tone; tone = null;
        if (t != null) {
            try { t.stopTone(); t.release(); } catch (RuntimeException e) { /* ignore */ }
        }
    }

    private void stopSounds() {
        Handler hd = audioHandler; soundUntil = 0;
        if (hd != null) { hd.removeMessages(MSG_TONE); hd.sendEmptyMessage(MSG_STOP); }
    }

    /** Queues a sound; a quieter sound never cuts off a more important one that is still playing. */
    private void playSound(int prio, int[] seq) {
        if (soundMuted) return;
        Handler hd = audioHandler;
        if (hd == null) return;
        long now = SystemClock.uptimeMillis();
        if (now < soundUntil && prio < soundPrio) return;
        hd.removeMessages(MSG_TONE);
        long t = now;
        for (int i = 0; i + 2 < seq.length; i += 3) {
            hd.sendMessageAtTime(hd.obtainMessage(MSG_TONE, seq[i], seq[i + 1]), t);
            t += seq[i + 2];
        }
        soundUntil = t; soundPrio = prio;
    }

    // ---------------------------------------------------------------- drawing

    private boolean hasActiveBoss() {
        if (mode != MODE_PLAYING && mode != MODE_ALL_LIVES_LOST) return false;
        for (Enemy e : enemies) {
            if (e.isBoss) return true;
        }
        return false;
    }

    private void drawBackdrop(Canvas c, int w, int h, long now) {
        boolean bossWarp = hasActiveBoss();

        // The battlefield subtly changes colour as Charlie reaches new sectors.
        int sector = Math.max(0, (wave - 1) / 5) % 4;
        int sectorTint = sector == 0 ? Color.rgb(38, 62, 118)
                : sector == 1 ? Color.rgb(83, 42, 125)
                : sector == 2 ? Color.rgb(20, 105, 130)
                : Color.rgb(130, 38, 72);
        // Keep the original full-screen deep-space starfield clear: no oval battlefield glow.

        float moonX = w * 0.88f, moonY = h * 0.16f, moonR = Math.min(w, h) * 0.032f;
        p.setColor(Color.rgb(22, 34, 68));
        c.drawCircle(moonX, moonY, moonR, p);
        p.setColor(navy);
        c.drawCircle(moonX - moonR * 0.35f, moonY - moonR * 0.2f, moonR * 0.88f, p);

        if (bossWarp) {
            // Subtle warp-speed forward star-streaks radiating from center-sky vanishing point
            float vx = w * 0.5f;
            float vy = h * 0.36f;
            float maxDist = (float) Math.hypot(w * 0.65f, h * 0.72f);
            for (int i = 0; i < 68; i++) {
                double angle = (i * 2.3999632) + ((i % 7) * 0.13);
                float cosA = (float) Math.cos(angle);
                float sinA = (float) Math.sin(angle);
                float speed = 0.00055f + (i % 5) * 0.00014f;
                float phase = ((now * speed) + (i * 0.173f)) % 1.0f;
                // Exponential radial acceleration gives authentic forward warp perspective
                float rHead = 14f + (phase * phase) * maxDist;
                float streakLen = 8f + (phase * phase) * (maxDist * 0.16f);
                float rTail = Math.max(10f, rHead - streakLen);

                float x1 = vx + cosA * rTail;
                float y1 = vy + sinA * rTail;
                float x2 = vx + cosA * rHead;
                float y2 = vy + sinA * rHead;

                int alpha = (int) (35 + 115 * Math.sin(phase * Math.PI));
                int col = (i % 4 == 0) ? Color.rgb(155, 200, 255)
                        : (i % 4 == 1) ? Color.rgb(115, 155, 225)
                        : Color.rgb(90, 125, 195);
                p.setColor(withAlpha(col, alpha));
                p.setStrokeWidth(1.2f + phase * 1.6f);
                c.drawLine(x1, y1, x2, y2, p);
            }
        } else {
            // Parallax two-layer drifting starfield
            for (int i = 0; i < 54; i++) {
                boolean nearLayer = (i % 3 == 0);
                float drift = (now * (nearLayer ? 0.0045f : 0.0018f)) % w;
                float baseX = (i * 97 % 997) / 997f * w;
                float x = (baseX + drift) % w;
                float y = (i * 173 % 733) / 733f * h * 0.85f;
                int twinkle = 115 + (int) (110 * Math.sin(now * 0.004f + i * 1.3f));
                p.setColor(withAlpha(nearLayer ? Color.rgb(155, 190, 245) : Color.rgb(105, 138, 205), twinkle));
                float r = nearLayer ? 2.3f : 1.4f;
                c.drawCircle(x, y, r, p);
            }
        }

        // Crisp deep-space horizon line
        float groundY = h * 0.935f;
        p.setColor(Color.rgb(22, 38, 72));
        p.setStrokeWidth(2f);
        c.drawLine(w * 0.02f, groundY, w * 0.98f, groundY, p);
    }

    /** Renders the live attract-mode game slightly dimmed so menu & leaderboard text pops cleanly. */
    private void drawDemoBackground(Canvas c, int w, int h, long now) {
        for (Shield s : shields) drawShield(c, s, w, h);
        for (Enemy e : enemies) drawDog(c, e, w, h, now);
        for (Shot s : shots) drawShot(c, s, w, h, gold, false);
        for (Shot s : dogShots) drawShot(c, s, w, h, coral, true);
        if (boneX >= 0) drawTopBone(c, boneX * w, boneY * h, now);
        if (mouseX >= 0) drawBonusMouse(c, mouseX * w, mouseY * h, now);
        for (BoneDrop d : boneDrops) drawBoneDrop(c, d, w, h, now);
        for (ShieldDrop sd : shieldDrops) drawShieldDrop(c, sd, w, h, now);
        for (ParachuteCat pc : parachuteCats) drawParachuteCat(c, pc, w, h, now);
        for (PowerDrop pd : powerDrops) drawPowerDrop(c, pd, w, h, now);
        if (charlieVisible(now)) drawCharlie(c, playerX * w, playerY * h, now);
        drawBlasts(c, w, h, now);

        p.setColor(withAlpha(navy, 145));
        c.drawRect(0, 0, w, h, p);
    }

    /** Top-10 leaderboard with proper font-based spacing between 1st and 2nd place in both views. */
    private void drawLeaderboardBehindMenu(Canvas c, int w, int h, long now) {
        float zoom = (mode == MODE_TITLE) ? getLeaderboardZoomProgress(now) : 0f;

        if (zoom > 0.005f) {
            int pw = Math.max(200, w / 3);
            int ph = Math.max(260, h / 3);
            if (lbZoomBmp == null || lbZoomBmp.getWidth() != pw || lbZoomBmp.getHeight() != ph) {
                lbZoomBmp = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888);
                lbZoomCanvas = new Canvas(lbZoomBmp);
            }
            lbZoomBmp.eraseColor(Color.TRANSPARENT);

            p.setColor(withAlpha(navy, 238));
            lbZoomCanvas.drawRect(0, 0, pw, ph, p);
            renderLeaderboardContent(lbZoomCanvas, pw, ph, false, true);

            float fullW = w * 0.96f;
            float fullH = h * 0.94f;
            float baseW = w * 0.68f;
            float baseH = h * 0.42f;
            float curW = baseW + (fullW - baseW) * zoom;
            float curH = baseH + (fullH - baseH) * zoom;
            float baseCx = w * 0.5f;
            float baseCy = h * 0.24f;
            float cx = baseCx + (w * 0.5f - baseCx) * zoom;
            float cy = baseCy + (h * 0.5f - baseCy) * zoom;

            lbZoomDst.set(cx - curW * 0.5f, cy - curH * 0.5f, cx + curW * 0.5f, cy + curH * 0.5f);
            boolean oldFilter = p.isFilterBitmap();
            p.setFilterBitmap(false);
            c.drawBitmap(lbZoomBmp, null, lbZoomDst, p);
            p.setFilterBitmap(oldFilter);
            return;
        }

        renderLeaderboardContent(c, w, h, true, false);
    }

    private void renderLeaderboardContent(Canvas canvas, int w, int h, boolean withTitleBand, boolean pixelArtMode) {
        boolean oldAA = p.isAntiAlias();
        if (pixelArtMode) p.setAntiAlias(false);

        float titleSize = pixelArtMode
                ? Math.max(11f, h * 0.044f)
                : Math.max(16f, h * 0.034f);
        float firstSize = pixelArtMode
                ? Math.max(13f, h * 0.052f)
                : Math.max(19f, Math.min(w * 0.048f, h * 0.040f));
        float otherSize = pixelArtMode
                ? Math.max(8f, h * 0.033f)
                : Math.max(12f, Math.min(w * 0.031f, h * 0.024f));

        // Dynamic vertical positions derived from font heights so 1st & 2nd never touch
        float titleY = pixelArtMode ? (h * 0.085f + titleSize * 0.45f) : (h * 0.072f);
        float firstScoreY = titleY + firstSize * 1.45f + h * 0.028f;
        float secondScoreY = firstScoreY + otherSize * 1.55f + h * 0.016f;
        float otherRowStep = Math.max(otherSize * 1.48f, h * (pixelArtMode ? 0.068f : 0.052f));

        p.setTypeface(condensedBold);
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(blue);
        p.setTextSize(titleSize);
        canvas.drawText("TOP 10 SCORES", w * 0.5f, titleY, p);

        int count = Math.min(10, leaderboard.size());
        for (int i = 0; i < count; i++) {
            ScoreEntry entry = leaderboard.get(i);
            float y = (i == 0)
                    ? firstScoreY
                    : secondScoreY + (i - 1) * otherRowStep;
            boolean isLatest = (latestEntryInitials != null
                    && entry.score == latestEntryScore
                    && latestEntryInitials.equals(entry.initials)
                    && latestEntryDiff != null
                    && latestEntryDiff.equals(entry.difficulty));

            p.setColor(i == 0 ? gold : (isLatest ? mint : white));
            p.setTextSize(i == 0 ? firstSize : otherSize);
            String badge = isLatest ? "  *NEW*" : "";
            String line = formatOrdinal(i + 1) + ".  " + entry.score + "  "
                    + entry.initials + "  " + entry.difficulty + badge;
            canvas.drawText(line, w * 0.5f, y, p);
        }

        if (withTitleBand) {
            p.setColor(withAlpha(navy, 110));
            canvas.drawRect(0, h * 0.69f, w, h * 0.90f, p);
        }
        p.setTextAlign(Paint.Align.LEFT);
        p.setAntiAlias(oldAA);
    }

    private void drawTitleCard(Canvas c, int w, int h) {
        float cx = w * 0.5f, cy = h * 0.77f;

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(condensedBold);
        p.setColor(gold);
        p.setTextSize(Math.max(18f, Math.min(w * 0.064f, h * 0.050f)));
        c.drawText("CHARLIE'S BARK BLASTERS", cx, cy - h * 0.085f, p);

        p.setTypeface(Typeface.create("cursive", Typeface.NORMAL));
        p.setColor(mint);
        p.setTextSize(Math.max(17f, Math.min(w * 0.034f, h * 0.030f)));
        c.drawText("by Mark Hollis", cx, cy - h * 0.040f, p);

        p.setTypeface(condensedBold);
        p.setColor(white);
        p.setTextSize(Math.max(13f, Math.min(w * 0.035f, h * 0.024f)));
        c.drawText("PRESS OK FOR DIFFICULTY  -  UP FOR HIGH SCORES", cx, cy + h * 0.018f, p);

        p.setColor(mint);
        p.setTextSize(Math.max(12f, Math.min(w * 0.031f, h * 0.021f)));
        String sndLabel = soundMuted ? "SOUND: OFF (L/R)" : "SOUND: ON (L/R)";
        c.drawText("BONES & MICE DROP BONUSES  -  " + sndLabel, cx, cy + h * 0.068f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawDifficultyCard(Canvas c, int w, int h) {
        float cx = w * 0.5f, cy = h * 0.66f;
        float halfW = w * 0.43f, halfH = h * 0.22f;

        p.setColor(0xF505091B);
        c.drawRoundRect(cx - halfW, cy - halfH, cx + halfW, cy + halfH, 16f, 16f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3f);
        p.setColor(blue);
        c.drawRoundRect(cx - halfW, cy - halfH, cx + halfW, cy + halfH, 16f, 16f, p);
        p.setStyle(Paint.Style.FILL);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(condensedBold);
        p.setColor(gold);
        p.setTextSize(Math.max(18f, h * 0.044f));
        c.drawText("SELECT DIFFICULTY", cx, cy - h * 0.135f, p);

        float itemStart = cy - h * 0.055f;
        float itemStep = h * 0.065f;
        for (int i = 0; i < 3; i++) {
            boolean selected = (i == diffCursor);
            float iy = itemStart + i * itemStep;
            if (selected) {
                p.setColor(withAlpha(gold, 45));
                c.drawRoundRect(cx - halfW * 0.85f, iy - h * 0.028f,
                        cx + halfW * 0.85f, iy + h * 0.018f, 8f, 8f, p);
            }
            p.setColor(selected ? gold : white);
            p.setTextSize(Math.max(16f, h * 0.034f));
            String prefix = selected ? "> " : "  ";
            c.drawText(prefix + DIFF_UPPER[i], cx, iy, p);
        }

        p.setColor(mint);
        p.setTextSize(Math.max(12f, h * 0.022f));
        String sndTag = soundMuted ? "MUTED" : "SOUND ON";
        c.drawText(DIFF_DESCS[diffCursor] + "  -  " + sndTag + " (L/R)", cx, cy + h * 0.175f, p);
        if (touchMode) {
            p.setColor(withAlpha(Color.rgb(55, 85, 135), 225));
            c.drawRoundRect(w * 0.795f, h * 0.075f, w * 0.965f, h * 0.155f, 10f, 10f, p);
            p.setColor(white);
            p.setTextSize(Math.max(13f, h * 0.027f));
            c.drawText("BACK", w * 0.88f, h * 0.127f, p);
            p.setColor(withAlpha(mint, 210));
            c.drawRoundRect(w * 0.37f, h * 0.805f, w * 0.63f, h * 0.875f, 10f, 10f, p);
            p.setColor(navy);
            p.setTextSize(Math.max(14f, h * 0.029f));
            c.drawText("START", w * 0.5f, h * 0.852f, p);
        }
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawPauseOverlay(Canvas c, int w, int h) {
        p.setColor(0xC805091B);
        c.drawRect(0, 0, w, h, p);

        float cx = w * 0.5f, cy = h * 0.5f;
        float halfW = w * 0.39f, halfH = h * (testRun ? 0.25f : 0.17f);

        p.setColor(navy);
        c.drawRoundRect(cx - halfW, cy - halfH, cx + halfW, cy + halfH, 16f, 16f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3f);
        p.setColor(gold);
        c.drawRoundRect(cx - halfW, cy - halfH, cx + halfW, cy + halfH, 16f, 16f, p);
        p.setStyle(Paint.Style.FILL);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(condensedBold);
        p.setColor(gold);
        p.setTextSize(Math.max(22f, h * 0.052f));
        c.drawText("PAUSED", cx, cy - h * 0.045f, p);

        p.setColor(white);
        p.setTextSize(Math.max(13f, h * 0.024f));
        c.drawText((pauseCursor == 0 ? "> " : "") + "RESUME", cx, cy + h * 0.018f, p);
        if (testRun) {
            p.setColor(pauseCursor == 1 ? gold : mint);
            c.drawText((pauseCursor == 1 ? "> " : "") + "TEST COLLECTABLES & WEAPONS", cx, cy + h * 0.065f, p);
            p.setColor(pauseCursor == 2 ? gold : white);
            c.drawText((pauseCursor == 2 ? "> " : "") + "RESTART TEST WAVE", cx, cy + h * 0.112f, p);
            p.setColor(pauseCursor == 3 ? gold : coral);
            c.drawText((pauseCursor == 3 ? "> " : "") + "EXIT TEST", cx, cy + h * 0.158f, p);
        } else {
            p.setColor(mint); c.drawText("BACK / OK: RESUME  -  DOWN: TITLE", cx, cy + h * 0.082f, p);
        }
        p.setColor(mint);
        p.setTextSize(Math.max(11f, h * 0.019f));
        c.drawText("LEFT / RIGHT: " + (soundMuted ? "UNMUTE SOUND" : "MUTE SOUND"), cx, cy + h * 0.205f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawNameEntryScreen(Canvas c, int w, int h, long now) {
        p.setColor(0xE805091B);
        c.drawRect(0, 0, w, h, p);

        float cx = w * 0.5f, cy = h * 0.5f;
        float halfW = w * 0.44f, halfH = h * 0.25f;

        p.setColor(navy);
        c.drawRoundRect(cx - halfW, cy - halfH, cx + halfW, cy + halfH, 18f, 18f, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3f);
        p.setColor(gold);
        c.drawRoundRect(cx - halfW, cy - halfH, cx + halfW, cy + halfH, 18f, 18f, p);
        p.setStyle(Paint.Style.FILL);

        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(condensedBold);

        p.setColor(gold);
        p.setTextSize(Math.max(18f, Math.min(w * 0.06f, h * 0.048f)));
        c.drawText("CONGRATULATIONS! TOP 10", cx, cy - h * 0.135f, p);

        p.setColor(white);
        p.setTextSize(Math.max(16f, Math.min(w * 0.048f, h * 0.036f)));
        c.drawText("Your Score - " + score + "pts", cx, cy - h * 0.065f, p);

        float boxW = Math.min(w * 0.11f, h * 0.075f);
        float boxH = boxW * 1.25f;
        float gap = boxW * 0.45f;
        float totalW = 3f * boxW + 2f * gap;
        float startX = cx - totalW * 0.5f;
        float boxTop = cy - h * 0.025f;

        boolean blink = ((now / 350) & 1) == 0;
        for (int i = 0; i < 3; i++) {
            float bx = startX + i * (boxW + gap);
            float bcx = bx + boxW * 0.5f;
            float bcy = boxTop + boxH * 0.5f;
            boolean active = (i == entrySlot);

            p.setColor(active ? Color.rgb(22, 36, 68) : Color.rgb(12, 20, 42));
            c.drawRoundRect(bx, boxTop, bx + boxW, boxTop + boxH, 8f, 8f, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(active ? 3.5f : 2f);
            p.setColor(active ? gold : Color.rgb(90, 120, 170));
            c.drawRoundRect(bx, boxTop, bx + boxW, boxTop + boxH, 8f, 8f, p);
            p.setStyle(Paint.Style.FILL);

            if (active) {
                p.setColor(mint);
                p.setTextSize(Math.max(12f, boxH * 0.28f));
                c.drawText("^", bcx, boxTop - 4f, p);
                c.drawText("v", bcx, boxTop + boxH + boxH * 0.26f, p);
            }

            String glyph = entryTouched[i] ? String.valueOf(entryLetters[i]) : "_";
            p.setColor(active ? gold : white);
            p.setTextSize(Math.max(18f, boxH * 0.68f));
            c.drawText(glyph, bcx, bcy - (p.ascent() + p.descent()) * 0.5f, p);

            if (active && blink) {
                p.setColor(gold);
                p.setStrokeWidth(2.5f);
                c.drawLine(bx + boxW * 0.2f, boxTop + boxH - 6f, bx + boxW * 0.8f, boxTop + boxH - 6f, p);
            }
        }

        p.setColor(mint);
        p.setTextSize(Math.max(14f, Math.min(w * 0.042f, h * 0.030f)));
        c.drawText(formatInitials(entryLetters, entryTouched), cx, boxTop + boxH + h * 0.068f, p);

        p.setColor(Color.rgb(175, 195, 225));
        p.setTextSize(Math.max(11f, Math.min(w * 0.028f, h * 0.020f)));
        c.drawText("UP/DN A-Z  -  L/R SLOT  -  OK NEXT  -  BACK DONE", cx, cy + h * 0.215f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawHud(Canvas c, int w, int h, long now) {
        p.setColor(blue);
        p.setTextSize(Math.max(18, h * .034f));
        p.setTypeface(condensedBold);
        c.drawText("SCORE  " + score, w * .035f, h * .062f, p);
        c.drawText("BEST  " + highScore, w * .38f, h * .062f, p);
        c.drawText("WAVE  " + wave, w * .68f, h * .062f, p);

        float statusX = w * .035f;
        if (isShieldActive(now)) {
            long remSec = Math.max(1L, (shieldUntil - now + 999L) / 1000L);
            p.setColor(mint);
            p.setTextSize(Math.max(13f, h * .022f));
            c.drawText("SHIELD " + remSec + "S", statusX, h * .092f, p);
            statusX += w * .15f;
        }
        if (isOvercharged(now)) {
            long remSec = Math.max(1L, (overchargeUntil - now + 999L) / 1000L);
            p.setColor(gold); p.setTextSize(Math.max(13f, h * .022f));
            c.drawText("BOWTIE TWIN " + remSec + "S", statusX, h * .092f, p);
            statusX += w * .20f;
        }
        if(now<freezeUntil){p.setColor(Color.rgb(130,220,255));p.setTextSize(Math.max(12f,h*.020f));c.drawText("FREEZE",statusX,h*.092f,p);statusX+=w*.10f;}
        if(now<magnetUntil){p.setColor(mint);p.setTextSize(Math.max(12f,h*.020f));c.drawText("MAGNET",statusX,h*.092f,p);statusX+=w*.11f;}
        if(now<scoreFrenzyUntil){p.setColor(gold);p.setTextSize(Math.max(12f,h*.020f));c.drawText("SCORE 2X",statusX,h*.092f,p);statusX+=w*.12f;}
        if(now<tripleShotUntil){p.setColor(gold);p.setTextSize(Math.max(12f,h*.020f));c.drawText("TRIPLE PAW",statusX,h*.092f,p);statusX+=w*.14f;}
        if(now<megaLaserUntil){p.setColor(blue);p.setTextSize(Math.max(12f,h*.020f));c.drawText("MEGA LASER",statusX,h*.092f,p);}

        // Tuxedo mini cat-ear life icons
        float r = Math.max(6f, h * .011f);
        for (int i = 0; i < lives; i++) {
            float lx = w * .88f + i * (r * 2.5f);
            float ly = h * .053f;
            p.setColor(outlineShadow);
            c.drawCircle(lx, ly, r + 1.3f, p);
            p.setColor(catBlack);
            c.drawCircle(lx, ly, r, p);
            drawTriangle(c, lx - r * 0.85f, ly - r * 0.4f, lx - r * 0.45f, ly - r * 1.45f, lx - r * 0.05f, ly - r * 0.65f);
            drawTriangle(c, lx + r * 0.05f, ly - r * 0.65f, lx + r * 0.45f, ly - r * 1.45f, lx + r * 0.85f, ly - r * 0.4f);
            p.setColor(catWhite);
            c.drawCircle(lx, ly + r * 0.35f, r * 0.45f, p);
        }
    }

    private void drawCheatBanner(Canvas c, int w, int h, long elapsed) {
        float t = elapsed / (float) CHEAT_BANNER_MS;
        int alpha = (t < 0.15f) ? (int) (255 * (t / 0.15f))
                : (t > 0.75f) ? (int) (255 * ((1f - t) / 0.25f)) : 255;
        boolean flash = ((elapsed / 110) & 1) == 0;
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(condensedBold);
        p.setColor(withAlpha(flash ? mint : gold, alpha));
        p.setTextSize(Math.max(20f, h * 0.044f));
        c.drawText(cheatMessage, w * 0.5f, h * 0.52f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawWaveBanner(Canvas c, int w, int h, long elapsed) {
        float t = elapsed / (float) WAVE_BANNER_MS;
        int alpha = (t < 0.2f) ? (int) (255 * (t / 0.2f))
                : (t > 0.75f) ? (int) (255 * ((1f - t) / 0.25f)) : 255;
        boolean isBossWave = (wave >= 5 && wave % 5 == 0);
        boolean isChallengeWave = (wave >= 4 && wave % 4 == 0 && !isBossWave);
        String name = isBossWave ? "ALPHA HOUND BOSS WAVE!"
                : isChallengeWave ? "SPECIAL CHALLENGE: KENNEL RIOT!"
                : WAVE_NAMES[(wave - 1) % WAVE_NAMES.length];
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(condensedBold);
        p.setColor(withAlpha(isBossWave ? coral : isChallengeWave ? Color.rgb(80, 235, 255) : gold, alpha));
        p.setTextSize(Math.max(20f, h * 0.048f));
        c.drawText("WAVE " + wave, w * 0.5f, h * 0.54f, p);
        p.setColor(withAlpha(white, alpha));
        p.setTextSize(Math.max(14f, h * 0.025f));
        c.drawText(name + "  (" + DIFF_UPPER[difficulty] + ")", w * 0.5f, h * 0.58f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawAlphaHoundBoss(Canvas c, Enemy e, int w, int h, long now) {
        float x = e.x * w, y = e.y * h;
        float s = Math.min(w, h) * 0.044f;
        boolean flash = now < e.flashUntil;

        // Cartoon-meets-neon boss presentation: a lightweight animated energy halo.
        int haloColour = e.bossType == BOSS_BULLDOG ? Color.rgb(80, 225, 255)
                : e.bossType == BOSS_DOBERMAN ? Color.rgb(255, 55, 145)
                : Color.rgb(255, 190, 65);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f, s * 0.075f));
        p.setColor(withAlpha(haloColour, 105 + (int) (55 * Math.sin(now * 0.008f))));
        c.drawCircle(x, y, s * 1.62f, p);
        p.setStrokeWidth(Math.max(1f, s * 0.035f));
        p.setColor(withAlpha(haloColour, 65));
        c.drawCircle(x, y, s * 1.78f, p);
        p.setStyle(Paint.Style.FILL);

        if (e.bossType == BOSS_BULLDOG) {
            // 2. Armored Mech-Bulldog
            p.setColor(flash ? white : Color.rgb(57, 66, 86));
            c.drawRect(x - s * 1.25f, y - s * 0.25f, x + s * 1.25f, y + s * 0.40f, p);

            p.setColor(flash ? white : Color.rgb(74, 53, 37));
            drawTriangle(c, x - s * 0.95f, y - s * 0.55f, x - s * 1.35f, y - s * 0.15f, x - s * 0.85f, y - s * 0.05f);
            drawTriangle(c, x + s * 0.95f, y - s * 0.55f, x + s * 1.35f, y - s * 0.15f, x + s * 0.85f, y - s * 0.05f);

            p.setColor(flash ? white : Color.rgb(110, 79, 58));
            c.drawOval(x - s * 0.92f, y - s * 0.83f, x + s * 0.92f, y + s * 0.73f, p);

            p.setColor(flash ? Color.rgb(240, 240, 240) : Color.rgb(82, 56, 40));
            c.drawOval(x - s * 0.84f, y + s * 0.01f, x, y + s * 0.69f, p);
            c.drawOval(x, y + s * 0.01f, x + s * 0.84f, y + s * 0.69f, p);

            p.setColor(Color.rgb(17, 21, 34));
            p.setStrokeWidth(Math.max(3f, s * 0.14f));
            c.drawLine(x - s * 0.68f, y - s * 0.46f, x - s * 0.08f, y - s * 0.26f, p);
            c.drawLine(x + s * 0.68f, y - s * 0.46f, x + s * 0.08f, y - s * 0.26f, p);

            p.setColor(flash ? gold : Color.rgb(255, 42, 42));
            c.drawCircle(x - s * 0.35f, y - s * 0.22f, s * 0.14f, p);
            c.drawCircle(x + s * 0.35f, y - s * 0.22f, s * 0.14f, p);

            p.setColor(Color.rgb(20, 12, 16));
            c.drawRect(x - s * 0.45f, y + s * 0.26f, x + s * 0.45f, y + s * 0.46f, p);
            p.setColor(white);
            drawTriangle(c, x - s * 0.38f, y + s * 0.46f, x - s * 0.28f, y + s * 0.16f, x - s * 0.18f, y + s * 0.46f);
            drawTriangle(c, x + s * 0.18f, y + s * 0.46f, x + s * 0.28f, y + s * 0.16f, x + s * 0.38f, y + s * 0.46f);

            p.setColor(gold);
            c.drawRect(x - s * 1.05f, y + s * 0.56f, x + s * 1.05f, y + s * 0.78f, p);
            p.setColor(Color.rgb(223, 231, 245));
            for (int idx = -2; idx <= 2; idx++) {
                float sx = x + idx * s * 0.42f;
                drawTriangle(c, sx - s * 0.12f, y + s * 0.78f, sx, y + s * 1.02f, sx + s * 0.12f, y + s * 0.78f);
            }
        } else if (e.bossType == BOSS_DOBERMAN) {
            // 3. Cyber-Doberman Dreadnought
            p.setColor(flash ? white : Color.rgb(42, 57, 94));
            c.drawRect(x - s * 1.45f, y - s * 0.42f, x - s * 1.00f, y + s * 0.43f, p);
            c.drawRect(x + s * 1.00f, y - s * 0.42f, x + s * 1.45f, y + s * 0.43f, p);
            p.setColor(coral);
            c.drawCircle(x - s * 1.22f, y - s * 0.18f, s * 0.11f, p);
            c.drawCircle(x - s * 1.22f, y + s * 0.18f, s * 0.11f, p);
            c.drawCircle(x + s * 1.22f, y - s * 0.18f, s * 0.11f, p);
            c.drawCircle(x + s * 1.22f, y + s * 0.18f, s * 0.11f, p);

            p.setColor(flash ? white : Color.rgb(27, 34, 52));
            drawTriangle(c, x - s * 0.72f, y - s * 0.35f, x - s * 0.85f, y - s * 1.25f, x - s * 0.28f, y - s * 0.58f);
            drawTriangle(c, x + s * 0.72f, y - s * 0.35f, x + s * 0.85f, y - s * 1.25f, x + s * 0.28f, y - s * 0.58f);

            p.setColor(flash ? white : Color.rgb(35, 43, 64));
            earPath.reset();
            earPath.moveTo(x - s * 0.82f, y - s * 0.55f);
            earPath.lineTo(x + s * 0.82f, y - s * 0.55f);
            earPath.lineTo(x + s * 0.62f, y + s * 0.55f);
            earPath.lineTo(x - s * 0.62f, y + s * 0.55f);
            earPath.close();
            c.drawPath(earPath, p);

            // Crimson Cyber Visor
            p.setColor(flash ? gold : Color.rgb(255, 23, 68));
            earPath.reset();
            earPath.moveTo(x - s * 0.62f, y - s * 0.30f);
            earPath.lineTo(x, y - s * 0.12f);
            earPath.lineTo(x + s * 0.62f, y - s * 0.30f);
            earPath.lineTo(x + s * 0.52f, y - s * 0.08f);
            earPath.lineTo(x, y + s * 0.02f);
            earPath.lineTo(x - s * 0.52f, y - s * 0.08f);
            earPath.close();
            c.drawPath(earPath, p);

            p.setColor(flash ? white : Color.rgb(141, 110, 99));
            c.drawRect(x - s * 0.36f, y + s * 0.06f, x + s * 0.36f, y + s * 0.51f, p);
            p.setColor(white);
            drawTriangle(c, x - s * 0.28f, y + s * 0.51f, x - s * 0.20f, y + s * 0.75f, x - s * 0.12f, y + s * 0.51f);
            drawTriangle(c, x + s * 0.12f, y + s * 0.51f, x + s * 0.20f, y + s * 0.75f, x + s * 0.28f, y + s * 0.51f);
        } else {
            // 1. Spiked Rottweiler Warlord (default Boss #1)
            p.setColor(Color.rgb(207, 216, 220));
            drawTriangle(c, x - s * 0.90f, y - s * 0.10f, x - s * 1.48f, y - s * 0.45f, x - s * 0.95f, y + s * 0.30f);
            drawTriangle(c, x + s * 0.90f, y - s * 0.10f, x + s * 1.48f, y - s * 0.45f, x + s * 0.95f, y + s * 0.30f);

            p.setColor(flash ? white : Color.rgb(28, 31, 43));
            c.drawCircle(x, y - s * 0.05f, s * 0.88f, p);

            p.setColor(flash ? gold : Color.rgb(217, 130, 43));
            c.drawOval(x - s * 0.52f, y - s * 0.49f, x - s * 0.16f, y - s * 0.27f, p);
            c.drawOval(x + s * 0.16f, y - s * 0.49f, x + s * 0.52f, y - s * 0.27f, p);

            p.setColor(Color.rgb(255, 61, 0));
            c.drawCircle(x - s * 0.34f, y - s * 0.18f, s * 0.14f, p);
            c.drawCircle(x + s * 0.34f, y - s * 0.18f, s * 0.14f, p);

            p.setColor(flash ? white : Color.rgb(201, 114, 36));
            c.drawOval(x - s * 0.55f, y - s * 0.10f, x + s * 0.55f, y + s * 0.66f, p);

            p.setColor(Color.rgb(17, 17, 17));
            c.drawCircle(x, y + s * 0.12f, s * 0.18f, p);

            p.setColor(white);
            drawTriangle(c, x - s * 0.34f, y + s * 0.36f, x - s * 0.25f, y + s * 0.64f, x - s * 0.16f, y + s * 0.36f);
            drawTriangle(c, x + s * 0.16f, y + s * 0.36f, x + s * 0.25f, y + s * 0.64f, x + s * 0.34f, y + s * 0.36f);

            p.setColor(Color.rgb(55, 71, 79));
            c.drawRect(x - s * 0.45f, y + s * 0.72f, x + s * 0.45f, y + s * 1.06f, p);
            p.setColor(coral);
            c.drawCircle(x, y + s * 0.89f, s * 0.14f, p);
        }

        // Health bar above Boss
        float barW = s * 2.3f;
        float barH = Math.max(4f, s * 0.20f);
        float bx = x - barW * 0.5f;
        float by = y - s * 1.38f;
        p.setColor(Color.rgb(30, 35, 55));
        c.drawRect(bx, by, bx + barW, by + barH, p);
        float frac = Math.max(0f, Math.min(1f, e.hp / (float) e.maxHp));
        p.setColor(frac > 0.35f ? gold : coral);
        c.drawRect(bx, by, bx + barW * frac, by + barH, p);
    }

    private void drawDog(Canvas c, Enemy e, int w, int h, long now) {
        if (e.isBoss) {
            drawAlphaHoundBoss(c, e, w, h, now);
            return;
        }
        float x = e.x * w, y = e.y * h;
        float s = Math.min(w, h) * .022f;
        float damageFraction = e.maxHp > 1 ? (e.maxHp - e.hp) / (float) (e.maxHp - 1) : 0f;
        float wobble = damageFraction > 0f ? (float) Math.sin(now * .035f + e.col) * .035f * damageFraction : 0f;
        float balloonScale = e.maxHp > 1 ? 1f + damageFraction * .42f + wobble : 1f;
        c.save();
        c.scale(balloonScale, balloonScale, x, y);
        if (Math.abs(e.x-e.trailX)+Math.abs(e.y-e.trailY)>.0003f) {
            p.setColor(withAlpha(rowColours[e.row],85)); p.setStrokeWidth(Math.max(2f,s*.28f));
            c.drawLine(e.trailX*w,e.trailY*h,x,y,p);
        }
        boolean flip = e.rowDir < 0f;
        if (flip) {
            c.save();
            c.scale(-1f, 1f, x, y);
        }
        boolean flashing = now < e.flashUntil;
        if(flashing){p.setColor(withAlpha(white,130));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2f,s*.14f));c.drawCircle(x,y,s*1.28f,p);p.setStyle(Paint.Style.FILL);}
        if(e.maxHp>1){
            // A soft glossy highlight makes each damaged hound look increasingly inflated.
            p.setColor(withAlpha(white, 55 + (int)(damageFraction * 85)));
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(2f,s*.10f));
            c.drawOval(x-s*.58f,y-s*.34f,x-s*.12f,y-s*.02f,p);
            p.setStyle(Paint.Style.FILL);
        }
        if(e.laserCharging){
            p.setColor(withAlpha(coral,115));p.setStrokeWidth(Math.max(3f,s*.18f));c.drawLine(x-s*1.8f,y+s*.9f,x+s*1.8f,y+s*.9f,p);
            if(Math.abs(e.x-playerX)<.24f){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(3f,s*.14f));p.setColor(withAlpha(coral,150));c.drawCircle(playerX*w,playerY*h,Math.min(w,h)*(.028f+.006f*(float)Math.sin(now*.04f)),p);p.setStyle(Paint.Style.FILL);}
        }
        int diveWarnCol = ((now / 65) & 1) == 0 ? coral : white;
        int colour = flashing ? (e.diving && e.divePhase < 0f ? diveWarnCol : white) : rowColours[e.row];
        // Six recognisable breed-inspired looks, rotated through the existing formations.
        int breed = Math.floorMod(e.row * 3 + e.col + wave - 1, 6);
        int[] coats = {
                Color.rgb(185, 125, 65),  // Labrador
                Color.rgb(225, 205, 165), // Beagle
                Color.rgb(70, 55, 48),    // Rottweiler
                Color.rgb(205, 165, 105), // German Shepherd
                Color.rgb(235, 225, 205), // Bulldog
                Color.rgb(150, 95, 55)    // Dachshund
        };
        if (!flashing) colour = coats[breed];

        p.setColor(colour);
        c.drawOval(x - s * .9f, y - s * .45f, x + s * .9f, y + s * .48f, p);
        c.drawCircle(x + s * .83f, y - s * .28f, s * .43f, p);

        if (!flashing && (e.isBoss || (e.row + e.col) % 2 == 0)) {
            p.setColor(Color.rgb(85, 65, 55));
            c.drawCircle(x - s * .15f, y - s * .08f, s * .22f, p);
        }

        int earColour = flashing ? white : (breed == 2 ? Color.rgb(24, 20, 20)
                : breed == 4 ? Color.rgb(185, 155, 125) : colour);
        p.setColor(earColour);
        if (breed == 1 || breed == 4) {
            // Beagle and Bulldog: floppy ears.
            c.drawOval(x + s * .55f, y - s * .35f, x + s * 1.08f, y + s * .48f, p);
        } else if (breed == 5) {
            // Dachshund: long drooping ear.
            c.drawOval(x + s * .58f, y - s * .40f, x + s * 1.02f, y + s * .70f, p);
        } else {
            // Labrador, Rottweiler and Shepherd: upright or semi-pricked ears.
            drawTriangle(c, x + s * .58f, y - s * .48f,
                    x + s * (breed == 3 ? .70f : .80f), y - s * (breed == 3 ? 1.12f : .92f),
                    x + s * 1.02f, y - s * .45f);
        }
        // Breed markings: Rottweiler tan points, Beagle saddle, Shepherd muzzle stripe.
        if (!flashing && breed == 2) {
            p.setColor(Color.rgb(190, 125, 65));
            c.drawCircle(x + s * .76f, y - s * .08f, s * .13f, p);
            c.drawCircle(x + s * .18f, y + s * .30f, s * .10f, p);
        } else if (!flashing && breed == 1) {
            p.setColor(Color.rgb(95, 55, 40));
            c.drawOval(x - s * .35f, y - s * .30f, x + s * .22f, y + s * .18f, p);
        } else if (!flashing && breed == 3) {
            p.setColor(Color.rgb(55, 42, 32));
            c.drawOval(x - s * .48f, y - s * .40f, x + s * .12f, y + s * .15f, p);
        }

        p.setColor(Color.rgb(35, 28, 35));
        c.drawCircle(x + s * 1.02f, y - s * .34f, s * .075f, p);
        p.setColor(Color.rgb(245, 215, 190));
        c.drawCircle(x + s * 1.2f, y - s * .12f, s * .17f, p);
        p.setColor(Color.rgb(45, 35, 35));
        c.drawCircle(x + s * 1.30f, y - s * .14f, s * .05f, p);

        float phase = (float) Math.sin(now * .018f + e.col * .7f + e.row);
        p.setColor(colour);
        p.setStrokeWidth(Math.max(2, s * .22f));
        c.drawLine(x - s * .45f, y + s * .25f, x - s * .48f + phase * s * .28f, y + s * .85f, p);
        c.drawLine(x + s * .35f, y + s * .25f, x + s * .38f - phase * s * .28f, y + s * .85f, p);

        p.setStrokeWidth(Math.max(2, s * .2f));
        float wagRate = breed == 0 ? .026f : breed == 4 ? .009f : .014f;
        float wagSize = breed == 0 ? .48f : breed == 5 ? .12f : .28f;
        c.drawLine(x - s * .78f, y - s * .12f, x - s * 1.35f,
                y - s * .35f + (float) Math.sin(now * wagRate + e.col) * s * wagSize, p);

        if (e.isBoss) {
            // Spiked gold collar on Alpha Hound Boss
            p.setColor(gold);
            c.drawRect(x + s * .45f, y - s * .48f, x + s * .66f, y + s * .12f, p);
        } else if (e.maxHp > 1) {
            p.setColor(e.hp > 1 ? gold : coral);
            c.drawRect(x - s * .35f, y + s * .18f, x + s * .52f, y + s * .36f, p);
            c.drawCircle(x + s * .52f, y + s * .38f, s * .11f, p);
        } else if (e.row >= 2) {
            p.setColor(blue);
            c.drawRect(x - s * .35f, y + s * .2f, x + s * .45f, y + s * .34f, p);
        }

        if (flip) c.restore();
        c.restore();

        // Health bar above Alpha Hound Boss
        if (e.isBoss) {
            float barW = s * 2.2f;
            float barH = Math.max(4f, s * 0.22f);
            float bx = x - barW * 0.5f;
            float by = y - s * 1.25f;
            p.setColor(Color.rgb(30, 35, 55));
            c.drawRect(bx, by, bx + barW, by + barH, p);
            float frac = Math.max(0f, Math.min(1f, e.hp / (float) e.maxHp));
            p.setColor(frac > 0.35f ? gold : coral);
            c.drawRect(bx, by, bx + barW * frac, by + barH, p);
        }
    }

    /** Black-and-white tuxedo Charlie with a thin outline shadow, muzzle flash, and optional 15s shield aura. */
    private void drawCharlie(Canvas c, float x, float y, long now) {
        float s = Math.min(getWidth(), getHeight()) * .033f;
        boolean golden = now < goldenFishUntil;
        if (golden) {
            c.save(); c.translate(x,y); c.scale(2f,2f); c.translate(-x,-y);
            float aura=s*(1.25f+.12f*(float)Math.sin(now*.025f));
            p.setColor(withAlpha(gold,70)); c.drawCircle(x,y,aura*1.55f,p);
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(3f,s*.18f)); p.setColor(withAlpha(gold,220));
            c.drawCircle(x,y,aura*1.2f,p); p.setStyle(Paint.Style.FILL);
        }

        // Thruster flame when moving
        if (left || right) {
            float flicker = 0.8f + 0.35f * (float) Math.sin(now * 0.05f);
            p.setColor(coral);
            c.drawCircle(x, y + s * .95f, s * .26f * flicker, p);
            p.setColor(gold);
            c.drawCircle(x, y + s * .90f, s * .15f * flicker, p);
        }

        float wag = (float) Math.sin(now * .009f) * s * .5f;

        // Thin outline shadow behind cat & ship for crisp separation from dark backdrop
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f, s * 0.11f));
        p.setColor(withAlpha(outlineShadow, 175));
        c.drawCircle(x, y - s * .45f, s * .65f, p);
        c.drawOval(x - s * .78f, y - s * .38f, x + s * .78f, y + s * .58f, p);
        c.drawRoundRect(x - s * 1.38f, y + s * .29f, x + s * 1.38f, y + s * .93f, s * .24f, s * .24f, p);
        p.setStyle(Paint.Style.FILL);

        // Swishing tuxedo tail (black with white tip)
        p.setColor(catBlack);
        p.setStrokeWidth(Math.max(3, s * .16f));
        c.drawLine(x + s * .55f, y + s * .3f, x + s * 1.25f, y + s * .05f + wag, p);
        p.setColor(catWhite);
        c.drawCircle(x + s * 1.25f, y + s * .05f + wag, s * .11f, p);

        // Black tuxedo body & head
        p.setColor(catBlack);
        c.drawOval(x - s * .75f, y - s * .35f, x + s * .75f, y + s * .55f, p);
        c.drawCircle(x, y - s * .45f, s * .62f, p);

        // Tuxedo white bib/chest & white paws
        p.setColor(catWhite);
        c.drawOval(x - s * .34f, y - s * .18f, x + s * .34f, y + s * .44f, p);
        c.drawCircle(x - s * .52f, y + s * .34f, s * .14f, p);
        c.drawCircle(x + s * .52f, y + s * .34f, s * .14f, p);

        // Tuxedo bowtie (glows mint when Bowtie Overcharge is active!)
        p.setColor(isOvercharged(now) ? mint : coral);
        drawTriangle(c, x, y - s * .05f, x - s * .22f, y - s * .16f, x - s * .22f, y + s * .06f);
        drawTriangle(c, x, y - s * .05f, x + s * .22f, y - s * .16f, x + s * .22f, y + s * .06f);
        c.drawCircle(x, y - s * .05f, s * .06f, p);

        // Black outer ears
        p.setColor(catBlack);
        drawTriangle(c, x - s * .48f, y - s * .72f, x - s * .18f, y - s * 1.35f, x - s * .02f, y - s * .62f);
        drawTriangle(c, x + s * .12f, y - s * .62f, x + s * .38f, y - s * 1.35f, x + s * .62f, y - s * .7f);

        // Pink inner ears
        p.setColor(Color.rgb(255, 165, 175));
        drawTriangle(c, x - s * .40f, y - s * .74f, x - s * .21f, y - s * 1.18f, x - s * .10f, y - s * .68f);
        drawTriangle(c, x + s * .18f, y - s * .68f, x + s * .37f, y - s * 1.18f, x + s * .52f, y - s * .72f);

        // White muzzle patch & eyes
        p.setColor(catWhite);
        c.drawCircle(x, y - s * .23f, s * .20f, p);
        p.setColor(mint);
        c.drawCircle(x - s * .22f, y - s * .46f, s * .085f, p);
        c.drawCircle(x + s * .22f, y - s * .46f, s * .085f, p);
        p.setColor(Color.rgb(20, 22, 30));
        c.drawCircle(x - s * .22f, y - s * .46f, s * .045f, p);
        c.drawCircle(x + s * .22f, y - s * .46f, s * .045f, p);
        p.setColor(Color.rgb(255, 145, 165));
        c.drawCircle(x, y - s * .27f, s * .045f, p);

        // White whiskers
        p.setColor(catWhite);
        p.setStrokeWidth(Math.max(1.5f, s * .045f));
        c.drawLine(x - s * .28f, y - s * .24f, x - s * .76f, y - s * .30f, p);
        c.drawLine(x - s * .28f, y - s * .18f, x - s * .74f, y - s * .14f, p);
        c.drawLine(x + s * .28f, y - s * .24f, x + s * .76f, y - s * .30f, p);
        c.drawLine(x + s * .28f, y - s * .18f, x + s * .74f, y - s * .14f, p);

        // Spaceship hull & yarn-cannon barrel
        p.setColor(blue);
        c.drawRoundRect(x - s * 1.35f, y + s * .32f, x + s * 1.35f, y + s * .9f, s * .22f, s * .22f, p);
        p.setColor(Color.rgb(120, 210, 255));
        c.drawRoundRect(x - s * .95f, y + s * .45f, x + s * .95f, y + s * .62f, s * .1f, s * .1f, p);
        p.setColor(isOvercharged(now) ? mint : gold);
        c.drawRect(x - s * .18f, y + s * .05f, x + s * .18f, y + s * .35f, p);

        // High-impact multi-spike muzzle flash burst on shot
        if (now < muzzleFlashUntil) {
            float tFlash = Math.max(0f, Math.min(1f, (muzzleFlashUntil - now) / 135f));
            float fy = y - s * .02f;
            float rOuter = s * (0.35f + 0.45f * tFlash);
            int alpha = (int) (240 * tFlash);
            p.setColor(withAlpha(gold, alpha));
            drawTriangle(c, x - rOuter, fy, x + rOuter, fy, x, fy - rOuter * 1.45f);
            drawTriangle(c, x - rOuter * 1.25f, fy + rOuter * 0.25f, x + rOuter * 1.25f, fy + rOuter * 0.25f, x, fy - rOuter * 0.55f);
            p.setColor(withAlpha(white, alpha));
            c.drawCircle(x, fy, rOuter * 0.52f, p);
        }

        // Active 15-second GOLD shield dome around Charlie
        if (isShieldActive(now)) {
            long rem = shieldUntil - now;
            boolean blinkWarn = rem < 3000L && ((now / 110) & 1) == 0;
            if (!blinkWarn) {
                float pulse = 1f + 0.05f * (float) Math.sin(now * 0.015f);
                int brightGold = Color.rgb(255, 215, 0);
                p.setColor(withAlpha(brightGold, 55));
                c.drawCircle(x, y + s * 0.05f, s * 1.62f * pulse, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(3f, s * 0.13f));
                p.setColor(withAlpha(brightGold, 235));
                c.drawCircle(x, y + s * 0.05f, s * 1.62f * pulse, p);
                p.setStyle(Paint.Style.FILL);
            }
        }
        if (golden) {
            p.setColor(gold); p.setStrokeWidth(Math.max(2f,s*.10f));
            for(int i=0;i<6;i++){float a=(float)(now*.006+i*Math.PI/3);float r=s*(1.55f+(i%2)*.22f);c.drawLine(x+(float)Math.cos(a)*r,y+(float)Math.sin(a)*r,x+(float)Math.cos(a)*(r+s*.35f),y+(float)Math.sin(a)*(r+s*.35f),p);}
            c.restore();
        }
    }

    private void drawGoldenFishBanner(Canvas c,int w,int h,long now) {
        boolean active=now<goldenFishUntil; float pulse=1f+.045f*(float)Math.sin(now*.018f);
        p.setTextAlign(Paint.Align.CENTER);p.setTypeface(condensedBold);
        p.setTextSize(h*(active?.052f:.034f)*pulse);p.setColor(withAlpha(gold,active?245:150));
        c.drawText(active?"GOLDEN FISH FRENZY!":"GOLDEN POWER FADING",w*.5f,h*.16f,p);
        if(active){p.setTextSize(h*.022f);p.setColor(white);long seconds=Math.max(0L,(goldenFishUntil-now+999L)/1000L);c.drawText(seconds+" SECONDS OF POWER",w*.5f,h*.195f,p);}
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawTriangle(Canvas c, float x1, float y1, float x2, float y2, float x3, float y3) {
        earPath.reset();
        earPath.moveTo(x1, y1);
        earPath.lineTo(x2, y2);
        earPath.lineTo(x3, y3);
        earPath.close();
        c.drawPath(earPath, p);
    }

    private void drawShield(Canvas c, Shield s, int w, int h) {
        if (s.hp <= 0) return;
        float left = s.x * w - s.width * w / 2f;
        float right = s.x * w + s.width * w / 2f;
        float top = s.y * h;
        float bottom = s.y * h + s.height * h;

        p.setColor(Color.rgb(70, 135, 170));
        c.drawRoundRect(left, top, right, bottom, 8, 8, p);
        p.setColor(navy);
        for (int i = 0; i < 3; i++) {
            c.drawRect(left + i * s.width * w / 3f, top + s.height * h * .55f,
                    left + (i + 1) * s.width * w / 3f - 3, bottom, p);
        }

        if (s.hp <= 2) {
            p.setColor(navy);
            p.setStrokeWidth(2.5f);
            c.drawLine(s.x * w - s.width * w * 0.2f, top, s.x * w, top + s.height * h * 0.35f, p);
            if (s.hp == 1) {
                c.drawLine(s.x * w + s.width * w * 0.22f, top, s.x * w + s.width * w * 0.05f, top + s.height * h * 0.42f, p);
            }
        }

        float cx = s.x * w, cy = top + s.height * h * .46f;
        p.setTypeface(condensedBold);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.max(12, Math.min(s.height * h * .58f, s.width * w * .48f)));
        p.setColor(s.hp == 1 ? coral : white);
        c.drawText(DIGITS[Math.min(s.hp, 5)], cx, cy - (p.ascent() + p.descent()) / 2f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawShot(Canvas c, Shot s, int w, int h, int colour, boolean isDogShot) {
        p.setColor(colour);
        p.setStrokeWidth(Math.max(3, h * .0045f));
        float x = s.x * w, y = s.y * h;
        float unit = Math.min(w, h);

        if (isDogShot) {
            if (s.missileStage == 4) {
                float beamLeft=Math.max(0f,(s.x-.20f)*w),beamRight=Math.min(w,(s.x+.20f)*w);
                p.setColor(withAlpha(coral,90));p.setStrokeWidth(Math.max(14f,h*.026f));c.drawLine(beamLeft,y,beamRight,y,p);
                p.setColor(withAlpha(white,230));p.setStrokeWidth(Math.max(4f,h*.006f));c.drawLine(beamLeft,y,beamRight,y,p);
            } else if (s.missileStage == 1) {
                // Larger primary Alpha Hound warhead missile with limited smoky/fiery trail
                float mw = unit * 0.011f;
                float mh = unit * 0.024f;
                p.setColor(withAlpha(Color.rgb(165, 175, 195), 115));
                c.drawCircle(x, y - mh * 1.75f, mw * 1.05f, p);
                p.setColor(withAlpha(Color.rgb(255, 145, 45), 185));
                c.drawCircle(x, y - mh * 1.15f, mw * 0.85f, p);

                p.setColor(Color.rgb(207, 216, 220));
                c.drawRect(x - mw, y - mh, x + mw, y + mh * 0.45f, p);
                p.setColor(coral);
                drawTriangle(c, x - mw * 1.15f, y + mh * 0.45f, x + mw * 1.15f, y + mh * 0.45f, x, y + mh * 1.35f);
                p.setColor(gold);
                drawTriangle(c, x - mw, y - mh, x - mw * 1.8f, y - mh * 1.3f, x - mw, y - mh * 0.35f);
                drawTriangle(c, x + mw, y - mh, x + mw * 1.8f, y - mh * 1.3f, x + mw, y - mh * 0.35f);
            } else if (s.missileStage == 2) {
                // Smaller split warheads (↙ and ↘) with short exhaust puff
                float mw = unit * 0.0068f;
                float mh = unit * 0.015f;
                float angleDeg = (float) Math.toDegrees(Math.atan2(s.v, s.vx)) - 90f;
                c.save();
                c.translate(x, y);
                c.rotate(angleDeg);
                p.setColor(withAlpha(Color.rgb(255, 160, 60), 160));
                c.drawCircle(0f, -mh * 1.1f, mw * 0.8f, p);
                p.setColor(gold);
                c.drawRect(-mw, -mh * 0.8f, mw, mh * 0.4f, p);
                p.setColor(coral);
                drawTriangle(c, -mw * 1.15f, mh * 0.4f, mw * 1.15f, mh * 0.4f, 0f, mh * 1.25f);
                c.restore();
            } else {
                float z = unit * 0.005f;
                c.drawLine(x - z, y, x + z, y + h * 0.012f, p);
                c.drawLine(x + z, y + h * 0.012f, x - z, y + h * 0.024f, p);
            }
        } else if (s.missileStage == 3) {
            float mw = unit * .010f, mh = unit * .022f;
            p.setColor(withAlpha(gold, 145)); c.drawCircle(x, y + mh, mw * 1.7f, p);
            p.setColor(coral); c.drawOval(x - mw, y - mh, x + mw, y + mh, p);
            p.setColor(white); c.drawCircle(x, y, mw * .48f, p);
            p.setColor(gold); c.drawLine(x, y + mh, x, y + mh * 2.2f, p);
        } else if (s.playerKind > 0) {
            float r=unit*(s.playerKind==3?.015f:s.playerKind==1?.012f:.008f);
            p.setColor(withAlpha(gold,s.playerKind==3?175:105));c.drawCircle(x,y,r*2.0f,p);
            p.setColor(s.playerKind==2?gold:white);c.drawCircle(x,y,r,p);
            p.setColor(withAlpha(mint,170));p.setStrokeWidth(Math.max(2f,h*.004f));c.drawLine(x,y,x-s.vx*16f,y-s.v*16f,p);
            if(s.playerKind==3){Enemy target=nearestEnemy(s.x,s.y);if(target!=null){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(Math.max(2f,unit*.003f));p.setColor(withAlpha(gold,175));c.drawCircle(target.x*w,target.y*h,unit*.022f,p);p.setStyle(Paint.Style.FILL);}}
        } else if (s.ricocheted) {
            // Glowing ricochet yarn-bolt
            float r = unit * 0.009f;
            p.setColor(withAlpha(mint, 110));
            c.drawCircle(x, y, r * 1.7f, p);
            p.setColor(white);
            c.drawCircle(x, y, r * 0.8f, p);
            p.setColor(mint);
            p.setStrokeWidth(Math.max(2.5f, h * .004f));
            c.drawLine(x - s.vx * 18f, y - s.v * 18f, x, y, p);
        } else {
            p.setColor(withAlpha(colour,85));p.setStrokeWidth(Math.max(6f,h*.010f));
            c.drawLine(x,y,x-s.vx*18f,y-s.v*18f,p);
            p.setColor(colour);p.setStrokeWidth(Math.max(3f,h*.0045f));
            c.drawLine(x, y, x, y + h * .025f, p);
            p.setColor(white);c.drawCircle(x,y,Math.max(2f,unit*.0035f),p);
        }
    }

    private void drawTopBone(Canvas c, float x, float y, long now) {
        float u = Math.min(getWidth(), getHeight()) * 0.020f;
        c.save();
        c.translate(x, y);
        c.rotate((float) Math.sin(now * 0.012f) * 14f);

        p.setColor(Color.rgb(255, 235, 190));
        c.drawRoundRect(-u, -u * 0.30f, u, u * 0.30f, u * 0.25f, u * 0.25f, p);
        c.drawCircle(-u, -u * 0.36f, u * 0.38f, p);
        c.drawCircle(-u, u * 0.36f, u * 0.38f, p);
        c.drawCircle(u, -u * 0.36f, u * 0.38f, p);
        c.drawCircle(u, u * 0.36f, u * 0.38f, p);
        c.restore();

        p.setColor(gold);
        p.setTypeface(condensedBold);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.max(11f, getHeight() * 0.019f));
        c.drawText("+" + boneValue, x, y + u * 1.45f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawBonusMouse(Canvas c, float x, float y, long now) {
        float s = Math.min(getWidth(), getHeight()) * 0.019f;
        c.save();
        c.translate(x, y);
        if (mouseDir < 0f) c.scale(-1f, 1f);

        // Curled tail
        p.setColor(Color.rgb(225, 165, 180));
        p.setStrokeWidth(Math.max(2f, s * 0.14f));
        c.drawLine(-s * 0.85f, s * 0.05f, -s * 1.45f, -s * 0.25f + (float) Math.sin(now * 0.02f) * s * 0.2f, p);

        // Mouse body & round ears
        p.setColor(Color.rgb(175, 185, 205));
        c.drawOval(-s * 0.9f, -s * 0.45f, s * 0.85f, s * 0.45f, p);
        p.setColor(Color.rgb(235, 175, 190));
        c.drawCircle(s * 0.35f, -s * 0.55f, s * 0.26f, p);
        c.drawCircle(s * 0.05f, -s * 0.52f, s * 0.22f, p);

        // Eye & pink nose
        p.setColor(catBlack);
        c.drawCircle(s * 0.52f, -s * 0.12f, s * 0.08f, p);
        p.setColor(Color.rgb(255, 130, 155));
        c.drawCircle(s * 0.92f, 0f, s * 0.10f, p);
        c.restore();

        p.setColor(mint);
        p.setTypeface(condensedBold);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.max(10f, getHeight() * 0.017f));
        c.drawText("1-UP MOUSE", x, y + s * 1.35f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }


    private void drawPowerDrop(Canvas c, PowerDrop d, int w, int h, long now) {
        float x=d.x*w, y=d.y*h, r=Math.min(w,h)*.023f;
        float pulse=1f+.16f*(float)Math.sin(now*.016f+d.seed);
        int colour=d.type==POWER_GOLDEN_FISH?gold:(d.type==POWER_FREEZE?Color.rgb(110,210,255):mint);
        p.setColor(withAlpha(colour,75)); c.drawCircle(x,y,r*1.8f*pulse,p);
        p.setColor(colour); c.drawCircle(x,y,r,p);
        p.setColor(navy); p.setTypeface(condensedBold); p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.max(10f,r*.78f)); c.drawText(powerGlyph(d.type),x,y-(p.ascent()+p.descent())*.5f,p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private String powerGlyph(int type) {
        switch(type) {
            case POWER_MISSILES:return "M"; case POWER_TRIPLE:return "3";
            case POWER_PAW_BOMB:return "P"; case POWER_FREEZE:return "ICE";
            case POWER_MAGNET:return "Y"; case POWER_GOLD_SHIELD:return "S";
            case POWER_LIGHTNING:return "Z"; case POWER_MEGA_LASER:return "L";
            case POWER_SCORE_FRENZY:return "2X"; default:return "★";
        }
    }

    private String powerName(int type) {
        switch(type) {
            case POWER_MISSILES:return "HEAT-SEEKING MISSILES"; case POWER_TRIPLE:return "TRIPLE PAW";
            case POWER_PAW_BOMB:return "PAW BOMB"; case POWER_FREEZE:return "FREEZE PAW";
            case POWER_MAGNET:return "YARN MAGNET"; case POWER_GOLD_SHIELD:return "GOLDEN PAW SHIELD";
            case POWER_LIGHTNING:return "LIGHTNING STRIKE"; case POWER_MEGA_LASER:return "MEGA LASER";
            case POWER_SCORE_FRENZY:return "SCORE FRENZY"; default:return "GOLDEN FISH FRENZY";
        }
    }

    private void activatePower(int type, long now) {
        switch(type) {
            case POWER_MISSILES:
                for(int i=0;i<3;i++) { Shot m=new Shot(playerX+(i-1)*.018f,playerY-.045f,-.00075f); m.missileStage=3; shots.add(m); }
                addPopup(playerX,playerY-.07f,"HEAT-SEEKING MISSILES!",gold); playSound(PRIO_LIFE,SND_CHEAT); break;
            case POWER_TRIPLE:
                tripleShotUntil=now+TRIPLE_PAW_DURATION_MS; addPopup(playerX,playerY-.07f,"TRIPLE PAW!",gold); playSound(PRIO_LIFE,SND_CHEAT); break;
            case POWER_PAW_BOMB: triggerMegaExplosion(playerX,.48f,now); addPopup(playerX,.42f,"PAW BOMB!",gold); break;
            case POWER_FREEZE:
                freezeUntil=now+FREEZE_DURATION_MS; addPopup(.5f,.48f,"FREEZE PAW!",Color.rgb(130,220,255)); playSound(PRIO_LIFE,SND_CHEAT); break;
            case POWER_MAGNET:
                magnetUntil=now+MAGNET_MIN_DURATION_MS+random.nextInt((int)(MAGNET_MAX_DURATION_MS-MAGNET_MIN_DURATION_MS+1L)); addPopup(playerX,playerY-.07f,"YARN MAGNET!",mint); playSound(PRIO_LIFE,SND_CHEAT); break;
            case POWER_GOLD_SHIELD:
                reflectUntil=now+REFLECT_DURATION_MS; shieldUntil=Math.max(shieldUntil,reflectUntil); addPopup(playerX,playerY-.07f,"GOLDEN PAW SHIELD!",gold); playSound(PRIO_LIFE,SND_SHIELD); break;
            case POWER_LIGHTNING:
                int strikes=0; Iterator<Enemy> li=enemies.iterator();
                while(li.hasNext() && strikes<5) { Enemy e=li.next(); e.hp-=e.isBoss?2:e.hp; e.flashUntil=now+220L; addBlast(e.x,e.y,Color.rgb(130,210,255),true,now); strikes++; if(e.hp<=0){awardEnemyPoints(e.isBoss?250+wave*40:(4-e.row)*10*e.maxHp,now);li.remove();} }
                triggerShake(now,.8f); addPopup(.5f,.43f,"LIGHTNING STRIKE!",Color.rgb(130,210,255)); playSound(PRIO_LIFE,SND_MEGA_BOOM); break;
            case POWER_MEGA_LASER:
                megaLaserUntil=now+MEGA_LASER_DURATION_MS; nextLaserTickAt=0L; addPopup(playerX,.76f,"MEGA LASER!",blue); playSound(PRIO_LIFE,SND_CHEAT); break;
            case POWER_SCORE_FRENZY:
                scoreFrenzyUntil=now+SCORE_FRENZY_DURATION_MS; addPopup(playerX,playerY-.07f,"SCORE FRENZY 2X!",gold); playSound(PRIO_LIFE,SND_CHEAT); break;
            case POWER_GOLDEN_FISH:
                goldenFishUntil=now+GOLDEN_FISH_DURATION_MS; nextGoldenShotAt=now; shieldUntil=Math.max(shieldUntil,goldenFishUntil);
                triggerShake(now,1.15f); addPopup(.5f,.45f,"GOLDEN FISH FRENZY!",gold); playSound(PRIO_OVER,SND_GOLDEN); break;
        }
    }

    private Enemy nearestEnemy(float x,float y) {
        Enemy best=null; float bestD=Float.MAX_VALUE;
        for(Enemy e:enemies){float dx=e.x-x,dy=e.y-y,d=dx*dx+dy*dy;if(d<bestD){bestD=d;best=e;}}
        return best;
    }

    private void awardEnemyPoints(int points,long now) {
        if(now<scoreFrenzyUntil) points*=2;
        if(now<comboUntil) killStreak++; else killStreak=1;
        comboUntil=now+1500L;
        int multiplier=killStreak>=10?4:killStreak>=8?3:killStreak>=5?2:1;
        score+=points*multiplier;
        String label=null;
        if(killStreak==2)label="DOUBLE KILL!"; else if(killStreak==3)label="TRIPLE KILL!";
        else if(killStreak==5)label="PAWESOME!"; else if(killStreak==8)label="UNLEASHED!";
        else if(killStreak==10)label="FURRY FRENZY!";
        if(label!=null)addPopup(.5f,.34f,label,gold);
    }

    private void drawBoneDrop(Canvas c, BoneDrop d, int w, int h, long now) {
        float x = d.x * w, y = d.y * h;
        float r = Math.min(w, h) * 0.022f;
        float pulse = 1f + 0.12f * (float) Math.sin(now * 0.018f);

        p.setColor(withAlpha(gold, 70));
        c.drawCircle(x, y, r * 1.45f * pulse, p);
        p.setColor(gold);
        c.drawCircle(x, y, r, p);
        p.setColor(navy);
        p.setTypeface(condensedBold);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.max(11f, r * 0.95f));
        c.drawText("+" + d.points, x, y - (p.ascent() + p.descent()) / 2f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void drawShieldDrop(Canvas c, ShieldDrop sd, int w, int h, long now) {
        float x = sd.x * w, y = sd.y * h;
        float r = Math.min(w, h) * 0.022f;
        float pulse = 1f + 0.12f * (float) Math.sin(now * 0.016f);
        int brightGold = Color.rgb(255, 215, 0);

        p.setColor(withAlpha(brightGold, 75));
        c.drawCircle(x, y, r * 1.45f * pulse, p);
        p.setColor(brightGold);
        c.drawCircle(x, y, r, p);
        p.setColor(catBlack);
        p.setTypeface(condensedBold);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.max(11f, r * 0.92f));
        c.drawText("SHD", x, y - (p.ascent() + p.descent()) / 2f, p);
        p.setTextAlign(Paint.Align.LEFT);
    }

    /** Forward-facing cat under a parachute with dangling legs gliding down. */
    private void drawParachuteCat(Canvas c, ParachuteCat pc, int w, int h, long now) {
        float x = pc.x * w, y = pc.y * h;
        float s = Math.min(w, h) * 0.023f;
        float sway = (float) Math.sin(now * 0.007f + pc.seed) * 6f;

        c.save();
        c.translate(x, y);
        c.rotate(sway * 0.45f);

        // Parachute canopy
        p.setColor(coral);
        c.drawArc(-s * 1.45f, -s * 2.55f, s * 1.45f, -s * 0.95f, 180f, 180f, true, p);
        p.setColor(catWhite);
        c.drawArc(-s * 0.55f, -s * 2.55f, s * 0.55f, -s * 0.95f, 180f, 180f, true, p);

        // Parachute cords
        p.setColor(catWhite);
        p.setStrokeWidth(Math.max(1.5f, s * 0.07f));
        c.drawLine(-s * 1.35f, -s * 1.75f, -s * 0.45f, -s * 0.55f, p);
        c.drawLine(s * 1.35f, -s * 1.75f, s * 0.45f, -s * 0.55f, p);

        // Dangling legs swinging gently
        float legSwing = (float) Math.sin(now * 0.012f + pc.seed) * s * 0.25f;
        p.setColor(catBlack);
        p.setStrokeWidth(Math.max(2f, s * 0.14f));
        c.drawLine(-s * 0.28f, s * 0.45f, -s * 0.35f + legSwing, s * 1.05f, p);
        c.drawLine(s * 0.28f, s * 0.45f, s * 0.35f - legSwing, s * 1.05f, p);

        // Forward-facing tuxedo cat
        p.setColor(outlineShadow);
        c.drawCircle(0f, -s * 0.32f, s * 0.56f, p);
        p.setColor(catBlack);
        c.drawOval(-s * 0.55f, -s * 0.15f, s * 0.55f, s * 0.55f, p);
        c.drawCircle(0f, -s * 0.35f, s * 0.50f, p);
        drawTriangle(c, -s * 0.42f, -s * 0.55f, -s * 0.22f, -s * 1.02f, -s * 0.02f, -s * 0.55f);
        drawTriangle(c, s * 0.02f, -s * 0.55f, s * 0.22f, -s * 1.02f, s * 0.42f, -s * 0.55f);

        p.setColor(catWhite);
        c.drawOval(-s * 0.24f, -s * 0.08f, s * 0.24f, s * 0.45f, p);
        p.setColor(mint);
        c.drawCircle(-s * 0.18f, -s * 0.36f, s * 0.07f, p);
        c.drawCircle(s * 0.18f, -s * 0.36f, s * 0.07f, p);

        c.restore();
    }

    private void drawPopups(Canvas c, int w, int h, long now) {
        p.setTypeface(condensedBold);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.max(14f, h * 0.026f));
        for (Popup pop : popups) {
            int alpha = (int) (255 * Math.max(0f, Math.min(1f, pop.life / 450f)));
            p.setColor(withAlpha(pop.colour, alpha));
            c.drawText(pop.text, pop.x * w, pop.y * h, p);
        }
        p.setTextAlign(Paint.Align.LEFT);
    }

    private static int withAlpha(int colour, int a) {
        return (colour & 0x00FFFFFF) | (Math.max(0, Math.min(255, a)) << 24);
    }

    /** Draws and expires all running explosions (everything is derived from elapsed time). */
    private void drawBlasts(Canvas c, int w, int h, long now) {
        float unit = Math.min(w, h);
        Iterator<Blast> it = blasts.iterator();
        while (it.hasNext()) {
            Blast b = it.next();
            long dur = b.flashRing ? 340L : (b.mega ? MEGA_BLAST_MS : (b.big ? CHARLIE_BLAST_MS : DOG_BLAST_MS));
            float t = (now - b.start) / (float) dur;
            if (t >= 1f) { it.remove(); continue; }
            if (t < 0f) t = 0f;
            if (b.flashRing) {
                float out = 1f - (1f - t) * (1f - t);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(3f, unit * 0.014f * (1f - t)));
                p.setColor(withAlpha(b.colour, (int) (235 * (1f - t))));
                c.drawCircle(b.x * w, b.y * h, unit * (0.04f + 0.22f * out), p);
                p.setStyle(Paint.Style.FILL);
            } else if (b.mega) drawMegaBlast(c, b, w, h, t, unit * .033f);
            else if (b.big) drawCharlieBlast(c, b, w, h, t, unit * .033f);
            else drawDogBlast(c, b, w, h, t, unit * .022f);
        }
        p.setStyle(Paint.Style.FILL);
    }

    /** ~2x the size of Charlie's explosion when a falling Bonus Points token is shot. */
    private void drawMegaBlast(Canvas c, Blast b, int w, int h, float t, float s) {
        float x = b.x * w, y = b.y * h, out = 1f - (1f - t) * (1f - t);
        int fade = (int) (255 * (1f - t));
        float bigS = s * 2.0f;

        p.setColor(withAlpha(Color.rgb(255, 65, 35), fade * 9 / 10));
        c.drawCircle(x, y, bigS * (.75f + 2.3f * out), p);
        p.setColor(withAlpha(Color.rgb(255, 185, 45), fade));
        c.drawCircle(x, y, bigS * (.5f + 1.55f * out), p);

        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(3f, bigS * .25f * (1f - t)));
        p.setColor(withAlpha(gold, fade));
        c.drawCircle(x, y, bigS * (.9f + 3.8f * out), p);
        p.setColor(withAlpha(coral, fade));
        c.drawCircle(x, y, bigS * (.6f + 2.8f * out), p);
        p.setStyle(Paint.Style.FILL);

        float q = Math.max(2.5f, bigS * .20f * (1f - t));
        for (int i = 0; i < 18; i++) {
            float a = b.seed + i * 0.34906f;
            float d = bigS * (.8f + 3.4f * out) * (.55f + (i % 4) * .15f);
            float px = x + (float) Math.cos(a) * d, py = y + (float) Math.sin(a) * d;
            int col = (i % 3 == 0) ? gold : (i % 3 == 1) ? coral : white;
            p.setColor(withAlpha(col, fade));
            c.drawRect(px - q, py - q, px + q, py + q, p);
        }
    }

    /** Small, quick burst: bright flash, thin ring and eight pixel sparks in the dog's colour. */
    private void drawDogBlast(Canvas c, Blast b, int w, int h, float t, float s) {
        float x = b.x * w, y = b.y * h, out = 1f - (1f - t) * (1f - t);
        int fade = (int) (255 * (1f - t));
        p.setColor(withAlpha(Color.rgb(255, 245, 200), fade));
        c.drawCircle(x, y, s * (1.4f - .9f * t), p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2, s * .28f * (1f - t)));
        p.setColor(withAlpha(Color.rgb(255, 200, 90), fade));
        c.drawCircle(x, y, s * (.5f + 2.4f * out), p);
        p.setStyle(Paint.Style.FILL);
        float q = Math.max(1.5f, s * .3f * (1f - t));
        for (int i = 0; i < 8; i++) {
            float a = b.seed + i * .7854f;
            float d = s * (.6f + 2.1f * out) * (i % 2 == 0 ? 1f : .7f);
            float px = x + (float) Math.cos(a) * d, py = y + (float) Math.sin(a) * d;
            p.setColor(withAlpha(i % 2 == 0 ? b.colour : Color.rgb(255, 245, 200), fade));
            c.drawRect(px - q, py - q, px + q, py + q, p);
        }
    }

    /** Big red/orange fireball with shockwave rings and falling debris in Charlie's colours. */
    private void drawCharlieBlast(Canvas c, Blast b, int w, int h, float t, float s) {
        float x = b.x * w, y = b.y * h - s * .1f, out = 1f - (1f - t) * (1f - t);
        int fade = (int) (255 * (1f - t));
        p.setColor(withAlpha(Color.rgb(255, 60, 40), fade * 9 / 10));
        c.drawCircle(x, y, s * (.7f + 2.3f * out), p);
        p.setColor(withAlpha(Color.rgb(255, 170, 50), fade));
        c.drawCircle(x, y, s * (.5f + 1.5f * out), p);
        if (t < .4f) {
            float u = t / .4f;
            p.setColor(withAlpha(Color.rgb(255, 255, 235), (int) (255 * (1f - u))));
            c.drawCircle(x, y, s * (.5f + 1.6f * u), p);
        }
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2, s * .24f * (1f - t)));
        p.setColor(withAlpha(Color.rgb(255, 90, 60), fade));
        c.drawCircle(x, y, s * (.9f + 4.2f * out), p);
        if (t > .2f) {
            float u = (t - .2f) / .8f;
            p.setColor(withAlpha(white, (int) (200 * (1f - u))));
            c.drawCircle(x, y, s * (.6f + 3.4f * u), p);
        }
        p.setStyle(Paint.Style.FILL);
        float q = Math.max(2f, s * .2f * (1f - t));
        for (int i = 0; i < 14; i++) {
            float a = b.seed + i * .4488f;
            float d = s * (.8f + 3.4f * out) * (.55f + (i % 4) * .15f);
            float px = x + (float) Math.cos(a) * d, py = y + (float) Math.sin(a) * d + s * 1.6f * t * t;
            int col = (i % 4 == 0) ? catBlack : (i % 4 == 1) ? blue : (i % 4 == 2) ? gold : catWhite;
            p.setColor(withAlpha(col, fade));
            c.drawRect(px - q, py - q, px + q, py + q, p);
        }
    }

    /** Giant pixelated slam-in for ALL LIVES LOST / GAME OVER before Top-10 entry or Game Over. */
    private void drawAllLivesLost(Canvas c, int w, int h, long now) {
        long elapsed = Math.max(0L, now - allLivesLostAt);

        int dim = (int) (145 * Math.min(1f, elapsed / 650f));
        p.setColor(withAlpha(Color.BLACK, dim));
        c.drawRect(0, 0, w, h, p);

        int bw = 240, bh = 96;
        if (pixelBannerBmp == null) {
            pixelBannerBmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
            pixelBannerCanvas = new Canvas(pixelBannerBmp);
        }
        pixelBannerBmp.eraseColor(Color.TRANSPARENT);

        boolean oldAA = p.isAntiAlias();
        p.setAntiAlias(false);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTypeface(condensedBold);

        p.setColor(0xE005091B);
        pixelBannerCanvas.drawRect(4, 6, bw - 4, bh - 6, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(2f);
        p.setColor(coral);
        pixelBannerCanvas.drawRect(4, 6, bw - 4, bh - 6, p);
        p.setStyle(Paint.Style.FILL);

        p.setColor(coral);
        p.setTextSize(22f);
        pixelBannerCanvas.drawText("ALL LIVES LOST", bw * 0.5f, 39f, p);

        p.setColor(gold);
        p.setTextSize(28f);
        pixelBannerCanvas.drawText("GAME OVER", bw * 0.5f, 74f, p);

        p.setTextAlign(Paint.Align.LEFT);
        p.setAntiAlias(oldAA);

        float zoomMs = 650f;
        float t = Math.min(1f, elapsed / zoomMs);
        float ease = 1f - (float) Math.pow(1f - t, 3);
        float bounce = (t < 1f) ? (float) Math.sin(t * Math.PI) * -0.08f : 0f;
        float scale = (5.2f - 4.2f * ease) + bounce;
        if (t < 1f) {
            scale = Math.round(scale * 12f) / 12f;
        }

        float targetW = w * 0.78f * scale;
        float targetH = targetW * (bh / (float) bw);
        float cx = w * 0.5f, cy = h * 0.5f;

        pixelBannerDst.set(
                cx - targetW * 0.5f,
                cy - targetH * 0.5f,
                cx + targetW * 0.5f,
                cy + targetH * 0.5f
        );

        boolean oldFilter = p.isFilterBitmap();
        p.setFilterBitmap(false);
        c.drawBitmap(pixelBannerBmp, null, pixelBannerDst, p);
        p.setFilterBitmap(oldFilter);
    }

    /** Dramatic game-over screen with cooldown before returning to the start screen. */
    private void drawGameOver(Canvas c, int w, int h, long now) {
        long elapsed = Math.max(0L, now - gameOverAt);
        float t = Math.min(1f, elapsed / (float) GAME_OVER_ANIM_MS);
        float k = (t < 1f) ? (1f - (float) Math.pow(1f - t, 3) * (float) Math.cos(t * Math.PI * 1.2f)) : 1f;
        k = Math.max(0.06f, k);

        int dim = (int) (150 * Math.min(1f, elapsed / 500f));
        p.setColor(withAlpha(Color.BLACK, dim));
        c.drawRect(0, 0, w, h, p);

        boolean ready = elapsed >= GAME_OVER_LOCK_MS;
        String stats = "SCORE " + score + "  -  WAVE " + wave + "  -  " + DIFF_UPPER[difficulty];
        String prompt;
        if (!ready) {
            int secs = (int) Math.ceil((GAME_OVER_LOCK_MS - elapsed) / 1000.0);
            prompt = "WAIT " + secs + "S...";
        } else {
            prompt = "PRESS OK FOR START MENU";
        }
        overlay(c, w, h, newRecord ? "NEW HIGH SCORE!" : "GAME OVER", stats, k, prompt);
    }

    /** Centred message panel; k scales panel and text about the screen centre (1 = full size). */
    private void overlay(Canvas c, int w, int h, String title, String subtitle, float k, String actionLine) {
        float cx = w * .5f, cy = h * .5f;
        float halfW = w * .42f * k;
        float halfH = h * (actionLine != null ? .20f : .16f) * k;

        p.setColor(0xE005091B);
        c.drawRoundRect(cx - halfW, cy - halfH, cx + halfW, cy + halfH, 16f * k, 16f * k, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f, 3f * k));
        p.setColor(gold);
        c.drawRoundRect(cx - halfW, cy - halfH, cx + halfW, cy + halfH, 16f * k, 16f * k, p);
        p.setStyle(Paint.Style.FILL);

        p.setColor(gold);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(Math.max(1f, h * .072f * k));
        p.setTypeface(condensedBold);
        c.drawText(title, cx, cy - (actionLine != null ? h * .055f : h * .025f) * k, p);

        p.setColor(white);
        p.setTextSize(Math.max(1f, h * .026f * k));
        c.drawText(subtitle, cx, cy + (actionLine != null ? h * .025f : h * .055f) * k, p);

        if (actionLine != null) {
            p.setColor(actionLine.startsWith("WAIT") ? coral : mint);
            p.setTextSize(Math.max(1f, h * .029f * k));
            c.drawText(actionLine, cx, cy + h * .115f * k, p);
        }
        p.setTextAlign(Paint.Align.LEFT);
    }

    private static class ScoreEntry {
        final String initials;
        final int score;
        final String difficulty;
        ScoreEntry(String initials, int score, String difficulty) {
            this.initials = initials;
            this.score = score;
            this.difficulty = difficulty;
        }
    }

    private static class Enemy {
        float x, y, baseX, baseY, rowDir = 1f, diveOriginX, diveOriginY, divePhase;
        int row, col, hp, maxHp, bossType;
        boolean diving, isBoss, laserCharging, canTeleport, canLaser;
        long flashUntil, nextBossFireAt, nextTeleportAt, laserFireAt, nextReinforceAt;
        float teleportMinX = 0.07f, teleportMaxX = 0.93f;
        float trailX,trailY;
        int diveStyle=0;
        Enemy(float x, float y, int r, int c, int hp, boolean isBoss) {
            this.x = x; this.y = y; this.baseX = x; this.baseY = y;
            row = r; col = c; this.hp = hp; this.maxHp = hp;
            this.isBoss = isBoss;
        }
    }
    private static class Shot {
        float x, y, vx, v;
        int pairId = 0;
        int playerKind = 0;
        int missileStage = 0; // 0 = regular, 1/2 = boss warheads, 3 = player heat-seeking missile
        boolean ricocheted = false;
        Enemy lastHitEnemy = null;
        Shot(float x, float y, float v) {
            this.x = x; this.y = y; this.vx = 0f; this.v = v;
        }
        static Shot playerPaired(float x, float y, float v, int pairId) {
            Shot s = new Shot(x, y, v);
            s.pairId = pairId;
            return s;
        }
        static Shot bossMissile(float x, float y, float vx, float v, int stage) {
            Shot s = new Shot(x, y, v);
            s.vx = vx;
            s.missileStage = stage;
            return s;
        }
    }
    private static class Shield { float x, y, width = .095f, height = .075f; int hp = 4; Shield(float x, float y) { this.x = x; this.y = y; } }
    private static class BoneDrop {
        float x, y, vy = 0.00018f, seed;
        int points;
        BoneDrop(float x, float y, int points) {
            this.x = x; this.y = y; this.points = points; this.seed = (x + y) * 19f;
        }
    }
    private static class PowerDrop {
        float x,y,vy=.00018f,seed; int type;
        PowerDrop(float x,float y,int type){this.x=x;this.y=y;this.type=type;this.seed=(x+y)*37f;}
    }
    private static class ShieldDrop {
        float x, y, vy = 0.00017f, seed;
        ShieldDrop(float x, float y) {
            this.x = x; this.y = y; this.seed = (x + y) * 23f;
        }
    }
    private static class ParachuteCat {
        float x, y, vy = 0.00013f, seed;
        ParachuteCat(float x, float y) {
            this.x = x; this.y = y; this.seed = (x + y) * 31f;
        }
    }
    private static class Popup {
        float x, y, life;
        final String text;
        final int colour;
        Popup(float x, float y, String text, int colour, float life) {
            this.x = x; this.y = y; this.text = text; this.colour = colour; this.life = life;
        }
    }
    private static class Blast {
        final float x, y, seed; final int colour; final boolean big, mega, flashRing; final long start;
        Blast(float x, float y, int colour, boolean big, long start, float seed, boolean mega) {
            this(x, y, colour, big, start, seed, mega, false);
        }
        Blast(float x, float y, int colour, boolean big, long start, float seed, boolean mega, boolean flashRing) {
            this.x = x; this.y = y; this.colour = colour; this.big = big;
            this.start = start; this.seed = seed; this.mega = mega; this.flashRing = flashRing;
        }
    }
}
