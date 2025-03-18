package com.cq.YunPhoto.Api.ImageSearch.sub;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import com.cq.YunPhoto.Api.ImageSearch.model.imageSearchResult;
import com.cq.YunPhoto.Exception.BusinessException;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * 根据firstUrl获取图片列表（hutool工具）
 * @Author: cpq
 * @Date: 2021/1/30
 */

@Slf4j
@Component
public class GetImageListApi {

    public static List<imageSearchResult> getImageList(String firstUrl) {
        try {
            HttpResponse execute = HttpRequest.post(firstUrl).execute();
            int status = execute.getStatus();
            String body = execute.body();
            if (status == 200) {
                JSONObject jsonObject = new JSONObject(body);
                if (!jsonObject.containsKey("data")) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片搜索失败");
                }
                JSONObject data = jsonObject.getJSONObject("data");
                if (!data.containsKey("list")) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片搜索失败");
                }
                List<imageSearchResult> list = data.getJSONArray("list").toList(imageSearchResult.class);
                return list;
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片搜索失败");
        }catch (Exception e){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "图片搜索失败");
        }
    }

    public static void main(String[] args) {
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=4157125107126023072&sign=121ade97cd54acd88139901741240273&tk=c8c1e&tpl_from=pc";
        List<imageSearchResult> imageList = getImageList(url);
        for (imageSearchResult image : imageList) {
            System.out.println(image);
        }
    }
}
