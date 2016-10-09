package com.amastigote.xdu.query.module;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.amastigote.xdu.query.util.XDUQueryModule;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;

public class PhysicalExperiment extends XDUQueryModule{
    private final static String HOST = "http://wlsy.xidian.edu.cn/phyEws";
    private final static String SELECTED_EXPERIMENT_SUFFIX = "/student/select.aspx";
    private final static String STUDENT_SUFFIX = "/student/student.aspx";
    private final static String LOGIN_SUFFIX = "/default.aspx";
    private String PhyEwsAuth = "";
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

    /**
     * 登录方法须传入 [ 学号 | 密码 ] 作为参数
     * 返回是否登录成功
     *
     * @return 是否登录成功
     */
    public boolean login(String... params) throws IOException {
        String username = params[0];
        String password = params[1];

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
            httpURLConnection.disconnect();
            return checkIsLogin(username);
        }
        httpURLConnection.disconnect();
        return false;
    }

    /*
     * 通过页面是否存在学生信息判断是否登陆成功
     */
    public boolean checkIsLogin(String username) throws IOException {
        Document document = getPage(STUDENT_SUFFIX);
        Elements elements = document.select("span[id=\"Stu\"]");

        if (elements.size() > 0) {
            ID = username;
            return true;
        }
        else {
            ID = "";
            return false;
        }
    }

    private Document getPage(String suffix) throws IOException{
        URL url = new URL(HOST+suffix);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "PhyEws_StuName=;PhyEws_StuType=1;.PhyEwsAuth=" + PhyEwsAuth);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), "GBK"));
        String temp;
        String htmlPage = "";
        while ((temp = bufferedReader.readLine()) != null)
            htmlPage += temp;
        bufferedReader.close();
        return Jsoup.parse(htmlPage);
    }

    public ArrayList<String> query(String... params) throws IOException {
        Document document = getPage(SELECTED_EXPERIMENT_SUFFIX);
        ArrayList<String> stringArrayList = new ArrayList<>();
        Elements elements = document.select("td[class=\"forumRow\"]");
        for (Element element : elements) stringArrayList.add(element.text());

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
    public String getID(){
        return ID;
    }
}
