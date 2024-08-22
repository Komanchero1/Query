package ServerHahdler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {


    //метод разбирает входящий HTTP запрос извлекая из него метод, путь, параметры запроса и тело запроса
    public static Request parseRequest(BufferedReader reader, InputStream in) throws IOException {
        //считывается первая строка запроса в которой предпологается должны быть метод ,путь и версия HTTP
        String Line = reader.readLine();
        //если в запросе нет строк
        if (Line == null) {
            throw new IOException("некоррекктная строка запроса");//выбрасывается исключение и сообщение об ошибке
        }
        //разбирается строка запроса на части и сохраняется в массив в качестве разделителя используется пробел
        String[] parts = Line.split(" ");
        //если длина получившегося массива не равна 3
        if (parts.length != 3) {
            throw new IOException("некоррекктная строка запроса");//выбрасывается исключение и сообщение об ошибке
        }
        //присваивается первый элемент массива переменной method
        String method = parts[0];
        //отделяется основной путь от параметров запроса
        //разбивается второй элемент массива используя разделитель \\?
        String[] pathParts = parts[1].split("\\?");
        //и первая часть сохраняется как путь в переменную path
        String path = pathParts[0];
        //создается коллекция для хранения параметров запроса
        Map<String, String> queryParams = pathParts.length > 1 ? parseQueryString(pathParts[1]) : new HashMap<>();
        //переменная будет использоваться для хранения длины содержимого тела запроса,
        // указанной в заголовке Content-Length.
        int contentLength = 0;
        //переменная будет использоваться для хранения строк заголовков HTTP-запроса
        String headerLine;
        //цикл выполняется до тех пор, пока очередная строка заголовка не окажется пустой
        // проверка пустая строка или нет происходит вызовом метода isEmpty
        while (!(headerLine = reader.readLine()).isEmpty()) {
            //проверяется, начинается ли текущая строка заголовка с Content-Length:
            if (headerLine.startsWith("Content-Length:")) {
                //Если заголовок Content-Length: найден, строка разбивается на две части
                // с использованием символа : в качестве разделителя,вторая часть (индекс [1]) содержит
                // значение длины контента, которое сначала обрезается от лишних пробелов с помощью trim(),
                // а затем конвертируется в целое число (int) с помощью Integer.parseInt(),
                // полученное значение присваивается переменной contentLength
                contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
            }
        }
        //считывается тело запроса
        byte[] body = Server.readBody(in, contentLength);
        //возвращается объект Request с извлеченными из запроса данными
        return new Request(method, path, queryParams, body);
    }


    //метод разбивает строку запроса на пары ключ значение
    private static Map<String, String> parseQueryString(String queryString) {
        //создается колекция для хранения пар ключ+значение
        Map<String, String> queryParams = new HashMap<>();
        //разбиваем строку запроса используя разделитель & и сохраняем полученные подстроки в массив
        String[] pairs = queryString.split("&");
        //перебирается полученный массив
        for (String pair : pairs) {
            //каждую строку массива pairs разбиваем на подстроки используя разделитель =
            //тем самым получая пару ключ + значение
            String[] keyValue = pair.split("=");
            //если длина массива равна 2
            if (keyValue.length == 2) {
                //то сохраняем элементы массива в коллекцию как пару ключ значение
                queryParams.put(keyValue[0], keyValue[1]);
            }
        }
        //возвращается заполненная коллекция
        return queryParams;
    }
}