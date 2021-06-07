package io.cruii.bilibili.util;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import io.cruii.bilibili.config.TencentApiConfig;

/**
 * @author cruii
 * Created on 2021/6/7
 */
public class ScfClient {
    private ScfClient(){}

    public static class Builder {
        private TencentApiConfig apiConfig;

        public Builder(TencentApiConfig apiConfig) {
            this.apiConfig = apiConfig;
        }

        public com.tencentcloudapi.scf.v20180416.ScfClient build() {
            Credential cred = new Credential(apiConfig.getSecretId(), apiConfig.getSecretKey());

            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(apiConfig.getScfEndpoint());

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            return new com.tencentcloudapi.scf.v20180416.ScfClient(cred, apiConfig.getRegion(), clientProfile);
        }
    }
}
