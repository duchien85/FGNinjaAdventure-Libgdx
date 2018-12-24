package com.fgdev.game.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.fgdev.game.Constants;

public class Assets implements Disposable, AssetErrorListener {

    public static final String TAG = Assets.class.getName();

    public static final Assets instance = new Assets();

    private AssetManager assetManager;
    // Textures
    public AssetTexture textures;
    // Sound && Font
    public AssetSounds sounds;
    public AssetMusic music;
    public AssetFonts fonts;
    // Player
    public AssetPlayer player;
    // Item
    public AssetItem item;
    public AssetGoldCoin goldCoin;
    public AssetFeather feather;
    // Object
    public AssetObjectDecoration assetObjectDecoration;
    // Enemy
    public AssetZombie zombie;
    public AssetRobot robot;

    // singleton: prevent instantiation from other classes
    private Assets() {}

    public void init(AssetManager assetManager) {
        this.assetManager = assetManager;
        // set asset manager error handler
        assetManager.setErrorListener(this);
        // load texture png
        assetManager.load("images/bg.png", Texture.class);
        assetManager.load("images/bg1.png", Texture.class);
        assetManager.load("images/bg2.png", Texture.class);
        assetManager.load("images/bg3.png", Texture.class);
        // load texture atlas
        assetManager.load(Constants.TEXTURE_ATLAS_ITEM, TextureAtlas.class);
        assetManager.load(Constants.TEXTURE_ATLAS_PLAYER_BOY, TextureAtlas.class);
        assetManager.load(Constants.TEXTURE_ATLAS_PLAYER_GIRL, TextureAtlas.class);
        assetManager.load(Constants.TEXTURE_ATLAS_ZOMBIE, TextureAtlas.class);
        assetManager.load(Constants.TEXTURE_ATLAS_ROBOT, TextureAtlas.class);
        // load sounds
        assetManager.load("sounds/click.wav", Sound.class);
        assetManager.load("sounds/glide.wav", Sound.class);
        assetManager.load("sounds/add_life.wav", Sound.class);
        assetManager.load("sounds/pickup_coin.wav", Sound.class);
        assetManager.load("sounds/pickup_feather.wav", Sound.class);
        assetManager.load("sounds/live_lost.wav", Sound.class);
        // load music
        assetManager.load("musics/background1.mp3", Music.class);
        assetManager.load("musics/background2.mp3", Music.class);
        assetManager.load("musics/menubackground.mp3", Music.class);
        assetManager.load("musics/run.mp3", Music.class);
        // start loading assets and wait until finished
        assetManager.finishLoading();
        Gdx.app.debug(TAG, "# of assets loaded: "
                + assetManager.getAssetNames().size);
        for (String a : assetManager.getAssetNames())
            Gdx.app.debug(TAG, "asset: " + a);

        TextureAtlas atlasPlayer =
                assetManager.get(GamePreferences.instance.isGirl ? Constants.TEXTURE_ATLAS_PLAYER_GIRL : Constants.TEXTURE_ATLAS_PLAYER_BOY);
        // enable texture filtering for pixel smoothing
        for (Texture texture: atlasPlayer.getTextures())
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        TextureAtlas atlasItem =
                assetManager.get(Constants.TEXTURE_ATLAS_ITEM);
        // enable texture filtering for pixel smoothing
        for (Texture texture: atlasItem.getTextures())
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        TextureAtlas atlasZombie =
                assetManager.get(Constants.TEXTURE_ATLAS_ZOMBIE);
        // enable texture filtering for pixel smoothing
        for (Texture texture: atlasZombie.getTextures())
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        TextureAtlas atlasRobot =
                assetManager.get(Constants.TEXTURE_ATLAS_ROBOT);
        // enable texture filtering for pixel smoothing
        for (Texture texture: atlasRobot.getTextures())
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // create game resource objects
        fonts = new AssetFonts();
        player = new AssetPlayer(atlasPlayer);
        goldCoin = new AssetGoldCoin(atlasItem);
        feather = new AssetFeather(atlasItem);
        item = new AssetItem(atlasItem);
        music = new AssetMusic(assetManager);
        sounds = new AssetSounds(assetManager);
        textures = new AssetTexture(assetManager);
        assetObjectDecoration = new AssetObjectDecoration(atlasItem);
        zombie = new AssetZombie(atlasZombie);
        robot = new AssetRobot(atlasRobot);
    }

    public void loadCharacter() {
        TextureAtlas atlasPlayer =
                assetManager.get(GamePreferences.instance.isGirl ? Constants.TEXTURE_ATLAS_PLAYER_GIRL : Constants.TEXTURE_ATLAS_PLAYER_BOY);

        // enable texture filtering for pixel smoothing
        for (Texture texture: atlasPlayer.getTextures())
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        player = new AssetPlayer(atlasPlayer);
    }

    @Override
    public void error(AssetDescriptor asset, Throwable throwable) {
        Gdx.app.error(TAG, "Couldn't load asset '"
                + asset.fileName + "'", throwable);
    }

    @Override
    public void dispose() {
        assetManager.dispose();
        fonts.defaultSmall.dispose();
        fonts.defaultNormal.dispose();
        fonts.defaultBig.dispose();
    }

    public class AssetTexture {

        public final Texture background;
        public final Texture background1;
        public final Texture background2;
        public final Texture background3;

        public AssetTexture (AssetManager am) {
            background = am.get("images/bg.png", Texture.class);
            background1 = am.get("images/bg1.png", Texture.class);
            background2 = am.get("images/bg2.png", Texture.class);
            background3 = am.get("images/bg3.png", Texture.class);
        }

    }

    public class AssetSounds {
        public final Sound glide;
        public final Sound pickupCoin;
        public final Sound pickupFeather;
        public final Sound liveLost;
        public final Sound addLife;
        public final Sound click;

        public AssetSounds (AssetManager am) {
            glide = am.get("sounds/glide.wav", Sound.class);
            pickupCoin = am.get("sounds/pickup_coin.wav", Sound.class);
            pickupFeather = am.get("sounds/pickup_feather.wav", Sound.class);
            liveLost = am.get("sounds/live_lost.wav", Sound.class);
            addLife = am.get("sounds/add_life.wav", Sound.class);
            click = am.get("sounds/click.wav", Sound.class);
        }

    }

    public class AssetMusic {

        public final Music background1;
        public final Music background2;
        public final Music menuBackground;
        public final Music run;

        public AssetMusic (AssetManager am) {
            background1 = am.get("musics/background1.mp3", Music.class);
            background2 = am.get("musics/background2.mp3", Music.class);
            menuBackground = am.get("musics/menubackground.mp3", Music.class);
            run = am.get("musics/run.mp3", Music.class);
        }

    }

    public class AssetFonts {
        public final BitmapFont defaultSmall;
        public final BitmapFont defaultNormal;
        public final BitmapFont defaultBig;
        public final BitmapFont textFontSmall;
        public final BitmapFont textFontNormal;
        public final BitmapFont textFont;

        public AssetFonts() {
            // create three fonts using Libgdx's 15px bitmap font
            defaultSmall = new BitmapFont(
                    Gdx.files.internal("fonts/arial-15.fnt"), true);
            defaultNormal = new BitmapFont(
                    Gdx.files.internal("fonts/arial-15.fnt"), true);
            defaultBig = new BitmapFont(
                    Gdx.files.internal("fonts/arial-15.fnt"), true);
            textFontSmall = new BitmapFont(
                    Gdx.files.internal("fonts/last-ninja.fnt"), true);
            textFontNormal = new BitmapFont(
                    Gdx.files.internal("fonts/last-ninja.fnt"), true);
            textFont = new BitmapFont(
                    Gdx.files.internal("fonts/last-ninja.fnt"), true);
            // set font sizes
            defaultSmall.getData().setScale(0.75f);
            textFontSmall.getData().setScale(0.75f);
            defaultNormal.getData().setScale(1.0f);
            textFontNormal.getData().setScale(1.0f);
            defaultBig.getData().setScale(2.0f);
            textFont.getData().setScale(2.0f);
            // enable linear texture filtering for smooth fonts
            defaultSmall.getRegion().getTexture().setFilter(
                    Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            defaultNormal.getRegion().getTexture().setFilter(
                    Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            defaultBig.getRegion().getTexture().setFilter(
                    Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textFontSmall.getRegion().getTexture().setFilter(
                    Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textFontNormal.getRegion().getTexture().setFilter(
                    Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            textFont.getRegion().getTexture().setFilter(
                    Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
    }

    public class AssetPlayer {
        public final TextureAtlas.AtlasRegion head;
        public final TextureAtlas.AtlasRegion player;
        public final TextureAtlas.AtlasRegion kunai;
        public final Animation animIddle;
        public final Animation animDelay;
        public final Animation animRun;
        public final Animation animJump;
        public final Animation animClimb;
        public final Animation animDead;
        public final Animation animGlide;
        public final Animation animGlideBack;
        public final Animation animJumpAttack;
        public final Animation animJumpThrow;
        public final Animation animAttack;
        public final Animation animSlide;
        public final Animation animThrow;


        public AssetPlayer(TextureAtlas atlas) {

            head = atlas.findRegion("head");

            player = atlas.findRegion("anim_iddle");

            kunai = atlas.findRegion("Kunai");

            Array<TextureAtlas.AtlasRegion> regions = null;
            TextureAtlas.AtlasRegion region = null;
            // Animation: Iddle
            regions = atlas.findRegions("anim_iddle");
            animIddle = new Animation(1.0f / 30.0f, regions, Animation.PlayMode.LOOP);
            // Animation: Delay
            regions = atlas.findRegions("anim_iddle");
            animDelay = new Animation(1.0f / 30.0f, regions);
            // Animation: Run
            regions = atlas.findRegions("anim_run");
            animRun = new Animation(1.0f / 30.0f, regions, Animation.PlayMode.LOOP);
            // Animation: Jump
            regions = atlas.findRegions("anim_jump");
            animJump = new Animation(1.0f / 12.0f, regions);
            // Animation: Climb
            regions = atlas.findRegions("anim_climb");
            animClimb = new Animation(1.0f / 30.0f, regions);
            // Animation: Dead
            regions = atlas.findRegions("anim_dead");
            animDead = new Animation(1.0f / 30.0f, regions);
            // Animation: Glide
            regions = atlas.findRegions("anim_glide");
            animGlide = new Animation(1.0f / 30.0f, regions);
            // Animation: Glide Back
            regions = atlas.findRegions("anim_glide");
            animGlideBack = new Animation(1.0f / 30.0f, regions, Animation.PlayMode.REVERSED);
            // Animation: Jump Attack
            regions = atlas.findRegions("anim_jump_attack");
            animJumpAttack = new Animation(1.0f / 30.0f, regions);
            // Animation: Jump Throw
            regions = atlas.findRegions("anim_jump_throw");
            animJumpThrow = new Animation(1.0f / 30.0f, regions);
            // Animation: Attack
            regions = atlas.findRegions("anim_attack");
            animAttack = new Animation(1.0f / 30.0f, regions);
            // Animation: Slide
            regions = atlas.findRegions("anim_slide");
            animSlide = new Animation(1.0f / 30.0f, regions);
            // Animation: Throw
            regions = atlas.findRegions("anim_throw");
            animThrow = new Animation(1.0f / 30.0f, regions);
        }
    }

    public class AssetGoldCoin {
        public final TextureAtlas.AtlasRegion goldCoin;
        public final Animation animGoldCoin;

        public AssetGoldCoin (TextureAtlas atlas) {
            goldCoin = atlas.findRegion("item_gold_coin");
            Array<TextureAtlas.AtlasRegion> regions = null;
            TextureAtlas.AtlasRegion region = null;
            // Animation: Gold Coin
            regions = atlas.findRegions("anim_gold_coin");
            animGoldCoin = new Animation(1.0f / 20.0f, regions,
                    Animation.PlayMode.LOOP_PINGPONG);
        }
    }

    public class AssetFeather {
        public final TextureAtlas.AtlasRegion feather;
        public AssetFeather (TextureAtlas atlas) {
            feather = atlas.findRegion("item_feather");
        }
    }

    public class AssetItem {

        public final TextureAtlas.AtlasRegion barrel1;
        public final TextureAtlas.AtlasRegion barrel2;
        public final TextureAtlas.AtlasRegion mushroom1;
        public final TextureAtlas.AtlasRegion mushroom2;
        public final TextureAtlas.AtlasRegion crate;
        public final TextureAtlas.AtlasRegion box;
        public final TextureAtlas.AtlasRegion icebox;
        public final TextureAtlas.AtlasRegion stone;
        public final TextureAtlas.AtlasRegion stoneblock;

        public AssetItem (TextureAtlas atlas) {
            barrel1 = atlas.findRegion("barrel", 1);
            barrel2 = atlas.findRegion("barrel", 2);
            mushroom1 = atlas.findRegion("mushroom", 1);
            mushroom2 = atlas.findRegion("mushroom", 2);
            crate = atlas.findRegion("crate");
            box = atlas.findRegion("box");
            icebox = atlas.findRegion("icebox");
            stone = atlas.findRegion("stone");
            stoneblock = atlas.findRegion("stoneblock");
        }

    }

    public class AssetObjectDecoration {
        public final TextureAtlas.AtlasRegion cloud01;
        public final TextureAtlas.AtlasRegion cloud02;
        public final TextureAtlas.AtlasRegion cloud03;
        public AssetObjectDecoration (TextureAtlas atlas) {
            cloud01 = atlas.findRegion("cloud01");
            cloud02 = atlas.findRegion("cloud02");
            cloud03 = atlas.findRegion("cloud03");
        }
    }

    public class AssetZombie {

        public final Animation animMaleIdle;
        public final Animation animMaleWalk;
        public final Animation animMaleDead;
        public final Animation animMaleAttack;

        public final Animation animFeMaleIdle;
        public final Animation animFeMaleWalk;
        public final Animation animFeMaleDead;
        public final Animation animFeMaleAttack;


        public AssetZombie(TextureAtlas atlas) {
            Array<TextureAtlas.AtlasRegion> regions = null;
            // Animation: Idle
            regions = atlas.findRegions("anim_male_idle");
            animMaleIdle = new Animation(1.0f / 12.0f, regions, Animation.PlayMode.LOOP);
            regions = atlas.findRegions("anim_female_idle");
            animFeMaleIdle = new Animation(1.0f / 12.0f, regions, Animation.PlayMode.LOOP);
            // Animation: Walk
            regions = atlas.findRegions("anim_male_walk");
            animMaleWalk = new Animation(1.0f / 12.0f, regions, Animation.PlayMode.LOOP_PINGPONG);
            regions = atlas.findRegions("anim_female_walk");
            animFeMaleWalk = new Animation(1.0f / 12.0f, regions, Animation.PlayMode.LOOP_PINGPONG);
            // Animation: Dead
            regions = atlas.findRegions("anim_male_dead");
            animMaleDead = new Animation(1.0f / 12.0f, regions);
            regions = atlas.findRegions("anim_female_dead");
            animFeMaleDead = new Animation(1.0f / 12.0f, regions);
            // Animation: Attack
            regions = atlas.findRegions("anim_male_attack");
            animMaleAttack = new Animation(1.0f / 30.0f, regions);
            regions = atlas.findRegions("anim_female_attack");
            animFeMaleAttack = new Animation(1.0f / 30.0f, regions);
        }
    }

    public class AssetRobot {

        public final Animation animIdle;
        public final Animation animRun;
        public final Animation animShoot;
        public final Animation animJump;
        public final Animation animJumpMelee;
        public final Animation animJumpShoot;
        public final Animation animRunShoot;
        public final Animation animDead;
        public final Animation animSlide;


        public AssetRobot(TextureAtlas atlas) {

            Array<TextureAtlas.AtlasRegion> regions = null;
            // Animation: Iddle
            regions = atlas.findRegions("anim_idle");
            animIdle = new Animation(1.0f / 12.0f, regions, Animation.PlayMode.LOOP);
            // Animation: Run
            regions = atlas.findRegions("anim_run");
            animRun = new Animation(1.0f / 12.0f, regions, Animation.PlayMode.LOOP);
            // Animation: Jump
            regions = atlas.findRegions("anim_jump");
            animJump = new Animation(1.0f / 12.0f, regions);
            // Animation: Jump Melee
            regions = atlas.findRegions("anim_jump_melee");
            animJumpMelee = new Animation(1.0f / 12.0f, regions);
            // Animation: Jump Shoot
            regions = atlas.findRegions("anim_jump_shoot");
            animJumpShoot = new Animation(1.0f / 12.0f, regions);
            // Animation: Run Shoot
            regions = atlas.findRegions("anim_run_shoot");
            animRunShoot = new Animation(1.0f / 12.0f, regions);
            // Animation: Shoot
            regions = atlas.findRegions("anim_shoot");
            animShoot = new Animation(1.0f / 12.0f, regions);
            // Animation: Dead
            regions = atlas.findRegions("anim_dead");
            animDead = new Animation(1.0f / 12.0f, regions);
            // Animation: Slide
            regions = atlas.findRegions("anim_slide");
            animSlide = new Animation(1.0f / 30.0f, regions);
        }
    }
}