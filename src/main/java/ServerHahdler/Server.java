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
    // ��������� ���������, ��� ����� ��������� �������� � ���� ����� - ����� ������� GET ��� POST,
    // �������� - ������ ���������, ��� ���� - ��� ���� ������� /messages, �������� - ���������� ������� Handler
    private static final Map<String, Map<String, Handler>> handlers = new HashMap<>();

    // ��������� ���� ��� ������������� ������� � handlers
    private static final ReentrantLock handlersLock = new ReentrantLock();

    // ����� ��������� ���������� �������� � handlers � �������� ���������� ������������ ����� �������,
    // ���� �������, ���������� �������
    public void addHandler(String method, String path, Handler handler) {
        handlersLock.lock(); // ��������� ������ � handlers
        try {
            // �����������, ���������� �� ��������� ��������� ��� ���������� �������, ���� ���, �����������
            handlers.computeIfAbsent(method, k -> new HashMap<>())
                    // ����������� ���������� �������� �� ��������� ����� ��� ���������� ����
                    .put(path, handler);
        } finally {
            handlersLock.unlock(); // ������������ ������ � handlers
        }
    }

    // ����� ��������� ������, ������� ������������ �������� ���������� �� ��������� �����.
    // �������� ������� �������������� � ���� �������.
    public static void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) { // ��������� �����
            // ��������� ��� �� 64 �������
            final var threadPool = Executors.newFixedThreadPool(64);
            while (true) {
                final var socket = serverSocket.accept();// �������������� ����������
                // ��������� ����� ����� �� ���� ������� � ���������� ��� ������-�������,
                // ������� �������� ����� handleClient � �������� ��������� socket
                threadPool.execute(() -> handleClient(socket));
            }

        } catch (IOException e) { // ������������� ����� ������ �����-������
            e.printStackTrace();
        }
    }

    // ����� ��� ��������� �������� �������� �� ������� � �������� ��������� ���������� socket
    private static void handleClient(Socket socket) {
        // ��������� BufferedReader � BufferedOutputStream ��� ������ � ������ ������ � �����
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {
            Request request = RequestParser.parseRequest(in);// ����������� ������ � ���������
            // ���������� ���������� �������� ��� ���������� ������ � ���� ������� �� ���� `handlers`
            Handler handler = getHandler(request.getMethod(), request.getPath());

            if (handler != null) {//���� ���������� �� null
                handler.handle(request, out);//������������ ��������� �������
                sendResponse(out, "Response from server");//������������ ����� �������
            } else {
                sendNotFound(out); // ���� ���������� �� ������, ���������� ������ 404
                sendResponse(out, "Not Found");
            }

        } catch (IOException e) { // ������������� ����� ������ �����-������
            e.printStackTrace();
        } finally {
            try {
                socket.close(); // ����������� ����� ����� ��������� �������
            } catch (IOException e) { // ������������� ����� ������ �����-������
                e.printStackTrace();
            }
        }
    }

    // �����  ��� ��������� �����������, ���������������� ������� HTTP-������ � ����
    private static Handler getHandler(String method, String path) {
        //���������� ���� �� ���������� ������� URL ����� ���������� �������
        // �������  ��������� ������� ?
        int questionMarkIndex = path.indexOf('?');

        //���� ������ ������
        if (questionMarkIndex != -1) {
            // �� ����� ��������� �� ������ �� ������� ? ��� ����� ����
            path = path.substring(0, questionMarkIndex);
        }

        //��������� ����� getOrDefault �������� �� ���������  handlers ����������
        // ��� ������� � �����
        return handlers.getOrDefault(method, new HashMap<>())
                .get(path);
    }

    //  ����� ���������� ������� ����� � ����� ��������� 404 (Not Found),
    //  ��������, ��� ����������� ������ �� ������.
    private static void sendNotFound(BufferedOutputStream out) throws IOException {
        out.write(( // ������������ ��������� ���� � �������� �����
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());// ��������������� ��������� � ������ ������
        out.flush();// ������� ������ ������
    }

    // ����� ��������� ���� ������� � ���������� ��� � ���� ������� ������
    static byte[] readBody(BufferedReader in) throws IOException {
        try {
            // ������ ���� ����� �� BufferedReader � ������
            var lines = in.lines().collect(Collectors.toList());

            // ������� ������ ������ ������, ������� �������� ��������� �� ����
            int bodyStartIndex = -1;
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).isEmpty()) {
                    bodyStartIndex = i + 1;
                    break;
                }
            }

            // ���� ���� ������� �� �������, ������� ������ ������ ������
            if (bodyStartIndex == -1 || bodyStartIndex >= lines.size()) {
                return new byte[0];
            }

            // ��������� ������ ���� ������� � ���� ������
            String body = String.join("\n", lines.subList(bodyStartIndex, lines.size()));

            // ���������� ���� ������� ��� ������ ������
            return body.getBytes();

        } finally {
            in.close(); // ��������� BufferedReader
        }
    }

    public static void main(String[] args) {
        Server server = new Server();//��������� ������ Server

        //����������� ���������� ��� GET ������� �� ���� "/messages"
        server.addHandler("GET", "/messages", (request, responseStream) -> {
            // ������������ �������� ��������� �� ��� ����� � ����������� � ����������
            String lastParam = request.getQueryParam("last");

            //���� �������� ������
            if (lastParam != null) {
                //���� �������� last ������ �� �� ����������������� � ����� �����
                int last = Integer.parseInt(lastParam);
                // ��������� ������� � ���������� last � �������� ��������� �������
                Server.sendResponse(responseStream, "GET /messages?last=" + last + " response");
            } else {
                //���� ��������� last ��� ������������ ��������� ������� ��� ����� ����� ���������
                Server.sendResponse(responseStream, "GET /messages response");
            }

        });

        //����������� ���������� ��� "POST" ������� �� ���� "/messages"
        server.addHandler("POST", "/messages", (request, responseStream) -> {
            //������������ ��������� �������
            Server.sendResponse(responseStream, "POST /messages response");
        });
        server.listen(9880);//�������� ������ ������ �� ����� 9880
    }

    // ������������ ����� �������, ��������� ��������� HTTP-������ � ���������� ������ � �������� �����
    private static void sendResponse(BufferedOutputStream out, String content) throws IOException {
        out.write(( // ������������ ��������� ���� � �������� �����
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/plain\r\n" +
                        "Content-Length: " + content.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());// ��������������� ��������� � ������ ������
        out.flush();// ������� ������ ������
    }
}