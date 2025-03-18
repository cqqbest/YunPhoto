package com.cq.YunPhoto.Api.ImageSearch;

import com.cq.YunPhoto.Api.ImageSearch.model.imageSearchResult;
import com.cq.YunPhoto.Api.ImageSearch.sub.GetImageFirstUrlApi;
import com.cq.YunPhoto.Api.ImageSearch.sub.GetImageListApi;
import com.cq.YunPhoto.Api.ImageSearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class imageSearchApiFacade {
    public static List<imageSearchResult> searchImage(String url) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(url);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<imageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }
    public static void main(String[] args) {
        String url = "https://www.codefather.cn/logo.png";
        List<imageSearchResult> imageList = searchImage(url);
        for (imageSearchResult image : imageList) {
            System.out.println(image);
        }

    }
}
