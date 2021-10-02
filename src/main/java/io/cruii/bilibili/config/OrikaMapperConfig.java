package io.cruii.bilibili.config;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.vo.BilibiliUserVO;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author cruii
 * Created on 2021/9/14
 */
@Configuration
public class OrikaMapperConfig {

    @Bean
    public MapperFactory mapperFactory() {
        DefaultMapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory
                .classMap(BilibiliUser.class, BilibiliUserVO.class)
                .exclude("medals")
                .customize(new CustomMapper<BilibiliUser, BilibiliUserVO>() {
                    @Override
                    public void mapAtoB(BilibiliUser bilibiliUser, BilibiliUserVO bilibiliUserVO, MappingContext context) {
                        String medals = bilibiliUser.getMedals();
                        if (CharSequenceUtil.isBlank(medals)) {
                            bilibiliUserVO.setMedals(JSONUtil.createArray());
                        } else {
                            bilibiliUserVO.setMedals(JSONUtil.parseArray(medals));
                        }
                        super.mapAtoB(bilibiliUser, bilibiliUserVO, context);
                    }

                    @Override
                    public void mapBtoA(BilibiliUserVO bilibiliUserVO, BilibiliUser bilibiliUser, MappingContext context) {
                        JSONArray medals = bilibiliUserVO.getMedals();
                        bilibiliUser.setMedals(medals.toJSONString(0));
                        super.mapBtoA(bilibiliUserVO, bilibiliUser, context);
                    }
                })
                .byDefault()
                .register();
        return mapperFactory;
    }
}
