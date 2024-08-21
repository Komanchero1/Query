import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {

    public static void main(String[] args) {
        //создаетс€ экземпл€р CloseableHttpClient с помощью HttpClientBuilder
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            //отправл€етс€ гет запрос на URL "http://localhost:9880/messages"
            HttpGet getRequest = new HttpGet("http://localhost:9880/messages");
            // get запрос сохран€етс€ в переменную
            var getResponse = httpClient.execute(getRequest);
            //получаетс€ код ответа от сервера
            int getStatusCode = getResponse.getStatusLine().getStatusCode();
            //извлекаетс€ тело ответа в виде строки
            String getResponseBody = EntityUtils.toString(getResponse.getEntity());
            //код и тело ответа выводитс€ в консоль
            System.out.println("GET Response: Status Code - " + getStatusCode + ", Body - " + getResponseBody);

            // отправл€етс€ POST-запрос на URL "http://localhost:9880/messages"
            HttpPost postRequest = new HttpPost("http://localhost:9880/messages");
            //создаетс€ объект StringEntity содержащий тело запроса
            StringEntity postEntity = new StringEntity("This is the request body");
            //передаетс€ тело пост запроса объекту postRequest
            postRequest.setEntity(postEntity);
            //результат POST запроса сохран€етс€ в переменную
            var postResponse = httpClient.execute(postRequest);
            //извлекаетс€ код ответа
            int postStatusCode = postResponse.getStatusLine().getStatusCode();
            //тело ответа преобразуетс€ в строку и сохран€етс€ в переменную
            String postResponseBody = EntityUtils.toString(postResponse.getEntity());
            //выводитс€ в консоль код и тело ответа от сервера
            System.out.println("POST Response: Status Code - " + postStatusCode + ", Body - " + postResponseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}