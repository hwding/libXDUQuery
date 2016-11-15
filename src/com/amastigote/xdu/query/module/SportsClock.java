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

import com.amastigote.xdu.query.util.IXDULoginNormal;
import com.amastigote.xdu.query.util.IXDUQueryNoParam;
import com.sun.istack.internal.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.amastigote.xdu.query.util.IXDUBase;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class SportsClock
        implements
        IXDUBase,
        IXDUQueryNoParam,
        IXDULoginNormal {
    private final static String HOST = "http://210.27.8.14";
    private final static String LOGIN_SUFFIX = "/login";
    private final static String RUNNER_SUFFIX = "/runner/";
    private final static String ACHIEVEMENTS_SUFFIX = "/runner/achievements.html";

    private String JSESSIONID = "";
    private String ID = "";

    /*
     * 登录方法须传入 [ 学号 | 密码 ] 作为参数
     * 返回是否登录成功
     */
    public boolean login(@NotNull String username, @NotNull String password) throws IOException {

        URL url = new URL(HOST + LOGIN_SUFFIX);
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
        JSESSIONID = JSESSIONID.substring(JSESSIONID.indexOf("=") + 1, JSESSIONID.indexOf(";"));
        httpURLConnection.disconnect();
        return checkIsLogin(username);
    }

    /*
     * 通过比对用户信息页面返回结果与登录时的学号判断是否登录成功(首次登录时自动调用)
     * 可用于检测当前SESSION(会话)是否因为已超时而需要重新登录
     * 传入参数为登录时的学号(卡号)
     */
    public boolean checkIsLogin(@NotNull String username) throws IOException {
        URL url = new URL(HOST + RUNNER_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID=" + JSESSIONID);
        httpURLConnection.connect();
        if ("OK".equals(httpURLConnection.getResponseMessage())) {
            ID = username;
            httpURLConnection.disconnect();
            return true;
        }

        ID = "";
        httpURLConnection.disconnect();
        return false;
    }

    public List<String> query() throws IOException {
        URL url = new URL(HOST + ACHIEVEMENTS_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "JSESSIONID=" + JSESSIONID);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String temp;
        String htmlPage = "";
        while ((temp = bufferedReader.readLine()) != null)
            htmlPage += temp;
        bufferedReader.close();

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
        List<String> stringArrayList = new ArrayList<>();

        for (Element td : tds) {
            if (!"".equals(td.text()))
                stringArrayList.add(td.text());
        }
        return stringArrayList;
    }

    /*
     * 用于返回当前会话的学号
     *
     * 注意: 当且仅当checkIsLogin()方法被调用且确认已登录成功(checkIsLogin()返回true)时, 其返回为当前会话的学号, 否则返回空内容
     */
    public String getID() {
        return ID;
    }
}
