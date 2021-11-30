package io.cruii.bilibili.util;

import cn.hutool.extra.spring.SpringUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import io.cruii.bilibili.config.TencentCloudConfig;

import java.io.File;

/**
 * @author cruii
 * Created on 2021/11/23
 */
public class CosUtil {
    private CosUtil() {
    }

    private static TencentCloudConfig tencentCloudConfig;

    private static COSClient cosClient;

    /**
     * 初始化
     */
    private static void setup() {
        if (tencentCloudConfig == null) {
            tencentCloudConfig = SpringUtil.getApplicationContext().getBean("tencentCloudConfig", TencentCloudConfig.class);
        }
        BasicCOSCredentials credentials = new BasicCOSCredentials(tencentCloudConfig.getSecretId(), tencentCloudConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(tencentCloudConfig.getCosRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(credentials, clientConfig);
    }

    /**
     * 上传对象
     *
     * @param file 待上传的文件
     */
    public static synchronized void upload(File file) {
        try {
            setup();
            String key = tencentCloudConfig.getFolder() + "/" + file.getName();
            PutObjectRequest putObjectRequest = new PutObjectRequest(tencentCloudConfig.getBucketName(), key, file);
            cosClient.putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cosClient.shutdown();
        }
    }
}
