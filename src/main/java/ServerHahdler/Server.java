package ServerHahdler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class Server {
    // Создается коллекция, где будут храниться значения в виде ключа - метод запроса GET или POST,
    // значение - другая коллекция, где ключ - это путь запроса /messages, значение - обработчик запроса Handler
    private static final Map<String, Map<String, Handler>> handlers = new HashMap<>();

    // Создается маяк для синхронизации доступа к handlers
    private static final ReentrantLock handlersLock = new ReentrantLock();

    // Метод добавляет обработчик запросов в handlers в качестве аргументов используется метод запроса,
    // путь запроса, обработчик запроса
    public void addHandler(String method, String path, Handler handler) {
        handlersLock.lock(); // Блокируем доступ к handlers
        try {
            // Проверяется, существует ли вложенная коллекция для указанного запроса, если нет, добавляется
            handlers.computeIfAbsent(method, k -> new HashMap<>())
                    // Добавляется обработчик запросов во вложенную карту для указанного пути
                    .put(path, handler);
        } finally {
            handlersLock.unlock(); // Разблокируем доступ к handlers
        }
    }

    // Метод запускает сервер, который прослушивает входящие соединения на указанном порту.
    // Входящие запросы обрабатываются в пуле потоков.
    public static void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) { // Создается сокет
            // Создается пул из 64 потоков
            final var threadPool = Executors.newFixedThreadPool(64);
            while (true) {
                final var socket = serverSocket.accept();// Прослушиваются соединения
                // Создается новый поток из пула потоков и передается ему лямбда-функция,
                // которая вызывает метод handleClient в качестве аргумента socket
                threadPool.execute(() -> handleClient(socket));
            }

        } catch (IOException e) { // Перехватываем любые ошибки ввода-вывода
            e.printStackTrace();
        }
    }

    // Метод для обработки входящих запросов от клиента в качестве аргумента передается socket
    private static void handleClient(Socket socket) {
        // Создаются BufferedReader и BufferedOutputStream для чтения и записи данных в сокет
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            Request request = RequestParser.parseRequest(in);// Считывается запрос и парситься
            // Получается обработчик запросов для указанного метода и пути запроса из поля `handlers`
            Handler handler = getHandler(request.getMethod(), request.getPath());

            if (handler != null) {//если обработчик не null
                handler.handle(request, out);//производится обработка запроса
                sendResponse(out, "Response from server");//отправляется ответ клиенту
            } else {
                sendNotFound(out); // Если обработчик не найден, отправляем ошибку 404
                sendResponse(out, "Not Found");
            }

        } catch (IOException e) { // Перехватываем любые ошибки ввода-вывода
            e.printStackTrace();
        } finally {
            try {
                socket.close(); // Закрывается сокет после обработки запроса
            } catch (IOException e) { // Перехватываем любые ошибки ввода-вывода
                e.printStackTrace();
            }
        }
    }

    // метод  для получения обработчика, соответствующего данному HTTP-методу и пути
    private static Handler getHandler(String method, String path) {
        //отделяется путь от параметров запроса URL путем нахождения индекса
        // первого  вхождения символа ?
        int questionMarkIndex = path.indexOf('?');

        //если символ найден
        if (questionMarkIndex != -1) {
            // то берем подстроку от начала до индекса ? это будет путь
            path = path.substring(0, questionMarkIndex);
        }

        //используя метод getOrDefault получаем из коллекции  handlers обработчик
        // для методов и путей
        return handlers.getOrDefault(method, new HashMap<>())
                .get(path);
    }

    //  Метод отправляет клиенту ответ с кодом состояния 404 (Not Found),
    //  указывая, что запрошенный ресурс не найден.
    private static void sendNotFound(BufferedOutputStream out) throws IOException {
        out.write(( // Записывается сообщение ниже в выходной поток
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());// Преобразовываем сообщение в массив байтов
        out.flush();// Очистка буфера обмена
    }

    // Метод считывает тело запроса и возвращает его в виде массива байтов
    static byte[] readBody(BufferedReader in) throws IOException {
        try {
            // Чтение всех строк из BufferedReader в список
            var lines = in.lines().collect(Collectors.toList());

            // Находим первую пустую строку, которая отделяет заголовки от тела
            int bodyStartIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).isEmpty()) {
                    bodyStartIndex = i + 1;
                    break;
                }
            }

            // Если тело запроса не найдено, вернуть пустой массив байтов
            if (bodyStartIndex == -1 || bodyStartIndex >= lines.size()) {
                return new byte[0];
            }

            // Соединяем строки тела запроса в одну строку
            String body = String.join("\n", lines.subList(bodyStartIndex, lines.size()));

            // Возвращаем тело запроса как массив байтов
            return body.getBytes();

        } finally {
            in.close(); // Закрываем BufferedReader
        }
    }

    public static void main(String[] args) {
        Server server = new Server();//создается объект Server

        //назначается обработчик для GET запроса по пути "/messages"
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            // возвращается значение параметра по его имени и сохраняется в переменную
            String lastParam = request.getQueryParam("last");

            //если параметр найден
            if (lastParam != null) {
                //если параметр last найден то он преобразовывается в целое число
                int last = Integer.parseInt(lastParam);
                // Обработка запроса с параметром last и отправка сообщения клиенту
                Server.sendResponse(responseStream, "GET /messages?last=" + last + " response");
            } else {
                //если параметра last нет отправляется сообщение клиенту без учета этого параметра
                Server.sendResponse(responseStream, "GET /messages response");
            }

        });

        //назначается обработчик для "POST" запроса по пути "/messages"
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            //отправляется сообщение клиенту
            Server.sendResponse(responseStream, "POST /messages response");
        });
        server.listen(9880);//стартует объект сервер на порту 9880
    }

    // Отправляется ответ клиенту, записывая заголовок HTTP-ответа и содержимое ответа в выходной поток
    private static void sendResponse(BufferedOutputStream out, String content) throws IOException {
        out.write(( // Записывается сообщение ниже в выходной поток
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + content.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());// Преобразовываем сообщение в массив байтов
        out.flush();// Очистка буфера обмена
    }
}