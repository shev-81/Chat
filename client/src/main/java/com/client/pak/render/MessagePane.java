package com.client.pak.render;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

/**
 * Класс описывающий панель сообщений пользователей.
 */
public class MessagePane extends GridPane {

    /**
     * Конструктор определяет Имя панели и ее размеры.
     * @param namePane Имя панели соответствует имени пользователя.
     */
    public MessagePane(String namePane) {
        super();
        setId(namePane);
        ColumnConstraints col1 = new ColumnConstraints();
        setPadding(new Insets(10, 10, 10, 10));
        setPrefHeight(1200);
        setAlignment(Pos.BOTTOM_RIGHT);
        col1.setMinWidth(10.0);
        col1.setPercentWidth(100.0);
        getColumnConstraints().addAll(col1);
    }
}
