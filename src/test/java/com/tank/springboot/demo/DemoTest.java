package com.tank.springboot.demo;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DemoTest {

    public static final String DIR = "E:\\image\\";
    public static final String PIC_SUFFIX = ".jpg";
    public static final String URL_PREFIX = "http://00kkaa.com";
    public static final String URL_CARTOON = "/09%E6%93%8D%E5%BD%B1%E9%99%A2_12_";
    public static final String URL_SUFFIX = ".html";
    //title中需要被去除的字符，创建文件夹需要根据title创建
    public static final Pattern PATTERN = Pattern.compile("[\\/:*\"'?<>|*“‘《》？ ]");



    @Test
    public void test1() throws Exception{
        for(int num=259;num<280;num++){
            downLoadByPageNum(num);
        }
    }

    private void downLoadByPageNum(Integer pageNum) throws Exception{

        String mainUrl = URL_PREFIX + URL_CARTOON + pageNum + URL_SUFFIX;
        String html = getHtmlStr(mainUrl);
        List<Page> pageList = parseUrlByHtml(html);
        System.out.println(pageList.size());
        for(Page page : pageList){
            String pageUrl = page.getUrl();
            String title = page.getTitle();
            Matcher m = PATTERN.matcher(title);
            String dir = DIR + m.replaceAll("").trim();
            File dirFile = new File(dir);
            dirFile.mkdirs();
            String currentHtml = getHtmlStr(URL_PREFIX + pageUrl);
            List<String> srcList = parseImgUrlByHtml(currentHtml);
            List<JPG> jpgList = new ArrayList<>();
            Supplier<JPG> jpgSupplier = JPG::new;
            for(int i=0;i<srcList.size();i++){
                JPG jpg = jpgSupplier.get();
                String src = srcList.get(i);
                jpg.setSrc(src);
                jpg.setFilePath(dir + "/" + i + PIC_SUFFIX);
                //屏蔽该资源，速度太慢
                if(src.contains("http://hanman.co")||src.contains("tu303.com")){
                    continue;
                }
                jpgList.add(jpg);
            }
            jpgList.parallelStream().forEach(this::savePicByUrl);
            System.out.println(dir + " 下载完成");
        }
    }
    /**
     * 通过当前分页获取页面内的各个page的url片段和title信息
     * @param html
     * @return
     */
    private List<Page> parseUrlByHtml(String html){
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByTag("a");
        Supplier<Page> pageSupplier = Page :: new;
        List<Page> list = elements.parallelStream()
                //筛选包含target属性的且含有两个子元素的<a>标签
                .filter(element -> element.hasAttr("target")&&element.childNodeSize()==2)
                .map(element -> {
                    Page page = pageSupplier.get();
                    String url = element.attr("href");
                    String title = element.ownText();
                    page.setUrl(url);
                    page.setTitle(title);
                    return page;
                }).collect(Collectors.toList());
        return list;
    }

    /**
     * 根据页面解析所有的Img标签
     * @param html
     * @return
     */
    private List<String> parseImgUrlByHtml(String html){
        Document document = Jsoup.parse(html);
        Elements imgElements = document.getElementsByTag("img");
        List<String> srcList = imgElements.eachAttr("src");
        return srcList;
    }
    /**
     * 通过网页地址抓取页面
     * @param url
     * @return
     * @throws Exception
     */
    private String getHtmlStr(String url) throws Exception{
        //创建httpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();
        //创建Get请求
        HttpGet httpget = new HttpGet(url);
        //执行Get请求，获取response
        try(CloseableHttpResponse response = httpclient.execute(httpget)){
            // 获取响应实体
            HttpEntity entity = response.getEntity();
            if(null != entity){
                return EntityUtils.toString(entity);
            }
        }
        return null;
    }
    /**
     * 根据图片地址下载保存到本地
     * @param jpg
     */
    private void savePicByUrl(JPG jpg) {
        URL url = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(jpg.getSrc());
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        }catch (Exception e){
            e.printStackTrace();
        }
        File jpgFile = new File(jpg.getFilePath());
        //文件存在则跳过
        if(jpgFile.exists()){
            return;
        }
        //try with resource
        try(
                DataInputStream dataInputStream = new DataInputStream(connection.getInputStream());
                FileOutputStream fileOutputStream = new FileOutputStream(new File(jpg.getFilePath()));
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                ){
            byte[] buffer = new byte[1024];
            int length;
            while ((length = dataInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
            fileOutputStream.write(output.toByteArray());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

class JPG {

    public String src;
    public String filePath;

    public JPG() {
    }

    public JPG(String src, String filePath) {
        this.src = src;
        this.filePath = filePath;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

class Page {

    public String url;
    public String title;

    public Page() {
        super();
    }

    public Page(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}