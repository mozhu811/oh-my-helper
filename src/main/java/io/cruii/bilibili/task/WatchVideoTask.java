package io.cruii.bilibili.task;

import cn.hutool.json.JSONObject;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

/**
 * @author cruii
 * Created on 2021/9/15
 */
@Log4j2
public class WatchVideoTask extends VideoTask {

    public WatchVideoTask(TaskConfig config) {
        super(config);
    }

    @Override
    public void run() {
        JSONObject resp = delegate.getExpRewardStatus();
        // 从热榜中随机选取一个视频
        String bvid = trend.get(random.nextInt(trend.size()));

        if (Boolean.FALSE.equals(resp.getByPath("data.watch", Boolean.class))) {
            playVideo(bvid);
        }

        if (Boolean.FALSE.equals(resp.getByPath("data.share", Boolean.class))) {
            shareVideo(bvid);
        }

        // TODO 测试用
        playVideo(bvid);
        shareVideo(bvid);
    }

    @Override
    public String getName() {
        return "观看分享视频";
    }

    /**
     * 播放视频
     */
    private void playVideo(String bvid) {
        int playedTime = random.nextInt(90) + 10;
        JSONObject resp = delegate.playVideo(bvid, playedTime);
        if (resp.getInt(CODE) == 0) {
            String title = getVideoTitle(bvid);
            log.info("播放视频[{}]成功,已观看至{}秒", title, playedTime);
        } else {
            log.error("播放视频[{}]出错：{}", bvid, resp.getStr(MESSAGE));
        }
    }


    /**
     * 分享视频
     *
     * @param bvid 视频的BVID
     */
    private void shareVideo(String bvid) {
        JSONObject resp = delegate.shareVideo(bvid);
        String title = getVideoTitle(bvid);
        if (resp.getInt(CODE) == 0) {
            log.info("分享视频[{}]成功", title);
        } else {
            log.error("分享视频[{}]失败：{}", title, resp.getStr(MESSAGE));
        }
    }
}
