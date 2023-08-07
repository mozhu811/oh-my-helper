package io.cruii.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cruii.exception.ConvertPusherConfigFailedException;
import io.cruii.exception.InvalidCookieException;
import io.cruii.mapper.PusherConfigMapper;
import io.cruii.model.pusher.PusherConfigDTO;
import io.cruii.model.pusher.PusherConfigVO;
import io.cruii.pojo.entity.PusherConfigDO;
import io.cruii.service.PushConfigService;
import io.cruii.service.TaskConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PushConfigServiceImpl implements PushConfigService {
    private final PusherConfigMapper pusherConfigMapper;

    private final TaskConfigService taskConfigService;


    public PushConfigServiceImpl(PusherConfigMapper pusherConfigMapper, TaskConfigService taskConfigService) {
        this.pusherConfigMapper = pusherConfigMapper;
        this.taskConfigService = taskConfigService;
    }

    @Override
    public PusherConfigVO saveOrUpdate(String userId, PusherConfigDTO pusherConfig) {
        String configJsonStr = JSONUtil.toJsonStr(pusherConfig);
        PusherConfigDO pusherConfigDO = new PusherConfigDO();
        pusherConfigDO.setUserId(userId);
        pusherConfigDO.setConfig(configJsonStr);

        Optional<PusherConfigDO> exist =
                Optional.ofNullable(pusherConfigMapper.selectOne(Wrappers.lambdaQuery(PusherConfigDO.class)
                .eq(PusherConfigDO::getUserId, userId)));

        exist.ifPresentOrElse(e -> {
            pusherConfigDO.setId(e.getId());
            pusherConfigMapper.updateById(pusherConfigDO);
        }, () -> pusherConfigMapper.insert(pusherConfigDO));

        try {
            // 多态反序列化
            ObjectMapper objectMapper = new ObjectMapper();
            JSONObject configJsonObj = JSONUtil.parseObj(configJsonStr);
            configJsonObj.set("id", pusherConfigDO.getId());
            return objectMapper.readValue(configJsonObj.toJSONString(0), PusherConfigVO.class);
        } catch (JsonProcessingException e) {
            throw new ConvertPusherConfigFailedException(e);
        }
    }

    @Override
    public PusherConfigVO get(String dedeuserid, String sessdata, String biliJct) {
        Optional.ofNullable(taskConfigService.get(dedeuserid, sessdata, biliJct))
                .orElseThrow(() -> new InvalidCookieException("Cookie is invalid"));
        PusherConfigDO pusherConfigDO = pusherConfigMapper.selectOne(
                Wrappers.<PusherConfigDO>lambdaQuery().eq(PusherConfigDO::getUserId, dedeuserid));
        if (pusherConfigDO == null) {
            throw new RuntimeException("Pusher config is not exist");
        }
        try {
            // 多态反序列化
            ObjectMapper objectMapper = new ObjectMapper();
            JSONObject configJsonObj = JSONUtil.parseObj(pusherConfigDO.getConfig());
            configJsonObj.set("id", pusherConfigDO.getId());
            return objectMapper.readValue(configJsonObj.toJSONString(0), PusherConfigVO.class);
        } catch (JsonProcessingException e) {
            throw new ConvertPusherConfigFailedException(e);
        }
    }

}
