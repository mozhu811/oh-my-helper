package io.cruii.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.model.BiliUser;
import io.cruii.pojo.entity.BiliTaskUserDO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2023/2/15
 */
public class BiliUserParser {
    private BiliUserParser() {
    }

    public static BiliTaskUserDO parseUser(BiliUser biliUser, JSON medalWallInfo) {
        // 将json对象转化为JavaBean
        //BiliTaskUserDO biliUserDO = new BiliTaskUserDO();
        //biliUserDO.setDedeuserid(String.valueOf(biliUser.getMid()))
        //        .setUsername(biliUser.getName())
        //        .setCoins(String.valueOf(biliUser.getCoins()))
        //        .setLevel(biliUser.getLevel())
        //        .setCurrentExp(biliUser.getLevelExp().getCurrentExp())
        //        .setNextExp(biliUser.getLevel() == 6 ? 0 : biliUser.getLevelExp().getNextExp())
        //        .setSign(CharSequenceUtil.isBlank(biliUser.getSign().trim()) ?
        //                "这个人非常懒，什么也没有写~\\(≧▽≦)/~" : biliUser.getSign())
        //        .setVipType(biliUser.getVip().getType())
        //        .setVipStatus(biliUser.getVip().getStatus())
        //        .setIsLogin(true);

        //List<JSONObject> medals = new ArrayList<>();
        //if (medalWallInfo instanceof JSONArray){
        //    JSONArray medalWall = (JSONArray) medalWallInfo;
        //    medals = medalWall.stream()
        //            .map(JSONUtil::parseObj)
        //            .map(medalObj -> {
        //                JSONObject medal = JSONUtil.createObj();
        //                medal.set("name", medalObj.getByPath("medal_info.medal_name", String.class));
        //                medal.set("level", medalObj.getByPath("medal_info.level", Integer.class));
        //                medal.set("colorStart", medalObj.getByPath("medal_info.medal_color_start", Integer.class));
        //                medal.set("colorEnd", medalObj.getByPath("medal_info.medal_color_end", Integer.class));
        //                medal.set("colorBorder", medalObj.getByPath("medal_info.medal_color_border", Integer.class));
        //                return medal;
        //            })
        //            .sorted((o1, o2) -> o2.getInt("level") - o1.getInt("level"))
        //            .limit(3L)
        //            .collect(Collectors.toList());
        //} else if (medalWallInfo instanceof JSONObject) {
        //
        //        JSONObject medal = JSONUtil.createObj();
        //        medal.set("name", userSpaceInfo.getByPath("fans_medal.medal.medal_name", String.class));
        //        medal.set("level", userSpaceInfo.getByPath("fans_medal.medal.level", Integer.class));
        //        medal.set("colorStart", userSpaceInfo.getByPath("fans_medal.medal.medal_color_start", Integer.class));
        //        medal.set("colorEnd", userSpaceInfo.getByPath("fans_medal.medal.medal_color_end", Integer.class));
        //        medal.set("colorBorder", userSpaceInfo.getByPath("fans_medal.medal.medal_color_border", Integer.class));
        //
        //        medals.add(medal);
        //}
        //biliUserDO.setMedals(JSONUtil.toJsonStr(medals));
        return null;
    }
}
