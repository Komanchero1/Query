import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {

    public static void main(String[] args) {
        // создается экземпляр CloseableHttpClient с помощью HttpClientBuilder
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            //отправляется гет запрос на URL "http://localhost:9880/messages"
            HttpGet getRequest = new HttpGet("http://localhost:9880/messages");
            // get запрос сохраняется в переменную
            var getResponse = httpClient.execute(getRequest);
            //получается код ответа от сервера
            int getStatusCode = getResponse.getStatusLine().getStatusCode();
            //извлекается тело ответа в виде строки
            String getResponseBody = EntityUtils.toString(getResponse.getEntity());
            ////код и тело ответа выводится в консоль
            System.out.println("GET ответ: статус кода - " + getStatusCode + ", тело GET ответа - " + getResponseBody);


            // отправляется POST-запрос на URL "http://localhost:9880/messages"
            HttpPost postRequest = new HttpPost("http://localhost:9880/messages");
            //создается объект StringEntity содержащий тело запроса
            StringEntity postEntity = new StringEntity("тело запроса");
            //передается тело POST запроса объекту postRequest
            postRequest.setEntity(postEntity);
            //устанавливается заголовок Content-Type в text/plain, что информирует сервер о том,
            // что тело запроса содержит простой текст
            postRequest.setHeader("Content-Type", "text/plain");
            //отладочный вывод
            System.out.println("отправка POST запроса на http://localhost:9880/messages");
            //результат POST запроса сохраняется в переменную
            var postResponse = httpClient.execute(postRequest);
            //отладочный вывод
            System.out.println("POST запрос отправлен...");
            ////извлекается код ответа
            int postStatusCode = postResponse.getStatusLine().getStatusCode();
            //тело ответа преобразуется в строку и сохраняется в переменную
            String postResponseBody = EntityUtils.toString(postResponse.getEntity());
            //выводится в консоль код и тело ответа от сервера
            System.out.println("POST ответ: статус кода - " + postStatusCode + ", тело POST ответа - " + postResponseBody);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}