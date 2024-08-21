package ServerHahdler;

import java.util.Map;

public class Request {
    private final String method;//��� �������� ������ �������
    private final String path;//��� �������� ���� �������
    private final byte[] body;//��� �������� ���� �������
    private final Map<String, String> queryParams;//��� �������� ���������� �������

    //����������� � �����������
    public Request(String method, String path, Map<String, String> queryParams, byte[] body) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.body = body;
    }

    //���������� ����� �������
    public String getMethod() {
        return method;
    }

    //���������� ���� �������
    public String getPath() {
        return path;
    }

    //���������� ��������� �������
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    //���������� ��������� ������� �� �����
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    //���������� ���� �������
    public byte[] getBody() {
        return body;
    }
}