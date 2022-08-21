package com.client.pak.services;

import com.client.pak.Controller;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import com.client.pak.render.Bubble;

import java.io.*;
import java.util.ArrayList;

/**
 * Класс предназначен для загрузки истории сообщений из файлов пользователей, а так же записи истории в файлы.
 */
public class FileWorker {

    /**
     * Переменная ссылка на контроллер.
     */
    private final Controller controller;

    /**
     * Конструктор сохраняет ссылку на контроллер.
     * @param controller контроллер приложения.
     */
    public FileWorker(Controller controller) {
        this.controller = controller;
    }

    /**
     * Читает из фйала истории пользователя последние 10 сообщений пользователя и передает их контроллеру для вывода пользователю.
     */
    public void loadAllMsg() {
        int i;
        String str;
        ArrayList<String> loadMsg = new ArrayList<>();
        File file = new File("client/chathistory/" + controller.getMyName() + "_msg.txt");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((str = reader.readLine()) != null) {
                loadMsg.add(str);
            }
            i = loadMsg.size() - 10;
            i = (i <= 0) ? 0 : loadMsg.size() - 10;
            for (; i < loadMsg.size(); i++) {
                String[] parts = loadMsg.get(i).split("\\s+");
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j < parts.length; j++) {
                    sb.append(parts[j]).append(" ");
                }
                Bubble chatMessage = new Bubble(parts[0], sb.toString().trim(), "");
                GridPane.setValignment(chatMessage, VPos.BOTTOM);
                if (!parts[0].equals(controller.getMyName())) {
                    GridPane.setHalignment(chatMessage, HPos.LEFT);
                } else {
                    GridPane.setHalignment(chatMessage, HPos.RIGHT);
                }
                Platform.runLater(() -> controller.getChat().addRow(controller.getChat().getRowCount(), chatMessage));
            }
            Platform.runLater(() -> controller.scrollDown());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Записывает исорию сообщения в конец файл пользователя.
     * @param msg Строка сообщение пользователя.
     */
    public void saveMsgToFile(String msg) {
        try (BufferedWriter in = new BufferedWriter(new FileWriter("client/chathistory/" + controller.getMyName() + "_msg.txt", true))) {
            in.write(msg);
            in.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
