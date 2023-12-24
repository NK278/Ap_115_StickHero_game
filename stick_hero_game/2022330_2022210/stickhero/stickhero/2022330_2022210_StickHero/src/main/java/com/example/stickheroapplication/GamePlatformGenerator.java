package com.example.stickheroapplication;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Random;

public class GamePlatformGenerator extends Rectangle {

    private final Random random = new Random();
    private final double screenWidth = 600.0;
    private static final double PLATFORM_HEIGHT = 100.0;
    private static final double minWidth = 20.0;
    private static final double maxWidth = 100.0;
    private static final double layoutY = 300.0;
    private static final double minLayoutX = 0.0;
    private static final double maxLayoutX = 600.0;


    public Rectangle initializePlatform() {
        Rectangle platform = new Rectangle();
        platform.setLayoutY(layoutY);
        platform.setLayoutX(50.0);
        platform.setWidth(90.0);
        platform.setHeight(PLATFORM_HEIGHT);
        platform.setFill(Color.BLACK);
        return platform;
    }

    public Rectangle generatePlatform() {
        Rectangle platform = new Rectangle();
        platform.setLayoutY(layoutY);
        platform.setWidth(random.nextDouble() * (maxWidth - minWidth) + minWidth);
        platform.setHeight(PLATFORM_HEIGHT);
        platform.setFill(Color.BLACK);
        return platform;
    }
}
