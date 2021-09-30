package io.cruii.bilibili.service.impl;

import io.cruii.bilibili.component.BilibiliDelegate;
import io.cruii.bilibili.dto.BilibiliUserDTO;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.repository.BilibiliUserRepository;
import io.cruii.bilibili.repository.TaskConfigRepository;
import io.cruii.bilibili.service.BilibiliUserService;
import io.cruii.bilibili.vo.BilibiliUserVO;
import lombok.extern.log4j.Log4j2;
import ma.glasnost.orika.MapperFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/22
 */
@Service
@Log4j2
public class BilibiliUserServiceImpl implements BilibiliUserService {
    private final BilibiliUserRepository bilibiliUserRepository;
    private final MapperFactory mapperFactory;

    public BilibiliUserServiceImpl(BilibiliUserRepository bilibiliUserRepository,
                                   MapperFactory mapperFactory,
                                   TaskConfigRepository taskConfigRepository) {
        this.bilibiliUserRepository = bilibiliUserRepository;
        this.mapperFactory = mapperFactory;
    }

    @Override
    public void saveAndUpdate(String dedeuserid, String sessdata, String biliJct) {

        TaskConfig config = new TaskConfig();
        config.setDedeuserid(dedeuserid);
        config.setSessdata(sessdata);
        config.setBiliJct(biliJct);

        BilibiliDelegate delegate = new BilibiliDelegate(config);
        BilibiliUser user = delegate.getUser();
        bilibiliUserRepository.save(user);
    }

    @Override
    public void saveAndUpdate(BilibiliUserDTO user) {
        BilibiliUser bilibiliUser = mapperFactory.getMapperFacade().map(user, BilibiliUser.class);
        bilibiliUserRepository.save(bilibiliUser);
    }

    @Override
    public boolean isExist(String dedeuserid) {
        return bilibiliUserRepository.findById(dedeuserid).isPresent();
    }

    @Override
    public List<BilibiliUserVO> list(HttpServletRequest request) {
        return bilibiliUserRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(BilibiliUser::getCurrentExp).reversed())
                .map(user -> {
                    BilibiliUserVO userVO = mapperFactory.getMapperFacade().map(user, BilibiliUserVO.class);
                    if (user.getLevel() < 6) {
                        userVO.setDiffExp(user.getNextExp() - user.getCurrentExp());
                    }

                    String host = request.getRequestURL().toString().split(request.getRequestURI())[0];
                    String avatarPath = UriComponentsBuilder.fromHttpUrl(host)
                            .path("/avatars/{id}.png").build(userVO.getDedeuserid()).toString();
                    userVO.setAvatar(avatarPath);
                    return userVO;
                })
                .collect(Collectors.toList());
    }
}
