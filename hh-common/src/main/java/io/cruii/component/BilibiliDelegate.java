package io.cruii.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.cruii.constant.BilibiliAPI;
import io.cruii.exception.BilibiliCookieExpiredException;
import io.cruii.exception.RequestException;
import io.cruii.model.*;
import io.cruii.pojo.entity.TaskConfigDO;
import io.cruii.util.CosUtil;
import io.cruii.util.OkHttpUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import okhttp3.*;
import org.apache.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author cruii
 * Created on 2021/9/14
 */
@Log4j2
public class BilibiliDelegate {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    @Getter
    private final TaskConfigDO config;

    private boolean safeMode = false;

    public BilibiliDelegate(String dedeuserid, String sessdata, String biliJct) {
        this(dedeuserid, sessdata, biliJct, true);
    }

    public BilibiliDelegate(String dedeuserid, String sessdata, String biliJct, boolean safeMode) {
        TaskConfigDO taskConfigDO = new TaskConfigDO();
        taskConfigDO.setDedeuserid(dedeuserid);
        taskConfigDO.setSessdata(sessdata);
        taskConfigDO.setBiliJct(biliJct);
        taskConfigDO.setUserAgent(UA);
        this.config = taskConfigDO;
        this.safeMode = safeMode;
    }

    public BilibiliDelegate(TaskConfigDO config) {
        this.config = config;
        if (CharSequenceUtil.isBlank(config.getUserAgent())) {
            config.setUserAgent(UA);
        }
    }

    /**
     * 获取用户B站导航栏状态信息
     */
    public BiliUser getUserDetails() {
        JSONObject resp = doGet(BilibiliAPI.GET_USER_SPACE_DETAILS_INFO);

        int code = resp.getInt("code");
        if (code != 0) {
            throw new BilibiliCookieExpiredException(config.getDedeuserid());
        }

        return resp.getJSONObject("data").toBean(BiliUser.class);
    }

    /**
     * 获取用户空间信息
     *
     * @param userId B站uid
     */
    public SpaceAccInfo getSpaceAccInfo(String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("mid", userId);
        JSONObject resp = doGet(BilibiliAPI.GET_USER_SPACE_INFO, params);
        Integer code = resp.getInt("code");
        if (code != 0) {
            log.error("用户[{}]信息获取异常", userId);
            throw new RuntimeException("获取用户空间信息异常: " + code);
        }
        return resp.getJSONObject("data").toBean(SpaceAccInfo.class, true);
    }

    /**
     * 获取当前等级信息
     *
     * @return 当前等级信息
     */
    public JSONObject getLevelInfo() {
        JSONObject resp = doGet(BilibiliAPI.GET_USER_INFO_NAV);
        JSONObject baseInfo = resp.getJSONObject("data");
        if (resp.getInt("code") == -404 || baseInfo == null) {
            log.error("用户[{}]不存在", config.getDedeuserid());
            return null;
        }
        return baseInfo.getJSONObject("level_info");
    }

    /**
     * 获取勋章墙
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public MedalWall getMedalWall() {
        Map<String, String> params = new HashMap<>();
        params.put("target_id", config.getDedeuserid());
        try {
            JSONObject resp = doGet(BilibiliAPI.GET_MEDAL_WALL, params);
            if (resp.getInt("code") == 0) {
                return resp.getJSONObject("data").toBean(MedalWall.class);
            }
        } catch (Exception e) {
            log.error("获取勋章墙异常", e);
        }
        return null;
    }

    /**
     * 检查Cookie有效性
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject checkCookie() {
        return doGet(BilibiliAPI.GET_USER_INFO_NAV, null);
    }

    /**
     * 查询硬币变化情况
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public BiliCoinLog getCoinChangeLog() {
        JSONObject resp = doGet(BilibiliAPI.GET_COIN_CHANGE_LOG);
        if (resp.getInt("code") != 0) {
            throw new RuntimeException("获取硬币日志出现错误: " + resp.getStr("msg"));
        }
        return resp.getJSONObject("data").toBean(BiliCoinLog.class);
    }

    /**
     * 查询每日奖励状态
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public BiliDailyReward getExpRewardStatus() {
        Map<String, String> params = new HashMap<>();
        params.put("csrf", config.getBiliJct());

        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add(HttpHeaders.REFERER, "https://account.bilibili.com/")
                .add("Origin", "https://account.bilibili.com/");
        JSONObject resp = doGet(BilibiliAPI.GET_EXP_REWARD_STATUS, params, headerBuilder.build());
        if (resp.getInt("code") != 0) {
            throw new RuntimeException(resp.getStr("message"));
        }
        return resp.getJSONObject("data").toBean(BiliDailyReward.class);
    }

    /**
     * 获取已关注的UP最近发布的视频的BVID
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getFollowedUpPostVideo() {
        Map<String, String> params = new HashMap<>();
        params.put("uid", config.getDedeuserid());
        params.put("type_list", "8");
        params.put("from", "");
        params.put("platform", "web");

        return doGet(BilibiliAPI.GET_FOLLOWED_UP_POST_VIDEO, params);
    }

    /**
     * 根据分区ID获取3日热榜视频
     *
     * @param regionId 分区ID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getTrendVideo(String regionId) {
        Map<String, String> params = new HashMap<>();
        params.put("rid", regionId);
        params.put("day", "3");

        return doGet(BilibiliAPI.GET_TREND_VIDEO, params);
    }

    /**
     * 观看视频
     *
     * @param vid        视频的id
     * @param playedTime 播放时长，秒
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject playVideo(String vid, int playedTime) {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("mid", config.getDedeuserid())
                .add("type", "4")
                .add("sub_type", "1")
                .add("play_type", "2")
                .add("played_time", String.valueOf(playedTime))
                .add("real_played_time", String.valueOf(playedTime))
                .add("csrf", config.getBiliJct());

        if (CharSequenceUtil.isNumeric(vid)) {
            fbb.add("aid", vid);
        } else {
            fbb.add("bvid", vid);
        }
        return doPost(BilibiliAPI.REPORT_HEARTBEAT, fbb.build());
    }

    //public JSONObject reportHistory(String bvid, int playedTime) {
    //    Map<String, String> params = new HashMap<>();
    //    params.put("bvid", bvid);
    //    params.put("played_time", String.valueOf(playedTime));
    //}

    /**
     * 分享视频
     *
     * @param bvid 视频的BVID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject shareVideo(String bvid) {
        FormBody requestBody = new FormBody.Builder()
                .add("bvid", bvid)
                .add("csrf", config.getBiliJct()).build();

        return doPost(BilibiliAPI.SHARE_VIDEO, requestBody);
    }


    /**
     * 漫画签到
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject mangaCheckIn(String platform) {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("platform", platform);
        Headers.Builder headerBuilder = new Headers.Builder()
                .add("Origin", "https://manga.bilibili.com");
        return doPost(BilibiliAPI.MANGA_SIGN, headerBuilder.build(), fbb.build());
    }

    /**
     * 获取今日通过投币获得的经验值
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public int getCoinExpToday() {
        JSONObject resp = doGet(BilibiliAPI.GET_COIN_EXP_TODAY);
        return resp.getInt("data", 0);
    }

    /**
     * 获取视频详细信息
     *
     * @param vid 视频ID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getVideoDetails(String vid) {
        Map<String, String> params = new HashMap<>();
        if (CharSequenceUtil.isNumeric(vid)) {
            params.put("aid", vid);
        } else {
            params.put("bvid", vid);
        }
        return doGet(BilibiliAPI.GET_VIDEO_DETAILS, params);
    }

    /**
     * 获取硬币账户余额
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getCoin() {
        return doGet(BilibiliAPI.GET_COIN);
    }


    /**
     * 判断视频是否被投币
     *
     * @param bvid 视频的bvid
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject checkDonateCoin(String bvid) {
        Map<String, String> params = new HashMap<>();
        params.put("bvid", bvid);
        return doGet(BilibiliAPI.CHECK_DONATE_COIN, params);
    }

    /**
     * 投币
     *
     * @param bvid    视频的bvid
     * @param numCoin 投币数
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject donateCoin(String bvid, int numCoin, int isLike) {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("bvid", bvid);
        fbb.add("multiply", String.valueOf(numCoin));
        fbb.add("select_like", String.valueOf(isLike));
        fbb.add("cross_domain", "true");
        fbb.add("csrf", config.getBiliJct());

        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("Referer", "https://www.bilibili.com/video/" + bvid)
                .add("Origin", "https://www.bilibili.com");
        return doPost(BilibiliAPI.DONATE_COIN, headerBuilder.build(), fbb.build());
    }

    /**
     * 获取直播间钱包
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getLiveWallet() {
        return doGet(BilibiliAPI.BILI_LIVE_WALLET);
    }

    /**
     * 银瓜子兑换硬币
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject silver2Coin() {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("csrf_token", config.getBiliJct());
        fbb.add("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.SILVER_2_COIN, fbb.build());
    }

    /**
     * 直播签到
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject liveCheckIn() {
        return doGet(BilibiliAPI.BILI_LIVE_CHECK_IN);
    }

    /**
     * 获取背包中的礼物
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject listGifts() {
        return doGet(BilibiliAPI.LIST_GIFTS);
    }

    /**
     * 获取直播间信息
     *
     * @param userId 主播id
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getLiveRoomInfo(String userId) {
        Map<String, String> params = new HashMap<>();
        params.put("mid", userId);

        return doGet(BilibiliAPI.GET_LIVE_ROOM_INFO, params);
    }

    /**
     * 赠送直播间礼物
     *
     * @param userId  主播的uid
     * @param roomId  主播的房间id
     * @param bagId   背包id
     * @param giftId  礼物id
     * @param giftNum 礼物数量
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject donateGift(String userId, Integer roomId,
                                 String bagId, String giftId, int giftNum) {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("biz_id", String.valueOf(roomId));
        fbb.add("ruid", userId);
        fbb.add("gift_id", giftId);
        fbb.add("bag_id", bagId);
        fbb.add("gift_num", String.valueOf(giftNum));
        fbb.add("uid", config.getDedeuserid());
        fbb.add("csrf", config.getBiliJct());
        fbb.add("send_ruid", "0");
        fbb.add("storm_beat_id", "0");
        fbb.add("price", "0");
        fbb.add("platform", "pc");
        fbb.add("biz_code", "live");

        return doPost(BilibiliAPI.SEND_GIFT, fbb.build());
    }

    /**
     * 获取充电信息
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getChargeInfo() {
        Map<String, String> params = new HashMap<>();
        params.put("mid", config.getDedeuserid());

        return doGet(BilibiliAPI.GET_CHARGE_INFO, params);
    }

    /**
     * 充电
     *
     * @param couponBalance B币券金额
     * @param upUserId      充电对象的userId
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject doCharge(int couponBalance, String upUserId) {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("bp_num", String.valueOf(couponBalance));
        fbb.add("is_bp_remains_prior", "true");
        fbb.add("up_mid", upUserId);
        fbb.add("otype", "up");
        fbb.add("oid", config.getDedeuserid());
        fbb.add("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.CHARGE, fbb.build());
    }

    /**
     * 提交充电留言
     *
     * @param orderNo 充电订单号
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject doChargeComment(String orderNo) {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("order_id", orderNo);
        fbb.add("message", "up的作品很棒");
        fbb.add("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.COMMIT_CHARGE_COMMENT, fbb.build());
    }

    /**
     * 获取大会员漫画权益
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getMangaVipReward() {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("reason_id", "1");

        Headers.Builder headerBuilder = new Headers.Builder()
                .add("Origin", "https://manga.bilibili.com");
        return doPost(BilibiliAPI.GET_MANGA_VIP_REWARD, headerBuilder.build(), fbb.build());
    }

    /**
     * 领取大会员权益
     *
     * @param type 1 = B币券  2 = 会员购优惠券
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getVipReward(int type) {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("type", String.valueOf(type));
        fbb.add("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.GET_VIP_REWARD, fbb.build());
    }

    /**
     * 阅读漫画
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject readManga() {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("device", "pc");
        fbb.add("platform", "web");
        fbb.add("comic_id", "26009");
        fbb.add("ep_id", "300318");

        Headers.Builder headerBuilder = new Headers.Builder()
                .add("Origin", "https://manga.bilibili.com");
        return doPost(BilibiliAPI.READ_MANGA, headerBuilder.build(), fbb.build());
    }

    public JSONObject followUser(String uid) {
        FormBody.Builder fbb = new FormBody.Builder();
        fbb.add("fid", uid);
        fbb.add("act", "1");
        fbb.add("re_src", "11");
        fbb.add("csrf", config.getBiliJct());

        return doPost(BilibiliAPI.RELATION_MODIFY, fbb.build());
    }

    public JSONObject receiveVipQuest(String code) {

        Map<String, String> params = new HashMap<>();
        params.put("taskCode", code);

        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add(HttpHeaders.CONTENT_TYPE, "application/json")
                .add(HttpHeaders.REFERER, "https://big.bilibili.com/mobile/bigPoint/task")
                .add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.158 Mobile Safari/537.36 os/android model/MuMu build/6720300 osVer/6.0.1 sdkInt/23 network/2 BiliApp/6720300 mobi_app/android channel/html5_search_baidu Buvid/XZFC135F5263B6897C8A4BE7AEB125BBF10F8 sessionID/72d3f4c9 innerVer/6720310 c_locale/zh_CN s_locale/zh_CN disable_rcmd/0 6.72.0 os/android model/MuMu mobi_app/android build/6720300 channel/html5_search_baidu innerVer/6720310 osVer/6.0.1 network/2" +
                        "Accept: application/json; q=0.001, application/xml; q=0.001")
                .add("Origin", "https://www.bilibili.com");

        RequestBody requestBody = RequestBody.create(JSONUtil.toJsonStr(params).getBytes(StandardCharsets.UTF_8),
                MediaType.parse("application/json"));
        return doPost(BilibiliAPI.VIP_QUEST_RECEIVE, headerBuilder.build(), requestBody);
    }

    public JSONObject vipQuestInfo() {
        return doGet(BilibiliAPI.VIP_QUEST_INFO);
    }

    public JSONObject checkIn() {
        return null;
    }

    private final Set<String> dailyQuests = Set.of("jp_channel", "tv_channel",
            "vipmallview", "ogvwatch", "tvodbuy", "vipmallbuy");

    public JSONObject doBigVipQuest(String code) {

        Headers headers = new Headers.Builder()
                .add(HttpHeaders.REFERER, "https://big.bilibili.com/mobile/bigPoint/task")
                .add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.158 Mobile Safari/537.36 os/android model/MuMu build/6720300 osVer/6.0.1 sdkInt/23 network/2 BiliApp/6720300 mobi_app/android channel/html5_search_baidu Buvid/XZFC135F5263B6897C8A4BE7AEB125BBF10F8 sessionID/72d3f4c9 innerVer/6720310 c_locale/zh_CN s_locale/zh_CN disable_rcmd/0 6.72.0 os/android model/MuMu mobi_app/android build/6720300 channel/html5_search_baidu innerVer/6720310 osVer/6.0.1 network/2").build();

        if (dailyQuests.contains(code)) {
            JSONObject statistics = new JSONObject();
            statistics.set("appId", 1)
                    .set("platform", 3)
                    .set("version", "6.85.0")
                    .set("abtest", "");
            FormBody requestBody = new FormBody.Builder()
                    .add("position", code)
                    .add("c_locale", "zh_CN")
                    .add("channel", "html5_search_baidu")
                    .add("disable_rcmd", "0")
                    .add("mobi_app", "android")
                    .add("platform", "android")
                    .add("s_locale", "zh_CN")
                    .add("statistics", statistics.toJSONString(0))
                    .build();

            return doPost(BilibiliAPI.VIP_QUEST_VIEW_COMPLETE, headers, requestBody);
        }
        Map<String, String> params = new HashMap<>();
        params.put("taskCode", code);

        RequestBody requestBody = RequestBody.create(JSONUtil.toJsonStr(params).getBytes(StandardCharsets.UTF_8),
                MediaType.parse("application/json"));
        return doPost(BilibiliAPI.VIP_QUEST_COMPLETE, headers, requestBody);
    }


    public JSONObject doVipSign() {
        Headers headers = new Headers.Builder()
                .add(HttpHeaders.REFERER, "https://big.bilibili.com/mobile/bigPoint/task")
                .add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.158 Mobile Safari/537.36 os/android model/MuMu build/6720300 osVer/6.0.1 sdkInt/23 network/2 BiliApp/6720300 mobi_app/android channel/html5_search_baidu Buvid/XZFC135F5263B6897C8A4BE7AEB125BBF10F8 sessionID/72d3f4c9 innerVer/6720310 c_locale/zh_CN s_locale/zh_CN disable_rcmd/0 6.72.0 os/android model/MuMu mobi_app/android build/6720300 channel/html5_search_baidu innerVer/6720310 osVer/6.0.1 network/2").build();

        return doPost(BilibiliAPI.VIP_SIGN, headers, RequestBody.create(new byte[0]));
    }
    /**
     * 获取B站用户头像文件流
     *
     * @param avatarUrl 头像地址
     * @return 头像文件流
     */
    private byte[] getAvatarStream(String avatarUrl) {
        Request.Builder builder = new Request.Builder()
                .url(avatarUrl)
                .get();
        try (Response response = OkHttpUtil.executeWithRetry(builder.build())) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().bytes();
            }
        } catch (IOException e) {
            log.error("获取头像文件流失败", e);
        }
        throw new RuntimeException("获取头像文件流失败");
    }

    /**
     * 匿名处理
     *
     * @param username B站用户名
     * @return 使用*号覆盖后的用户名
     */
    @Deprecated
    private String coverUsername(String username) {
        StringBuilder sb = new StringBuilder();

        if (username.length() > 2) {
            // 当用户名长度大于2时，使中间的字符被*号覆盖
            for (int i = 0; i < username.length(); i++) {
                if (i > 0 && i < username.length() - 1) {
                    sb.append("*");
                } else {
                    sb.append(username.charAt(i));
                }
            }
        } else {
            // 当用户名长度不大于2时，只覆盖最后一个字符
            sb.append(username.charAt(0)).append("*");
        }

        return sb.toString();
    }

    private JSONObject doGet(String url) {
        return doGet(url, MapUtil.empty(), null);
    }

    private JSONObject doGet(String url, Map<String, String> params) {
        return doGet(url, params, null);
    }

    /**
     * 实际处理B站API访问
     *
     * @param url    访问API地址
     * @param params 查询字符串参数 {@link MultiValueMap}
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    private JSONObject doGet(String url, Map<String, String> params, Headers headers) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        assert httpUrl != null;
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        Request request = buildRequest(headers, urlBuilder, params, "GET");
        return call(request);
    }
    private JSONObject doPost(String url, RequestBody requestBody) {
        return doPost(url, null, null, requestBody);
    }

    private JSONObject doPost(String url, Headers headers, RequestBody requestBody) {
        return doPost(url, null, headers, requestBody);
    }

    private JSONObject doPost(String url, Map<String, String> params, Headers headers, RequestBody requestBody) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        assert httpUrl != null;
        HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
        Request request = buildRequest(headers, urlBuilder, params, requestBody, "POST");
        return call(request);
    }


    private JSONObject call(Request request) {
        String uri = request.url().uri().toString();
        if (safeMode) {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RequestException(uri, e);
            }
        }

        try (Response response = OkHttpUtil.executeWithRetry(request, safeMode)) {
            if (response.body() != null) {
                String body = response.body().string();
                return JSONUtil.parseObj(body);
            }
            log.error("请求API[{}]失败", uri);
            throw new RuntimeException("请求失败:" + response.code());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上传头像
     *
     * @param avatar 头像文件
     */
    private void uploadAvatar(byte[] avatar) {
        String path = "avatars" + File.separator + config.getDedeuserid() + ".png";
        try {
            File avatarFile = new File(path);
            if (!avatarFile.getParentFile().exists()) {
                FileUtil.mkParentDirs(avatarFile);
            }
            if (avatar != null) {
                if (avatarFile.exists()) {
                    String localMd5 = SecureUtil.md5().digestHex(avatarFile);
                    String remoteMd5 = SecureUtil.md5().digestHex(avatar);
                    if (!localMd5.equals(remoteMd5)) {
                        FileUtil.writeBytes(avatar, avatarFile);
                    }
                } else {
                    FileUtil.writeBytes(avatar, avatarFile);
                }
                // 上传到 oss
                CosUtil.upload(avatarFile);
            }
        } catch (Exception e) {
            log.error("获取头像失败", e);
        } finally {
            FileUtil.del(path);
        }
    }

    private Request buildRequest(Headers headers,
                                 HttpUrl.Builder urlBuilder,
                                 Map<String, String> queryParams,
                                 String method) {
        return buildRequest(headers, urlBuilder, queryParams, null, method);
    }

    private Request buildRequest(Headers headers,
                                 HttpUrl.Builder urlBuilder,
                                 Map<String, String> queryParams,
                                 RequestBody requestBody,
                                 String method) {
        Request.Builder requestBuilder = new Request.Builder()
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36")
                .header(HttpHeaders.REFERER, "https://www.bilibili.com/")
                //.header("Origin", "https://www.bilibili.com/")
                .header("Cookie", "bili_jct=" + config.getBiliJct() +
                        ";SESSDATA=" + config.getSessdata() +
                        ";DedeUserID=" + config.getDedeuserid() +
                        ";buvid3=helper-hub" +
                        ";innersign=0" + ";");
        if (headers != null) {
            headers.forEach(header -> requestBuilder.header(header.getFirst(), header.getSecond()));
        }

        if (queryParams != null) {
            for (Map.Entry<String, String> param : queryParams.entrySet()) {
                urlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }

        return requestBuilder.url(urlBuilder.build()).method(method, requestBody).build();
    }
}
