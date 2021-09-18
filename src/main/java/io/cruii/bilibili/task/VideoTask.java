package io.cruii.bilibili.task;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.cruii.bilibili.entity.TaskConfig;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author cruii
 * Created on 2021/9/16
 */
@Log4j2
public abstract class VideoTask extends AbstractTask {
    /**
     * 1.   动画
     * 13.  番剧（不使用该分区）
     * 167. 国创（不使用该分区）
     * 3.   音乐（不使用该分区）
     * 129. 舞蹈
     * 4.   游戏
     * 36.  知识
     * 188. 科技
     * 234. 运动
     * 223. 汽车
     * 160. 生活
     * 211. 美食
     * 217. 动物圈
     * 119. 鬼畜
     * 155. 时尚（不使用该分区）
     * 202. 资讯（无排名）
     * 165. 广告（已下线）
     * 5.   娱乐（不使用该分区）
     * 181. 影视
     * 177. 纪录片（不使用该分区）
     * 23.  电影（不使用该分区）
     * 11.  电视剧（不使用该分区）
     */
    private final int[] regionIds = {1, 129, 4, 36, 188, 234, 223, 160, 211, 217, 119, 181};

    public final Random random = new Random();

    public final List<String> follow = new ArrayList<>();

    public final List<String> trend = new ArrayList<>();

    VideoTask(TaskConfig config) {
        super(config);
        initFollowList();
        initTrend();
    }

    /**
     * 初始化已关注UP主最近发布视频的BVID
     */
    private void initFollowList() {
        JSONObject resp = delegate.getFollowedUpPostVideo();
        JSONArray videos = resp.getJSONObject("data").getJSONArray("cards");
        if (videos == null || videos.isEmpty()) {
            return;
        }
        for (Object video : videos) {
            String bvid = ((JSONObject) video).getByPath("desc.bvid", String.class);
            follow.add(bvid);
        }
    }

    /**
     * 初始化热门视频的BVID
     */
    private void initTrend() {
        int index = random.nextInt(regionIds.length);
        String regionId = String.valueOf(regionIds[index]);
        JSONObject resp = delegate.getTrendVideo(regionId);
        JSONArray videos = resp.getJSONArray("data");
        for (Object video : videos) {
            String bvid = ((JSONObject) video).getStr("bvid");
            trend.add(bvid);
        }
    }

    /**
     * 获取视频标题
     *
     * @param bvid 视频的BVID
     * @return 视频标题
     */
    public String getVideoTitle(String bvid) {
        String title;
        JSONObject detailsResp = delegate.getVideoDetails(bvid);
        if (detailsResp.getInt(CODE) == 0) {
            title = detailsResp.getByPath("data.owner.name", String.class)
                    + "：" + detailsResp.getByPath("data.title", String.class);
        } else {
            title = "？？？？？？";
            log.error("获取[{}]的视频标题出错：{}", bvid, detailsResp.getStr(MESSAGE));
        }

        return title;
    }
}
