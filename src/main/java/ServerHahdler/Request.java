package ServerHahdler;

import java.util.Map;

public class Request {
    private final String method;//для хранения метода запроса
    private final String path;//для хранения пути запроса
    private final byte[] body;//для хранения тела запроса
    private final Map<String, String> queryParams;//для хранения параметров запроса

    //конструктор с параметрами
    public Request(String method, String path, Map<String, String> queryParams, byte[] body) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.body = body;
    }

    //возвращает метод запроса
    public String getMethod() {
        return method;
    }

    //возвращает путь запроса
    public String getPath() {
        return path;
    }

    //возвращает параметры запроса
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    //возвращает параметры запроса по имени
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    //возвращает тело запроса
    public byte[] getBody() {
        return body;
    }
}