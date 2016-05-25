package FooPackage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SportsClock {
    private final static String HOST = "http://210.27.8.14";
    private final static String LOGIN_SUFFIX = "/login";
    private final static String RUNNER_SUFFIX = "/runner/";
    private final static String ACHIEVEMENTS_SUFFIX = "/runner/achievements.html";
    private String JSESSIONID;
    private String ID = "";

    /*
     * 登录方法须传入 [ 学号 | 密码 ] 作为参数
     * 返回输入的用户名用以直接传参数给checkIsLogin()方法
     */
    String login (String username, String password) throws IOException {
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
        return username;
    }

    /*
     * 通过直接请求内部页面并检查返回值判断是否登录成功(非必须调用, 但建议进行验证)
     */
    boolean checkIsLogin(String username) throws IOException {
        URL url = new URL(HOST+RUNNER_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        httpURLConnection.connect();
        if ("OK".equals(httpURLConnection.getResponseMessage())){
            ID = username;
            return true;
        }
        return false;
    }

    ArrayList<String> queryAchievements() throws IOException {
        URL url = new URL(HOST+ACHIEVEMENTS_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String temp;
        String htmlPage = "";
        while ((temp = bufferedReader.readLine()) != null)
            htmlPage += temp;

        /*
         * 解析页面的内容
         */
        Document document = Jsoup.parse(htmlPage);
        Elements elements = document.select("tr[class=\"\"]");
        Elements tds = elements.select("td");

        /*
         * 返回字符串数组(stringArrayList)说明:
         *      - 从数组第0项开始, 每五项是一条完整的打卡记录
         *      - 此五项依次代表 [ 列表编号 | 打卡日期 | 打卡时段 | 里程 | 平均速度 ]
         *      - 因此, 数组长度为(5n), n即代表打卡记录的总条数
         *      - 打卡记录的顺序按打卡日期从早到晚排列
         *
         *      - 注意: 如果结果中没有记录将返回空数组而非null!
         */
        return tds.stream().filter(each -> !each.text().equals(""))
                .map(Element::text).collect(Collectors.toCollection(ArrayList::new));
    }

    /*
     * 用于返回当前会话的学号
     *
     * 注意: 当且仅当checkIsLogin()方法被调用且确认已登录成功(checkIsLogin()返回true)时, 其返回为当前会话的学号, 否则返回空内容
     */
    String getID(){
        return ID;
    }

    /*
     * Demo
     * 此部分用于单独测试SportsClock模块
     */
    public static void main(String[] args) throws IOException {
        SportsClock sportsClock = new SportsClock();
        if (sportsClock.checkIsLogin(sportsClock.login("此处传入用户名", "此处传入密码"))) {
            sportsClock.queryAchievements();
            System.out.println(sportsClock.getID());
        }
    }
}
