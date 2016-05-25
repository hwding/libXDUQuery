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

    void login(String username, String password) throws IOException {
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
        PhyEwsAuth = PhyEwsAuth.substring(PhyEwsAuth.indexOf("=")+1, PhyEwsAuth.indexOf(";"));
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
        return stringArrayList;
    }

    public static void main(String[] args) throws IOException {
        PhysicalExperiment physicalExperiment = new PhysicalExperiment();
        physicalExperiment.login("15130188016", "");
        physicalExperiment.queryAchievements();
    }
}
