package com.client.pak.render;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

/**
 * A class for adding a user image (User avatars).
 */
public class UserPicture extends Group {

  /**
   * The radius of the user icon.
   */
  private static final int RADIUS = 27;

  /**
   * The constructor defines the icon image and its radius.
   */
  public UserPicture() {
    Circle circle = new Circle();
    circle.setRadius(RADIUS);
    circle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/images/default.png"))));
    getChildren().addAll(circle);
  }
}
