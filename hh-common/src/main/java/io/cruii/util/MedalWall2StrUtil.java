package io.cruii.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.model.MedalWall;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2023/2/21
 */
public class MedalWall2StrUtil {
    private MedalWall2StrUtil() {
    }

    public static String medalWall2JsonStr(MedalWall medalWall) {
        List<MedalWall.Medal> medals = medalWall.getList();
        return JSONUtil.toJsonStr(
                medals.stream()
                        .map(MedalWall.Medal::getMedalInfo)
                        .sorted((m1, m2) -> m2.getLevel() - m1.getLevel())
                        .limit(3L)
                        .map(mi -> {
                            JSONObject obj = JSONUtil.createObj();
                            obj.set("name", mi.getMedalName())
                                    .set("level", mi.getLevel())
                                    .set("colorStart", mi.getMedalColorStart())
                                    .set("colorEnd", mi.getMedalColorEnd())
                                    .set("colorBorder", mi.getMedalColorBorder());
                            return obj;
                        }).collect(Collectors.toList())
        );
    }
}
