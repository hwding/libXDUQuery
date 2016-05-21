package FooPackage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class SportsClock {
    private final static String HOST = "http://210.27.8.14";
    private final static String LOGIN_SUFFIX = "/login";
    private final static String RUNNER_SUFFIX = "/runner/";
    private final static String ACHIEVEMENTS_SUFFIX = "/runner/achievements.html";
    private String JSESSIONID;

    /*
     * 登录方法须传入 [ 学号 | 密码 ] 作为参数
     */
    void login (String username, String password) throws IOException {
        URL url = new URL(HOST+LOGIN_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setInstanceFollowRedirects(false);
        String OUTPUT_DATA = "username=";
        OUTPUT_DATA += username;
        OUTPUT_DATA += "&password=";
        OUTPUT_DATA += password;
        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
        outputStreamWriter.write(OUTPUT_DATA);
        outputStreamWriter.flush();
        outputStreamWriter.close();
        httpURLConnection.getResponseMessage();
        JSESSIONID = httpURLConnection.getHeaderField("Set-Cookie");
        JSESSIONID = JSESSIONID.substring(JSESSIONID.indexOf("=")+1, JSESSIONID.indexOf(";"));
        httpURLConnection.disconnect();
    }

    /*
     * 通过直接请求内部页面并检查返回值判断是否登录成功(非必须调用, 但建议进行验证)
     */
    boolean checkIsLogin() throws IOException {
        URL url = new URL(HOST+RUNNER_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        httpURLConnection.connect();
        return "OK".equals(httpURLConnection.getResponseMessage());
    }

    /*
     * 此部分用于单独测试SportsClock模块
     */
    public static void main(String[] args) throws IOException {
        SportsClock sportsClock = new SportsClock();
        sportsClock.login("15130188016", "15130188016");
        System.out.println(sportsClock.checkIsLogin());
    }
}
