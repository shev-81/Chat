package com.client.pak;

import com.client.pak.render.Bubble;
import com.client.pak.render.CellRenderer;
import com.client.pak.render.MessagePane;
import com.client.pak.render.UserCell;
import com.client.pak.services.FileWorker;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.Data;
import message.*;

import java.net.URL;
import java.util.*;

/**
 * Класс контроллер приложения. Связывает в себе все методы приложения,
 * выполняющиеся от действий пользователя в UI приложения. Содержит ссылку
 * на текущее сетевое соединение с сервером, имя пользователя полученное после
 * прохождения регистрации.
 */
@Data
public class Controller implements Initializable {

    /**
     * Сетевое соединение.
     */
    private static Connection connection = null;

    /**
     * Имя пользователя.
     */
    private String myName;

    /**
     * Список пользователей для отрисовки в FX модели приложения.
     */
    private ObservableList<UserCell> listUserModel;

    /**
     * Список объектов описывающих статус пользователя.
     */
    private List<UserCell> userList;

    /**
     * Карта панелей для индивидуальных сообщений с другими пользователями,
     * для каждого пользователя создается отдельная панель переписки и
     * помещается в эту карту по имени пользователя.
     */
    private Map<String, GridPane> messagePanes;

    /**
     * Имя панели используемой в текущий момент времени для общения. Определяется
     * тем с каким пользователем идет общение. т.е. это поле содержит имя
     * подсоединившегося к общению другого пользователя.
     */
    private String useNowPane;

    /**
     * Переменная ссылка на сервис работы с файлами.
     */
    private FileWorker fileWorker;

    /**
     * Панель регистрации.
     */
    @FXML
    GridPane regPane;

    /**
     * Поле ввода логина, при регистрации нового пользователя.
     */
    @FXML
    TextField regLogin;

    /**
     * Поле ввода пароля, при регистрации нового пользователя.
     */
    @FXML
    PasswordField regPassword;

    /**
     * Поле повтора ввода пароля, при регистрации нового пользователя.
     */
    @FXML
    PasswordField regPasswordRep;

    /**
     * Поле ввода имени, при регистрации нового пользователя.
     */
    @FXML
    TextField regName;

    /**
     * Метка вывода сообщения панели регистрации.
     */
    @FXML
    Label regMessage;

    /**
     * Панель ScrollPane.
     */
    @FXML
    ScrollPane scrollPane;

    /**
     * Список представление FX в UI пользователя.
     */
    @FXML
    ListView<UserCell> listFx;

    /**
     * Текстовое поле ввода сообщения.
     */
    @FXML
    TextField textField;

    /**
     * Текстовое поле ввода статуса.
     */
    @FXML
    TextField status;

    /**
     * Основная панель.
     */
    @FXML
    HBox chatPane;

    /**
     * Панель для переписки.
     */
    @FXML
    GridPane chat;

    /**
     * Поле для ввода логина при авторизации.
     */
    @FXML
    TextField authLogin;

    /**
     * Поле для ввода пароля при авторизации.
     */
    @FXML
    PasswordField authPassword;

    /**
     * Панель Авторизации.
     */
    @FXML
    GridPane authPane;

    /**
     * Метка на панели авторизации.
     */
    @FXML
    Label authMessage;

    /**
     * Панель смены ника пользователя.
     */
    @FXML
    GridPane setPane;

    /**
     * Поле ввода нового имени пользователя.
     */
    @FXML
    TextField setName;

    /**
     * Метка вывода сообщения на панели смены имени пользователя.
     */
    @FXML
    Label setMessage;

    /**
     * Инициализирует видимость панели Авторизации пользователя, создает карту панелей для подсоединяющихся пользователей.
     * Создает {@link FileWorker FileWorker}.
     *
     * @param location  не используемый параметр от наследуемого интерфейса.
     * @param resources не используемый параметр от наследуемого интерфейса.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeStageToAuth();
        scrollPane.setFitToWidth(true);
        messagePanes = new HashMap();
        messagePanes.put("Общий чат", chat);
        useNowPane = "Общий чат";
        userList = new ArrayList<>();
        fileWorker = new FileWorker(this);
    }

    /**
     * Реагирует на нажатие кнопки послать сообщение. Выводит сообщение на экран
     * пользователя, сохраняет текст сообщения в файл истории пользователя, посылает
     * сообщение в зависимости с какой панели идет сообщение пользователю с которым
     * идет переписка.
     */
    @FXML
    public void SendButton() {
        if (!textField.getText().trim().isEmpty()) {
            String strFromClient = textField.getText();
            Bubble chatMessage = new Bubble(myName, strFromClient, "");
            messagePanes.get(useNowPane).setHalignment(chatMessage, HPos.RIGHT);
            messagePanes.get(useNowPane).setValignment(chatMessage, VPos.BOTTOM);
            Platform.runLater(() -> {
                messagePanes.get(useNowPane).addRow(messagePanes.get(useNowPane).getRowCount(), chatMessage);
                scrollDown();
            });
            Message message = new Message();
            if (useNowPane.equals("Общий чат")) {
                message.setType(Message.MessageType.UMESSAGE);
            } else {
                message.setType(Message.MessageType.PERSONAL);
                message.setToNameU(useNowPane);
            }
            message.setNameU(myName);
            message.setText(strFromClient);
            fileWorker.saveMsgToFile(myName + " " + strFromClient);
            connection.sendMessage(message);
            textField.clear();
            textField.requestFocus();
        }
    }

    /**
     * Реагирует на нажатие кнопки выйти из чата. Посылает на сервер сообщение об отсоединении пользователя.
     * Очищает списки пользователей закрываемого сеанса. Закрывает сетевое соединение и переключает панель с чата на
     * панель авторизации.
     */
    @FXML
    public void sendDisconnect() {
        connection.sendMessage(new Message(Message.MessageType.END));
        userList.clear();
        listFx.refresh();
        connection.closeConnection();
        connection = null;
        myName = " ";
        changeStageToAuth();
    }

    /**
     * Переключает на панель изменения имени пользователя.
     */
    @FXML
    public void changeStageToSet() {
        Platform.runLater(() -> {
            setName.clear();
            setMessage.setVisible(false);
        });
        authPane.setVisible(false);
        regPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(true);
    }

    /**
     * Посылает новый статус пользователя.
     */
    @FXML
    public void sendStatus() {
        if (!status.getText().isBlank()) {
            Message message = new Message();
            message.setType(Message.MessageType.STATUS);
            message.setText(status.getText());
            message.setNameU(myName);
            connection.sendMessage(message);
            status.clear();
        }
    }

    /**
     * Вход в чат, после ввода логана и пароля на панели авторизации. Открывает соединение если его еще нет.
     * Посылает сообщение на сервер с логином и паролем.
     */
    @FXML
    public void enterChat() {
        if (authLogin.getText().isEmpty() || authPassword.getText().isEmpty()) {
            authMessage.setText("Enter login and password");
            authMessage.setVisible(true);
        } else {
            if (connection == null) {
                connection = new Connection(this);
                new Thread(connection).start();
            }
            Message message = new Message(Message.MessageType.AUTH);
            message.setLogin(authLogin.getText());
            message.setPass(authPassword.getText());
            connection.sendMessage(message);
        }
    }

    /**
     * Меняет текущую сцену на сцену авторизации.
     */
    public void changeStageToAuth() {
        Platform.runLater(() -> {
            authLogin.clear();
            authPassword.clear();
        });
        authPane.setVisible(true);
        authMessage.setVisible(false);
        regPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(false);
    }

    /**
     * Меняет текущую сцену на сцену чата.
     */
    public void changeStageToChat() {
        chatPane.setVisible(true);
        authPane.setVisible(false);
        regPane.setVisible(false);
        setPane.setVisible(false);
    }

    /**
     * Меняет текущую сцену на сцену регистрации.
     */
    public void changeStageToReg() {
        Platform.runLater(() -> {
            regLogin.clear();
            regPassword.clear();
            regPasswordRep.clear();
            regName.clear();
        });
        regPane.setVisible(true);
        regMessage.setVisible(false);
        authPane.setVisible(false);
        chatPane.setVisible(false);
        setPane.setVisible(false);
    }

    /**
     * Наполняет список в UI именами пользователей.
     *
     * @param parts масиив со списком имен пользователей в сети.
     */
    public void loadListUsers(String[] parts) {
        listUserModel = null;
        userList.add(new UserCell("Общий чат", "посетителей"));
        for (String part : parts) {
            if (part.equals(myName)) {
                continue;
            }
            userList.add(new UserCell(part, "On line"));
            messagePanes.put(part, new MessagePane(part));
        }
        listUserModel = FXCollections.observableArrayList(userList);
        Platform.runLater(() -> {
            listFx.setItems(listUserModel);
            listFx.setCellFactory(new CellRenderer());
        });
    }

    /**
     * Посылает данные для регистрации нового пользователя. Передварительно
     * проверив наличие и соответствие введенных данных.
     */
    public void register() {
        if (regLogin.getText().isEmpty() || regPassword.getText().isEmpty() ||
                regPasswordRep.getText().isEmpty() || regName.getText().isEmpty()) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Enter login, password and name");
            regMessage.setVisible(true);
            return;
        }
        if (!regPassword.getText().equals(regPasswordRep.getText())) {
            regMessage.setTextFill(Color.RED);
            regMessage.setText("Passwords do not match");
            regMessage.setVisible(true);
            return;
        }
        if (connection == null) {
            connection = new Connection(this);
            new Thread(connection).start();
        }
        Message message = new Message(Message.MessageType.REGUSER);
        message.setNameU(regName.getText());
        message.setLogin(regLogin.getText());
        message.setPass(regPassword.getText());
        connection.sendMessage(message);
    }

    /**
     * Перемещает фокус к последней записи в чате.
     */
    public void scrollDown() {
        final Timeline timeline = new Timeline();
        final KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), 1.0);
        final KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    /**
     * Переключает панель чата к панели выбранного пользователя.
     */
    public void moseClickOnListItem() {
        try {
            String nameUser = listFx.getSelectionModel().getSelectedItem().getName();
            scrollPane.setContent(messagePanes.get(nameUser));
            useNowPane = nameUser;
        } catch (NullPointerException ignored) {
        }
    }

    /**
     * Изменяет старое имя пользователя на новое.
     */
    public void saveAccChanges() {
        if (!setName.getText().isEmpty()) {
            new Thread(() -> {
                try {
                    if (!setName.getText().trim().isEmpty() && !setName.getText().trim().equals(myName)) {
                        for (UserCell userInList : userList) {
                            if (setName.getText().trim().equals(userInList.getName())) {
                                Platform.runLater(() -> {
                                    setMessage.setTextFill(Color.RED);
                                    setMessage.setText("Error change name.");
                                    setMessage.setVisible(true);
                                });
                                return;
                            }
                        }
                        Platform.runLater(() -> Main.getpStage().setTitle("Net-chat:  " + setName.getText()));
                        Message message = new Message(Message.MessageType.CHANGENAME);
                        message.setNameU(myName);
                        message.setToNameU(setName.getText());
                        connection.sendMessage(message);
                        Platform.runLater(() -> {
                            setMessage.setTextFill(Color.GREEN);
                            setMessage.setText("Name change accepted.");
                            setMessage.setVisible(true);
                        });
                    } else {
                        Platform.runLater(() -> {
                            setMessage.setTextFill(Color.RED);
                            setMessage.setText("Enter new name.");
                            setMessage.setVisible(true);
                        });
                    }
                } catch (NullPointerException e) {
                    Platform.runLater(() -> {
                        setMessage.setTextFill(Color.RED);
                        setMessage.setText("Can't change name.");
                        setMessage.setVisible(true);
                    });
                }
            }).start();
        }
    }

    /**
     * Вызывается при неверном логине или пароле.
     */
    public void wrongUser() {
        Platform.runLater(() -> authMessage.setText("Wrong login or password"));
        authMessage.setVisible(true);
    }

    /**
     * Удаляет пользователя из списка в UI.
     * @param userName Имя пользователя.
     */
    public void removeUser(String userName) {
        for (int i = 0; i < userList.size(); i++) {
            UserCell userCell = userList.get(i);
            if (userCell.getName().equals(userName)) {
                userList.remove(i);
            }
        }
        Platform.runLater(() -> {
            ObservableList<UserCell> users = FXCollections.observableArrayList(userList);
            listFx.setItems(users);
            listFx.setCellFactory(new CellRenderer());
        });
    }

    /**
     * Обновляет статусы в списке пользователей UI.
     * @param message Сообщение.
     */
    public void updateUsersListStatus(Message message) {
        for (UserCell u : userList) {
            if (u.getName().equals(message.getNameU())) {
                u.setStatus(message.getText());
            }
        }
        listFx.refresh();
    }

    public Connection getConnection() {
        return connection;
    }
}
