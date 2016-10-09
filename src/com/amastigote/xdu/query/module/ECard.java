package com.amastigote.xdu.query.module;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.amastigote.xdu.query.util.XDUQueryModule;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ECard extends XDUQueryModule{
    private final static String HOST = "http://ecard.xidian.edu.cn";
    private final static String PRE_LOGIN_SUFFIX = "/login.jsp";
    private final static String CARD_USER_INFO_SUFFIX = "/cardUserManager.do?method=searchCardUserInfo";
    private final static String LOGIN_SUFFIX = "/cardUserManager.do?method=checkLogin";
    private final static String CAPTCHA_SUFFIX = "/authImage";
    private final static String TRANSFER_INFO_SUFFIX = "/cardUserManager.do?method=searchTrjnInfos";
    private String JSESSIONID = "";
    private String ID = "";

    /*
     * 初始化时从服务器获得一个新的JSESSIONID并储存
     * 此JSESSIONID将作为整个SESSION(会话)的凭证
     */
    public ECard() throws IOException {
        URL url = new URL(HOST + PRE_LOGIN_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        JSESSIONID = urlConnection.getHeaderField("Set-Cookie");
        JSESSIONID = JSESSIONID.substring(JSESSIONID.indexOf("=")+1, JSESSIONID.indexOf(";"));
    }

    /*
     * 加载验证码须传入 [ 目标文件 ] 作为参数
     * 验证码将会写入目标文件, 文件须为JPEG格式
     * 登录(或重新登录)前必须调用此方法以刷新此次SESSION(会话)的验证码
     */
    public void getCaptcha(File file) throws IOException {
        URL url = new URL(HOST + CAPTCHA_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
        byte[] bytes = new byte[1024];
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        int LENGTH;
        while ((LENGTH = inputStream.read(bytes)) != -1){
            fileOutputStream.write(bytes, 0, LENGTH);
        }
        inputStream.close();
        fileOutputStream.close();
    }

    /*
     * 登录方法须传入 [ 当前验证码 | 学号(卡号) | 一卡通密码 ] 作为参数
     * 返回是否登录成功
     */
    public boolean login(String... params) throws IOException {
        String username = params[0];
        String password = params[1];
        String captcha = params[2];

        URL url = new URL(HOST + LOGIN_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        String OUTPUT_DATA = "flag=1&code=";
        OUTPUT_DATA+=username;
        OUTPUT_DATA+="&pwd=";
        OUTPUT_DATA+=password;
        OUTPUT_DATA+="&cardCheckCode=";
        OUTPUT_DATA+=captcha;
        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
        outputStreamWriter.write(OUTPUT_DATA);
        outputStreamWriter.flush();
        outputStreamWriter.close();
        httpURLConnection.getResponseMessage();
        httpURLConnection.disconnect();
        return checkIsLogin(username);
    }

    /*
     * 通过比对用户信息页面返回结果与登录时的学号判断是否登录成功(首次登录时自动调用)
     * 可用于检测当前SESSION(会话)是否因为已超时而需要重新登录
     * 传入参数为登录时的学号(卡号)
     */
    public boolean checkIsLogin(String username) throws IOException {
        URL url = new URL(HOST + CARD_USER_INFO_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        urlConnection.connect();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String BUFFER;
        if (username.length() != 11) {
            bufferedReader.close();
            return false;
        }
        while ((BUFFER = bufferedReader.readLine()) != null){
            if (BUFFER.contains(username.substring(0,7))) {
                ID = username;
                bufferedReader.close();
                return true;
            }
        }
        ID = "";
        bufferedReader.close();
        return false;
    }

    /*
     * 查询方法须传入 [ 开始日期 | 结束日期 ] 作为参数
     * 日期格式: yyyy-MM-dd
     *
     * 注意: 起止日期区间不得超过一个月, 否则将返回垃圾结果
     */
    public ArrayList<String> query(String... params) throws IOException {
        String fromDate = params[0];
        String toDate = params[1];

        int maxPage = 1;
        boolean FLAG_GOT_MAX_PAGE = false;
        final char SPACE = 160;
        ArrayList<String> stringArrayList = new ArrayList<>();
        URL url = new URL(HOST + TRANSFER_INFO_SUFFIX);
        HttpURLConnection httpURLConnection = null;

        /*
         * 遍历所有结果页面
         */
        for (int page=1; page<=maxPage; page++) {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
            String OUTPUT_DATA = "page=";
            OUTPUT_DATA += page;
            OUTPUT_DATA += "&startTime=";
            OUTPUT_DATA += fromDate;
            OUTPUT_DATA += "&endTime=";
            OUTPUT_DATA += toDate;
            OUTPUT_DATA += "&findType=";

            /*
             * 请求类型(findType)说明:
             *      - 1210 卡消费流水
             *      - 1130 卡充值流水
             *      - 1261 卡转账流水
             *      - 2230 卡补助流水
             *      - 1140 自动充值流水
             *
             *      - 经测试只有卡消费流水类型可用, 因此锁定此查询类型
            */
            OUTPUT_DATA += "1210";
            httpURLConnection.connect();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
            outputStreamWriter.write(OUTPUT_DATA);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            BufferedReader bufferedReader = new BufferedReader(
                                            new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
            String temp;
            String htmlPage = "";
            while ((temp = bufferedReader.readLine()) != null)
                htmlPage+=temp;
            httpURLConnection.getResponseMessage();
            Document document = Jsoup.parse(htmlPage);
            bufferedReader.close();

            /*
             * 获取查询结果占用的页面数
             */
            if (!FLAG_GOT_MAX_PAGE) {
                Elements selectors = document.select("select");
                Elements options = selectors.select("option");
                for (Element each : options) {
                    if (StringUtil.isNumeric(each.text())) {
                        maxPage = Integer.valueOf(each.text());
                        FLAG_GOT_MAX_PAGE = true;
                    }
                }
            }

            /*
             * 解析当前页面的内容
             */
            Elements tables = document.select("table");
            Elements trs = tables.select("[id=\"tabInfo\"]");
            Elements tds = trs.select("td");

            for (Element td : tds) {
                if (!"".equals(td.text().replace(String.valueOf(SPACE), "")))
                    stringArrayList.add(td.text());
            }
        }

        httpURLConnection.disconnect();

        /*
         * 如果结果中没有记录将返回null而非空数组!
         */
        if (stringArrayList.size() == 0)
            return null;
        stringArrayList.set(stringArrayList.size()-1,
                stringArrayList.get(stringArrayList.size()-1)
                .substring(stringArrayList.get(stringArrayList.size()-1).indexOf("：")+1,
                        stringArrayList.get(stringArrayList.size()-1).indexOf(" ")));

        /*
         * 返回字符串数组(stringArrayList)说明:
         *      - 从数组第0项开始, 每五项是一条完整的流水记录
         *      - 此五项依次代表 [ 交易地点 | 设备编号 | 交易时间 | 交易金额 | 余额 ]
         *      - 数组的最后一项为查询区间内的总消费金额
         *      - 因此, 数组长度为(5n+1), n即代表消费记录的总条数
         *      - 消费记录的顺序按消费时间从晚到早排列
         *
         *      - 注意: 如果结果中没有记录将返回null而非空数组!
         */
        return stringArrayList;
    }

    /**
     * 用于返回当前会话的卡号(学号)
     *
     * 注意: 当且仅当checkIsLogin()方法被调用且确认已登录成功(checkIsLogin()返回true)时, 其返回为当前会话的卡号(学号), 否则返回空内容
     */
    public String getID(){
        return ID;
    }
}
