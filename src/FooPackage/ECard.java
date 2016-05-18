package FooPackage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ECard {
    private final static String HOST = "http://ecard.xidian.edu.cn";
    private final static String LOGIN_SUFFIX = "/login.jsp";
    private final static String CAPTCHA_SUFFIX = "/authImage";
    private String JSESSIONID;

    private ECard() throws IOException {
        URL url = new URL(HOST + LOGIN_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        JSESSIONID = urlConnection.getHeaderField("Set-Cookie");
        JSESSIONID = JSESSIONID.substring(JSESSIONID.indexOf("=")+1, JSESSIONID.indexOf(";"));
    }

    private void getCaptcha() throws IOException {
        URL url = new URL(HOST + CAPTCHA_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
        byte[] bytes = new byte[1024];
        FileOutputStream fileOutputStream = new FileOutputStream("CACHE.jpeg");
        int LENGTH;
        while ((LENGTH = inputStream.read(bytes)) != -1){
            fileOutputStream.write(bytes, 0, LENGTH);
        }
        inputStream.close();
        fileOutputStream.close();
    }

    public static void main(String[] args) throws IOException {
        ECard eCard = new ECard();
        eCard.getCaptcha();
    }
}
