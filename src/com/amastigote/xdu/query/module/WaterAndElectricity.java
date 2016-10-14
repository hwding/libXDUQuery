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

import com.amastigote.xdu.query.util.XDUQueryBase;
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
import java.net.URLConnection;
import java.util.ArrayList;

public class WaterAndElectricity implements XDUQueryBase {
    private final static String HOST = "http://10.168.55.50:8088";
    private final static String PRE_LOGIN_SUFFIX = "/searchWap/Login.aspx";
    private final static String LOGIN_SUFFIX = "/ajaxpro/SearchWap_Login,App_Web_fghipt60.ashx";
    private final static String USEINFO_SUFFIX = "/SearchWap/webFrm/useInfo.aspx";
    private final static String PAYINFO_SUFFIX = "/SearchWap/webFrm/pay.aspx";
    private final static String METINFO_SUFFIX = "/SearchWap/webFrm/met.aspx";

    private static String VIEWSTATE = "";
    private String ID = "";
    private String ASP_dot_NET_SessionId = "";

    private void preLogin() throws IOException {
        URL url = new URL(HOST+PRE_LOGIN_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        String tmp = urlConnection.getHeaderField("Set-Cookie");

        ASP_dot_NET_SessionId = tmp.substring(
                tmp.indexOf("=") + 1,
                tmp.indexOf(";"));
    }


    public boolean login(String... params) throws IOException {
        if (params.length != 2)
            throw new IllegalArgumentException("Bad parameter, check document for help");
        preLogin();

        String username = params[0];
        String password = params[1];

        URL url = new URL(HOST+LOGIN_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Cookie", "ASP.NET_SessionId=" + ASP_dot_NET_SessionId);
        httpURLConnection.setRequestProperty("AjaxPro-Method", "getLoginInput");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("webName", username);
        jsonObject.put("webPass", password);
        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
        outputStreamWriter.write(jsonObject.toString());
        outputStreamWriter.flush();
        outputStreamWriter.close();
        httpURLConnection.getResponseCode();

        httpURLConnection.disconnect();
        return checkIsLogin(username);
    }

    /*
        static fields for query kind
     */
    @SuppressWarnings("WeakerAccess")
    public static final String PAY = "payInfo";
    @SuppressWarnings("WeakerAccess")
    public static final String USE = "useInfo";
    @SuppressWarnings("WeakerAccess")
    public static final String METER = "metInfo";

    /*
     * 查询方法须传入 [ 查询类型 | (查询参数) ] 作为参数
     * 缴费查询和用量查询需要提供时长参数，表具查询不需要额外参数
     *
     * 注意: 此处传入的参数请使用类静态字段，否则将抛出非法参数异常
     */
    public ArrayList<String> query(String... params) throws IOException {
        if (params.length != 1 && params.length != 2)
            throw new IllegalArgumentException("Bad parameter, check document for help");
        String type = params[0];
        ArrayList<String> stringArrayList;
        switch (type) {
            case PAY:
                if (params.length != 2)
                    throw new IllegalArgumentException("Bad parameter, check document for help");
                stringArrayList = query_payInfo(params[1]);
                break;
            case USE:
                if (params.length != 2)
                    throw new IllegalArgumentException("Bad parameter, check document for help");
                stringArrayList = query_useInfo(params[1]);
                break;
            case METER:
                if (params.length != 1)
                    throw new IllegalArgumentException("Bad parameter, check document for help");
                stringArrayList = query_metInfo();
                break;
            default:
                throw new IllegalArgumentException("Bad parameter, check document for help");
        }

        return stringArrayList;
    }

    /*
        static fields for query duration
     */
    @SuppressWarnings("WeakerAccess")
    public static final String ONE_MONTH = "近一个月";
    @SuppressWarnings("WeakerAccess")
    public static final String THREE_MONTH = "近三个月";

    private ArrayList<String> query_payInfo(String duration) throws IOException {
        getPageAttributes(PAYINFO_SUFFIX);
        String OUTPUT_DATA = "But_Seach3=";
        switch (duration) {
            case ONE_MONTH:
                OUTPUT_DATA += ONE_MONTH;
                break;
            case THREE_MONTH:
                OUTPUT_DATA += THREE_MONTH;
                break;
            default:
                throw new IllegalArgumentException("Bad parameter, check document for help");
        }
        OUTPUT_DATA += "&__VIEWSTATE=";
        OUTPUT_DATA += VIEWSTATE;
        OUTPUT_DATA += "&HiddenField_webName=";
        OUTPUT_DATA += "&HiddenField_UserID=";
        OUTPUT_DATA += ID;

        Document document = getPage(OUTPUT_DATA, PAYINFO_SUFFIX);
        Elements elements = document.select("td");

        ArrayList<String> stringArrayList = new ArrayList<>();

        for (Element td : elements) {
            String tmp = td.text();
            if(!"".equals(tmp)) {
                stringArrayList.add(tmp);
            }
        }

        for (int i = 0; i < stringArrayList.size(); i ++) {
            if(stringArrayList.get(i).contains("￥")) {
                stringArrayList.set(i, stringArrayList.get(i).substring(stringArrayList.get(i).indexOf("：") + 2));
                continue;
            }
            stringArrayList.set(i, stringArrayList.get(i).substring(stringArrayList.get(i).indexOf("：") + 1));
        }

        /*
         * 返回字符串数组(stringArrayList)说明:
         *      - 从数组第0项开始, 每两项是一条完整的用量信息
         *      - 此两项依次代表 [ 交易时间 | 实缴金额 ]
         *      - 因此, 数组长度为(2n), n即代表用量信息的总数
         *
         *      - 注意: 如果结果中没有记录将返回空数组而非null!
         */
        return stringArrayList;
    }


    private ArrayList<String> query_useInfo(String duration) throws IOException {
        getPageAttributes(USEINFO_SUFFIX);
        String OUTPUT_DATA = "But_Seach3=";
        switch (duration) {
            case ONE_MONTH:
                OUTPUT_DATA += ONE_MONTH;
                break;
            case THREE_MONTH:
                OUTPUT_DATA += THREE_MONTH;
                break;
            default:
                throw new IllegalArgumentException("Bad parameter, check document for help");
        }
        OUTPUT_DATA += "&__VIEWSTATE=";
        OUTPUT_DATA += VIEWSTATE;
        OUTPUT_DATA += "&HiddenField_webName=";
        OUTPUT_DATA += "&HiddenField_UserID=";
        OUTPUT_DATA += ID;

        Document document = getPage(OUTPUT_DATA,USEINFO_SUFFIX);
        Elements elements = document.select("td");

        ArrayList<String> stringArrayList = new ArrayList<>();

        for (Element td : elements) {
            String tmp = td.text();
            tmp = tmp.replaceAll(" ", "");
            if(!"".equals(tmp)) {
                if (tmp.contains("减免量")) {
                    stringArrayList.add(tmp.substring(0, tmp.indexOf("减免量")));
                    stringArrayList.add(tmp.substring(tmp.indexOf("减免量")));
                    continue;
                }
                stringArrayList.add(tmp);
            }
        }

        for (int i = 0; i < stringArrayList.size(); i ++) {
            stringArrayList.set(i, stringArrayList.get(i).substring(stringArrayList.get(i).indexOf("：") + 1));
        }

        /*
         * 返回字符串数组(stringArrayList)说明:
         *      - 从数组第0项开始, 每五项是一条完整的用量信息
         *      - 此五项依次代表 [ 计量项目 | 起止日期 | 起止量 | 用量/购量 | 减免量 ]
         *      - 因此, 数组长度为(5n), n即代表用量信息的总数
         *
         *      - 注意: 如果结果中没有记录将返回空数组而非null!
         */
        return stringArrayList;
    }

    private ArrayList<String> query_metInfo() throws IOException {
        Document document = getPage("", METINFO_SUFFIX);
        Elements elements = document.select("td");

        ArrayList<String> stringArrayList = new ArrayList<>();

        for (Element td : elements) {
            String tmp = td.text();
            if(!"".equals(tmp)) {
                stringArrayList.add(tmp);
            }
        }

        for (int i = 0; i < stringArrayList.size(); i ++) {
            stringArrayList.set(i, stringArrayList.get(i).substring(stringArrayList.get(i).indexOf("：") + 1));
        }

        /*
         * 返回字符串数组(stringArrayList)说明:
         *      - 从数组第0项开始, 每三项是一条完整的用量信息
         *      - 此五项依次代表 [ 表名称 | 剩余量 | 安装位置 ]
         *      - 因此, 数组长度为(3n), n即代表用量信息的总数
         *
         *      - 注意: 如果结果中没有记录将返回空数组而非null!
         */
        return stringArrayList;
    }

    private void getPageAttributes(String host_suffix) throws IOException {
        Document document = getPage("", host_suffix);
        Elements elements_VIEWSTATE = document
                .select("input[type=\"hidden\"][name=\"__VIEWSTATE\"]");
        VIEWSTATE = elements_VIEWSTATE.get(0).attr("value");
    }

    private Document getPage(String output_data, String host_suffix) throws IOException {
        URL url = new URL(HOST + host_suffix);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.setRequestProperty("Cookie", "ASP.NET_SessionId=" + ASP_dot_NET_SessionId);

        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(),"UTF-8");
        outputStreamWriter.write(output_data);
        outputStreamWriter.flush();
        outputStreamWriter.close();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

        String temp;
        String htmlPage = "";
        while ((temp = bufferedReader.readLine()) != null)
            htmlPage += temp;

        bufferedReader.close();
        httpURLConnection.disconnect();

        htmlPage = htmlPage.replaceAll("&nbsp;", " ");

        return Jsoup.parse(htmlPage);
    }

    /*
     * 通过比对用户信息页面返回结果与登录时的学号判断是否登录成功(首次登录时自动调用)
     * 可用于检测当前SESSION(会话)是否因为已超时而需要重新登录
     * 传入参数为登录时的学号(卡号)
     */
    public boolean checkIsLogin(String username) throws IOException {
        Document document = getPage("", USEINFO_SUFFIX);
        if (document.toString().contains(username)) {
            ID = username;
            return true;
        }
        else {
            ID = "";
            return false;
        }
    }

    /**
     * 用于返回当前会话的卡号(学号)
     *
     * 注意: 当且仅当checkIsLogin()方法被调用且确认已登录成功(checkIsLogin()返回true)时, 其返回为当前会话的卡号(学号), 否则返回空内容
     */
    public String getID() {
        return ID;
    }
}