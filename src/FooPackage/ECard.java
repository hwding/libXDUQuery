package FooPackage;

import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ECard {
    private final static String HOST = "http://ecard.xidian.edu.cn";
    private final static String PRE_LOGIN_SUFFIX = "/login.jsp";
    private final static String CARD_USER_INFO_SUFFIX = "/cardUserManager.do?method=searchCardUserInfo";
    private final static String LOGIN_SUFFIX = "/cardUserManager.do?method=checkLogin";
    private final static String CAPTCHA_SUFFIX = "/authImage";
    private final static String TRANSFER_INFO_SUFFIX = "/cardUserManager.do?method=searchTrjnInfos";
    private String JSESSIONID;

    /*
     * 初始化时从服务器获得一个新的JSESSIONID并储存
     * 此JSESSIONID将作为整个SESSION(会话)的凭证
     */
    private ECard() throws IOException {
        URL url = new URL(HOST + PRE_LOGIN_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        JSESSIONID = urlConnection.getHeaderField("Set-Cookie");
        JSESSIONID = JSESSIONID.substring(JSESSIONID.indexOf("=")+1, JSESSIONID.indexOf(";"));
    }

    /*
     * 抓取并保存图片验证码于运行目录下, 登录(或重新登录)前必须调用此方法以刷新此次SESSION(会话)的验证码
     */
    private void getCaptcha() throws IOException {
        URL url = new URL(HOST + CAPTCHA_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        urlConnection.connect();
        InputStream inputStream = urlConnection.getInputStream();
        byte[] bytes = new byte[1024];
        FileOutputStream fileOutputStream = new FileOutputStream("temp_captcha.jpeg");
        int LENGTH;
        while ((LENGTH = inputStream.read(bytes)) != -1){
            fileOutputStream.write(bytes, 0, LENGTH);
        }
        inputStream.close();
        fileOutputStream.close();
    }

    /*
     * 登录方法须传入 [ 当前验证码 | 学号(卡号) | 一卡通密码 ] 作为参数
     */
    private String login(String CAPTCHA, String ID, String PASSWORD) throws IOException {
        URL url = new URL(HOST + LOGIN_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        String OUTPUT_DATA = "flag=1&code=";
        OUTPUT_DATA+=ID;
        OUTPUT_DATA+="&pwd=";
        OUTPUT_DATA+=PASSWORD;
        OUTPUT_DATA+="&cardCheckCode=";
        OUTPUT_DATA+=CAPTCHA;
        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
        outputStreamWriter.write(OUTPUT_DATA);
        outputStreamWriter.flush();
        outputStreamWriter.close();
        httpURLConnection.getResponseMessage();
        httpURLConnection.disconnect();
        return ID;
    }

    /*
     * 通过比对用户信息页面返回结果与登录时的学号判断是否登录成功(非必须调用, 但建议进行验证)
     * 可用于检测当前SESSION(会话)是否因为已超时而需要重新登录
     * 传入参数为登录时的学号(卡号)
     */
    private boolean checkIsLogin(String string) throws IOException {
        URL url = new URL(HOST + CARD_USER_INFO_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Cookie", "JSESSIONID="+JSESSIONID);
        urlConnection.connect();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        String BUFFER;
        if (string.length() != 11)
            return false;
        while ((BUFFER = bufferedReader.readLine()) != null){
            if (BUFFER.contains(string.substring(0,7)))
                return true;
        }
        return false;
    }

    /*
     * 建设/测试中
     */
    private ArrayList<String> queryTransferInfo(String fromDate, String toDate) throws IOException {
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
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //page=1&startTime=2016-04-20&endTime=2016-04-20&findType=1210
            String OUTPUT_DATA = "page=";
            OUTPUT_DATA += page;
            OUTPUT_DATA += "&startTime=";
            OUTPUT_DATA += fromDate;
            OUTPUT_DATA += "&endTime=";
            OUTPUT_DATA += toDate;
            OUTPUT_DATA += "&findType=";
            OUTPUT_DATA += "1210";
            //1210 卡消费流水
            //1130 卡充值流水
            //1261 卡转账流水
            //2230 卡补助流水
            //1140 自动充值流水
            httpURLConnection.connect();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8");
            outputStreamWriter.write(OUTPUT_DATA);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
            String temp;
            String htmlPage = "";
            while ((temp = bufferedReader.readLine()) != null)
                htmlPage+=temp;
            httpURLConnection.getResponseMessage();
            Document document = Jsoup.parse(htmlPage);

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
            //System.out.println(each.text());
            stringArrayList.addAll(tds.stream().filter(each -> !"".equals(each.text().replace(String.valueOf(SPACE),
                    ""))).map(Element::text).collect(Collectors.toList()));
        }
        httpURLConnection.disconnect();
        stringArrayList.set(stringArrayList.size()-1,
                stringArrayList.get(stringArrayList.size()-1)
                .substring(stringArrayList.get(stringArrayList.size()-1).indexOf("：")+1,
                        stringArrayList.get(stringArrayList.size()-1).indexOf(" ")));
        System.out.println(stringArrayList.get(stringArrayList.size()-1));

        /*
         * 返回字符串数组(stringArrayList)说明:
         *      - 从数组第0项开始, 每五项是一条完整的流水记录
         *      - 此五项依次代表 [ 交易地点 | 设备编号 | 交易时间 | 交易金额 | 余额 ]
         *      - 数组的最后一项为查询区间内的总消费金额
         *      - 因此, 数组长度为(5n+1), n即代表消费记录的总条数
         */
        return stringArrayList;
    }

    /*
     * 此部分用于单独测试eCard模块
     */
    public static void main(String[] args) throws IOException {
        ECard eCard = new ECard();
        eCard.getCaptcha();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Captcha image generated, please input: ");
        String CAPTCHA = scanner.nextLine();
        System.out.print("Student ID (also card number): ");
        String ID = scanner.nextLine();
        System.out.print("Password for eCard (6 numbers): ");
        String PASSWORD = scanner.nextLine();
        System.out.println(eCard.checkIsLogin(eCard.login(CAPTCHA, ID, PASSWORD)));
        /*
         * 建设/测试中
         */
        if (eCard.checkIsLogin(ID))
            eCard.queryTransferInfo("2016-04-20", "2016-05-20");
    }
}
