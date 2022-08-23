# Сhat - Сетевой чат в режиме реального времени (Java IO + Java FX)

## 0. Предисловие 
Проект написан, как пример клиент-серверноого взаимодействия на Java IO. Сервер запускается и подключает клиентов, пересылает сообщения от клиентов, отвечает за авторизацию пользователей, использует `Data Base SQLite`, для хранения данных пользователей. Взаимодействие с Data Base реализованно с использованием `JDBC`.  Клиентская часть написана с использованием `Java FX + CSS`. Транспортная система обмена сообщениями, основана на обмене объектами между клиентом и сервером, используется сериализация и десериализация объектов сообщений. `(Java IO)`.
 
>  <b>В проекте реализованны 2 модуля Client & Server.</b>
- <b>Server</b> - В модуле реализованы Слушатель клиента `ClientHandler`, сервис Авторизации `AuthService` пользователей работающий с базой данных `SQLite` и сервис чтения и обработки входящих сообщений `ReaderMessages`.  Cервис Авторизации поддерживает соединение с базой данных и осуществляет основные CRUD операции с записями пользователей за исключением Delete. Сервис чтения входящих сообщений `ReaderMesages` обрабатывает сообщения в зависимости от их типа. 
- <b>Client</b> - В модуле реализованны: `Транспортная система` описанная в классе `Connection` - работает с сетевым подключением, получает и отправляет сообщения.  Сервис для работы с файлами `FileWorker` - подгружающий и сохраняющий историю пользователей. Сервис для обработки входящих сообщений `ReaderMessages`. А так же `User Interface`, состоящий из нескольких панелей: панель для регистрации нового пользователя, панель авторизации, панель измнения своего статуса, набор панелей индивидуальной переписки. 

> <b>Техническая часть</b>
 - IDE: IntelliJ IDEA 2021.3.3
 - Версия JDK: 1.8.0_121 + 16 на стороне клиента.
 - SQLite
> <b>Используемые технологии:</b>
 - Java FX
 - Java IO
 - CSS
 - JDBC
 - Мавен 3.5



## 1. Модуль Server

Запускаясь сервер создает пул потоков, необходимых для выполнения слушателей клиента, а так же запускает сервис авторизации, который работает с базой пользователей. Далее создает `Server Socket на порту "8189"` и заходит в бесконечный цикл ожидая подключения клиентов.
        
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
             
Метод <b>`accept()`</b> является блокирующим, поэтому для нового подключения запускается слушатель `ClientHandler` в выделенном ему из пула потоков потке. Слушатель получает ссылку на сервер и сокет. Обобщенная схема работы сервера представлена на рисунке ниже.

![Общая схема работы сервера](https://user-images.githubusercontent.com/89448563/185929114-ffa7d5d1-4548-481d-8546-17d0a878da57.png)

#### 1.1 ClientHandler

Как и упоминалось ранее слушатель работает в выделенном потоке и сохраняет в себе ссылку на сокет соединения. При создании, создает два потока ввода вывода работающих с сокетом, запускает сервис чтения сообщений. После начинают последовательно выполнятся два цикла. `Первый - цикл аутентификации` пользователя, выйдя из которого он попадет в `цикл чтения сообщений`. Подробнее процесс ниже на схеме.

![ClientHandler](https://user-images.githubusercontent.com/89448563/185952902-f5428227-595f-4365-945a-4966f2604c5e.png)

#### 1.2 ReaderMessages

`Сервис чтения`, обрабатывает сообщения получаемые методом <b>`read()`</b> метод проверяет с каким типом сообщение и передает его на обработку соотвтетсвующему методу. Большая часть типов является определителем того, что сообщение будет являться служебной командой. 

        public boolean read(Message message, ClientHandler clientHandler){
            switch (message.getType()) {
                case AUTH: return auth(message, clientHandler);
                case REGUSER: return regUser(message, clientHandler);
                case END: end(clientHandler); break;
                case CHANGENAME: changeName(message, clientHandler); break;
                case PERSONAL: personal(message, clientHandler); break;
                case UMESSAGE: uMessage (message); break;
                case STATUS: status(message); break;
            }
            return true;
        }

> В зависимости от типа сообщения конструкция <b>swich</b> перенаправит сообщение в необходимый метод обработки.
    
    AUTH, REGUSER, END, CHANGENAME, PERSONAL, UMESSAGE, STATUS.

Как видно из контекста наименований `AUTH` говорит, что пришел запрос на аутентификацию. `REGUSER` используется при регистрации нового пользователя.
`END` говорит о закрытии пользователем своего сеанса связи (при условии корректного прекращения). `CHANGENAME` говорит о смене имени пользователя. `PERSONAL` тип используется, когда сообщение является личным и адресованно другому пользователю.  `UMESSAGE` используется что бы определить сообщение для общей рассылке всем пользователям сервера. `STATUS` используется при смене статуса пользователя. 
 
 ## 2. Модуль Client
 
Клиентская часть реализованна с использованием `Java FX` и разбавлена поключенными стилями `CSS`. `User Interface` описывается в подключаемом файле `sample.fxml`.
Транспортная часть похожа по реализации на серверную, поэтому детальное рассмотрение уделим общей логике работы клиента, в частности сервису сохранения истории общения пользователей и `GUI - Graphical User interface`. 

#### 2.1 FileWorker сервис сохранения истории

Каждое сообщение, которое печатает пользователь и отправляет по сети или получает сохраняется в истории переписки это пользователя. 
Переписка пользователя хранится на стороне клиента, а при каждом входе в сеть подгружает ему последние 10 сообщений из сохраненной истории. 


#### 2.2 Graphical User interface (GUI) 

> <b>Панели авторизации и регистрации.</b>

При старте приложения запускается форма ввода логина и пароля, картинка ниже.   

![Окошко авторизации](https://user-images.githubusercontent.com/89448563/185994124-e0a6ea6a-f6a0-4854-811b-e7232f4c4bb0.png)

Если аккаунта нет, можно перейти по ссылке "Don't hаve an accaunt?" и произойдет смена панелей. Панель ввода логина и пароля станет неактивной и вместо нее активный фокус заберет панель регистрации нового пользователя, картинка ниже.

![Форма регистрации пользователя](https://user-images.githubusercontent.com/89448563/186001232-9566d7f6-0140-49be-b4d0-f0f4103a4ca6.png)

Обе формы принимающие пользовательский ввод, проверяют корректность заполнения предлагаемых полей. К примеру если в форме авторизации не ввести логин или пароль, то соединение для передачи информации не будет открыто! Потому что, обработчик выдаст ошибку и подсветит текстовую метку красным цветом, картинка ниже.

![Окошко авторизации с ошибкой](https://user-images.githubusercontent.com/89448563/185995897-0a6808e8-2993-439c-9206-260f67a4f159.png)

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
> <b>Панель чата.</b>

Если все впорядке и данные введены, будет созданно сетевое подключение и сообщение с типом `AUTH`. Затем запрос авторизации будет отправлен на сервер.
Пользователь пройдет процесс аутентификации, при котором клиент и сервер несколько раз обменяются служебными сообщениями. Клиент будет проверен по его логину и паролю в Базе данных и при успешной проверке Сервис `AuthService` вернет Имя `NickName` пользователя и сервер проведет массовую рассылку всем клиентам сообщения о присоединении к чату нового пользователя. Все активные клиенты при получении сообщения о присоединении пользователя обновят свои списки активных клиентов и добавят для этого пользователя панель для личной переписки. А сам пользователь перейдет на панель чата и сможет вести переписку со всеми пользователями в общей группе или индивидуально.    

![Sprite-0003](https://user-images.githubusercontent.com/89448563/186018263-44a60233-4685-4b45-956f-8ff8449acc69.gif)

Слева вверху поле `status` с двумя иконками `Шестеренка` и `Выход`. По умолчанию, каждый подключающийся пользователь имеет статус - `On line` но его легко поменять введя в это поле новый статус и подтвердив ввод клавишей `Enter`. Если кликнуть на `Шестеренку` будет активированна панель для смены Имени пользователя, а панель чата станет не видимой, после смены имени можно вернуться на панель чата без потерь информации. 

![Смена имени пользователя](https://user-images.githubusercontent.com/89448563/186020568-1cfb5aa4-3fe0-44ac-bfb9-fee3df6be288.png)

По клику на иконку `Выход` панель чата пеерстенет быть активной все данные по текущему сеансу будут обнулены (такущий список активных пользователей и история переписки в закрепленных за ними панелях), а панель Авторизации в прижении будет активированна. Текущее сетевое соединение будет закрыто.
Спасибо если дочитали до конца.


