package com.cq.YunPhoto.Config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {


    private String secretId;

    private String secretKey;

    private String region;

    private String bucket;

    private String host;
    @Bean
    public COSClient cosClient() {
        COSCredentials basicCOSCredentials = new BasicCOSCredentials(secretId, secretKey);
        Region region1 = new Region(region);
        ClientConfig clientConfig = new ClientConfig(region1);
        return new COSClient(basicCOSCredentials, clientConfig);
    }
}
