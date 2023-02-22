package io.cruii.task;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.cruii.component.BilibiliDelegate;
import io.cruii.model.BiliCoinLog;
import lombok.extern.log4j.Log4j2;

/**
 * 查询硬币变化情况
 *
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class GetCoinChangeLogTask extends AbstractTask {

    public GetCoinChangeLogTask(BilibiliDelegate delegate) {
        super(delegate);
    }

    @Override
    public void run() {
        BiliCoinLog coinChangeLog = delegate.getCoinChangeLog();
        log.info("最近一周共产生{}条变更日志", coinChangeLog.getCount());

        double in = 0.0;
        double out = 0.0;

        for (BiliCoinLog.CoinLog r : coinChangeLog.getList()) {
            if (r.getDelta() > 0) {
                in += r.getDelta();
            } else {
                out += r.getDelta();
            }
        }

        log.info("最近一周收入{}个硬币", in);
        log.info("最近一周支出{}个硬币", out);
    }

    @Override
    public String getName() {
        return "硬币变更日志";
    }
}
