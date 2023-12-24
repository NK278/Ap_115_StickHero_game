package com.example.stickheroapplication;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class GamePlayController {

    public Label gameOverLabel;
    @FXML
    private Pane gameScreen;

    @FXML
    private Label score;
    @FXML
    private ImageView stickHero;

    private boolean isGrowing = false;
    private boolean istranslating = false;
    private Line stick;
    private double stickLength = 0.0;
    private final double fixedCharDist = 55.0;
    private final double minDist = 30.0;
    private boolean isCharacterFacingRight = true;
    private final double maxDist = 150.0;
    private ArrayList<Rectangle> platforms = new ArrayList<>();
    private Rectangle prevPlatform;
    private Rectangle currPlatform;
    private double angle = 270.0;

    private SoundManager soundManager = new SoundManager();

    private AnimationTimer stickExtension;

    private Timeline timeline;

    private GamePlatformGenerator gamePlatformGenerator = new GamePlatformGenerator();

    Random random = new Random();

    @FXML
    public void initialize() {
        Rectangle initPlatform = gamePlatformGenerator.initializePlatform();
        gameScreen.getChildren().add(initPlatform);
        prevPlatform = initPlatform;
        currPlatform = initPlatform;
        platforms.add(initPlatform);
        startGeneratingPlatforms();
        gameScreen.setOnMousePressed(event -> {
            if (!istranslating && !isGrowing) {
                isGrowing = true;
                handleMousePressed();
            }
        });
        gameScreen.setOnMouseReleased(event -> {
            if (isGrowing) {
                isGrowing = false;
                handleMouseReleased();
            }
        });

    }

    private void startGeneratingPlatforms() {
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(0.1),
                event -> generatePlatform()
        );

        timeline = new javafx.animation.Timeline(keyFrame);
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
    }

    private void stopGeneratingPlatforms() {
        //stop the platform generator
        timeline.stop();
    }

    private void generatePlatform() {
        Rectangle platform = gamePlatformGenerator.generatePlatform();
        double minLayoutX = prevPlatform.getLayoutX() + prevPlatform.getWidth() + minDist;
        double maxLayoutX = prevPlatform.getLayoutX() + prevPlatform.getWidth() + maxDist;
        double layoutX = random.nextDouble() * (maxLayoutX - minLayoutX) + minLayoutX;
        platform.setLayoutX(layoutX);
        gameScreen.getChildren().add(platform);
        platforms.add(platform);
        prevPlatform = platform;
    }
//    private void setCharacterOrientation() {
//        if (isCharacterFacingRight) {
//            stickHero.setY(35);
//            stickHero.setScaleY(1); // Facing right
//        } else {
//            stickHero.setY(70);
//            stickHero.setScaleY(-1); // Facing left (inverse)
//        }
//    }


    public void handleMousePressed() {
        double pivotX = currPlatform.getLayoutX() + currPlatform.getWidth();
        double pivotY = currPlatform.getLayoutY();
        stick = new Line(pivotX, pivotY, pivotX, pivotY);
        stick.setStroke(Color.BLACK);
        stick.setStrokeWidth(4.0);
        gameScreen.getChildren().add(stick);
        soundManager.playStickGrowSound();

        final long[] startTime = { System.nanoTime() };

        stickExtension = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double elapsedTime = (now - startTime[0]) / 1e9;
                if (elapsedTime > 0.1) {
                    stickLength += 10;
                    stick.setEndY(stick.getStartY() - stickLength);
                    startTime[0] = System.nanoTime();
                }
            }
        };

        // Toggle character orientation when the stick is growing
        isCharacterFacingRight = !isCharacterFacingRight;
//        setCharacterOrientation();

        stickExtension.start();
    }



    public void handleMouseReleased() {
        //stop the animation timer
        stickExtension.stop();
        rotateStick();
    }


    private void rotateStick() {
        //rotate stick by 90 degrees by adding setX and setY using timeline
        double centerX = stick.getStartX();
        double centerY = stick.getStartY();

        Timeline timeline = new Timeline(new javafx.animation.KeyFrame(Duration.millis(40), e -> {
            angle += 5;

            double angleInRadians = Math.toRadians(angle);
            double endX = centerX + stickLength * Math.cos(angleInRadians);
            double endY = centerY + stickLength * Math.sin(angleInRadians);

            stick.setStartX(centerX);
            stick.setStartY(centerY);
            stick.setEndX(endX);
            stick.setEndY(endY);

        }));
        timeline.setCycleCount(18);
        timeline.play();
        //pause till stick animation completes
        timeline.setOnFinished(event -> {
            checkIfStickIsLongEnough();
        });
    }

    private void checkIfStickIsLongEnough() {
        //check if stick is long enough to reach the next platform
        //if it is, move the character to the next platform
        //if it is not, end the game
        double currPlatformEndX = currPlatform.getLayoutX() + currPlatform.getWidth();
        double nextPlatformStartX = platforms.get(platforms.indexOf(currPlatform) + 1).getLayoutX();
        double nextPlatformEndX = nextPlatformStartX + platforms.get(platforms.indexOf(currPlatform) + 1).getWidth();
        if (stickLength >= nextPlatformStartX - currPlatformEndX && stickLength <= nextPlatformEndX - currPlatformEndX) {
            moveCharacterToNextPlatform();
            soundManager.playStickFallSound();
        } else {
            endGame();
        }
    }

    private void moveCharacterToNextPlatform() {
        istranslating = true;
        Rectangle previousPlatform = currPlatform;
        int nextPlatformIndex = platforms.indexOf(currPlatform) + 1;
        currPlatform = platforms.get(nextPlatformIndex);
        //Animate the character to move to the next platform
        double characterX = stickHero.getLayoutX();
        double characterEndX = currPlatform.getLayoutX() + currPlatform.getWidth() - fixedCharDist;
        TranslateTransition tt = new TranslateTransition(Duration.millis(2000), stickHero);
        tt.setToX(characterEndX - characterX);
        tt.play();
        tt.setOnFinished(event -> {
            istranslating = false;
            if (score.getText().equals("00")){
                score.setText("01");
            } //check if score is less than or equal to 09
            else if (Integer.parseInt(score.getText()) <= 9) {
                int currScore = Integer.parseInt(score.getText());
                currScore++;
                score.setText(String.format("%02d", currScore)); // 2 digits
            } else{
                int currScore = Integer.parseInt(score.getText());
                currScore++;
                score.setText(currScore + "");
            }
            gameScreen.getChildren().remove(stick);
            stickLength = 0.0;
            angle = 270.0;
            double shiftAmount = currPlatform.getLayoutX() - previousPlatform.getLayoutX();
            stickHero.setLayoutX(stickHero.getLayoutX() - shiftAmount);
            for (Rectangle platform : platforms) {
                platform.setLayoutX(platform.getLayoutX() - shiftAmount);
            }
        });
    }
    private void endGame() {
        stopGeneratingPlatforms();
        //move player till end of stick and then fall
        double characterX = stickHero.getLayoutX();
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), stickHero);
        tt.setToX(stick.getEndX() - characterX - 30);
        tt.play();
        tt.setOnFinished(event -> {
            gameScreen.getChildren().remove(stick);
            stickLength = 0.0;
            angle = 270.0;
            playerFalls();
            displayGameOverMessage();
        });
    }
    private void displayGameOverMessage() {
        soundManager.playGameOverSound();
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), gameOverLabel);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(2)); // Adjust the duration as needed
        pause.setOnFinished(event -> switchToMainScreen());
        pause.play();
    }


    private void switchToMainScreen() {
        try {
            // Load the main screen from FXML
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("home-screen.fxml")));

            // Get the current stage
            Stage stage = (Stage) gameScreen.getScene().getWindow();

            // Set the main screen scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void playerFalls() {
        double characterY = stickHero.getLayoutY();
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), stickHero);
        tt.setToY(characterY + 250);
        tt.play();
        tt.setOnFinished(event -> {
            soundManager.playPlayerFallSound();
        });
    }

//    @FXML
//    protected void onHelloButtonClick() {
//        welcomeText.setText("Welcome to JavaFX Application!");
//    }
//    private void loadFXML(String fxmlFile) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
//            loader.setController(this); // Set the controller to the same instance
//            Parent root = loader.load();
//            Scene scene = new Scene(root);
//            Stage currentStage = (Stage) play.getScene().getWindow();
//            currentStage.setScene(scene);
//            currentStage.show();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}

}
