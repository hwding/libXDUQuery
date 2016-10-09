package module;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class WaterAndElectricity {
    private final static String HOST = "http://10.168.55.50:8088";
    private final static String PRE_LOGIN_SUFFIX = "/searchWap/Login.aspx";
    private final static String LOGIN_SUFFIX = "/ajaxpro/SearchWap_Login,App_Web_fghipt60.ashx";
    private final static String USEINFO_SUFFIX = "/SearchWap/webFrm/useInfo.aspx";
    private String ASP_dot_NET_SessionId;
    private String __VIEWSTATE = "/wEPDwUKLTUwNjExOTI3Nw9kFgICAw9kFgICAQ8PFgIeBFRleHQFCjIwMTEwMjIxMDlkZGQ=";


    public static void main(String args[]) throws IOException{
        WaterAndElectricity waterAndElectricity = new WaterAndElectricity();
        System.out.println(waterAndElectricity.login("2011022109","123456"));
        waterAndElectricity.useInfo_query();
    }

    public WaterAndElectricity() throws IOException {
        URL url = new URL(HOST+PRE_LOGIN_SUFFIX);
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        ASP_dot_NET_SessionId = urlConnection.getHeaderField("Set-Cookie");

        ASP_dot_NET_SessionId = ASP_dot_NET_SessionId.substring(
                ASP_dot_NET_SessionId.indexOf("=")+1,
                ASP_dot_NET_SessionId.indexOf(";"));
    }

    public boolean login(String userName, String password) throws IOException {
        URL url = new URL(HOST+LOGIN_SUFFIX);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setInstanceFollowRedirects(false);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestProperty("Cookie", "ASP.NET_SessionId="+ASP_dot_NET_SessionId);
        httpURLConnection.setRequestProperty("AjaxPro-Method", "getLoginInput");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("webName", userName);
        jsonObject.put("webPass", password);
        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
        outputStreamWriter.write(jsonObject.toString());
        outputStreamWriter.flush();
        outputStreamWriter.close();
        BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(httpURLConnection.getInputStream()));
        String temp;
        String result = "";
       /* while(!((temp = bufferedReader.readLine()) == null)){
            result += temp;
        }

        System.out.println(result);*/

      if ("\"1\"".equals(bufferedReader.readLine())) {
            httpURLConnection.disconnect();
            return true;
        }


        httpURLConnection.disconnect();
        return false;
    }

    public ArrayList<String> useInfo_query(String... params) throws IOException{
        URL url = new URL(HOST+USEINFO_SUFFIX);
       HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setInstanceFollowRedirects(false);

        String OUTPUT_DATA = "But_Seach3=";
        OUTPUT_DATA += "近三个月";
        OUTPUT_DATA += "&__VIEWSTATE=";
        OUTPUT_DATA += __VIEWSTATE;
        OUTPUT_DATA += "&HiddenField_webName=";
        OUTPUT_DATA += "&HiddenField_UserID=";
        OUTPUT_DATA += "2011022109";

        httpURLConnection.connect();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(),"UTF-8");
        outputStreamWriter.write(OUTPUT_DATA);
        outputStreamWriter.flush();
        outputStreamWriter.close();
        System.out.println(httpURLConnection.getResponseMessage());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String temp;
        String htmlPage = "";
        while ((temp = bufferedReader.readLine()) != null)
            htmlPage += temp + "\n" ;
        bufferedReader.close();

        System.out.println(htmlPage);
        return null;
    }

}
