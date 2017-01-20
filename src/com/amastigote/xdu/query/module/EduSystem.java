/*
        Copyright 2016 @hwding & @TrafalgarZZZ

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.

            GitHub: https://github.com/hwding/libXDUQuery
            E-mail: m@amastigote.com
*/

package com.amastigote.xdu.query.module;

import com.amastigote.xdu.query.conf.QueryType;
import com.amastigote.xdu.query.util.IXDUBase;
import com.amastigote.xdu.query.util.IXDULoginNormal;
import com.amastigote.xdu.query.util.IXDUQueryEduSysType;
import com.sun.istack.internal.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class EduSystem
        implements
        IXDULoginNormal,
        IXDUBase,
        IXDUQueryEduSysType,
        Serializable {
    private final static String SYS_HOST = "http://jwxt.xidian.edu.cn/";
    private final static String LOGIN_HOST = "http://ids.xidian.edu.cn/";
    private final static String LOGIN_SUFFIX = "authserver/login?service=http://jwxt.xidian.edu.cn/caslogin.jsp";
    private final static String SYS_SUFFIX = "caslogin.jsp";
    private final static String GRADE_QUERY_SUFFIX = "gradeLnAllAction.do?oper=qbinfo";
    private static final long serialVersionUID = -5116999038665499130L;

    private String LOGIN_PARAM_lt = "";
    private String LOGIN_PARAM_execution = "";
    private String LOGIN_PARAM__eventId = "";
    private String LOGIN_PARAM_rmShown = "";

    private String LOGIN_JSESSIONID = "";
    private String BIGIP_SERVER_IDS_NEW = "";
    private String ROUTE = "";

    private String SYS_JSESSIONID = "";

    private String ID = "";

    private void preLogin() throws IOException {
        URL url = new URL(SYS_HOST);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.connect();
        List<String> cookies_to_set_a = httpURLConnection.getHeaderFields().get("Set-Cookie");
        for (String e : cookies_to_set_a)
            if (e.contains("JSESSIONID="))
                SYS_JSESSIONID = e.substring(e.indexOf("JSESSIONID=") + 11, e.indexOf(";"));
        httpURLConnection.disconnect();

        httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(true);
        httpURLConnection.connect();

        List<String> cookies_to_set = httpURLConnection.getHeaderFields().get("Set-Cookie");
        for (String e : cookies_to_set) {
            if (e.contains("route="))
                ROUTE = e.substring(6);
            else if (e.contains("JSESSIONID="))
                LOGIN_JSESSIONID = e.substring(11, e.indexOf(";"));
            else if (e.contains("BIGipServeridsnew.xidian.edu.cn="))
                BIGIP_SERVER_IDS_NEW = e.substring(32, e.indexOf(";"));
        }

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String html = "";
        String temp;

        while ((temp = bufferedReader.readLine()) != null) {
            html += temp;
        }

        Document document = Jsoup.parse(html);
        Elements elements = document.select("input[type=hidden]");

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

    @Override
    public boolean checkIsLogin(String username) throws IOException {
        URL url = new URL(SYS_HOST + SYS_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        if (document.select("title").size() == 0) {
            return false;
        } else if (document.select("title").get(0).text().equals("学分制综合教务")) {
            ID = username;
            return true;
        }
        return false;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
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

        httpURLConnection_a.setRequestProperty("Cookie", "route=" + ROUTE + "; org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE=zh_CN; JSESSIONID=" + LOGIN_JSESSIONID + "; BIGipServeridsnew.xidian.edu.cn=" + BIGIP_SERVER_IDS_NEW + ";");

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
        return checkIsLogin(username);
    }

    @Override
    public
    @Nullable
    JSONObject query(QueryType type) throws IOException, JSONException {
        switch (type) {
            case COURSE:
                return lessonsQuery();
            case STUDENT:
                return personalInfoQuery();
            case GRADE:
                return gradesQuery();
        }
        return null;
    }

    private
    @Nullable
    JSONObject lessonsQuery() throws IOException, JSONException {
        if (!checkIsLogin(ID))
            return null;

        URL url = new URL(SYS_HOST + "xkAction.do?actionType=6");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        document = Jsoup.parse(document.toString().replaceAll("&nbsp;", ""));
        Elements lessons = document.select("table[class=titleTop2]");
        Element lessonsElement = lessons.get(1);

        Elements lessonsInfo = lessonsElement.select("tr[onmouseout=this.className='even';]");
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
            JLessonObject.put(CourseKey.ID, lessonDetails.get(1).text());
            JLessonObject.put(CourseKey.NAME, lessonDetails.get(2).text());
            JLessonObject.put(CourseKey.CREDIT, lessonDetails.get(4).text());
            JLessonObject.put(CourseKey.LENGTH, lessonDetails.get(5).text());
            JLessonObject.put(CourseKey.ATTR, lessonDetails.get(6).text());
            JLessonObject.put(CourseKey.EXAM_TYPE, lessonDetails.get(7).text());
            JLessonObject.put(CourseKey.TEACHER, lessonDetails.get(8).text());

            JSONArray JLessonTimeAndPosArray = new JSONArray();
            JSONObject JLessonTimeAndPos = new JSONObject();

            JLessonTimeAndPos.put(CourseKey.WEEK, lessonDetails.get(12).text());
            JLessonTimeAndPos.put(CourseKey.WEEK_DAY, lessonDetails.get(13).text());
            JLessonTimeAndPos.put(CourseKey.SECTION_TIME, lessonDetails.get(14).text());
            JLessonTimeAndPos.put(CourseKey.SECTION_LENGTH, lessonDetails.get(15).text());
            JLessonTimeAndPos.put(CourseKey.CAMPUS, lessonDetails.get(16).text());
            JLessonTimeAndPos.put(CourseKey.BUILDING, lessonDetails.get(17).text());
            JLessonTimeAndPos.put(CourseKey.CLASSROOM, lessonDetails.get(18).text());

            JLessonTimeAndPosArray.put(JLessonTimeAndPos);

            i++;

            //判断是否仍有其他的上课时间或地点，并加入到Array中
            int row_span;

            //row_span缺省值为1
            if ("".equals(lessonInfo.select("td").get(0).attr("rowspan"))) {
                row_span = 1;
            } else {
                row_span = Integer.parseInt(lessonInfo.select("td").get(0).attr("rowspan"));
            }

            //当row_span小于等于1时，以下代码不会执行
            for (int j = 0; j < row_span - 1; j++, i++) {
                Elements EExtraTimeAndPos = lessonsInfo.get(i).select("td");
                JSONObject JExtraLessonTimeAndPos = new JSONObject();

                JExtraLessonTimeAndPos.put(CourseKey.WEEK, EExtraTimeAndPos.get(0).text());
                JExtraLessonTimeAndPos.put(CourseKey.WEEK_DAY, EExtraTimeAndPos.get(1).text());
                JExtraLessonTimeAndPos.put(CourseKey.SECTION_TIME, EExtraTimeAndPos.get(2).text());
                JExtraLessonTimeAndPos.put(CourseKey.SECTION_LENGTH, EExtraTimeAndPos.get(3).text());
                JExtraLessonTimeAndPos.put(CourseKey.CAMPUS, EExtraTimeAndPos.get(4).text());
                JExtraLessonTimeAndPos.put(CourseKey.BUILDING, EExtraTimeAndPos.get(5).text());
                JExtraLessonTimeAndPos.put(CourseKey.CLASSROOM, EExtraTimeAndPos.get(6).text());

                JLessonTimeAndPosArray.put(JExtraLessonTimeAndPos);
            }

            JLessonObject.put(CourseKey.TIME_AND_LOCATION_DERAIL, JLessonTimeAndPosArray);
            jsonArray.put(JLessonObject);
        }

        return new JSONObject().put("ARRAY", jsonArray);
    }

    private
    @Nullable
    JSONObject personalInfoQuery() throws IOException, JSONException {
        if (!checkIsLogin(ID)) {
            return null;
        }

        URL url = new URL(SYS_HOST + "xjInfoAction.do?oper=xjxx");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        document = Jsoup.parse(document.toString().replaceAll("&nbsp;", ""));

        Elements elements1 = document.select("td[width=275]");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(StudentKey.ID, elements1.get(0).text());
        jsonObject.put(StudentKey.NAME, elements1.get(1).text());
        jsonObject.put(StudentKey.GENDER, elements1.get(6).text());
        jsonObject.put(StudentKey.NATION, elements1.get(10).text());
        jsonObject.put(StudentKey.NATIVE_PLACE, elements1.get(11).text());
        jsonObject.put(StudentKey.DEPARTMENT, elements1.get(24).text());
        jsonObject.put(StudentKey.MAJOR, elements1.get(25).text());
        jsonObject.put(StudentKey.CLASS, elements1.get(28).text());

        return jsonObject;
    }

    private
    @Nullable
    JSONObject gradesQuery() throws IOException, JSONException {
        if (!checkIsLogin(ID)) {
            return null;
        }

        URL url = new URL(SYS_HOST + GRADE_QUERY_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + SYS_JSESSIONID);
        httpURLConnection.connect();

        Document document = Jsoup.parse(httpURLConnection.getInputStream(), "gb2312", httpURLConnection.getURL().toString());
        document = Jsoup.parse(document.toString().replaceAll("&nbsp;", ""));

        JSONObject jsonObject = new JSONObject();
        Elements elements_content = document.select("td[class=pageAlign]");
        Elements elements_titles = document.select("b");
        for (int i = 0; i < elements_titles.size(); i++) {
            JSONObject jsonObject_semester = new JSONObject();
            String semester_key = elements_titles.get(i).text().trim();

            Element table_for_this_semester = elements_content.get(i);
            Elements elements_rows = table_for_this_semester.select("td[align=center]");

            for (int j = 0; j < elements_rows.size() / 7; j++) {
                JSONObject jsonObject_course = new JSONObject();
                String course_key = elements_rows.get(j * 7 + 2).text().trim();

                jsonObject_course.put(GradeKey.ID, elements_rows.get(j * 7).text().trim());
                jsonObject_course.put(GradeKey.CREDIT, elements_rows.get(j * 7 + 4).text().trim());
                jsonObject_course.put(GradeKey.ATTR, elements_rows.get(j * 7 + 5).text().trim());
                jsonObject_course.put(GradeKey.GRADE, elements_rows.get(j * 7 + 6).text().trim());
                jsonObject_semester.put(course_key, jsonObject_course);
            }
            jsonObject.put(semester_key, jsonObject_semester);
        }
        return jsonObject;
    }

    //成绩信息的Keys
    public static class GradeKey {
        public static final String ID = "ID";           //课程号
        public static final String CREDIT = "CREDIT";   //学分
        public static final String ATTR = "ATTR";       //课程属性
        public static final String GRADE = "GRADE";   //成绩
    }

    //课表信息的Keys
    public static class StudentKey {
        public static final String ID = "ID";                       //学号
        public static final String NAME = "NAME";                   //姓名
        public static final String GENDER = "GENDER";               //性别
        public static final String NATION = "NATION";               //民族
        public static final String NATIVE_PLACE = "NATIVE_PLACE";   //籍贯
        public static final String DEPARTMENT = "DEPARTMENT";       //系所
        public static final String MAJOR = "MAJOR";                 //专业
        public static final String CLASS = "CLASS";                 //班级
    }

    //课表信息的Keys
    public static class CourseKey {
        public static final String ID = "ID";                           //编号
        public static final String NAME = "NAME";                       //名称
        public static final String CREDIT = "CREDIT";                   //学分
        public static final String LENGTH = "LENGTH";                   //学时
        public static final String ATTR = "ATTR";                       //属性
        public static final String EXAM_TYPE = "EXAM_TYPE";             //考试类型
        public static final String TEACHER = "TEACHER";                 //教师

        public static final String TIME_AND_LOCATION_DERAIL = "TIME_AND_LOCATION_DERAIL";   //时间与地点的细节 用于取出包含以下键的键值对

        public static final String WEEK = "WEEK";                       //周次
        public static final String WEEK_DAY = "WEEK_DAY";               //星期
        public static final String SECTION_TIME = "SECTION_TIME";       //节次
        public static final String SECTION_LENGTH = "SECTION_LENGTH";   //节数
        public static final String CAMPUS = "CAMPUS";                   //校区
        public static final String BUILDING = "BUILDING";               //教学楼
        public static final String CLASSROOM = "CLASSROOM";             //教师

    }
}