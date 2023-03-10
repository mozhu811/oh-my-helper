package io.cruii.push.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.pojo.dto.PushMessageDTO;
import io.cruii.pojo.entity.PushConfigDO;
import io.cruii.push.mapper.PushConfigMapper;
import io.cruii.push.pusher.Pusher;
import io.cruii.push.pusher.impl.*;
import io.cruii.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author cruii
 * Created on 2022/4/6
 */
@Service
@Slf4j
public class PushServiceImpl implements PushService {
    private final PushConfigMapper pushConfigMapper;

    public PushServiceImpl(PushConfigMapper pushConfigMapper) {
        this.pushConfigMapper = pushConfigMapper;
    }

    @Override
    public boolean push(PushMessageDTO messageDTO) {
        String dedeuserid = messageDTO.getDedeuserid();
        String content = messageDTO.getContent();
        PushConfigDO pushConfig = pushConfigMapper.selectOne(Wrappers.lambdaQuery(PushConfigDO.class).eq(PushConfigDO::getDedeuserid, dedeuserid));
        if (pushConfig == null) {
            log.info("该账号[{}]未配置推送", dedeuserid);
            return false;
        }
        Pusher pusher = generatePusher(pushConfig);
        if (pusher == null) {
            log.error("生成Pusher异常: {}", dedeuserid);
            return false;
        }
        if (pusher instanceof QyWechatPusher) {
            content = content.replace("\n", "<br>");
        }

        boolean result = pusher.push(content);

        log.info("UserId: {}, Pusher: {}, Result：{}", dedeuserid, pusher.getClass().getSimpleName(), result);

        return result;
    }

    @Override
    public void notifyExpired(List<String> idList) {
        idList.forEach(id -> {
            PushConfigDO pushConfigDO = pushConfigMapper.selectById(id);
            Pusher pusher = generatePusher(pushConfigDO);
            if (pusher == null) {
                log.error("生成Pusher异常: {}", id);
                return;
            }
            pusher.notifyExpired(id);
        });
    }

    private Pusher generatePusher(PushConfigDO pushConfig) {
        if (CharSequenceUtil.isNotBlank(pushConfig.getBarkDeviceKey())) {
            return new BarkPusher(pushConfig.getBarkDeviceKey());
        } else if (!CharSequenceUtil.hasBlank(pushConfig.getCorpId(), pushConfig.getCorpSecret(), pushConfig.getAgentId(), pushConfig.getMediaId())) {
            return new QyWechatPusher(pushConfig.getCorpId(), pushConfig.getCorpSecret(), pushConfig.getAgentId(), pushConfig.getMediaId());
        } else if (!CharSequenceUtil.hasBlank(pushConfig.getTgBotToken(), pushConfig.getTgBotChatId())) {
            return new TelegramBotPusher(pushConfig.getTgBotToken(), pushConfig.getTgBotChatId());
        } else if (CharSequenceUtil.isNotBlank(pushConfig.getScKey())) {
            return new ServerChanPusher(pushConfig.getScKey());
        } else if (CharSequenceUtil.isNotBlank(pushConfig.getFeiShuCustomBot())) {
            return new FeiShuCustomBotPusher(pushConfig.getFeiShuCustomBot());
        }
        return null;
    }
}
