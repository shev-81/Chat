package com.client.pak.render;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

/**
 * Класс для добавления изображения пользователя (Аватарки пользователя).
 */
public class UserPicture extends Group {

  /**
   * Радиус иконки пользователя.
   */
  private static final int RADIUS = 27;

  /**
   * Конструктор определяет изображение иконки и его радиус.
   */
  public UserPicture() {
    Circle circle = new Circle();
    circle.setRadius(RADIUS);
    circle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/images/default.png"))));
    getChildren().addAll(circle);
  }
}
