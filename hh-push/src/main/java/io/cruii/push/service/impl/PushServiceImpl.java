package io.cruii.push.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.cruii.pojo.dto.PushConfigDTO;
import io.cruii.pojo.po.PushConfig;
import io.cruii.push.mapper.PushConfigMapper;
import io.cruii.push.pusher.impl.BarkPusher;
import io.cruii.push.pusher.impl.QyWechatPusher;
import io.cruii.push.pusher.impl.ServerChanPusher;
import io.cruii.push.pusher.impl.TelegramBotPusher;
import io.cruii.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author cruii
 * Created on 2022/4/6
 */
@Service
@Slf4j
public class PushServiceImpl implements PushService {
    private final PushConfigMapper pushConfigMapper;
    private final MapperFactory mapperFactory;

    public PushServiceImpl(PushConfigMapper pushConfigMapper,
                           MapperFactory mapperFactory) {
        this.pushConfigMapper = pushConfigMapper;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public boolean push(String dedeuserid, String content) {
        PushConfig pushConfig = pushConfigMapper.selectOne(Wrappers.lambdaQuery(PushConfig.class).eq(PushConfig::getDedeuserid, dedeuserid));
        boolean result = false;
        if (CharSequenceUtil.isNotBlank(pushConfig.getBarkDeviceKey())) {
            BarkPusher barkPusher = new BarkPusher(pushConfig.getBarkDeviceKey());
            result = barkPusher.push(content);
        } else if (!CharSequenceUtil.hasBlank(pushConfig.getCorpId(), pushConfig.getCorpSecret(), pushConfig.getAgentId(), pushConfig.getMediaId())) {
            QyWechatPusher pusher = new QyWechatPusher(pushConfig.getCorpId(), pushConfig.getCorpSecret(), pushConfig.getAgentId(), pushConfig.getMediaId());
            result = pusher.push(content.replace("\n", "<br>"));
        } else if (!CharSequenceUtil.hasBlank(pushConfig.getTgBotToken(), pushConfig.getTgBotChatId())) {
            TelegramBotPusher pusher = new TelegramBotPusher(pushConfig.getTgBotToken(), pushConfig.getTgBotChatId());
            result = pusher.push(content);
        } else if (CharSequenceUtil.isNotBlank(pushConfig.getScKey())) {
            ServerChanPusher pusher = new ServerChanPusher(pushConfig.getScKey());
            result = pusher.push(content);
        } else {
            log.info("该账号未配置推送或推送配置异常");
        }

        log.info("推送结果：{}", result);

        return result;
    }

    @Override
    public void save(PushConfigDTO pushConfigDTO) {
        PushConfig pushConfig = mapperFactory.getMapperFacade().map(pushConfigDTO, PushConfig.class);
        PushConfig exist = pushConfigMapper.selectOne(Wrappers.lambdaQuery(PushConfig.class).eq(PushConfig::getDedeuserid, pushConfig.getDedeuserid()));
        if (Objects.isNull(exist)) {
            pushConfigMapper.insert(pushConfig);
        } else {
            pushConfig.setId(exist.getId());
            pushConfigMapper.updateById(pushConfig);
        }
    }
}
