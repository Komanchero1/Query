package ServerHahdler;

import ServerHahdler.Request;
import ServerHahdler.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {
    //метод разбирает входящий HTTP запрос
    // извлекая из него метод, путь, параметры запроса и тело запроса
    public static Request parseRequest(BufferedReader in) throws IOException {
        //читаем строку запроса и сохраняем в переменную
        String requestLine = in.readLine();

        //если строка пустая
        if (requestLine == null) {
            //выбрасываем сообщение об ошибке
            throw new IOException("Invalid request line");
        }

        //разбиваем строку используя в качестве разделителя пробел
        String[] parts = requestLine.split(" ");

        //проверяется что массив состоит  из 3х подстрок
        //метод, путь и версия HTTP
        if (parts.length != 3) {
            //если нет выбрасываем сообщение об ошибке
            throw new IOException("Invalid request line");
        }

        //присваивается первый элемент массива переменной method
        String method = parts[0];
        //отделяется основной путь от параметров запроса
        //разбивается второй элемент массива используя разделитель ?
        String[] pathParts = parts[1].split("\\?");
        //присваивается первый элемент из массива в переменную path
        String path = pathParts[0];
        //создается коллекция для хранения параметров запроса
        Map<String, String> queryParams = new HashMap<>();

        //если в массиве pathParts больше одного элемента
        //знначит в запроссе есть параметры
        if (pathParts.length > 1) {
            //сохраняется второй элемент из массива в переменную queryString
            //это будут параметры запроса
            String queryString = pathParts[1];
            //парсим строку запроса и результат сохраняем в переменную
            queryParams = parseQueryString(queryString);
        }

        //считывается тело запроса
        byte[] body = Server.readBody(in);

        //возвращается объект ServerHahdler.Request с извлеченными из запроса данными
        return new Request(method, path, queryParams, body);
    }

    //метод разбивает строку запроса на пары ключ значение
    private static Map<String, String> parseQueryString(String queryString) {
        //создается колекция для хранения пар ключ+значение
        Map<String, String> queryParams = new HashMap<>();
        //разбиваем строку запроса используя разделитель & и сохраняем
        // полученные подстроки в массив
        String[] pairs = queryString.split("&");
        //перебираем массив
        for (String pair : pairs) {
            //каждую строку массива pairs разбиваем на подстроки используя разделитель =
            //тем самым получая пару ключ + значение
            String[] keyValue = pair.split("=");

            //если длина массива равна 2
            if (keyValue.length == 2) {
                //то первый элемент массива будет ключ , сохраняем его в переменную key
                String key = keyValue[0];
                //второй элемент будет значением ,сохраняем его в переменную value
                String value = keyValue[1];
                //сохраняется полученная пара ключ + значение в коллекцию queryParams
                queryParams.put(key, value);
            }

        }

        //возвращается заполненная коллекция
        return queryParams;
    }
}