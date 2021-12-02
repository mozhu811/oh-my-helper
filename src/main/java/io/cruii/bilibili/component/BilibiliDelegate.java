package io.cruii.bilibili.component;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.rholder.retry.*;
import io.cruii.bilibili.constant.BilibiliAPI;
import io.cruii.bilibili.entity.BilibiliUser;
import io.cruii.bilibili.entity.TaskConfig;
import io.cruii.bilibili.util.CosUtil;
import io.cruii.bilibili.util.ProxyUtil;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/9/14
 */
@Log4j2
public class BilibiliDelegate {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    @Getter
    private final TaskConfig config;

    @Getter
    private HttpRequest httpRequest;

    private String proxyHost;
    private Integer proxyPort;

    private final Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
            .retryIfExceptionOfType(HttpException.class)
            .retryIfExceptionOfType(IORuntimeException.class)
            .withStopStrategy(StopStrategies.stopAfterAttempt(12))
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(Attempt<V> attempt) {
                    if (attempt.hasException()) {
                        log.error("第{}次调用失败: {}, 进行重试", attempt.getAttemptNumber(), attempt.getExceptionCause().getMessage());
                    }
                }
            })
            .build();

    public BilibiliDelegate(String dedeuserid, String sessdata, String biliJct) {
        TaskConfig taskConfig = new TaskConfig();
        taskConfig.setDedeuserid(dedeuserid);
        taskConfig.setSessdata(sessdata);
        taskConfig.setBiliJct(biliJct);
        taskConfig.setUserAgent(UA);
        this.config = taskConfig;
    }

    public BilibiliDelegate(TaskConfig config) {
        this.config = config;
        if (CharSequenceUtil.isBlank(config.getUserAgent())) {
            config.setUserAgent(UA);
        }
        changeProxy();
    }

    public void changeProxy() {
        String proxy = ProxyUtil.get();

        setProxy(proxy);
    }

    private void setProxy(String proxy) {
        this.proxyHost = proxy.split(":")[0];
        this.proxyPort = Integer.parseInt(proxy.split(":")[1]);
    }

    /**
     * 获取用户B站导航栏状态信息
     *
     * @return B站用户信息 {@link BilibiliUser}
     */
    public BilibiliUser getUser() {
        JSONObject resp = doGet(BilibiliAPI.GET_USER_INFO_NAV);

        // 解析响应信息
        JSONObject data = resp.getJSONObject("data");
        // 是否登录成功
        Boolean isLogin = data.getBool("isLogin");

        if (Boolean.FALSE.equals(isLogin)) {
            log.warn("账号Cookie已失效, {}, {}", config.getDedeuserid(), config.getSessdata());

            // 如果没有登录成功，则返回简要信息
            return getUser(config.getDedeuserid());
        }

        // 登录成功，获取详细信息
        // 获取头像
        InputStream avatarStream = getAvatarStream(data.getStr("face"));
        String path = "avatars" + File.separator + config.getDedeuserid() + ".png";
        File avatarFile = new File(path);
        if (avatarFile.exists()) {
            String localMd5 = SecureUtil.md5(avatarFile);
            String remoteMd5 = SecureUtil.md5(avatarStream);
            if (!localMd5.equals(remoteMd5)) {
                FileUtil.writeFromStream(avatarStream, avatarFile);
            }
        } else {
            FileUtil.writeFromStream(avatarStream, avatarFile);
        }

        // 上传到 oss
        CosUtil.upload(avatarFile);

        String uname = data.getStr("uname");
        // 获取硬币数
        String coins = data.getStr("money");

        // 获取大会员信息
        JSONObject vip = data.getJSONObject("vip");

        // 获取等级信息
        JSONObject levelInfo = data.getJSONObject("level_info");
        Integer currentLevel = levelInfo.getInt("current_level");

        // 获取勋章墙
        JSONObject medalWallResp = getMedalWall();
        List<JSONObject> medals = medalWallResp.getJSONObject("data")
                .getJSONArray("list")
                .stream()
                .map(JSONUtil::parseObj)
                .map(medalObj -> {
                    JSONObject medal = JSONUtil.createObj();
                    medal.set("name", medalObj.getByPath("medal_info.medal_name", String.class));
                    medal.set("level", medalObj.getByPath("medal_info.level", Integer.class));
                    medal.set("colorStart", medalObj.getByPath("medal_info.medal_color_start", Integer.class));
                    medal.set("colorEnd", medalObj.getByPath("medal_info.medal_color_end", Integer.class));
                    medal.set("colorBorder", medalObj.getByPath("medal_info.medal_color_border", Integer.class));
                    return medal;
                })
                .sorted((o1, o2) -> o2.getInt("level") - o1.getInt("level"))
                .limit(2L)
                .collect(Collectors.toList());

        BilibiliUser info = new BilibiliUser();
        info.setDedeuserid(config.getDedeuserid());
        info.setUsername(uname);
        info.setCoins(coins);
        info.setLevel(currentLevel);
        info.setCurrentExp(levelInfo.getInt("current_exp"));
        info.setNextExp(currentLevel == 6 ? 0 : levelInfo.getInt("next_exp"));
        info.setMedals(JSONUtil.toJsonStr(medals));
        info.setVipType(vip.getInt("type"));
        info.setVipStatus(vip.getInt("status"));
        info.setIsLogin(true);

        return info;
    }

    /**
     * 当Cookie过期时获取用户的部分信息
     *
     * @param userId B站uid
     * @return B站用户信息 {@link BilibiliUser}
     */
    public BilibiliUser getUser(String userId) {
        String body = HttpRequest.get(BilibiliAPI.GET_USER_SPACE_INFO + "?mid=" + userId).execute().body();

        JSONObject resp = JSONUtil.parseObj(body);
        JSONObject baseInfo = resp.getJSONObject("data");
        if (resp.getInt("code") == -404 || baseInfo == null) {
            log.error("用户[{}]不存在", userId);
            return null;
        }
        InputStream avatarStream = getAvatarStream(baseInfo.getStr("face"));
        String path = "avatars" + File.separator + config.getDedeuserid() + ".png";
        File avatarFile = new File(path);
        if (avatarFile.exists()) {
            String localMd5 = SecureUtil.md5(avatarFile);
            String remoteMd5 = SecureUtil.md5(avatarStream);
            if (!localMd5.equals(remoteMd5)) {
                FileUtil.writeFromStream(avatarStream, avatarFile);
            }
        } else {
            FileUtil.writeFromStream(avatarStream, avatarFile);
        }

        // 上传到 oss
        CosUtil.upload(avatarFile);

        BilibiliUser info = new BilibiliUser();
        info.setDedeuserid(userId);
        info.setUsername(baseInfo.getStr("name"));
        info.setLevel(baseInfo.getInt("level"));
        info.setIsLogin(false);

        return info;
    }

    /**
     * 获取勋章墙
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getMedalWall() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("target_id", CollUtil.newArrayList(config.getDedeuserid()));
        return doGet(BilibiliAPI.GET_MEDAL_WALL, params);
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
    public JSONObject getCoinChangeLog() {
        return doGet(BilibiliAPI.GET_COIN_CHANGE_LOG);
    }

    /**
     * 查询每日奖励状态
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getExpRewardStatus() {
        return doGet(BilibiliAPI.GET_EXP_REWARD_STATUS);
    }

    /**
     * 获取已关注的UP最近发布的视频的BVID
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getFollowedUpPostVideo() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("uid", CollUtil.newArrayList(config.getDedeuserid()));
        params.put("type_list", CollUtil.newArrayList("8"));
        params.put("from", CollUtil.newArrayList());
        params.put("platform", CollUtil.newArrayList("web"));

        return doGet(BilibiliAPI.GET_FOLLOWED_UP_POST_VIDEO, params);
    }

    /**
     * 根据分区ID获取3日热榜视频
     *
     * @param regionId 分区ID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getTrendVideo(String regionId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("rid", CollUtil.newArrayList(regionId));
        params.put("day", CollUtil.newArrayList("3"));

        return doGet(BilibiliAPI.GET_TREND_VIDEO, params);
    }

    /**
     * 观看视频
     *
     * @param bvid       视频的BVID
     * @param playedTime 播放时长，秒
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject playVideo(String bvid, int playedTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("bvid", bvid);
        params.put("played_time", playedTime);
        String requestBody = HttpUtil.toParams(params);
        return doPost(BilibiliAPI.REPORT_HEARTBEAT, requestBody);
    }

    /**
     * 分享视频
     *
     * @param bvid 视频的BVID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject shareVideo(String bvid) {
        Map<String, Object> params = new HashMap<>();
        params.put("bvid", bvid);
        params.put("csrf", config.getBiliJct());
        String requestBody = HttpUtil.toParams(params);

        return doPost(BilibiliAPI.SHARE_VIDEO, requestBody);
    }


    /**
     * 漫画签到
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject mangaCheckIn(String platform) {
        Map<String, Object> params = new HashMap<>();
        params.put("platform", platform);
        String requestBody = HttpUtil.toParams(params);
        return doPost(BilibiliAPI.MANGA_SIGN, requestBody);
    }

    /**
     * 获取今日通过投币获得的经验值
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getCoinExpToday() {
        return doGet(BilibiliAPI.GET_COIN_EXP_TODAY);
    }

    /**
     * 获取视频详细信息
     *
     * @param bvid 视频BVID
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getVideoDetails(String bvid) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("bvid", CollUtil.newArrayList(bvid));
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
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("bvid", CollUtil.newArrayList(bvid));
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
        Map<String, Object> params = new HashMap<>();
        params.put("bvid", bvid);
        params.put("multiply", numCoin);
        params.put("select_like", isLike);
        params.put("cross_domain", true);
        params.put("csrf", config.getBiliJct());
        String requestBody = HttpUtil.toParams(params);

        Map<String, String> headers = new HashMap<>();
        headers.put("Referer", "https://www.bilibili.com/video/" + bvid);
        headers.put("Origin", "https://www.bilibili.com");

        return doPost(BilibiliAPI.DONATE_COIN, requestBody, headers);
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
        Map<String, Object> params = new HashMap<>();
        params.put("csrf_token", config.getBiliJct());
        params.put("csrf", config.getBiliJct());
        String requestBody = HttpUtil.toParams(params);

        return doPost(BilibiliAPI.SILVER_2_COIN, requestBody);
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
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("mid", CollUtil.newArrayList(userId));

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
    public JSONObject donateGift(String userId, String roomId,
                                 String bagId, String giftId, int giftNum) {
        Map<String, Object> params = new HashMap<>();
        params.put("biz_id", roomId);
        params.put("ruid", userId);
        params.put("gift_id", giftId);
        params.put("bag_id", bagId);
        params.put("gift_num", giftNum);
        params.put("uid", config.getDedeuserid());
        params.put("csrf", config.getBiliJct());
        params.put("send_ruid", 0);
        params.put("storm_beat_id", 0);
        params.put("price", 0);
        params.put("platform", "pc");
        params.put("biz_code", "live");
        String requestBody = HttpUtil.toParams(params);

        return doPost(BilibiliAPI.SEND_GIFT, requestBody);
    }

    /**
     * 获取充电信息
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getChargeInfo() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("mid", CollUtil.newArrayList(config.getDedeuserid()));

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
        Map<String, Object> params = new HashMap<>();
        params.put("bp_num", couponBalance);
        params.put("is_bp_remains_prior", true);
        params.put("up_mid", upUserId);
        params.put("otype", "up");
        params.put("oid", config.getDedeuserid());
        params.put("csrf", config.getBiliJct());

        String requestBody = HttpUtil.toParams(params);

        return doPost(BilibiliAPI.CHARGE, requestBody);
    }

    /**
     * 提交充电留言
     *
     * @param orderNo 充电订单号
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject doChargeComment(String orderNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("order_id", orderNo);
        params.put("message", "up的作品很棒");
        params.put("csrf", config.getBiliJct());

        String requestBody = HttpUtil.toParams(params);

        return doPost(BilibiliAPI.COMMIT_CHARGE_COMMENT, requestBody);
    }

    /**
     * 获取大会员漫画权益
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getMangaVipReward() {
        Map<String, Object> params = new HashMap<>();
        params.put("reason_id", 1);

        String requestBody = JSONUtil.parseObj(params).toJSONString(0);
        return doPost(BilibiliAPI.GET_MANGA_VIP_REWARD, requestBody);
    }

    /**
     * 领取大会员权益
     *
     * @param type 1 = B币券  2 = 会员购优惠券
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject getVipReward(int type) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("csrf", config.getBiliJct());

        String requestBody = HttpUtil.toParams(params);
        return doPost(BilibiliAPI.GET_VIP_REWARD, requestBody);
    }

    /**
     * 阅读漫画
     *
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    public JSONObject readManga() {
        Map<String, String> params = new HashMap<>(4);
        params.put("device", "pc");
        params.put("platform", "web");
        params.put("comic_id", "26009");
        params.put("ep_id", "300318");

        String requestBody = JSONUtil.parseObj(params).toJSONString(0);
        return doPost(BilibiliAPI.READ_MANGA, requestBody);
    }

    public JSONObject followUser(String uid) {
        Map<String, Object> params = new HashMap<>();
        params.put("fid", uid);
        params.put("act", 1);
        params.put("re_src", 11);
        params.put("csrf", config.getBiliJct());

        String requestBody = HttpUtil.toParams(params);
        return doPost(BilibiliAPI.RELATION_MODIFY, requestBody);
    }

    public String getAvatar() {
        JSONObject resp = doGet(BilibiliAPI.GET_USER_INFO_NAV);
        // 解析响应信息
        JSONObject data = resp.getJSONObject("data");
        InputStream avatarStream = getAvatarStream(data.getStr("face"));
        return Base64.encode(avatarStream);
    }

    /**
     * 获取B站用户头像文件流
     *
     * @param avatarUrl 头像地址
     * @return 头像文件流
     */
    private InputStream getAvatarStream(String avatarUrl) {
        return HttpRequest
                .get(avatarUrl)
                .execute().bodyStream();
    }

    /**
     * 匿名处理
     *
     * @param username B站用户名
     * @return 使用*号覆盖后的用户名
     */
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
        return doGet(url, null);
    }

    /**
     * 实际处理B站API访问
     *
     * @param url    访问API地址
     * @param params 查询字符串参数 {@link MultiValueMap}
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    private JSONObject doGet(String url, MultiValueMap<String, String> params) {
        url = UriComponentsBuilder.fromHttpUrl(url)
                .queryParams(params)
                .build().toUriString();
        httpRequest = HttpRequest.get(url)
                .timeout(100000)
                .header(Header.CONNECTION, "keep-alive")
                .header(Header.USER_AGENT, config.getUserAgent())
                .cookie("bili_jct=" + config.getBiliJct() +
                        ";SESSDATA=" + config.getSessdata() +
                        ";DedeUserID=" + config.getDedeuserid() + ";");

        if (!ObjectUtil.hasNull(proxyHost, proxyPort)) {
            httpRequest.setHttpProxy(proxyHost, proxyPort);
        }

        return retryableCall(httpRequest);
    }

    private JSONObject doPost(String url, String requestBody) {
        return doPost(url, requestBody, null);
    }

    private JSONObject doPost(String url, String requestBody, Map<String, String> headers) {
        httpRequest = HttpRequest.post(url)
                .timeout(10000)
                .header(Header.CONTENT_TYPE, JSONUtil.isJson(requestBody) ?
                        MediaType.APPLICATION_JSON_VALUE : MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(Header.CONNECTION, "keep-alive")
                .header(Header.USER_AGENT, config.getUserAgent())
                .header(Header.REFERER, "https://www.bilibili.com/")
                .addHeaders(headers)
                .cookie("bili_jct=" + config.getBiliJct() +
                        ";SESSDATA=" + config.getSessdata() +
                        ";DedeUserID=" + config.getDedeuserid() + ";");
        if (!ObjectUtil.hasNull(proxyHost, proxyPort)) {
            httpRequest.setHttpProxy(proxyHost, proxyPort);
        }

        return retryableCall(httpRequest.body(requestBody));
    }

    private JSONObject retryableCall(HttpRequest httpRequest) {
        Callable<String> task = () -> httpRequest.execute().body();
        String responseBody = null;
        try {
            responseBody = retryer.call(task);
        } catch (ExecutionException e) {
            log.error("重试调用接口[{}]失败, {}", httpRequest.getUrl(), e.getMessage());
        } catch (RetryException e) {
            log.error("调用接口[{}]超过执行次数, {}", httpRequest.getUrl(), e.getMessage());
        }
        return JSONUtil.parseObj(responseBody);
    }
}
