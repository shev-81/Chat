package com.client.pak.render;

import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Класс расширяет {@link Group Group} отрисовывает в приложении сообщениее пользователя. В зависимости от используемого конструктора. используются различные декорации.
 */
public class Bubble extends Group {

  private static final int PADDING = 10;
  private static final int GAP = 4;
  private static final int ARC = 30;
  private static final Font TEXT_FONT = Font.font("Ubuntu", 15);
  private static final Font TEXT_FONT_BOLD = Font.font("Ubuntu", FontWeight.BOLD, 15);
  private static final Font META_FONT = Font.font("Ubuntu", 12);

  private Paint nameColor;
  private Paint textColor;
  private Paint metaColor;
  private Paint bubbleColor;

  /**
   * Используется для определения свойств сообщения выводимого при присоединении
   * нового пользователя, при дисконекте, а так же при смене имени пользователя.
   * @param text текст сообщения.
   */
  public Bubble(String text) {
    super();
    bubbleColor = Color.LIGHTSLATEGREY;
    nameColor = Color.BLACK;
    textColor = Color.WHITE;
    metaColor = Color.BLACK;
    paintBubble("", text, "");
  }

  /**
   * Используется для определения свойств сообщения пользователей в
   * @param name Имя пользователя.
   * @param text Текст сообщения.
   * @param meta Мета информация.
   */
  public Bubble(String name, String text, String meta) {
    super();
    bubbleColor = Color.WHITESMOKE;
    nameColor = Color.ROYALBLUE;
    textColor = Color.BLACK;
    metaColor = Color.GREY;
    paintBubble(name, text, meta);
  }

  /**
   * Определяет основные свойства визуализации сообщения.
   * @param name Имя пользователя.
   * @param text Текст сообщения.
   * @param meta Мета информация.
   */
  private void paintBubble(String name, String text, String meta) {
    int nameW = getWidth(name, TEXT_FONT_BOLD);
    int nameH = getHeight(name, TEXT_FONT_BOLD);

    int textW = getWidth(text, TEXT_FONT);
    int textH = getHeight(text, TEXT_FONT);

    int metaW = getWidth(meta, META_FONT);
    int metaH = getHeight(meta, META_FONT);

    int maxW = Math.max(Math.max(textW, metaW), nameW);

    int countGaps = 0;

    Label labelName = new Label(name);
    if (!name.equals("")) {
      countGaps++;
      labelName.setFont(TEXT_FONT_BOLD);
      labelName.setTextFill(nameColor);
      labelName.setTranslateX(PADDING );
      labelName.setTranslateY(PADDING);

    }

    Label labelText = new Label(text);
    labelText.setFont(TEXT_FONT);
    labelText.setTextFill(textColor);
    labelText.setTranslateX(PADDING);
    labelText.setTranslateY(PADDING + nameH + GAP * countGaps);

    Label labelMeta = new Label(meta);
    if (!meta.equals("")) {
      countGaps++;
      labelMeta.setFont(META_FONT);
      labelMeta.setTextFill(metaColor);
      labelMeta.setTranslateX(PADDING + maxW - metaW);
      labelMeta.setTranslateY(PADDING + nameH + textH + GAP * countGaps);
    }

    Rectangle rectangle = new Rectangle();
    rectangle.setWidth(PADDING * 2 + maxW);
    rectangle.setHeight(PADDING * 2 + nameH + textH + metaH + GAP * countGaps);
    rectangle.setArcHeight(ARC);
    rectangle.setArcWidth(ARC);
    rectangle.setFill(bubbleColor);

    getChildren().addAll(rectangle, labelName, labelText, labelMeta);
  }

  private int getWidth(String string, Font textFont) {
    Text temp = new Text(string);
    temp.setFont(textFont);
    return (string.equals("")) ? 0 : (int) temp.getLayoutBounds().getWidth();
  }

  private int getHeight(String string, Font textFont) {
    Text temp = new Text(string);
    temp.setFont(textFont);
    return (string.equals("")) ? 0 : (int) temp.getLayoutBounds().getHeight();
  }
}