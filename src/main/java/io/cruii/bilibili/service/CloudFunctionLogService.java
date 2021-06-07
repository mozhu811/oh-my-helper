package io.cruii.bilibili.service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import io.cruii.bilibili.entity.CloudFunctionLog;

import java.util.List;

/**
 * @author cruii
 * Created on 2021/6/7
 */
public interface CloudFunctionLogService {
    List<CloudFunctionLog> listLogs(String dedeuserid, long startTime, long endTime) throws TencentCloudSDKException;
}
