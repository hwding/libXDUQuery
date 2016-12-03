package com.amastigote.xdu.query.module;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("FieldCanBeLocal")
public class EduSystem {
    private final String SYS_HOST = "http://jwxt.xidian.edu.cn/";
    private final String LOGIN_HOST = "http://ids.xidian.edu.cn/";
    private final String LOGIN_SUFFIX = "authserver/login?service=http://jwxt.xidian.edu.cn/caslogin.jsp";
    private final String SYS_SUFFIX = "caslogin.jsp";

    private String LOGIN_PARAM_lt;
    private String LOGIN_PARAM_execution;
    private String LOGIN_PARAM__eventId;
    private String LOGIN_PARAM_rmShown;

    private String LOGIN_JSESSIONID;
    private String BIGipServeridsnew;
    private String route;

    private String SYS_JSESSIONID;

    private void preLogin() throws IOException {

        //获取用于进入教务系统的JSESSIONID
        URL url = new URL("http://jwxt.xidian.edu.cn/");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.connect();
        List<String> cookies_to_set_a = httpURLConnection.getHeaderFields().get("Set-Cookie");
        for (String e : cookies_to_set_a)
            if (e.contains("JSESSIONID="))
                SYS_JSESSIONID = e.substring(e.indexOf("JSESSIONID=") + 11, e.indexOf(";"));
        httpURLConnection.disconnect();

        //获取登录时的Cookie数据
        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.connect();

        List<String> cookies_to_set = httpURLConnection.getHeaderFields().get("Set-Cookie");
        for (String e : cookies_to_set) {
            if (e.contains("route="))
                route = e.substring(6);
            else if (e.contains("JSESSIONID="))
                LOGIN_JSESSIONID = e.substring(11, e.indexOf(";"));
            else if (e.contains("BIGipServeridsnew.xidian.edu.cn="))
                BIGipServeridsnew = e.substring(32, e.indexOf(";"));
        }

        //获取登录时的参数
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String html = "";
        String temp;

        while ((temp = bufferedReader.readLine()) != null) {
            html += temp;
        }

        Document document = Jsoup.parse(html);
        Elements elements = document.select("input[type=\"hidden\"]");

        for (Element element : elements) {
            switch (element.attr("name")) {
                case "lt":
                    LOGIN_PARAM_lt = element.attr("value");
                    break;
                case "execution":
                    LOGIN_PARAM_execution = element.attr("value");
                    break;
                case "_eventId":
                    LOGIN_PARAM__eventId = element.attr("value");
                    break;
                case "rmShown":
                    LOGIN_PARAM_rmShown = element.attr("value");
                    break;
            }
        }
    }

    private boolean checkIsLogin() throws IOException {
        URL url = new URL(SYS_HOST + SYS_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        if (document.select("title").size() == 0) {
            return false;
        } else if (document.select("title").get(0).text().equals("学分制综合教务")) {
            return true;
        }
        return false;
    }

    public boolean login(String username, String password) throws IOException {
        preLogin();
        URL url = new URL(LOGIN_HOST + LOGIN_SUFFIX);
        HttpURLConnection httpURLConnection_a = (HttpURLConnection) url.openConnection();
        httpURLConnection_a.setRequestMethod("POST");
        httpURLConnection_a.setUseCaches(false);
        httpURLConnection_a.setInstanceFollowRedirects(false);
        httpURLConnection_a.setDoOutput(true);

        String OUTPUT_DATA = "username=";
        OUTPUT_DATA += username;
        OUTPUT_DATA += "&password=";
        OUTPUT_DATA += password;
        OUTPUT_DATA += "&submit=";
        OUTPUT_DATA += "&lt=" + LOGIN_PARAM_lt;
        OUTPUT_DATA += "&execution=" + LOGIN_PARAM_execution;
        OUTPUT_DATA += "&_eventId=" + LOGIN_PARAM__eventId;
        OUTPUT_DATA += "&rmShown=" + LOGIN_PARAM_rmShown;

        httpURLConnection_a.setRequestProperty("Cookie", "route=" + route + "; org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE=zh_CN; JSESSIONID=" + LOGIN_JSESSIONID + "; BIGipServeridsnew.xidian.edu.cn=" + BIGipServeridsnew + ";");

        httpURLConnection_a.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection_a.getOutputStream(), "UTF-8");
        outputStreamWriter.write(OUTPUT_DATA);
        outputStreamWriter.flush();
        outputStreamWriter.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection_a.getInputStream()));
        String html = "";
        String temp;

        while ((temp = bufferedReader.readLine()) != null) {
            html += temp;
        }

        Document document = Jsoup.parse(html);
        Elements elements = document.select("a");
        if (elements.size() == 0)
            return false;
        String SYS_LOCATION = elements.get(0).attr("href");

        URL sys_location = new URL(SYS_LOCATION);
        HttpURLConnection httpUrlConnection_b = (HttpURLConnection) sys_location.openConnection();
        httpUrlConnection_b.setInstanceFollowRedirects(false);
        httpUrlConnection_b.connect();
        List<String> cookies_to_set = httpUrlConnection_b.getHeaderFields().get("Set-Cookie");
        for (String e : cookies_to_set)
            if (e.contains("JSESSIONID="))
                SYS_JSESSIONID = e.substring(11, e.indexOf(";"));
        httpURLConnection_a.disconnect();
        httpUrlConnection_b.disconnect();
        return checkIsLogin();
    }

    public JSONObject query(EduAdminSysType type) throws IOException, JSONException {
        switch (type) {
            case LESSONS:
                return lessonsQuery();
            case STUDENT:
                return StudentQuery();
            case GRADES:
                return gradesSemesterQuery();
        }
        return null;
    }

    private JSONObject lessonsQuery() throws IOException, JSONException {
        if (!checkIsLogin())
            return null;

        URL url = new URL("http://jwxt.xidian.edu.cn/xkAction.do?actionType=6");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        document = Jsoup.parse(document.toString().replaceAll("&nbsp;", ""));
        Elements lessons = document.select("table[class=\"titleTop2\"]");
        Element lessonsElement = lessons.get(1);

        Elements lessonsInfo = lessonsElement.select("tr[onmouseout=\"this.className='even';\"]");

        int lessons_quantity = lessonsInfo.size();

        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < lessons_quantity; ) {
            Element lessonInfo = lessonsInfo.get(i);

            Elements lessonDetails = lessonInfo.select("td");


            //跳过没有具体上课时间的课程
            if (lessonDetails.get(14).text().equals("")) {
                i++;
                continue;
            }

            JSONObject JLessonObject = new JSONObject();
            JLessonObject.put("课程号", lessonDetails.get(1).text());
            JLessonObject.put("课程名", lessonDetails.get(2).text());
            JLessonObject.put("学分", lessonDetails.get(4).text());
            JLessonObject.put("总学时", lessonDetails.get(5).text());
            JLessonObject.put("课程属性", lessonDetails.get(6).text());
            JLessonObject.put("考试类型", lessonDetails.get(7).text());
            JLessonObject.put("教师", lessonDetails.get(8).text());

            JSONArray JLessonTimeAndPosArray = new JSONArray();

            JSONObject JLessonTimeAndPos = new JSONObject();

            JLessonTimeAndPos.put("周次", lessonDetails.get(12).text());
            JLessonTimeAndPos.put("星期", lessonDetails.get(13).text());
            JLessonTimeAndPos.put("节次", lessonDetails.get(14).text());
            JLessonTimeAndPos.put("节数", lessonDetails.get(15).text());
            JLessonTimeAndPos.put("校区", lessonDetails.get(16).text());
            JLessonTimeAndPos.put("教学楼", lessonDetails.get(17).text());
            JLessonTimeAndPos.put("教室", lessonDetails.get(18).text());

            JLessonTimeAndPosArray.put(JLessonTimeAndPos);

            i++;

            //判断是否仍有其他的上课时间或地点，并加入到Array中
            int rowspan;

            //rowspan缺省值为1
            if (lessonInfo.select("td").get(0).attr("rowspan").equals("")) {
                rowspan = 1;
            } else {
                rowspan = Integer.parseInt(lessonInfo.select("td").get(0).attr("rowspan"));
            }

            //当rowspan小于等于1时，以下代码不会执行
            for (int j = 0; j < rowspan - 1; j++, i++) {
                Elements EExtraTimeAndPos = lessonsInfo.get(i).select("td");

                JSONObject JExtraLessonTimeAndPos = new JSONObject();

                JExtraLessonTimeAndPos.put("周次", EExtraTimeAndPos.get(0).text());
                JExtraLessonTimeAndPos.put("星期", EExtraTimeAndPos.get(1).text());
                JExtraLessonTimeAndPos.put("节次", EExtraTimeAndPos.get(2).text());
                JExtraLessonTimeAndPos.put("节数", EExtraTimeAndPos.get(3).text());
                JExtraLessonTimeAndPos.put("校区", EExtraTimeAndPos.get(4).text());
                JExtraLessonTimeAndPos.put("教学楼", EExtraTimeAndPos.get(5).text());
                JExtraLessonTimeAndPos.put("教室", EExtraTimeAndPos.get(6).text());

                JLessonTimeAndPosArray.put(JExtraLessonTimeAndPos);
            }

            JLessonObject.put("时间地点", JLessonTimeAndPosArray);

            jsonArray.put(JLessonObject);
        }

        return new JSONObject().put("array", jsonArray);
    }

    private JSONObject StudentQuery() throws IOException, JSONException {
        if (!checkIsLogin()) {
            return null;
        }

        URL url = new URL("http://jwxt.xidian.edu.cn/xjInfoAction.do?oper=xjxx");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        document = Jsoup.parse(document.toString().replaceAll("&nbsp;", ""));

        Elements elements1 = document.select("td[width=\"275\"]");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("edusys_id", elements1.get(0).text());    //学号
        jsonObject.put("edusys_name", elements1.get(1).text());    //姓名
        jsonObject.put("edusys_gender", elements1.get(6).text());    //性别
        jsonObject.put("edusys_nation", elements1.get(10).text());  //民族
        jsonObject.put("edusys_native_place", elements1.get(11).text());  //籍贯
        jsonObject.put("edusys_department", elements1.get(24).text());  //系所
        jsonObject.put("edusys_major", elements1.get(25).text());  //专业
        jsonObject.put("edusys_class", elements1.get(28).text());  //班级

        return jsonObject;
    }

    private JSONObject gradesSemesterQuery() throws IOException, JSONException {
        if (!checkIsLogin()) {
            return null;
        }

        URL url = new URL("http://jwxt.xidian.edu.cn/gradeLnAllAction.do?type=ln&oper=qb");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        document = Jsoup.parse(document.toString().replaceAll("&nbsp;", ""));
        Elements elements = document.select("a[target=\"lnqbIfra\"]");

        JSONArray durationArray = new JSONArray();
        JSONArray jsonArray = new JSONArray();
        for (Element elem : elements) {
            durationArray.put(elem.text());
            jsonArray.put(SYS_HOST + elem.attr("href"));
        }

        return new JSONObject().put("array",jsonArray).put("duration",durationArray);

    }

    public List<JSONObject> gradesQuery(String queryUrl, int index) throws IOException, JSONException {
        if (!checkIsLogin()) {
            return null;
        }

        URL url = new URL(queryUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        document = Jsoup.parse(document.toString().replaceAll("&nbsp;", ""));

        Elements semestersGrades = document.select("table[class=\"titleTop2\"]");

        List<JSONObject> arrayList = new ArrayList<>();

        Elements gradesDetails = semestersGrades.get(index - 1).select("tr").select("table[id=\"user\"]").select("tbody").select("tr");

        for (Element EgradesDetail : gradesDetails) {
            JSONObject JGradeObject = new JSONObject();

            Elements items = EgradesDetail.select("td");

            JGradeObject.put("课程号", items.get(0).text());
            JGradeObject.put("课程名", items.get(2).text());
            JGradeObject.put("学分", items.get(4).text());
            JGradeObject.put("课程属性", items.get(5).text());
            JGradeObject.put("成绩", items.get(6).text());

            arrayList.add(JGradeObject);
        }
        return arrayList;
    }

    public enum EduAdminSysType {
        LESSONS,
        STUDENT,
        GRADES
    }
}