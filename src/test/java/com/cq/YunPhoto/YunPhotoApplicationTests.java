package com.cq.YunPhoto;

import com.cq.YunPhoto.Config.CosClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class YunPhotoApplicationTests {
    @Resource
    private CosClientConfig cosClientConfig;

    @Test
    void contextLoads() {
        System.out.println("hello");
    }

    @Test
    void test() {
        String bucket = cosClientConfig.getBucket();
        System.out.println(bucket);
    }
    @Test
    void test2(){
        String s = "a1b2c3";
        System.out.println(method(s));
    }
    public static String method(String s){
        char[] c = s.toCharArray();
        StringBuilder s1 = new StringBuilder();
        for(int i = 0;i<c.length;i++){
            if(c[i]>97){
                s1.append(c[i]);
            }
            else{
                s1.append("number");
            }
        }
        return s1.toString();



    }

}
