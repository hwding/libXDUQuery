package FooPackage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class ECard {
    private final static String HOST = "http://ecard.xidian.edu.cn";
    private final static String PRE_LOGIN_SUFFIX = "/login.jsp";
    private final static String CARD_USER_INFO_SUFFIX = "/cardUserManager.do?method=searchCardUserInfo";
    private final static String LOGIN_SUFFIX = "/cardUserManager.do?method=checkLogin";
    private final static String CAPTCHA_SUFFIX = "/authImage";
    private String JSESSIONID;

    private ECard() throws IOException {
        URL url = new URL(HOST + PRE_LOGIN_SUFFIX);
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

    private String login() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Captcha image generated, please input: ");
        String CHAPTCHA = scanner.nextLine();
        System.out.println("Student ID (also card number): ");
        String ID = scanner.nextLine();
        System.out.println("Password for eCard (6 numbers): ");
        String PASSWORD = scanner.nextLine();
        URL url = new URL(HOST + LOGIN_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String OUTPUT_DATA = "flag=1&code=";
        OUTPUT_DATA+=ID;
        OUTPUT_DATA+="&pwd=";
        OUTPUT_DATA+=PASSWORD;
        OUTPUT_DATA+="&cardCheckCode=";
        OUTPUT_DATA+=CHAPTCHA;
        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
        outputStreamWriter.write(OUTPUT_DATA);
        outputStreamWriter.flush();
        outputStreamWriter.close();
        httpURLConnection.getResponseMessage();
        httpURLConnection.disconnect();
        return ID;
    }

    private boolean check_is_login(String string) throws IOException {
        URL url = new URL(HOST + CARD_USER_INFO_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        urlConnection.connect();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String BUFFER;
        while ((BUFFER = bufferedReader.readLine()) != null){
            if (BUFFER.contains(string.substring(0,7)))
                return true;
        }
        return false;
    }

    public static void main(String[] args) throws IOException {
        ECard eCard = new ECard();
        eCard.getCaptcha();
        System.out.println(eCard.check_is_login(eCard.login()));
    }
}
