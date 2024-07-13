package gui;

import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Manages gameplay logic and rendering for a 2D shooting game using JavaFX.
 * Allows player movement, laser shooting, monster spawning, boss mechanics, and score tracking.
 * Displays player score and game over conditions. Uses images for graphical elements.
 */

public class Game {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_SIZE = 100;
    private static final int MONSTER_SIZE = 50;
    private static final int BOSS_SIZE = 200;
    private static final int PLAYER_SPEED = 5;
    private static final int MONSTER_SPEED = 4;
    private static final int BOSS_SPEED = 2;
    private static int LASER_SPEED = 6;


    private boolean upPressed, downPressed, leftPressed, rightPressed, spacePressed;
    private boolean shootingAllowed = true;
    private int laserCooldown = 0;
    private int score = 0;

    private Group root = new Group();
    private Canvas canvas = new Canvas(WIDTH, HEIGHT);
    private GraphicsContext gc = canvas.getGraphicsContext2D();

    private ImageView player;
    private List<ImageView> monsters = new ArrayList<>();
    private List<Rectangle> lasers = new ArrayList<>();

    private Image playerImage = new Image("/image/drone2.png");
    private Image monsterImage = new Image("/image/monster2.png");
    private Image backgroundImage = new Image("/image/4_background.png" );
    private Image bossImage = new Image("/image/boss.png");
    private Image bossImage1 = new Image("/image/boos1.png");
    private Image bossImage2 = new Image("/image/boss2.png");
    private ImageView boss;
    private boolean bossActive = false;
    private int bossHealth = BOSS_MAX_HEALTH;
    private TranslateTransition bossTransition;


    private static final int BOSS_MAX_HEALTH = 20;
    private static final int BOSS_HEALTHBAR_WIDTH = 200;
    private static final int BOSS_HEALTHBAR_HEIGHT = 20;
    private static final int BOSS_HEALTHBAR_X = WIDTH / 2 - BOSS_HEALTHBAR_WIDTH / 2;
    private static final int BOSS_HEALTHBAR_Y = 50;
    private Runnable gameOverCallback;
    private AnimationTimer gameLoop;

    public Game(Runnable gameOverCallback) {
        this.gameOverCallback = gameOverCallback;

        // Set up background
        ImageView background = new ImageView(backgroundImage);
        background.setFitWidth(WIDTH);
        background.setFitHeight(HEIGHT);
        root.getChildren().add(background);

        // Set up player
        player = new ImageView(playerImage);
        player.setFitWidth(PLAYER_SIZE);
        player.setFitHeight(PLAYER_SIZE);
        player.setX(WIDTH / 2 - PLAYER_SIZE / 2);
        player.setY(HEIGHT - PLAYER_SIZE - 10);
        root.getChildren().add(player);

        // Add canvas
        root.getChildren().add(canvas);
    }

    public Group createContent() {
        return root;
    }

    public void handleKeyPress(javafx.scene.input.KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (keyCode == KeyCode.UP) upPressed = true;
        if (keyCode == KeyCode.DOWN) downPressed = true;
        if (keyCode == KeyCode.LEFT) leftPressed = true;
        if (keyCode == KeyCode.RIGHT) rightPressed = true;
        if (keyCode == KeyCode.SPACE) spacePressed = true;
    }

    public void handleKeyRelease(javafx.scene.input.KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (keyCode == KeyCode.UP) upPressed = false;
        if (keyCode == KeyCode.DOWN) downPressed = false;
        if (keyCode == KeyCode.LEFT) leftPressed = false;
        if (keyCode == KeyCode.RIGHT) rightPressed = false;
        if (keyCode == KeyCode.SPACE) spacePressed = false;
    }

    public void update() {
        // Player movement
        if (upPressed && player.getY() > 0) {
            player.setY(player.getY() - PLAYER_SPEED);
        }
        if (downPressed && player.getY() < HEIGHT - PLAYER_SIZE) {
            player.setY(player.getY() + PLAYER_SPEED);
        }
        if (leftPressed && player.getX() > 0) {
            player.setX(player.getX() - PLAYER_SPEED);
        }
        if (rightPressed && player.getX() < WIDTH - PLAYER_SIZE) {
            player.setX(player.getX() + PLAYER_SPEED);
        }

        // Laser shooting
        if (spacePressed && shootingAllowed && laserCooldown <= 0) {
            Rectangle laser = new Rectangle(player.getX() + PLAYER_SIZE / 2 - 2, player.getY(), 4, 10);
            laser.setFill(Color.RED);
            lasers.add(laser);
            root.getChildren().add(laser);
            shootingAllowed = true;
            LASER_SPEED=3;
            laserCooldown = 20;
        }

        // Update lasers
        Iterator<Rectangle> laserIterator = lasers.iterator();
        while (laserIterator.hasNext()) {
            Rectangle laser = laserIterator.next();
            laser.setY(laser.getY() - LASER_SPEED);
            if (laser.getY() < 0) {
                laserIterator.remove();
                root.getChildren().remove(laser);
            }
        }

        updateMonsters();
        // Boss logic
        if (score >= 2 && !bossActive) {
            spawnBoss();
        }
        if (bossActive) {
            updateBoss();
        }

        // Cooldown for shooting
        if (!spacePressed && !shootingAllowed) {
            shootingAllowed = true;
        }
        if (laserCooldown > 0) {
            laserCooldown--;
        }
    }

    private void spawnBoss() {
        if (boss != null || bossActive) {

            return; // Return early if boss already exists or is active
        }

        boss = new ImageView(bossImage);
        boss.setFitWidth(BOSS_SIZE);
        boss.setFitHeight(BOSS_SIZE);
        boss.setX(Math.random()*WIDTH /4 - BOSS_SIZE / 2);
        boss.setY(HEIGHT /4 - BOSS_SIZE /2 );
        root.getChildren().add(boss);
        bossActive = true;

        bossTransition = new TranslateTransition(Duration.seconds(2), boss);
        bossTransition.setFromX(boss.getX());
        bossTransition.setFromY(boss.getY());
        bossTransition.setToX( (WIDTH - BOSS_SIZE));
        bossTransition.setToY( BOSS_SIZE); // Adjust endpoint as needed
        bossTransition.setCycleCount(TranslateTransition.INDEFINITE);
        bossTransition.setAutoReverse(true);
        bossTransition.setInterpolator(Interpolator.LINEAR); // Optional: Ensure linear movement
        bossTransition.play();
    }


    private void updateMonsters() {

        Iterator<ImageView> monsterIterator = monsters.iterator();
        while (monsterIterator.hasNext()) {
            ImageView monster = monsterIterator.next();
            monster.setY(monster.getY() + MONSTER_SPEED);

            // Collision detection with player
            if (monster.getBoundsInParent().intersects(player.getBoundsInParent())) {
                gameOver();
                return;
            }

            // Collision detection with lasers
            Iterator<Rectangle> laserIterator = lasers.iterator();
            while (laserIterator.hasNext()) {
                Rectangle laser = laserIterator.next();
                if (monster.getBoundsInParent().intersects(laser.getBoundsInParent())) {
                    laserIterator.remove();
                    root.getChildren().remove(laser);
                    monsterIterator.remove();
                    root.getChildren().remove(monster);
                    score++;
                    break; // Break out of inner loop to handle one collision at a time
                }
            }
        }

        // Spawn new monsters
        if (Math.random() < 0.02 ) {
            ImageView monster = new ImageView(monsterImage);
            monster.setFitWidth(MONSTER_SIZE);
            monster.setFitHeight(MONSTER_SIZE);
            monster.setX(Math.random() * (WIDTH - MONSTER_SIZE));
            monster.setY(0);
            monsters.add(monster);
            root.getChildren().add(monster);
        }
    }

    private void updateBoss() {
        // Collision detection with player
        if (boss.getBoundsInParent().intersects(player.getBoundsInParent())) {
            gameOver();
            return;
        }

        // Collision detection with lasers
        Iterator<Rectangle> laserIterator = lasers.iterator();
        while (laserIterator.hasNext()) {
            Rectangle laser = laserIterator.next();
            if (boss.getBoundsInParent().intersects(laser.getBoundsInParent())) {
                laserIterator.remove();
                root.getChildren().remove(laser);
                bossHealth--;
                if (bossHealth <= 0) {
                    boss.setImage(bossImage2);
                    bossHealth = 4;
                    PauseTransition pause = new PauseTransition(Duration.seconds(0.3)); // 1-second delay
                    pause.setOnFinished(event -> {
                        bossDefeated();
                    });
                    pause.play();

                }
            }
        }
    }


    private void bossDefeated() {
        if (boss != null) {
            bossTransition.stop();
            root.getChildren().remove(boss);
        }
        bossActive = false;
        bossHealth = 0; // Reset boss health
        score += 35;
        if (bossActive==false){
            boss = new ImageView(bossImage2);
        }
        updateMonsters();

    }

    public void startGameLoop() {
        gameLoop = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                update();
                render();
            }
        };
        gameLoop.start();
    }

    private void render() {
        // Clear canvas
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        // Draw lasers
        for (Rectangle laser : lasers) {
            gc.setFill(laser.getFill());
            gc.fillRect(laser.getX(), laser.getY(), laser.getWidth(), laser.getHeight());
        }

        // Draw boss health bar
        gc.setFill(Color.RED);
        gc.fillRect(BOSS_HEALTHBAR_X, BOSS_HEALTHBAR_Y, BOSS_HEALTHBAR_WIDTH * ((double) bossHealth / BOSS_MAX_HEALTH), BOSS_HEALTHBAR_HEIGHT);

        // Draw score
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 20));
        gc.fillText("Score: " + score, 10, 30);
    }

    private void gameOver() {
        gameLoop.stop();
        Platform.runLater(gameOverCallback);
    }

    public int getScore() {
        return score;
    }
}
