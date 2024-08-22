package ServerHahdler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Server {

    // Создается коллекция, где будут храниться значения в виде ключа - метод запроса GET или POST,
    // значение - другая коллекция, где ключ - это путь запроса /messages, значение - обработчик запроса Handler
    private static final Map<String, Map<String, Handler>> handlers = new HashMap<>();
    // Создается маяк для синхронизации доступа к handlers
    private static final ReentrantLock handlersLock = new ReentrantLock();


    public static void main(String[] args) {
        Server server = new Server();//создается объект Server
        //назначается обработчик для GET запроса по пути "/messages"
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            // возвращается значение параметра по его имени и сохраняется в переменную
            String lastParam = request.getQueryParam("last");
            //если параметр найден
            if (lastParam != null) {
                // Обрабатывается запрос с параметром last и отправка сообщения клиенту
                sendResponse(responseStream, "GET /messages?last=" + lastParam + " response");
            } else {
                //если параметра last нет отправляется сообщение клиенту без учета этого параметра
                sendResponse(responseStream, "GET /messages response");
            }
        });

        //назначается обработчик для "POST" запроса по пути "/messages"
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            //преобразовываем массив полученных байтов в строку
            String requestBody = new String(request.getBody());
            //отладочный вывод
            System.out.println("получен POST запрос: " + requestBody);
            //отправляется сообщение клиенту
            sendResponse(responseStream, "POST /messages received: " + requestBody);
            //отладочный вывод
            System.out.println("ответ на POST запро отправлен");
        });

        server.listen(9880);//стартует объект сервер на порту 9880
    }


    // Метод добавляет обработчик запросов в handlers в качестве аргументов используется метод запроса,
    // путь запроса, обработчик запроса
    public void addHandler(String method, String path, Handler handler) {
        handlersLock.lock();// Блокируем доступ к handlers
        try {
            // Проверяется, существует ли вложенная коллекция для указанного запроса,
            // если нет, добавляется
            handlers.computeIfAbsent(method, k -> new HashMap<>())
                    // Добавляется обработчик запросов во вложенную карту для указанного пути
                    .put(path, handler);
        } finally {
            handlersLock.unlock();// Разблокируем доступ к handlers
        }
    }


    // Метод запускает сервер, который прослушивает входящие соединения на указанном порту.
    // Входящие запросы обрабатываются в пуле потоков.
    public static void listen(int port) {
        // Создается сокет
        try (var serverSocket = new ServerSocket(port)) {
            // Создается пул из 64 потоков
            var threadPool = Executors.newFixedThreadPool(64);
            while (true) {
                var socket = serverSocket.accept();// Прослушиваются соединения
                // Создается новый поток в пуле потоков и передается ему лямбда-функция,
                // которая вызывает метод handleClient в качестве аргумента socket
                threadPool.execute(() -> handleClient(socket));
            }
        } catch (IOException e) {// Перехватываем любые ошибки ввода-вывода
            e.printStackTrace();
        }
    }


    // Метод для обработки входящих запросов от клиента в качестве аргумента передается socket
    private static void handleClient(Socket socket) {
        //Отладочный вывод
        System.out.println("запрос принят " + socket.getInetAddress());
        // создается поток ввода InputStream для чтения байтов данных, поступающих от клиента через сокет
        //reader - оборачиваем поток in  в BufferedReader чтобы считывать данные построчно
        //out - оздается поток вывода OutputStream для отправки данных клиенту через сокет
        try (var in = socket.getInputStream();
             var reader = new BufferedReader(new InputStreamReader(in));
             var out = new BufferedOutputStream(socket.getOutputStream())) {

            // Отладочный вывод
            System.out.println("запрос прарсится...");
            // Считывается запрос и парситься
            Request request = RequestParser.parseRequest(reader, in);
            // Отладочный вывод
            System.out.println("результат парсинга: " + request.getMethod() + " " + request.getPath());
            // Получается обработчик запросов для указанного метода и пути запроса из поля `handlers`
            Handler handler = getHandler(request.getMethod(), request.getPath());
            //если обработчик найден
            if (handler != null) {
                handler.handle(request, out);//производится обработка запроса
            } else {
                sendNotFound(out);//отправляется сообщение об ошибке
            }
        } catch (IOException e) { //перехватываются любые ошибки ввода или вывода
            e.printStackTrace();
        } finally {
            try {
                socket.close(); // Закрывается сокет после обработки запроса
            } catch (IOException e) { //перехватываются любые ошибки не коректного закрытия сокета
                e.printStackTrace();
            }
        }
    }


    // метод  для получения обработчика, соответствующего данному HTTP-методу и пути
    private static Handler getHandler(String method, String path) {
        //текущий поток получает эксклюзивный доступ к колекции handlers
        handlersLock.lock();
        try {
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
            return handlers.getOrDefault(method, new HashMap<>()).get(path);
        } finally {
            //снимается блокировка
            handlersLock.unlock();
        }
    }


    //  Метод отправляет клиенту ответ с кодом состояния 404 (Not Found),
    //  указывая, что запрошенный ресурс не найден.
    private static void sendNotFound(BufferedOutputStream out) throws IOException {
        // Записывается сообщение в выходной поток
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());// Преобразовываем сообщение в массив байтов
        out.flush();// отправка сообщения и очистка буфера обмена
    }


    // Отправляется ответ клиенту, записывая заголовок HTTP-ответа и содержимое ответа в выходной поток
    public static void sendResponse(BufferedOutputStream out, String content) throws IOException {
        // Записывается сообщение  в выходной поток
        out.write(("HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + content.length() + "\r\n" +
                "Connection: close\r\n" +
                "\r\n" + content).getBytes());// Преобразовываем сообщение в массив байтов
        out.flush();// отправка сообщения и очистка буфера обмена
    }


    //метод возвращает массив байтов в качестве параметров принимает входной поток in  и
    // contentLength количество байтов, которые нужно прочитать из потока
    static byte[] readBody(InputStream in, int contentLength) throws IOException {
        //создаем массив байтов размер которого равен количеству байтов, которые нужно прочитать из потока
        byte[] body = new byte[contentLength];
        in.read(body);//считываются данные из потока и записываем их в массив
        return body;//возвращается массив
    }


}