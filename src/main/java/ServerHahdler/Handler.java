package ServerHahdler;


import java.io.BufferedOutputStream;
import java.io.IOException;

interface Handler {
    void handle(Request request, BufferedOutputStream responseStream) throws IOException;
}