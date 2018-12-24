package com.fgdev.game.logics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fgdev.game.Constants;
import com.fgdev.game.entitiles.Player;
import com.fgdev.game.entitiles.enemies.Robot;
import com.fgdev.game.entitiles.enemies.Zombie;
import com.fgdev.game.entitiles.tiles.Coin;
import com.fgdev.game.entitiles.tiles.Crate;
import com.fgdev.game.entitiles.tiles.Feather;
import com.fgdev.game.entitiles.objects.Clouds;
import com.fgdev.game.helpers.BackgroundTiledMapRenderer;
import com.fgdev.game.helpers.ScoreIndicator;
import com.fgdev.game.screens.DirectedGame;
import com.fgdev.game.screens.GameOverOverlay;
import com.fgdev.game.screens.MenuScreen;
import com.fgdev.game.screens.transitions.ScreenTransition;
import com.fgdev.game.screens.transitions.ScreenTransitionFade;
import com.fgdev.game.helpers.B2WorldCreator;
import com.fgdev.game.helpers.WorldContactListener;
import com.fgdev.game.utils.*;

import static com.fgdev.game.Constants.*;

public class GameScreenLogic extends InputAdapter implements Disposable {

    private static final String TAG = GameScreenLogic.class.getName();

    private DirectedGame game;

    private World world;
    // Tiled map variables
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private BackgroundTiledMapRenderer renderer;
    // Camera & Batch
    private OrthographicCamera camera;
    private OrthographicCamera cameraGUI;
    private Viewport gamePort;
    private SpriteBatch batch;
    // Shader
    private ShaderProgram shaderMonochrome;
    // Box2d variables
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;
    // Objects
    private Player player;
    // Decoration
    private Clouds clouds;
    // Accumulator
    private float accumulator;
    // Background
    private Texture background;
    // ScoreIndicator
    private ScoreIndicator scoreIndicator;
    // HelperScreen
    private GameOverOverlay gameOverOverlay;
    // Turn on / off debug
    private boolean isDebug;
    // Limit
    private float cameraLeftLimit;
    private float cameraRightLimit;
    // Map Width
    private float mapWidth;

    public GameScreenLogic(DirectedGame game) {
        this.game = game;
        isDebug = GamePreferences.instance.debug;
        init();
    }

    private void init() {
        ValueManager.instance.init();

        initCamera();
        initMap();
        initLevel();
        initObject();

        accumulator = 0;
        mapWidth = ((TiledMapTileLayer) map.getLayers().get(0)).getWidth();
        cameraLeftLimit = V_WIDTH / 2;
        cameraRightLimit =  mapWidth - V_WIDTH / 2;
    }

    private void initCamera() {
        // Init batch
        batch = new SpriteBatch();
        // Create cam used to follow mario through cam world
        camera = new OrthographicCamera();
        // Create a FitViewport to maintain virtual aspect ratio despite screen size
        gamePort = new FitViewport(Constants.V_WIDTH, Constants.V_HEIGHT, camera);
        //initially set our gamcam to be centered correctly at the start of
        camera.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);
        // Camera gui
        cameraGUI = new OrthographicCamera(Constants.WINDOW_WIDTH,
                Constants.WINDOW_HEIGHT);
        cameraGUI.position.set(0, 0, 0);
        cameraGUI.setToOrtho(true); // flip y-axis
        cameraGUI.update();
    }

    private void initMap() {
        // Init world
        world = new World(GRAVITY, true);
        world.setContactListener(new WorldContactListener());
        // Load our map and setup our map renderer
        mapLoader = new TmxMapLoader();
        map = mapLoader.load(LEVEL);

        background = Assets.instance.textures.background;

        renderer = new BackgroundTiledMapRenderer(map, 1 / PPM , background);

        b2dr = new Box2DDebugRenderer();

        // decoration
        clouds = new Clouds(V_WIDTH * 1000);
        clouds.getPosition().set(0, 2);

    }

    private void initLevel () {
        player = new Player(world);
    }


    private void initObject() {
        // Shader
        shaderMonochrome = new ShaderProgram(
                Gdx.files.internal(Constants.shaderMonochromeVertex),
                Gdx.files.internal(Constants.shaderMonochromeFragment));
        if (!shaderMonochrome.isCompiled()) {
            String msg = "Could not compile shader program: "
                    + shaderMonochrome.getLog();
            throw new GdxRuntimeException(msg);
        }
        // Game over overlay
        gameOverOverlay = new GameOverOverlay(batch, cameraGUI);
        // Init score indicator
        scoreIndicator = new ScoreIndicator(this, batch);
        creator = new B2WorldCreator(world, map, scoreIndicator);
    }

    private void resetPlayer() {
        world.destroyBody(player.getBody());
        player = new Player(world);
    }

    public void update (float deltaTime) {
        // Box2D world step
        accumulator += Math.min(deltaTime, 0.25f);
        while (accumulator >= TIME_STEP) {
            accumulator -= TIME_STEP;
            world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
        // Check game over
        if (ValueManager.instance.isGameOver()) {
            ValueManager.instance.timeLeftGameOverDelay -= deltaTime;
            if (ValueManager.instance.timeLeftGameOverDelay < 0) backToMenu();
        } else {
            if (!player.isDead()) {
                handleInput(deltaTime);
            } else {
                ValueManager.instance.timeLeftLiveLost -= deltaTime;
                if (ValueManager.instance.timeLeftLiveLost < 0) resetPlayer();
            }
        }
        // Handle Debug Input
        // handleDebugInput(deltaTime);
        // Update player
        player.update(deltaTime);
        // Update clouds
        clouds.update(deltaTime);
        // Update object
        updateTile(deltaTime);
        // update ScoreIndicator
        scoreIndicator.update(deltaTime);

        if (ValueManager.instance.livesVisual > ValueManager.instance.lives) {
            ValueManager.instance.livesVisual = Math.max(ValueManager.instance.lives, ValueManager.instance.livesVisual - 1 * deltaTime);
        }
        if (ValueManager.instance.scoreVisual < ValueManager.instance.score)
            ValueManager.instance.scoreVisual = Math.min(ValueManager.instance.score, ValueManager.instance.scoreVisual + 250 * deltaTime);
        if (!ValueManager.instance.isGameOver() && player.isPlayerFalling()) {
            player.playerDie();
        }
    }

    private void updateTile(float deltaTime) {
        // Update crates
        for(Crate crate: creator.getCrates()) {
            crate.update(deltaTime);
            if (player.getX() + V_WIDTH / 2 + 4 > crate.getX())
                crate.getBody().setActive(true);
        }
        // Update coins
        for(Coin coin: creator.getCoins()) {
            coin.update(deltaTime);
            if (player.getX() + V_WIDTH / 2 + 4 > coin.getX())
                coin.getBody().setActive(true);
        }
        // Update feathers
        for(Feather feather: creator.getFeathers()) {
            feather.update(deltaTime);
            if (player.getX() + V_WIDTH / 2 + 4 > feather.getX())
                feather.getBody().setActive(true);
        }
        // Update zombies
        for(Zombie zombie: creator.getZombies()) {
            zombie.update(deltaTime);
            if (player.getX() + V_WIDTH / 2 + 4 > zombie.getX())
                zombie.getBody().setActive(true);
        }
        // Update robots
        for(Robot robot: creator.getRobots()) {
            robot.update(deltaTime);
            if (player.getX() + V_WIDTH / 2 + 4 > robot.getX())
                robot.getBody().setActive(true);
        }
    }

    public void render() {
        renderWorld(batch);
        renderTile(batch);
        renderObject(batch);
        renderGui(batch);
        renderShader(batch);
        if (isDebug) renderDebug();
    }

    private void renderShader(SpriteBatch batch) {
        batch.begin();
        if (GamePreferences.instance.useMonochromeShader) {
            batch.setShader(shaderMonochrome);
            shaderMonochrome.setUniformf("u_amount", 1.0f);
        }
        batch.setShader(null);
        batch.end();
    }

    private void renderTile(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        // Coins
        for (Coin coin: creator.getCoins())
            coin.draw(batch);
        // Feathers
        for (Feather feather: creator.getFeathers())
            feather.draw(batch);
        // Crates
        for (Crate crate: creator.getCrates())
            crate.draw(batch);
        // Zombies
        for (Zombie zombie: creator.getZombies())
            zombie.draw(batch);
        // Robots
        for (Robot robot: creator.getRobots())
            robot.draw(batch);
        batch.end();
    }

    private void renderObject(SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        clouds.render(batch);
        batch.end();
    }

    private void renderWorld (SpriteBatch batch) {
        batch.setProjectionMatrix(camera.combined);
        // camera control
        float targetX = camera.position.x;
        if (!player.isDead()) {
            targetX = MathUtils.clamp(player.getPosition().x, cameraLeftLimit, cameraRightLimit);
        }

        camera.position.x = MathUtils.lerp(camera.position.x, targetX, 0.1f);
        if (Math.abs(camera.position.x - targetX) < 0.1f) {
            camera.position.x = targetX;
        }
        //attach our game camera to our players.x coordinate
        // camera.position.x = player.getBody().getPosition().x;
        // update our camera with correct coordinates after changes
        camera.update();
        // set the TiledMapRenderer view based on what the
        // camera sees, and render the map
        renderer.setView(camera);
        renderer.render();
    }

    private void renderGui (SpriteBatch batch) {
        batch.setProjectionMatrix(cameraGUI.combined);
        batch.begin();
        // draw collected gold coins icon + text
        // (anchored to top left edge)
        renderGuiScore(batch);
        // draw extra lives icon + text (anchored to top right edge)
        renderGuiExtraLive(batch);
        // draw FPS text (anchored to bottom right edge)
        if (GamePreferences.instance.showFpsCounter)
            renderGuiFpsCounter(batch);
        // draw Game Over
        renderGuiGameOverMessage(batch);
        // draw collected feather icon (anchored to top left edge)
        renderGuiFeatherPowerup(batch);
        // draw ScoreIndicator
        scoreIndicator.draw();
        batch.end();
    }

    private void renderDebug() {
        //render our Box2DDebugLines
        b2dr.render(world, camera.combined);
    }

    public void resize(int width, int height) {
        // updated our game viewport
        gamePort.update(width, height);
        cameraGUI.viewportHeight = Constants.WINDOW_HEIGHT;
        cameraGUI.viewportWidth = (Constants.WINDOW_HEIGHT
                / (float)height) * (float)width;
        cameraGUI.position.set(cameraGUI.viewportWidth / 2,
                cameraGUI.viewportHeight / 2, 0);
        cameraGUI.update();
    }

    private void renderGuiScore (SpriteBatch batch) {
        float x = -15;
        float y = -15;
        float offsetX = 50;
        float offsetY = 50;
        if (ValueManager.instance.scoreVisual < ValueManager.instance.score) {
            long shakeAlpha = System.currentTimeMillis() % 360;
            float shakeDist = 1.5f;
            offsetX += MathUtils.sinDeg(shakeAlpha * 2.2f) * shakeDist;
            offsetY += MathUtils.sinDeg(shakeAlpha * 2.9f) * shakeDist;
        }
        batch.draw(Assets.instance.goldCoin.goldCoin, x, y, offsetX,
                offsetY, 100, 100, 0.35f, -0.35f, 0);
        Assets.instance.fonts.textFontNormal.draw(batch,
                "" + (int) ValueManager.instance.scoreVisual,
                x + 75, y + 40);
    }

    private void renderGuiExtraLive (SpriteBatch batch) {
        float x = cameraGUI.viewportWidth - 50 - Constants.LIVES_START * 50;
        float y = -15;
        for (int i = 0; i < Constants.LIVES_START; i++) {
            if (ValueManager.instance.lives <= i)
                batch.setColor(0.5f, 0.5f, 0.5f, 0.5f);
            batch.draw(Assets.instance.player.head,
                    x + i * 50, y, 50, 50, 120, 120, 0.35f, -0.35f, 0);
            batch.setColor(1, 1, 1, 1);
        }
        if (ValueManager.instance.lives >= 0
                && ValueManager.instance.livesVisual > ValueManager.instance.lives) {
            int i = ValueManager.instance.lives;
            float alphaColor = Math.max(0, ValueManager.instance.livesVisual
                    - ValueManager.instance.lives - 0.5f);
            float alphaScale = 0.35f * (2 + ValueManager.instance.lives
                    - ValueManager.instance.livesVisual) * 2;
            float alphaRotate = -45 * alphaColor;
            batch.setColor(1.0f, 0.7f, 0.7f, alphaColor);
            batch.draw(Assets.instance.player.head,
                    x + i * 50, y, 50, 50, 120, 120, alphaScale, -alphaScale,
                    alphaRotate);
            batch.setColor(1, 1, 1, 1);
        }
    }

    private void renderGuiFpsCounter (SpriteBatch batch) {
        float x = cameraGUI.viewportWidth - 55;
        float y = cameraGUI.viewportHeight - 15;
        int fps = Gdx.graphics.getFramesPerSecond();
        BitmapFont fpsFont = Assets.instance.fonts.defaultNormal;
        if (fps >= 45) {
            // 45 or more FPS show up in green
            fpsFont.setColor(0, 1, 0, 1);
        } else if (fps >= 30) {
            // 30 or more FPS show up in yellow
            fpsFont.setColor(1, 1, 0, 1);
        } else {
            // less than 30 FPS show up in red
            fpsFont.setColor(1, 0, 0, 1);
        }
        fpsFont.draw(batch, "FPS: " + fps, x, y);
        fpsFont.setColor(1, 1, 1, 1); // white
    }

    private void renderGuiGameOverMessage (SpriteBatch batch) {
        if (ValueManager.instance.isGameOver()) {
            gameOverOverlay.render(Gdx.graphics.getDeltaTime());
        }
    }

    private void renderGuiFeatherPowerup (SpriteBatch batch) {
        float x = -15;
        float y = 30;
        float timeLeftFeatherPowerup =
                player.getTimeLeftFeatherPowerup();
        if (timeLeftFeatherPowerup > 0) {
            // Start icon fade in/out if the left power-up time
            // is less than 4 seconds. The fade interval is set
            // to 5 changes per second.
            if (timeLeftFeatherPowerup < 4) {
                if (((int)(timeLeftFeatherPowerup * 5) % 2) != 0) {
                    batch.setColor(1, 1, 1, 0.5f);
                }
            }
            batch.draw(Assets.instance.feather.feather,
                    x, y, 50, 50, 100, 100, 0.35f, -0.35f, 0);
            batch.setColor(1, 1, 1, 1);
            Assets.instance.fonts.textFontSmall.draw(batch,
                    "" + (int)timeLeftFeatherPowerup, x + 60, y + 57);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        map.dispose();
        world.dispose();
        renderer.dispose();
        b2dr.dispose();
        background.dispose();
        scoreIndicator.dispose();
        shaderMonochrome.dispose();
        gameOverOverlay.dispose();
    }

    private void backToMenu () {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                // switch to menu screen
                ScreenTransition transition = ScreenTransitionFade.init(0.75f);
                game.setScreen(new MenuScreen(game), transition);
            }
        });
    }

    private void handleInput(float deltaTime) {
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.left();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.right();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            player.jump();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            player.down();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            player.attack();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            player.attackThrow();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.X)) {
            player.climb();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            player.jumpThrow();
        }

        // Hacking
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)) {
            renderer.setBackground(Assets.instance.textures.background);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            renderer.setBackground(Assets.instance.textures.background1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            renderer.setBackground(Assets.instance.textures.background2);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            renderer.setBackground(Assets.instance.textures.background3);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.COMMA)) {
            ValueManager.instance.lives++;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SLASH)) {
            player.setFeatherPowerup(true);
        }
    }

    private void handleDebugInput(float deltaTime) {
        if (Gdx.app.getType() != Application.ApplicationType.Desktop) return;

        // Selected Sprite Controls
        float sprMoveSpeed = 5 * deltaTime;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {}
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {}
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {}
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {}
    }

    @Override
    public boolean keyUp(int keycode) {
        // Reset game world
        if (keycode == Input.Keys.R) {
            init();
            Gdx.app.debug(TAG, "Game world reseted");
        }
        // Toggle camera follow
        else if (keycode == Input.Keys.ENTER) {

        }
        // Back to Menu
        else if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
            backToMenu();
        }
        return false;
    }


    public OrthographicCamera getCamera() {
        return camera;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public OrthographicCamera getCameraGUI() {
        return cameraGUI;
    }

    public ScoreIndicator getScoreIndicator() {
        return scoreIndicator;
    }

    public float getMapWidth() {
        return mapWidth;
    }
}
