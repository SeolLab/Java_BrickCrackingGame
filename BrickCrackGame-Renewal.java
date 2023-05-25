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
    static int circle_WIDTH = 20;
    static int circle_HEIGHT = 20;
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
    static Brick[][] bricks = new Brick[BLOCK_ROWS][BLOCK_COLUMNS];
    static Bar bar = new Bar();
    static Circle circle = new Circle();
    static int barXTarget = bar.x; // Target Value - interpolation
    static int dir = 0; // 0: Up-Right 1: Down-Right 2: Up-Left 3: Down-Left
    static int circleSpeed = 5;
    static boolean isGameFinish = false;

    static class Circle {
        int x = CANVAS_WIDTH / 2 - circle_WIDTH / 2;
        int y = CANVAS_HEIGHT / 2 - circle_HEIGHT / 2;
        int width = circle_WIDTH;
        int height = circle_HEIGHT;

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

    static class Brick {
        int x = 0;
        int y = 0;
        int width = BLOCK_WIDTH;
        int height = BLOCK_HEIGHT;
        int color = 0; // 0:white 1:yellow 2:blue 3:magenta 4:red
        boolean isHidden = false; // after collision, brick will be hidden.

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
                checkCollisionBrick();
                drawGameElements(gc);
                if (isGameFinish) {
                    stop();
                }
            }
        };

        primaryStage.setTitle("Brick Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        timer.start();
    }

    public static void main(String[] args) {
    	System.out.println("If the circle falls to the ground the game is over!");
    	System.out.println("Use your bar! with <- and -> keys");
        launch(args);
    }

    public void initData() {
        int currentX = BLOCK_GAP;
        int currentY = 50;
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLUMNS; col++) {
                bricks[row][col] = new Brick();
                bricks[row][col].x = currentX;
                bricks[row][col].y = currentY;
                bricks[row][col].color = row; // Each row will have different colors
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
                    circle.x += circleSpeed;
                    circle.y -= circleSpeed;
                    break;
                case 1: // Down-Right
                    circle.x += circleSpeed;
                    circle.y += circleSpeed;
                    break;
                case 2: // Up-Left
                    circle.x -= circleSpeed;
                    circle.y -= circleSpeed;
                    break;
                case 3: // Down-Left
                    circle.x -= circleSpeed;
                    circle.y += circleSpeed;
                    break;
            }

            // Check if the circle hits the walls
            if (circle.x <= 0) { // Left wall
                if (dir == 2) {
                    dir = 0; // Change to Up-Right
                } else if (dir == 3) {
                    dir = 1; // Change to Down-Right
                }
            } else if (circle.x + circle.width >= CANVAS_WIDTH) { // Right wall
                if (dir == 0) {
                    dir = 2; // Change to Up-Left
                } else if (dir == 1) {
                    dir = 3; // Change to Down-Left
                }
            }

            // Check if the circle hits the top or bottom walls
            if (circle.y <= 0) { // Top wall
                if (dir == 0) {
                    dir = 1; // Change to Down-Right
                } else if (dir == 2) {
                    dir = 3; // Change to Down-Left
                }
            } else if (circle.y + circle.height >= CANVAS_HEIGHT) { // Bottom wall
                if (dir == 1) {
                    dir = 0; // Change to Up-Right
                } else if (dir == 3) {
                    dir = 2; // Change to Up-Left
                }
            }
            
            
            // Bottom wall에 닿으면 게임 종료 
            
            if (circle.y + circle.height >= CANVAS_HEIGHT) { // Bottom wall
                isGameFinish = true; // 게임 종료
                System.out.println("Your Score Is: " + score); // 점수 표시
                System.out.println("...Game Over..."); // 게임 종료 메시지
            }

            
            

            // Check if the circle hits the bar
            if (circle.getBoundary().intersects(bar.getBoundary())) {
                if (dir == 0 || dir == 1) {
                    dir = 2; // Change to Up-Left
                } else if (dir == 2 || dir == 3) {
                    dir = 0; // Change to Up-Right
                }
            }

            // Adjust circle position if it goes beyond the boundaries
            if (circle.x < 0) {
                circle.x = 0;
            } else if (circle.x + circle.width > CANVAS_WIDTH) {
                circle.x = CANVAS_WIDTH - circle.width;
            }
            if (circle.y < 0) {
                circle.y = 0;
            } else if (circle.y + circle.height > CANVAS_HEIGHT) {
                circle.y = CANVAS_HEIGHT - circle.height;
                isGameFinish = true; // Game finishes when the circle hits the bottom wall
                System.out.println("Your Score Is: " + score); // Display the score
                System.out.println("Game Over");
            }
        }
    }


    public void checkCollision() {
        // Check if the circle hits the bricks
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLUMNS; col++) {
                Brick brick = bricks[row][col];
                if (!brick.isHidden && circle.getBoundary().intersects(brick.getBoundary())) {
                    brick.isHidden = true;
                    score += 10;

                    // Adjust circle direction based on collision side
                    if (circle.getTopBoundary().intersects(brick.getBoundary())
                            || circle.getBottomBoundary().intersects(brick.getBoundary())) {
                        if (dir == 0 || dir == 1) {
                            dir = (dir + 2) % 4;
                        } else {
                            dir = (dir - 2) % 4;
                            if (dir < 0) {
                                dir += 4;
                            }
                        }
                    } else if (circle.getLeftBoundary().intersects(brick.getBoundary())
                            || circle.getRightBoundary().intersects(brick.getBoundary())) {
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

    public void checkCollisionBrick() {
        boolean isBrickExist = false;
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLUMNS; col++) {
                if (!bricks[row][col].isHidden) {
                    isBrickExist = true;
                    break;
                }
            }
            if (isBrickExist) {
                break;
            }
        }
        if (!isBrickExist) {
            isGameFinish = true;
        }
    }

    public void drawGameElements(GraphicsContext gc) {
        // Draw bricks
        for (int row = 0; row < BLOCK_ROWS; row++) {
            for (int col = 0; col < BLOCK_COLUMNS; col++) {
                Brick brick = bricks[row][col];
                if (!brick.isHidden) {
                    switch (brick.color) {
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
                    gc.fillRect(brick.x, brick.y, brick.width, brick.height);
                }
            }
        }

        // Draw bar
        gc.setFill(Color.GREEN);
        gc.fillRect(bar.x, bar.y, bar.width, bar.height);

        // Draw circle
        gc.setFill(Color.ORANGE);
        gc.fillOval(circle.x, circle.y, circle.width, circle.height);

        // Draw score
        gc.setFill(Color.BLACK);
        gc.fillText("Score: " + score, 10, 20);
    }
}
