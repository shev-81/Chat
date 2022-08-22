# Сhat - Сетевой чат

#### Проект на клиент - серверной архитектуре.
Сервер запускается и подключает клиентов, пересылает сообщения от клиентов, отвечает за авторизацию пользователей, использует Data Base SQLite, для хранения данных пользователей. Взаимодействие с Data Base реализованно с использованием JDBC.  Клиентская часть написана с использованием Java FX + CSS. Транспортная система обмена сообщениями, основана на обмене объектами между клиентом и сервером, используется сериализация и десериализация объектов сообщений. (Java IO).
 
#### В проекте реализованны 2 модуля Client & Server.
- <b>Server</b> - В модуле реализованы сервис Авторизации <b>(AuthService)</b> пользователей работающий с базой данных SQLite и сервис чтения и обработки входящих сообщений <b>(ReaderMessages)</b>.  Cервис Авторизации поддерживает соединение с базой данных и осуществляет основные CRUD операции с записями пользователей за исключением Delete. Сервис чтения входящих сообщений обрабатывает сообщения в зависимости от их типа. 
- <b>Client</b> - В модуле реализованны: <b>Транспортная система</b> описанная в классе "Connection" - работает с сетевым подключением, получает и отправляет сообщения. <b>Сервис для работы с файлами (FileWorker)</b> - подгружающий и сохраняющий историю пользователей. <b>Сервис для обработки входящих сообщений (ReaderMessages)</b>. А так же <b>User Interface</b>, состоящий из нескольких панелей: панель для регистрации нового пользователя, панель авторизации, панель измнения своего статуса, набор панелей индивидуальной переписки. 

## Модуль Server

Запускаясь сервер создает пул потоков, необходимых для выполнения слушателей клиента, а так же запускает сервис авторизации, который работает с базой пользователей. Далее создает Server Socket на порту "8189" и заходит в бесконечный цикл ожидая подключения клиентов.
        
        while (true) {
             socket = serverSocket.accept();
             service.execute(() -> {
                   try {
                        new ClientHandler(this, socket);
                   } 
                   catch (IOException e) {
                        LOGGER.throwing(Level.FATAL, e);
                        ...
                   }
             });
        }
             
Метод <b>accept()</b> является блокирующим, поэтому для нового подключения запускается слушатель <b>ClientHandler</b> в выделенном ему из пула потоков потке. Слушатель получает ссылку на сервер и сокет. Обобщенная схема работы сервера представлена на рисунке ниже.

![Общая схема работы сервера](https://user-images.githubusercontent.com/89448563/185929114-ffa7d5d1-4548-481d-8546-17d0a878da57.png)

#### ClientHandler

Как и упоминалось ранее слушатель работает в выделенном потоке и сохраняет в себе ссылку на сокет соединения. При создании, создает два потока ввода вывода работающих с сокетом, запускает сервис чтения сообщений. После начинают последовательно выполнятся два цикла. Первый - цикл аутентификации пользователя выйдя из которого он попадет в цикл чтения сообщений. Подробнее процесс ниже ан схеме.

![ClientHandler](https://user-images.githubusercontent.com/89448563/185951235-06b8cb1b-3ba3-46c4-a44a-d9754682de14.png)




