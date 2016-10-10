package com.amastigote.xdu.query.module;

import com.amastigote.xdu.query.util.XDUQueryModule;
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

public class WaterAndElectricity extends XDUQueryModule{
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
                tmp.indexOf("=")+1,
                tmp.indexOf(";"));
    }


    public boolean login(String... params) throws IOException {
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

    public ArrayList<String> query(String... params) throws IOException{

        String type = params[0];
        ArrayList<String> stringArrayList = new ArrayList<>();
        switch (type){
            case "payInfo":stringArrayList = query_payInfo();break;
            case "useInfo":stringArrayList = query_useInfo();break;
            case "metInfo":stringArrayList = query_metInfo();break;
        }

        return stringArrayList;


        // TODO: 2016/10/9 这里的查询类别还需要分流，不是一个最终版本

    }

    private ArrayList<String> query_payInfo() throws IOException{
        getPageAttributes(PAYINFO_SUFFIX);
        String OUTPUT_DATA = "But_Seach3=";
        OUTPUT_DATA += "近三个月";
        OUTPUT_DATA += "&__VIEWSTATE=";
        OUTPUT_DATA += VIEWSTATE;
        OUTPUT_DATA += "&HiddenField_webName=";
        OUTPUT_DATA += "&HiddenField_UserID=";
        OUTPUT_DATA += ID;

        Document document = getPage(OUTPUT_DATA,PAYINFO_SUFFIX);
        Elements elements = document.select("td");

        ArrayList<String> stringArrayList = new ArrayList<>();

        for (Element td : elements){
            String tmp = td.text();
            if(!"".equals(tmp)){
                stringArrayList.add(tmp);
            }
        }

        for (int i = 0; i < stringArrayList.size(); i++) {
            if(stringArrayList.get(i).contains("￥")){
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


    private ArrayList<String> query_useInfo() throws IOException{
        getPageAttributes(USEINFO_SUFFIX);
        String OUTPUT_DATA = "But_Seach3=";
        OUTPUT_DATA += "近三个月";
        OUTPUT_DATA += "&__VIEWSTATE=";
        OUTPUT_DATA += VIEWSTATE;
        OUTPUT_DATA += "&HiddenField_webName=";
        OUTPUT_DATA += "&HiddenField_UserID=";
        OUTPUT_DATA += ID;

        Document document = getPage(OUTPUT_DATA,USEINFO_SUFFIX);
        Elements elements = document.select("td");

        ArrayList<String> stringArrayList = new ArrayList<>();

        for (Element td : elements){
            String tmp = td.text();
            tmp = tmp.replaceAll(" ", "");
            if(!"".equals(tmp)){
                if (tmp.contains("减免量")) {
                    stringArrayList.add(tmp.substring(0, tmp.indexOf("减免量")));
                    stringArrayList.add(tmp.substring(tmp.indexOf("减免量")));
                    continue;
                }
                stringArrayList.add(tmp);
            }
        }

        for (int i = 0; i < stringArrayList.size(); i++) {
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

    private ArrayList<String> query_metInfo() throws IOException{
        URL url = new URL(HOST + METINFO_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie","ASP.NET_SessionId=" + ASP_dot_NET_SessionId);

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
        String temp;
        String htmlPage = "";
        while ((temp = bufferedReader.readLine()) != null)
            htmlPage += temp;
        bufferedReader.close();

        htmlPage = htmlPage.replaceAll("&nbsp;", " ");
        Document document = Jsoup.parse(htmlPage);
        Elements elements = document.select("td");

        ArrayList<String> stringArrayList = new ArrayList<>();

        for (Element td : elements){
            String tmp = td.text();
            if(!"".equals(tmp)){
                stringArrayList.add(tmp);
            }
        }

        for (int i = 0; i < stringArrayList.size(); i++) {
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
        Document document = getPage("",host_suffix);    //根据host_suffix选取不同的__VIEWSTATE属性
        Elements elements_VIEWSTATE = document
                .select("input[type=\"hidden\"][name=\"__VIEWSTATE\"]");
        VIEWSTATE = elements_VIEWSTATE.get(0).attr("value");
    }

    private Document getPage(String output_data,String host_suffix) throws IOException {
        URL url = new URL(HOST + host_suffix);
        HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
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

    public boolean checkIsLogin(String username) throws IOException{
        Document document = getPage("",USEINFO_SUFFIX);     //选取UseInfo页面判断是否已登录
        if (document.toString().contains(username)){
            ID = username;
            return true;
        }
        else {
            ID = "";
            return false;
        }
    }

    public String getID() {
        return ID;
    }



}