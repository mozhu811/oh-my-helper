package io.cruii.task;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.component.BilibiliDelegate;
import io.cruii.context.BilibiliUserContext;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cruii
 * Created on 2023/2/8
 */
@Slf4j
public class CalExpTask extends AbstractTask {
    public CalExpTask(BilibiliDelegate delegate) {
        super(delegate);
    }

    @Override
    public void run() throws Exception {
        //JSONObject user = BilibiliUserContext.get();
        JSONObject user = JSONUtil.createObj();
        JSONObject levelInfo = delegate.getLevelInfo();
        int exp = 0;
        // 获取当日获取的经验
        JSONObject expRewardStatus = delegate.getExpRewardStatus();
        JSONObject body = expRewardStatus.getJSONObject("data");
        Boolean share = body.getBool("share", false);
        Boolean watch = body.getBool("watch", false);
        Boolean login = body.getBool("login", false);
        JSONObject coinExpToday = delegate.getCoinExpToday();
        Integer coinExp = coinExpToday.getInt("data", 0);

        if (Boolean.TRUE.equals(share)) {
            exp += 5;
        }

        if (Boolean.TRUE.equals(watch)) {
            exp += 5;
        }

        if (Boolean.TRUE.equals(login)) {
            exp += 5;
        }

        exp += coinExp;
        log.info("今日已获得[{}]点经验", exp);
        if (exp > 0) {
            Integer currentLevel = levelInfo.getInt("current_level");
            if (currentLevel < 6) {
                int diff = levelInfo.getInt("next_exp") - levelInfo.getInt("current_exp");

                int days = (diff / exp) + 1;
                user.set("upgradeDays", days);
                if (diff <= exp) {
                    user.set("upgradeDays", null);
                }
                log.info("按照当前进度，升级到Lv{}还需要: {}天", currentLevel + 1, days + 1);
            } else {
                user.set("upgradeDays", null);
                log.info("当前等级Lv6，经验值为：{}", levelInfo.getInt("current_exp"));
            }

        } else {
            log.error("经验值为0，无法计算升级天数");
        }

    }

    @Override
    public String getName() {
        return "任务总结";
    }
}
