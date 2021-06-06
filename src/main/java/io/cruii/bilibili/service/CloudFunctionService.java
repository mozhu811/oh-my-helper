package io.cruii.bilibili.service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import io.cruii.bilibili.dto.BilibiliUserDTO;
import io.cruii.bilibili.entity.CloudFunctionLog;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/6
 */
public interface CloudFunctionService {
    List<CloudFunctionLog> listLogs(String username, long startTime, long endTime) throws TencentCloudSDKException;

    List<BilibiliUserDTO> listFunctions() throws TencentCloudSDKException;
}
