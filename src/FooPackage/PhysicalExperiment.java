package FooPackage;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PhysicalExperiment {
    private final static String HOST = "http://wlsy.xidian.edu.cn/phyEws";
    private final static String SELECTED_EXPERIMENT_SUFFIX = "/student/select.aspx";
    private final static String LOGIN_SUFFIX = "/default.aspx";
    private String PhyEwsAuth;
    private String ID = "";

    private static String preLogin() throws IOException {
        URL url = new URL(HOST+LOGIN_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(urlConnection.getInputStream()));
        String temp;
        String htmlPage = "";
        while ((temp = bufferedReader.readLine()) != null)
            htmlPage+=temp;
        return htmlPage;

    }

    private static ArrayList<String> getPageAttributes(String htmlPage){
        Document document = Jsoup.parse(htmlPage);
        Elements elements_VIEWSTATEGENERATOR = document
                .select("input[type=\"hidden\"][name=\"__VIEWSTATEGENERATOR\"]");
        Elements elements_VIEWSTATE = document
                .select("input[type=\"hidden\"][name=\"__VIEWSTATE\"]");
        Elements elements_EVENTVALIDATION = document
                .select("input[type=\"hidden\"][name=\"__EVENTVALIDATION\"]");
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.add(elements_VIEWSTATE.get(0).attr("value"));
        stringArrayList.add(elements_VIEWSTATEGENERATOR.get(0).attr("value"));
        stringArrayList.add(elements_EVENTVALIDATION.get(0).attr("value"));
        return stringArrayList;
    }

    /*
     * 登录方法须传入 [ 学号 | 密码 ] 作为参数
     * 返回是否登录成功
     */
    boolean login(String username, String password) throws IOException {
        ArrayList<String> pageAttributes = getPageAttributes(preLogin());
        URL url = new URL(HOST+LOGIN_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setInstanceFollowRedirects(false);
        String OUTPUT_DATA = "login1%24StuLoginID=";
        OUTPUT_DATA += username;
        OUTPUT_DATA += "&login1%24StuPassword=";
        OUTPUT_DATA += password;
        OUTPUT_DATA += "&__VIEWSTATE=";
        OUTPUT_DATA += URLEncoder.encode(pageAttributes.get(0), "UTF-8");
        OUTPUT_DATA += "&__VIEWSTATEGENERATOR=";
        OUTPUT_DATA += URLEncoder.encode(pageAttributes.get(1), "UTF-8");
        OUTPUT_DATA += "&__EVENTVALIDATION=";
        OUTPUT_DATA += URLEncoder.encode(pageAttributes.get(2), "UTF-8");
        OUTPUT_DATA += "&login1%24UserRole=Student&login1%24btnLogin.x=0&login1%24btnLogin.y=0";
        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
        outputStreamWriter.write(OUTPUT_DATA);
        outputStreamWriter.flush();
        outputStreamWriter.close();
        httpURLConnection.getResponseMessage();
        PhyEwsAuth = httpURLConnection.getHeaderField("Set-Cookie");
        if (PhyEwsAuth != null) {
            PhyEwsAuth = PhyEwsAuth.substring(PhyEwsAuth.indexOf("=") + 1, PhyEwsAuth.indexOf(";"));
            ID = username;
            return true;
        }
        return false;
    }

    ArrayList<String> queryAchievements() throws IOException {
        ArrayList<String> stringArrayList = new ArrayList<>();
        URL url = new URL(HOST+SELECTED_EXPERIMENT_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "PhyEws_StuName=;PhyEws_StuType=1;.PhyEwsAuth="+PhyEwsAuth);
        BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(urlConnection.getInputStream(), "GBK"));
        String temp;
        String htmlPage = "";
        while ((temp = bufferedReader.readLine()) != null)
            htmlPage += temp;
        Document document = Jsoup.parse(htmlPage);
        Elements elements = document.select("td[class=\"forumRow\"]");
        stringArrayList.addAll(elements.stream().map(Element::text).collect(Collectors.toList()));

        /*
         * 返回字符串数组(stringArrayList)说明:
         *      - 从数组第0项开始, 每十项是一条完整的已选实验信息
         *      - 此十项依次代表 [ 序号 | 实验项目 | 实验周次 | 实验时间 | 实验日期 | 上课教室 | 讲义出处 | 实验成绩 | 归一成绩 | 备注 ]
         *      - 因此, 数组长度为(10n), n即代表已选实验的总数
         *
         *      - 注意: 如果结果中没有记录将返回空数组而非null!
         */
        return stringArrayList;
    }

    /*
     * 用于返回当前会话的学号
     *
     * 注意: 当且仅当登录成功时, 其返回为当前会话的学号, 否则返回空内容
     */
    String getID(){
        return ID;
    }

    /*
     * Demo
     * 此部分用于单独测试PhysicalExperiment模块
     */
    public static void main(String[] args) throws IOException {
        PhysicalExperiment physicalExperiment = new PhysicalExperiment();
        if(physicalExperiment.login("此处传入用户名", "此处传入密码"))
            physicalExperiment.queryAchievements();
    }
}
