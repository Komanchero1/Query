package ServerHahdler;

import java.util.Map;

public class Request {
    private final String method;//äëÿ õðàíåíèÿ ìåòîäà çàïðîñà
    private final String path;//äëÿ õðàíåíèÿ ïóòè çàïðîñà
    private final byte[] body;//äëÿ õðàíåíèÿ òåëà çàïðîñà
    private final Map<String, String> queryParams;//äëÿ õðàíåíèÿ ïàðàìåòðîâ çàïðîñà

    //êîíñòðóêòîð ñ ïàðàìåòðàìè
    public Request(String method, String path, Map<String, String> queryParams, byte[] body) {
        this.method = method;
        this.path = path;
        this.queryParams = queryParams;
        this.body = body;
    }

    //âîçâðàùàåò ìåòîä çàïðîñà
    public String getMethod() {
        return method;
    }

    //âîçâðàùàåò ïóòü çàïðîñà
    public String getPath() {
        return path;
    }

    //âîçâðàùàåò ïàðàìåòðû çàïðîñà
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    //âîçâðàùàåò ïàðàìåòðû çàïðîñà ïî èìåíè
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    //âîçâðàùàåò òåëî çàïðîñà
    public byte[] getBody() {
        return body;
    }
}
