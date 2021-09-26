package io.cruii.bilibili.service.impl;

import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.repository.BilibiliUserRepository;
import io.cruii.bilibili.service.BilibiliUserService;
import io.cruii.bilibili.vo.BilibiliUserVO;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/22
 */
@Service
public class BilibiliUserServiceImpl implements BilibiliUserService {
    private final BilibiliUserRepository bilibiliUserRepository;
    private final MapperFactory mapperFactory;

    public BilibiliUserServiceImpl(BilibiliUserRepository bilibiliUserRepository,
                                   MapperFactory mapperFactory) {
        this.bilibiliUserRepository = bilibiliUserRepository;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public void save(String dedeuserid, String sessdata, String biliJct) {

        TaskConfig config = new TaskConfig();
        config.setDedeuserid(dedeuserid);
        config.setSessdata(sessdata);
        config.setBiliJct(biliJct);

        BilibiliDelegate delegate = new BilibiliDelegate(config);
        BilibiliUser user = delegate.getUser();
        bilibiliUserRepository.save(user);
    }

    @Override
    public boolean isExist(String dedeuserid) {
        return bilibiliUserRepository.findById(dedeuserid).isPresent();
    }

    @Override
    public List<BilibiliUserVO> list() {
        return bilibiliUserRepository.findAll()
                .stream()
                .map(user -> mapperFactory.getMapperFacade().map(user, BilibiliUserVO.class))
                .collect(Collectors.toList());
    }
}
