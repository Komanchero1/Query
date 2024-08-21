package ServerHahdler;

import ServerHahdler.Request;
import ServerHahdler.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {
    //����� ��������� �������� HTTP ������
    // �������� �� ���� �����, ����, ��������� ������� � ���� �������
    public static Request parseRequest(BufferedReader in) throws IOException {
        //������ ������ ������� � ��������� � ����������
        String requestLine = in.readLine();

        //���� ������ ������
        if (requestLine == null) {
            //����������� ��������� �� ������
            throw new IOException("Invalid request line");
        }

        //��������� ������ ��������� � �������� ����������� ������
        String[] parts = requestLine.split(" ");

        //����������� ��� ������ �������  �� 3� ��������
        //�����, ���� � ������ HTTP
        if (parts.length != 3) {
            //���� ��� ����������� ��������� �� ������
            throw new IOException("Invalid request line");
        }

        //������������� ������ ������� ������� ���������� method
        String method = parts[0];
        //���������� �������� ���� �� ���������� �������
        //����������� ������ ������� ������� ��������� ����������� ?
        String[] pathParts = parts[1].split("\\?");
        //������������� ������ ������� �� ������� � ���������� path
        String path = pathParts[0];
        //��������� ��������� ��� �������� ���������� �������
        Map<String, String> queryParams = new HashMap<>();

        //���� � ������� pathParts ������ ������ ��������
        //������� � �������� ���� ���������
        if (pathParts.length > 1) {
            //����������� ������ ������� �� ������� � ���������� queryString
            //��� ����� ��������� �������
            String queryString = pathParts[1];
            //������ ������ ������� � ��������� ��������� � ����������
            queryParams = parseQueryString(queryString);
        }

        //����������� ���� �������
        byte[] body = Server.readBody(in);

        //������������ ������ ServerHahdler.Request � ������������ �� ������� �������
        return new Request(method, path, queryParams, body);
    }

    //����� ��������� ������ ������� �� ���� ���� ��������
    private static Map<String, String> parseQueryString(String queryString) {
        //��������� �������� ��� �������� ��� ����+��������
        Map<String, String> queryParams = new HashMap<>();
        //��������� ������ ������� ��������� ����������� & � ���������
        // ���������� ��������� � ������
        String[] pairs = queryString.split("&");
        //���������� ������
        for (String pair : pairs) {
            //������ ������ ������� pairs ��������� �� ��������� ��������� ����������� =
            //��� ����� ������� ���� ���� + ��������
            String[] keyValue = pair.split("=");

            //���� ����� ������� ����� 2
            if (keyValue.length == 2) {
                //�� ������ ������� ������� ����� ���� , ��������� ��� � ���������� key
                String key = keyValue[0];
                //������ ������� ����� ��������� ,��������� ��� � ���������� value
                String value = keyValue[1];
                //����������� ���������� ���� ���� + �������� � ��������� queryParams
                queryParams.put(key, value);
            }

        }

        //������������ ����������� ���������
        return queryParams;
    }
}