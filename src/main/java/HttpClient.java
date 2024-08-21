import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {

    public static void main(String[] args) {
        //��������� ��������� CloseableHttpClient � ������� HttpClientBuilder
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            //������������ ��� ������ �� URL "http://localhost:9880/messages"
            HttpGet getRequest = new HttpGet("http://localhost:9880/messages");
            // get ������ ����������� � ����������
            var getResponse = httpClient.execute(getRequest);
            //���������� ��� ������ �� �������
            int getStatusCode = getResponse.getStatusLine().getStatusCode();
            //����������� ���� ������ � ���� ������
            String getResponseBody = EntityUtils.toString(getResponse.getEntity());
            //��� � ���� ������ ��������� � �������
            System.out.println("GET Response: Status Code - " + getStatusCode + ", Body - " + getResponseBody);

            // ������������ POST-������ �� URL "http://localhost:9880/messages"
            HttpPost postRequest = new HttpPost("http://localhost:9880/messages");
            //��������� ������ StringEntity ���������� ���� �������
            StringEntity postEntity = new StringEntity("This is the request body");
            //���������� ���� ���� ������� ������� postRequest
            postRequest.setEntity(postEntity);
            //��������� POST ������� ����������� � ����������
            var postResponse = httpClient.execute(postRequest);
            //����������� ��� ������
            int postStatusCode = postResponse.getStatusLine().getStatusCode();
            //���� ������ ������������� � ������ � ����������� � ����������
            String postResponseBody = EntityUtils.toString(postResponse.getEntity());
            //��������� � ������� ��� � ���� ������ �� �������
            System.out.println("POST Response: Status Code - " + postStatusCode + ", Body - " + postResponseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}