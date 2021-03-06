package Views;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.utils.Array;

import Constants.ConsoleMessageType;
import Constants.GameSpeed;
import Constants.UIConstants;
import Controllers.GameController;
import Controllers.GameVariables;
import Models.Robot;
import Models.Team;
import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenManager;

/**
 * A GUI view for the main menu
 * 
 * @author Corey
 *
 */
public class MapView extends ScreenAdapter implements EventListener {
    // The controller which called the view
    private final GameController controller;

    private float WINDOW_WIDTH;
    private float WINDOW_HEIGHT;

    // Size of the distance between tiles
    private static final int sizeX = 50;
    private static final int sizeY = 60;

    // For the various sprites
    private TextureAtlas atlas;
    private Array<Sprite> tiles;

    private Texture projectileTexture;
    private Sprite projectile;

    // For our explosion animation
    private Animation explosionAnimation;
    private TextureAtlas explosionAtlas;
    private TextureRegion[] explosionFrames;
    private TextureRegion currentExplosionFrame;
    private Sprite explosionPos;

    float stateTime;

    // Camera settings
    private int cameraWidth;
    private OrthographicCamera cam;

    // Some map settings
    private int mapSize;
    private int mapDiameter;

    // Sprites for the various teams
    // It is a set of teams, which each holds a set of sprites
    private List<List<Sprite>> teamList;

    // For rendering tweens
    private TweenManager tweenManager;
    private Queue<AudibleTimeline> timelineTweenQueue;

    // For rendering sprites
    private SpriteBatch batch;

    private Stage stage;
    private Table topTable;
    private LabelStyle blackLabelStyle;
    private LabelStyle redLabelStyle;
    private LabelStyle blueLabelStyle;
    private LabelStyle purpleLabelStyle;
    private Label nameLabel;
    private Label teamLabel;
    private Label turnLabel;
    private Label numLabel;
    private TextButton pauseBtn;
    private TextButton speedBtn;
    private Label movesLabel;
    private Label healthLabel;
    private Label strengthLabel;

    private ArrayList<String> consoleList;
    private ArrayList<ConsoleMessageType> consoleTypeList;
    private boolean consoleUpdatesAvailable;

    // TODO For future fonts
    // private BitmapFont font = new
    // BitmapFont(Gdx.files.internal("assets/MoonFlower.fnt"),Gdx.files.internal("assets/MoonFlower.png"),false);

    /**
     * Creates a mapView screen
     * 
     * @param controller
     *            the controller creating this screen
     * @param teamsInMatch
     *            The number of teams in the current game
     */
    public MapView(final GameController controller, List<Team> teamsInMatch) {
        this.controller = controller;

        this.stage = new Stage();

        WINDOW_WIDTH = Gdx.graphics.getWidth();
        WINDOW_HEIGHT = Gdx.graphics.getHeight();

        // Getting our texture atlas of all the sprites
        atlas = new TextureAtlas(Gdx.files.internal("assets/game_sprites/gamesprites.pack"));

        // Setting up the tiles
        tiles = atlas.createSprites("tile");


        projectileTexture = new Texture("assets/game_sprites/projectile.png");
        projectile = new Sprite(projectileTexture);
        projectile.setPosition(5000f, 5000f);

        // Setting up the explosion animation
        explosionAtlas = new TextureAtlas(Gdx.files.internal("assets/explosion/explosion.pack"),
                Gdx.files.internal("assets/explosion/"));
        explosionFrames = new TextureRegion[12];

        for (int i = 0; i < 12; i++) {
            explosionFrames[i] = explosionAtlas.createSprite("explosion", i);
        }

        explosionAnimation = new Animation(0.025f, explosionFrames);
        stateTime = 0f;

        explosionPos = new Sprite();
        explosionPos.setX(5000f);
        explosionPos.setY(5000f);

        // Setting up the camera based on map size
        mapSize = GameVariables.mapSize;
        mapDiameter = mapSize * 2 - 1;

        cameraWidth = (int) (mapDiameter * sizeY * WINDOW_WIDTH / WINDOW_HEIGHT);

        cam = new OrthographicCamera(cameraWidth, cameraWidth * (WINDOW_HEIGHT / WINDOW_WIDTH));

        cam.position.set(3 * sizeX * mapSize / 4, 5, 0);
        cam.update();

        // Creates our robots
        this.teamList = new ArrayList<List<Sprite>>();

        Iterator<Team> it = teamsInMatch.iterator();
        while (it.hasNext()) {
            createRobots(it.next(), teamsInMatch.size());
        }

        tweenManager = new TweenManager();
        Tween.registerAccessor(Sprite.class, new SpriteAccessor());
        timelineTweenQueue = new LinkedList<AudibleTimeline>();
        batch = new SpriteBatch();

        // add table to sides
        // set up scroll bar style
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(UIConstants.BLUE_ATLAS));
        Skin skin = new Skin();
        skin.addRegions(atlas);

        TextButtonStyle buttonStyle = new TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.up = skin.getDrawable(UIConstants.BUTTON_UP);
        buttonStyle.down = skin.getDrawable(UIConstants.BUTTON_DOWN);

        // put lists in scroll panes, so we can scroll to see all entries

        pauseBtn = new TextButton("Pause", buttonStyle);
        pauseBtn.addListener(this);
        speedBtn = new TextButton("1X", buttonStyle);
        speedBtn.addListener(this);

        Table master = new Table();
        master.setSize(WINDOW_WIDTH / 3, WINDOW_HEIGHT);
        master.setPosition(WINDOW_WIDTH - (WINDOW_WIDTH / 3), 0);
        topTable = new Table();
        topTable.clearListeners();
        Table bottom = new Table();

        master.add(topTable);
        master.row();
        master.add(speedBtn);
        master.add(pauseBtn);
        master.row();
        master.add(bottom).padBottom(300);
        stage.addActor(master);

        BitmapFont font = new BitmapFont();

        blackLabelStyle = new LabelStyle();
        blackLabelStyle.fontColor = Color.BLACK;
        blackLabelStyle.font = font;

        blueLabelStyle = new LabelStyle();
        blueLabelStyle.fontColor = Color.BLUE;
        blueLabelStyle.font = font;

        redLabelStyle = new LabelStyle();
        redLabelStyle.fontColor = Color.RED;
        redLabelStyle.font = font;

        purpleLabelStyle = new LabelStyle();
        purpleLabelStyle.fontColor = Color.PURPLE;
        purpleLabelStyle.font = font;

        Label titleLabel = new Label("Current Robot Information: ", blackLabelStyle);
        Label nameTitle = new Label("Name: ", blackLabelStyle);
        Label teamTitle = new Label("Team: ", blackLabelStyle);
        Label turnTitle = new Label("Turn: ", blackLabelStyle);
        Label numTitle = new Label("Member: ", blackLabelStyle);
        Label healthTitle = new Label("Health: ", blackLabelStyle);
        Label strengthTitle = new Label("Strength: ", blackLabelStyle);
        Label movesTitle = new Label("Moves: ", blackLabelStyle);
        nameLabel = new Label("", blackLabelStyle);
        teamLabel = new Label("", blackLabelStyle);
        turnLabel = new Label("", blackLabelStyle);
        numLabel = new Label("", blackLabelStyle);
        healthLabel = new Label("", blackLabelStyle);
        strengthLabel = new Label("", blackLabelStyle);
        movesLabel = new Label("", blackLabelStyle);
        bottom.add(titleLabel);
        bottom.row();
        bottom.add(nameTitle);
        bottom.add(nameLabel);
        bottom.row();
        bottom.add(teamTitle);
        bottom.add(teamLabel);
        bottom.row();
        bottom.add(numTitle);
        bottom.add(numLabel);
        bottom.row();
        bottom.add(turnTitle);
        bottom.add(turnLabel);
        bottom.row();
        bottom.add(healthTitle).padTop(20);
        bottom.add(healthLabel).padTop(20);
        bottom.row();
        bottom.add(strengthTitle);
        bottom.add(strengthLabel);
        bottom.row();
        bottom.add(movesTitle);
        bottom.add(movesLabel);

        this.consoleList = new ArrayList<String>();
        this.consoleTypeList = new ArrayList<ConsoleMessageType>();
        this.consoleUpdatesAvailable = true;
    }

    /**
     * used to update the console logger
     * 
     * @param newMessage
     *            the latest message to display
     * @param type
     *            Type of Console Message
     */
    public void displayMessage(String newMessage, ConsoleMessageType type) {
        this.consoleList.add(newMessage);
        this.consoleTypeList.add(type);
        // we mark the table as needing updates, but we don't acyually update
        // the table ourselves
        // we want to leave all UI updating to the main thread, otherwise there
        // may be crashes
        // the main thread will mark this bool as false once it updates the
        // table
        this.consoleUpdatesAvailable = true;
    }

    /**
     * Called by by the render function to update the table when updates are
     * available Rebuilds the table using the latest console data
     */
    private void updateConsoleTable() {
        this.topTable.clear();
        int size = this.consoleList.size();
        for (int i = size - 21; i < size; i++) {
            String message;
            LabelStyle style;
            try {
                message = this.consoleList.get(i);
                ConsoleMessageType type = this.consoleTypeList.get(i);
                switch (type) {
                case CONSOLE_ERROR:
                    style = redLabelStyle;
                    break;
                case CONSOLE_ROBOT_MESSAGE:
                    style = purpleLabelStyle;
                    break;
                case CONSOLE_SIMULATOR_MESSAGE:
                    style = blueLabelStyle;
                    break;
                default:
                    style = blackLabelStyle;
                    break;
                }
            } catch (IndexOutOfBoundsException e) {
                message = " ";
                style = blackLabelStyle;
            }
            Label messageLabel = new Label(message, style);
            this.topTable.add(messageLabel);
            this.topTable.row();
        }
        this.consoleUpdatesAvailable = false;
    }

    /**
     * used to update the current robot's info on the screen
     * 
     * @param robot
     *            the robot that is running it's turn
     * @param turnNum
     *            The turn number the controller is on
     */
    public void updateRobotInfo(Robot robot, int turnNum) {
        nameLabel.setText(robot.getName());
        teamLabel.setText("" + robot.getTeamNumber());
        numLabel.setText("" + robot.getMemberNumber());
        if (turnNum == 0) {
            turnLabel.setText("init");
        } else {
            turnLabel.setText("" + turnNum);
        }
        healthLabel.setText("" + robot.getHealth());
        strengthLabel.setText("" + robot.getStrength());
        // TODO: make this value change as the robot moves. Can do this in game
        // controller, whenever a move is made
        movesLabel.setText("" + robot.getMovesPerTurn());
    }

    public void createRobots(Team teamToAdd, int numTeams) {
        Queue<Robot> robotList = teamToAdd.getAllRobots();
        Iterator<Robot> it = robotList.iterator();
        ArrayList<Sprite> spriteList = new ArrayList<Sprite>();

        while (it.hasNext()) {
            it.next();
            Sprite s = atlas.createSprite("robot", teamToAdd.getTeamNumber());
            setRobotStartingPosition(teamToAdd.getTeamNumber(), s, numTeams);
            spriteList.add(s);
        }
        this.teamList.add(spriteList);
    }

    /**
     * Sets a robot's initial position based on team size and team
     * 
     * @param teamNum
     *            the team number
     * @param s
     *            the robot sprite
     * @param numTeams
     *            The number of teams the game will have
     */

    public void setRobotStartingPosition(int teamNum, Sprite s, int numTeams) {
        s.setPosition(-14, -23);

        if (teamNum == 0) {
            s.translate(-(mapSize - 1) * sizeX, 0);
        } else if (teamNum == 1) {
            if (numTeams == 2) {
                s.translate((mapSize - 1) * sizeX, 0);
            } else if (numTeams == 3) {
                s.translate((mapSize / 2) * sizeX, (0.75f * (float) mapSize - 0.75f) * sizeY);
            } else if (numTeams == 6) {
                s.translate(-(mapSize / 2) * sizeX, (0.75f * (float) mapSize - 0.75f) * sizeY);
            }
        } else if (teamNum == 2) {
            if (numTeams == 3) {
                s.translate((mapSize / 2) * sizeX, (-0.75f * (float) mapSize + 0.75f) * sizeY);
            } else if (numTeams == 6) {
                s.translate((mapSize / 2) * sizeX, (0.75f * (float) mapSize - 0.75f) * sizeY);
            }
        } else if (teamNum == 3) {
            s.translate((mapSize - 1) * sizeX, 0);
        } else if (teamNum == 4) {
            s.translate((mapSize / 2) * sizeX, (-0.75f * (float) mapSize + 0.75f) * sizeY);
        } else if (teamNum == 5) {
            s.translate(-(mapSize / 2) * sizeX, (-0.75f * (float) mapSize + 0.75f) * sizeY);
        }
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();
        renderTiles();
        renderRobots();
        renderExplosion();
        renderTesting();
        if (timelineTweenQueue.peek() != null) {
            if (timelineTweenQueue.peek().getTimeline().isFinished()) {
                timelineTweenQueue.poll();
            } else if (!timelineTweenQueue.peek().getTimeline().isStarted()) {
                timelineTweenQueue.peek().startTimeline(tweenManager);
            }
        }
        tweenManager.update(delta);
        projectile.draw(batch);
        batch.end();

        // if there is a new console message, update the console table
        if (this.consoleUpdatesAvailable) {
            this.updateConsoleTable();
        }

        stage.draw();
        this.controller.checkGameComplete();
    }

    public void renderExplosion() {
        stateTime += Gdx.graphics.getDeltaTime();
        currentExplosionFrame = explosionAnimation.getKeyFrame(stateTime, true);
        batch.draw(currentExplosionFrame, explosionPos.getX(), explosionPos.getY());
    }

    public void renderTesting() {
        if (Gdx.input.isKeyJustPressed(Keys.NUM_1)) {
            moveRobot(1, 1, 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_2)) {
            moveRobot(1, 1, 2);
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_3)) {
            moveRobot(1, 1, 3);
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_4)) {
            moveRobot(1, 1, 4);
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_5)) {
            moveRobot(1, 1, 5);
        }
        if (Gdx.input.isKeyJustPressed(Keys.NUM_6)) {
            moveRobot(1, 1, 6);
        }
        if (Gdx.input.isKeyJustPressed(Keys.Q)) {
            fireShot(1, 1, 1, 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.W)) {
            fireShot(1, 1, 1, 2);
        }
        if (Gdx.input.isKeyJustPressed(Keys.E)) {
            fireShot(1, 1, 1, 3);
        }
        if (Gdx.input.isKeyJustPressed(Keys.A)) {
            fireShot(1, 1, 5, 1);
        }
        if (Gdx.input.isKeyJustPressed(Keys.S)) {
            fireShot(1, 1, 9, 2);
        }
        if (Gdx.input.isKeyJustPressed(Keys.D)) {
            fireShot(1, 1, 13, 3);
        }
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            destroyRobot(1, 1);
        }
    }

    public void renderTiles() {
        int width = mapDiameter;
        int height = mapSize;

        int xPos = -mapDiameter * sizeX / 2;
        int yPos = (mapSize - 2) * sizeY / 2;

        // TODO remove the hard coded numbers
        // Call the controller instead, getMapSize()
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles.get(chooseIndex(i, j)).setPosition(xPos, yPos);
                tiles.get(chooseIndex(i, j)).draw(batch);
                yPos = yPos - sizeY;
            }
            if ((width) / (i + 1) >= 2) {
                yPos = yPos + sizeY * height + sizeY / 2;
                height++;
            } else {
                yPos = yPos + sizeY * height - sizeY / 2;
                height--;
            }
            xPos = xPos + sizeX;
        }
    }

    /**
     * This function finds a index (0 through 3) based on map position
     * 
     * @param i
     *            the current column
     * @param j
     *            the current height
     * @return the index based of the current map tile
     */
    public int chooseIndex(int i, int j) {
        // This is essentially a bunch of magic number manipulation
        // It generates consistent results
        // But makes it so the map isnt either random or just all of one type of
        // tile
        if (i == 9 || i == 12 || (i == 3 && j > 7)) {
            return 1;
        }
        if ((i == 0 || i == mapDiameter - 1) && j == mapSize - mapSize / 2 - 1) {
            return 1;
        }
        if (j < i - mapSize && (i % 3 == 2 || i % 3 == 2)) {
            return 2;
        }
        if (j > i + 3) {
            return 2;
        }

        if (j < 3 && (i % 3 == 2 || i % 3 == 1)) {
            return 2;
        }
        if (i < (mapDiameter / 2 + 1) && i < (mapDiameter / 2 - 1) && (j % 3 == 2 || j % 3 == 1)) {
            return 3;
        }
        if ((i - 1 == mapSize / 2) && j < 6 && j > 3) {
            return 3;
        }
        if (i == mapSize / 2 + 3 && j == 6) {
            return 3;
        }
        if (i < 4 && i > 1 && j < 4 && j > 2) {
            return 0;
        }
        if (i > 5 && (j % 5 == 4)) {
            return 0;
        }
        return 1;
    }

    public void renderRobots() {
        // Starts at team 1
        for (int i = 0; i < teamList.size(); i++) {
            for (int j = 0; j < teamList.get(i).size(); j++) {
                teamList.get(i).get(j).draw(batch);
            }
        }
    }

    /**
     * Moves a robot in a certain direction
     * 
     * @param team
     *            the number of the team the robot is on
     * @param robot
     *            the number of the robot on the team
     * @param direction
     *            the direct (1 is north, 2 is north east, etc. to 6)
     */
    public void moveRobot(int team, int robot, int direction) {
        int moveX = 0;
        int moveY = 0;

        // Doing all of our x translations
        if (direction == 1 || direction == 2) {
            moveX = sizeX;
        }
        if (direction == 4 || direction == 5) {
            moveX = -sizeX;
        }

        // Doing all of our y translations
        if (direction == 0) {
            moveY = sizeY;
        }
        if (direction == 1 || direction == 5) {
            moveY = sizeY / 2;
        }
        if (direction == 2 || direction == 4) {
            moveY = -sizeY / 2;
        }
        if (direction == 3) {
            moveY = -sizeY;
        }
        AudibleTimeline aTimeline = new AudibleTimeline();
        int speedMils = this.controller.getAnimationSpeed();
        aTimeline.setTimeline(Timeline.createSequence()
                .push(Tween.to(teamList.get(team).get(robot), SpriteAccessor.POSITION_XY, speedMils / 1000f)
                        .targetRelative(moveX, moveY)));
        timelineTweenQueue.add(aTimeline);
    }

    /**
     * Displays an animation of a shot being fired from robot on team to a
     * relative position at direction and range
     * 
     * @param team
     *            the team number of the robot
     * @param robot
     *            the robot number in the team
     * @param direction
     *            the number of the direction (0 is north, increment clockwise)
     * @param range
     *            a range from 0 to 3
     */
    public void fireShot(int team, int robot, int direction, int range) {
        // Getting the xy coordinates based of the direction and range
        Float xTranslate;
        Float yTranslate;

        double theta;
        // In case we fire on ourselves
        if (range == 0) {
            theta = 0;
        } else {
            theta = (float) -direction / ((float) range * 6) * 2 * Math.PI + Math.PI / 2;
        }

        xTranslate = (float) ((float) 1.1 * sizeX * range * Math.cos(theta) - 2);
        yTranslate = (float) ((float) sizeY * range * Math.sin(theta) + 5);

        AudibleTimeline aTimeline = new AudibleTimeline();
        aTimeline.setProjectile(projectile);
        aTimeline.setSource(teamList.get(team).get(robot));
        int speedMils = this.controller.getAnimationSpeed();
        Timeline t = Timeline.createSequence().push(Tween.to(projectile, SpriteAccessor.POSITION_XY, speedMils / 1000f)
                .targetRelative(xTranslate, yTranslate));
        aTimeline.setTimeline(t);
        timelineTweenQueue.add(aTimeline);
    }

    public void destroyRobot(int team, int robot) {
        AudibleTimeline aTimeline = new AudibleTimeline();
        aTimeline.setSource(teamList.get(team).get(robot));
        aTimeline.setExplosion(explosionPos);
        int speedMils = this.controller.getAnimationSpeed();
        Timeline t = Timeline.createSequence().delay(speedMils / 1000f);
        aTimeline.setTimeline(t);
        timelineTweenQueue.add(aTimeline);
    }

    public void setExplosionPosition(Float x, Float y) {
        explosionPos.setX(x);
        explosionPos.setY(y);
    }

    public Float getExplosionXPos() {
        return explosionPos.getX();
    }

    public Float getExplosionYPos() {
        return explosionPos.getY();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    /**
     * set this screen to receive buttons whenever it becomes active
     */
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public boolean handle(Event arg0) {
        if (arg0.getTarget() instanceof TextButton && ((TextButton) arg0.getTarget()).isPressed()) {
            // handle button presses
            TextButton sender = (TextButton) arg0.getTarget();
            if (sender == this.pauseBtn) {
                if (this.controller.isPaused()) {
                    this.controller.resume();
                    pauseBtn.setText("Pause");
                } else {
                    this.controller.pause();
                    pauseBtn.setText("Play");
                }
            } else if (sender == this.speedBtn) {
                GameSpeed newSpeed = this.controller.switchGameSpeed();
                switch (newSpeed) {
                case GAME_SPEED_1X:
                    this.speedBtn.setText("1X");
                    break;
                case GAME_SPEED_2X:
                    this.speedBtn.setText("2X");
                    break;
                case GAME_SPEED_4X:
                    this.speedBtn.setText("4X");
                    break;
                case GAME_SPEED_16X:
                    this.speedBtn.setText("16X");
                    break;
                }
            }
        }
        return false;
    }
}
