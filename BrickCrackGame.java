package application;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BrickCrackGame extends Application {

    // Constants
    static int BALL_WIDTH = 20;
    static int BALL_HEIGHT = 20;
    static int BLOCK_ROWS = 8;
    static int BLOCK_COLUMNS = 10;
    static int BLOCK_WIDTH = 40;
    static int BLOCK_HEIGHT = 20;
    static int BLOCK_GAP = 3;
    static int BAR_WIDTH = 80;
    static int BAR_HEIGHT = 20;
    static int CANVAS_WIDTH = 400 + (BLOCK_GAP * BLOCK_COLUMNS) - BLOCK_GAP;
    static int CANVAS_HEIGHT = 600;

    // Variables
    static int score = 0;
    static Block[][] blocks = new Block[BLOCK_ROWS][BLOCK_COLUMNS];
    static Bar bar = new Bar();
    static Ball ball = new Ball();
    static int barXTarget = bar.x; // Target Value - interpolation
    static int dir = 0; // 0: Up-Right 1: Down-Right 2: Up-Left 3: Down-Left
    static int ballSpeed = 5;
    static boolean isGameFinish = false;

    static class Ball {
        int x = CANVAS_WIDTH / 2 - BALL_WIDTH / 2;
        int y = CANVAS_HEIGHT / 2 - BALL_HEIGHT / 2;
        int width = BALL_WIDTH;
        int height = BALL_HEIGHT;

        Rectangle2D getBoundary() {
            return new Rectangle2D(x, y, width, height);
        }

        Rectangle2D getTopBoundary() {
            return new Rectangle2D(x + (width / 2), y, 1, 1);
        }

        Rectangle2D getBottomBoundary() {
            return new Rectangle2D(x + (width / 2), y + height, 1, 1);
        }

        Rectangle2D getLeftBoundary() {
            return new Rectangle2D(x, y + (height / 2), 1, 1);
        }

        Rectangle2D getRightBoundary() {
            return new Rectangle2D(x + width, y + (height / 2), 1, 1);
        }
    }

    static class Bar {
        int x = CANVAS_WIDTH / 2 - BAR_WIDTH / 2;
        int y = CANVAS_HEIGHT - 100;
        int width = BAR_WIDTH;
        int height = BAR_HEIGHT;

        Rectangle2D getBoundary() {
            return new Rectangle2D(x, y, width, height);
        }
    }

    static class Block {
        int x = 0;
        int y = 0;
        int width = BLOCK_WIDTH;
        int height = BLOCK_HEIGHT;
        int color = 0; // 0:white 1:yellow 2:blue 3:magenta 4:red
        boolean isHidden = false; // after collision, block will be hidden.

        Rectangle2D getBoundary() {
            return new Rectangle2D(x, y, width, height);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);

        GraphicsContext gc = canvas.getGraphicsContext2D();

        initData();
        setKeyPressHandler(scene);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                movement();
                checkCollision();
                checkCollisionBlock();
                drawGameElements(gc);
                if (isGameFinish) {
                    stop();
                }
            }
        };

        primaryStage.setTitle("Block Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void initData() {
        int currentX = BLOCK_GAP;
        int currentY = 50;
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLUMNS; col++) {
                blocks[row][col] = new Block();
                blocks[row][col].x = currentX;
                blocks[row][col].y = currentY;
                blocks[row][col].color = row; // Each row will have different colors
                currentX += BLOCK_WIDTH + BLOCK_GAP;
            }
            currentX = BLOCK_GAP;
            currentY += BLOCK_HEIGHT + BLOCK_GAP;
        }
    }

    public void setKeyPressHandler(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                barXTarget -= 10;
                if (barXTarget < 0) {
                    barXTarget = 0; // 왼쪽 끝으로 제한
                }
            } else if (event.getCode() == KeyCode.RIGHT) {
                barXTarget += 10;
                if (barXTarget > CANVAS_WIDTH - bar.width) {
                    barXTarget = CANVAS_WIDTH - bar.width; // 오른쪽 끝으로 제한
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.RIGHT) {
                // Stop bar movement when the key is released
                barXTarget = bar.x;
            }
        });
    }

    public void movement() {
        if (bar.x < barXTarget) {
            bar.x += 5;
        } else if (bar.x > barXTarget) {
            bar.x -= 5;
        }

        if (!isGameFinish) {
            switch (dir) {
                case 0: // Up-Right
                    ball.x += ballSpeed;
                    ball.y -= ballSpeed;
                    break;
                case 1: // Down-Right
                    ball.x += ballSpeed;
                    ball.y += ballSpeed;
                    break;
                case 2: // Up-Left
                    ball.x -= ballSpeed;
                    ball.y -= ballSpeed;
                    break;
                case 3: // Down-Left
                    ball.x -= ballSpeed;
                    ball.y += ballSpeed;
                    break;
            }

            // Check if the ball hits the walls
            if (ball.x <= 0) { // Left wall
                if (dir == 2) {
                    dir = 0; // Change to Up-Right
                } else if (dir == 3) {
                    dir = 1; // Change to Down-Right
                }
            } else if (ball.x + ball.width >= CANVAS_WIDTH) { // Right wall
                if (dir == 0) {
                    dir = 2; // Change to Up-Left
                } else if (dir == 1) {
                    dir = 3; // Change to Down-Left
                }
            }

            // Check if the ball hits the top or bottom walls
            if (ball.y <= 0) { // Top wall
                if (dir == 0) {
                    dir = 1; // Change to Down-Right
                } else if (dir == 2) {
                    dir = 3; // Change to Down-Left
                }
            } else if (ball.y + ball.height >= CANVAS_HEIGHT) { // Bottom wall
                if (dir == 1) {
                    dir = 0; // Change to Up-Right
                } else if (dir == 3) {
                    dir = 2; // Change to Up-Left
                }
            }

            // Check if the ball hits the bar
            if (ball.getBoundary().intersects(bar.getBoundary())) {
                if (dir == 0 || dir == 1) {
                    dir = 2; // Change to Up-Left
                } else if (dir == 2 || dir == 3) {
                    dir = 0; // Change to Up-Right
                }
            }

            // Adjust ball position if it goes beyond the boundaries
            if (ball.x < 0) {
                ball.x = 0;
            } else if (ball.x + ball.width > CANVAS_WIDTH) {
                ball.x = CANVAS_WIDTH - ball.width;
            }
            if (ball.y < 0) {
                ball.y = 0;
            } else if (ball.y + ball.height > CANVAS_HEIGHT) {
                ball.y = CANVAS_HEIGHT - ball.height;
                isGameFinish = true; // Game finishes when the ball hits the bottom wall
                System.out.println("Your Score Is: " + score); // Display the score
                System.out.println("Game Over");
            }
        }
    }


    public void checkCollision() {
        // Check if the ball hits the blocks
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLUMNS; col++) {
                Block block = blocks[row][col];
                if (!block.isHidden && ball.getBoundary().intersects(block.getBoundary())) {
                    block.isHidden = true;
                    score += 10;

                    // Adjust ball direction based on collision side
                    if (ball.getTopBoundary().intersects(block.getBoundary())
                            || ball.getBottomBoundary().intersects(block.getBoundary())) {
                        if (dir == 0 || dir == 1) {
                            dir = (dir + 2) % 4;
                        } else {
                            dir = (dir - 2) % 4;
                            if (dir < 0) {
                                dir += 4;
                            }
                        }
                    } else if (ball.getLeftBoundary().intersects(block.getBoundary())
                            || ball.getRightBoundary().intersects(block.getBoundary())) {
                        if (dir == 0 || dir == 2) {
                            dir = (dir + 1) % 4;
                        } else {
                            dir = (dir - 1) % 4;
                            if (dir < 0) {
                                dir += 4;
                            }
                        }
                    }
                }
            }
        }
    }

    public void checkCollisionBlock() {
        boolean isBlockExist = false;
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLUMNS; col++) {
                if (!blocks[row][col].isHidden) {
                    isBlockExist = true;
                    break;
                }
            }
            if (isBlockExist) {
                break;
            }
        }
        if (!isBlockExist) {
            isGameFinish = true;
        }
    }

    public void drawGameElements(GraphicsContext gc) {
        // Draw blocks
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLUMNS; col++) {
                Block block = blocks[row][col];
                if (!block.isHidden) {
                    switch (block.color) {
                        case 0:
                            gc.setFill(Color.WHITE);
                            break;
                        case 1:
                            gc.setFill(Color.RED);
                            break;
                        case 2:
                            gc.setFill(Color.ORANGE);
                            break;
                        case 3:
                            gc.setFill(Color.YELLOW);
                            break;
                        case 4:
                            gc.setFill(Color.GREEN);
                            break;
                        case 5: 
                        	gc.setFill(Color.BLUE);
                        	break;
                        case 6: 
                        	gc.setFill(Color.PURPLE);
                        	break;
                        case 7: 
                        	gc.setFill(Color.BLACK);
                        	break;
                        	
                        	
                    }
                    gc.fillRect(block.x, block.y, block.width, block.height);
                }
            }
        }

        // Draw bar
        gc.setFill(Color.GREEN);
        gc.fillRect(bar.x, bar.y, bar.width, bar.height);

        // Draw ball
        gc.setFill(Color.ORANGE);
        gc.fillOval(ball.x, ball.y, ball.width, ball.height);

        // Draw score
        gc.setFill(Color.BLACK);
        gc.fillText("Score: " + score, 10, 20);
    }
}
