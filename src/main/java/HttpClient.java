import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClient {
    //метод выполняет GET запрос и обрабатывает ответ
    public static void main(String[] args) {
        //создается CloseableHttpClient с помощью  HttpClientBuilder
        //блок  try-with-resources закроет клиент автоматически
        // после выполнения кода
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            //создается объект представляющий HTTP GET запрос
            // с указанным URL адресом
            HttpGet request = new HttpGet("http://localhost:9880");
            //выполняется GET запрос и результат сохраняется в переменной
            var response = httpClient.execute(request);
            //получается код HTTP ответа
            int statusCode = response.getStatusLine().getStatusCode();

            //если код ответа от сервера равен 200
            if (statusCode == 200) {
                //то тело ответа преобразуется в строку
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("Response from server: " + responseBody);
            } else {
                //если нет выводится сообщение об ошибке
                System.out.println("Request failed with status code: " + statusCode);
            }

        } catch (IOException e) {
            e.printStackTrace();//выводится трассировка ошибки
        }
    }
}