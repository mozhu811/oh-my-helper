package io.cruii.task;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.cruii.component.BilibiliDelegate;
import io.cruii.context.BilibiliUserContext;
import io.cruii.pojo.po.BilibiliUser;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2023/1/15
 */
@Slf4j
public class BigVipTask extends AbstractTask {
    private final List<String> excludes = Arrays.asList("vipmallbuy", "tvodbuy", "ogvwatch", "vipmallview");

    public BigVipTask(BilibiliDelegate delegate) {
        super(delegate);
    }

    @Override
    public void run() throws Exception {
        // 判断是否是大会员
        BilibiliUser user = BilibiliUserContext.get();
        if (user.getVipType() != 1 && user.getVipType() != 2) {
            log.error("账号非B站大会员，停止执行大会员中心每日任务 ❌");
            return;
        }
        // 列出任务
        JSONObject vipQuestInfo = delegate.vipQuestInfo();
        Map<String, Integer> taskStatus = new HashMap<>(8);
        JSONArray modules = vipQuestInfo.getByPath("data.task_info.modules", JSONArray.class);
        modules.forEach(m -> {
            JSONObject module = (JSONObject) m;
            JSONArray commonTaskItem = module.getJSONArray("common_task_item");
            commonTaskItem.stream()
                    .filter(t -> !excludes.contains(((JSONObject) t).getStr("task_code")))
                    .forEach(t -> {
                        JSONObject task = (JSONObject) t;
                        taskStatus.put(task.getStr("task_code"), task.getInt("state"));
                    });
        });
        // 接任务
        Set<String> codes = taskStatus.entrySet()
                .stream().filter(kv -> kv.getValue() != 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        Iterator<String> iterator = codes.iterator();
        while (iterator.hasNext()) {
            String code = iterator.next();
            String taskTitle = getTaskTitle(code);
            JSONObject body = delegate.receiveVipQuest(code);
            if (body.getInt("code") == 0) {
                log.info("领取任务【{}】成功 ✔️", taskTitle);
            } else {
                log.error("领取任务【{}】失败 ❌", taskTitle);
                iterator.remove();
            }
        }
        // 完成任务
        doTask(codes);
        // 获取当前积分
        vipQuestInfo = delegate.vipQuestInfo();
        Integer vipPoint = vipQuestInfo.getByPath("data.point_info.point", Integer.class);
        log.info("当前大会员积分: {}", vipPoint);
    }

    private void doTask(Set<String> codes) {
        codes.forEach(c -> {
            JSONObject resp = delegate.doBigVipQuest(c);
            String taskTitle = getTaskTitle(c);
            if (resp.getInt(CODE) == 0) {
                log.info("完成任务【{}】成功 ✔️", taskTitle);
            } else {
                log.error("完成任务【{}】失败 ❌", taskTitle);
            }
        });
    }

    private String getTaskTitle(String code) {
        switch (code) {
            case "bonus":
                return "大会员福利大积分";
            case "privilege":
                return "浏览大会员权益页面";
            case "getrights":
                return "领取大会员卡券包权益";
            case "animatetab":
                return "浏览追番频道页10秒";
            case "filmtab":
                return "浏览影视频道页10秒";
            case "vipmallview":
                return "浏览会员购页面10秒";
            case "ogvwatch":
                return "观看任意正片内容";
            case "tvodbuy":
                return "购买单点付费影片";
            case "vipmallbuy":
                return "购买指定会员购商品";
            default:
                return code;
        }
    }

    @Override
    public String getName() {
        return "大会员中心签到";
    }
}
