package io.cruii.task;

import cn.hutool.json.JSONObject;
import io.cruii.component.BilibiliDelegate;
import io.cruii.context.BilibiliUserContext;
import io.cruii.pojo.po.BilibiliUser;
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
        BilibiliUser user = BilibiliUserContext.get();
        int current = delegate.getExp();
        user.setCurrentExp(current);
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
            if (user.getLevel() < 6) {
                int diff = user.getNextExp() - user.getCurrentExp();

                int days = (diff / exp) + 1;
                user.setUpgradeDays(days);
                if (diff <= exp) {
                    user.setUpgradeDays(null);
                }
            } else {
                user.setUpgradeDays(null);
            }

            if (user.getLevel() < 6) {
                int upgradeDays = (user.getNextExp() - user.getCurrentExp()) / exp;
                log.info("按照当前进度，升级到Lv{}还需要: {}天", user.getLevel() + 1, upgradeDays + 1);
            } else {
                log.info("当前等级Lv6，经验值为：{}", user.getCurrentExp());
            }
        } else {
            log.error("经验值为0，无法计算升级天数");
        }

        BilibiliUserContext.set(user);
    }

    @Override
    public String getName() {
        return "任务总结";
    }
}
