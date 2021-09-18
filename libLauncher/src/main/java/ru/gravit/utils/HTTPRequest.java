package ru.gravit.utils;

import java.io.Reader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import com.google.gson.JsonElement;
import ru.gravit.utils.helper.IOHelper;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonParser;

public final class HTTPRequest
{
    private static final int TIMEOUT = 10000;
    private static final JsonParser parser;
    
    public static int sendCrashreport(final String strurl, final byte[] data) throws IOException {
        final URL url = new URL(strurl);
        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length", Integer.toString(data.length));
        connection.setRequestProperty("Content-Language", "en-US");
        final OutputStream outputStream = connection.getOutputStream();
        outputStream.write(data);
        outputStream.close();
        return connection.getResponseCode();
    }
    
    public static int sendCrashreport(final String strurl, final String data) throws IOException {
        return sendCrashreport(strurl, data.getBytes(IOHelper.UNICODE_CHARSET));
    }
    
    public static JsonElement jsonRequest(final JsonElement request, final URL url) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10000);
        final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), Charset.forName("UTF-8"));
        writer.write(request.toString());
        writer.flush();
        writer.close();
        final int statusCode = connection.getResponseCode();
        InputStreamReader reader;
        if (200 <= statusCode && statusCode < 300) {
            reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
        }
        else {
            reader = new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8);
        }
        try {
            return HTTPRequest.parser.parse(reader);
        }
        catch (Exception e) {
            return null;
        }
    }
    
    private HTTPRequest() {
    }
    
    static {
        parser = new JsonParser();
    }
}
