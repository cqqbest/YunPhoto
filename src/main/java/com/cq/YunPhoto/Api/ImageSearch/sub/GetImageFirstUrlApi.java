package com.cq.YunPhoto.Api.ImageSearch.sub;

import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 获取firstUrl
 * @Author: CQ
 */
@Slf4j
@Component
public class GetImageFirstUrlApi {
    public static String getImageFirstUrl(String url){
        try {
            Document document = Jsoup.connect(url).timeout(5000).get();
            Elements scriptElements = document.getElementsByTag("script");
            for (Element scriptElement : scriptElements) {
                String html = scriptElement.html();
                if (html.contains("\"firstUrl\"")) {
                    //正则表达式提取firstUrl的值
                    Pattern compile = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = compile.matcher(html);
                    if (matcher.find()) {
                        String group = matcher.group(1);
                        //字符转义
                        return group.replace("\\/", "/");
                    }
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到 url");
        }catch (IOException e){
            log.error("获取图片失败",e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"获取图片失败");
        }
    }

    public static void main(String[] args) {
        // 请求目标 URL
        String url = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData[isLogoShow]=1&f=all&isLogoShow=1&session_id=4157125107126023072&sign=121ade97cd54acd88139901741240273&tpl_from=pc";
        String imageFirstUrl = getImageFirstUrl(url);
        System.out.println("搜索成功，结果 URL：" + imageFirstUrl);
    }
}

