package io.cruii.bilibili.constant;

/**
 * @author cruii
 * Created on 2021/08/13
 */
public interface BilibiliAPI {
    String GET_QR_CODE_LOGIN_URL = "http://passport.bilibili.com/qrcode/getLoginUrl";

    String GET_QR_CODE_LOGIN_INFO_URL = "http://passport.bilibili.com/qrcode/getLoginInfo";

    /**
     * 导航栏用户信息
     */
    String GET_USER_INFO_NAV = "http://api.bilibili.com/x/web-interface/nav";

    /**
     * 获取用户空间详细信息
     */
    String GET_USER_SPACE_INFO = "http://api.bilibili.com/x/space/acc/info";

    /**
     * 查询硬币变化情况
     */
    String GET_COIN_CHANGE_LOG = "http://api.bilibili.com/x/member/web/coin/log";

    /**
     * 查询每日奖励状态
     */
    String GET_EXP_REWARD_STATUS = "http://api.bilibili.com/x/member/web/exp/reward";

    /**
     * 获取已关注的UP发布的视频动态列表
     */
    String GET_FOLLOWED_UP_POST_VIDEO = "http://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new";

    /**
     * 根据分区获取热门视频
     */
    String GET_TREND_VIDEO = "http://api.bilibili.com/x/web-interface/ranking/region";

    /**
     * 上报视频播放心跳
     */
    String REPORT_HEARTBEAT = "http://api.bilibili.com/x/click-interface/web/heartbeat";

    /**
     * 获取视频详细信息
     */
    String GET_VIDEO_DETAILS = "http://api.bilibili.com/x/web-interface/view";

    /**
     * 分享视频
     */
    String SHARE_VIDEO = "http://api.bilibili.com/x/web-interface/share/add";

    /**
     * 漫画签到
     */
    String MANGA_SIGN = "https://manga.bilibili.com/twirp/activity.v1.Activity/ClockIn";

    /**
     * 获取今日通过投币获得的经验值
     */
    String GET_COIN_EXP_TODAY = "http://api.bilibili.com/x/web-interface/coin/today/exp";

    /**
     * 获取账户硬币余额
     */
    String GET_COIN = "http://account.bilibili.com/site/getCoin";

    /**
     * 判断视频是否被投币
     */
    String CHECK_DONATE_COIN = "http://api.bilibili.com/x/web-interface/archive/coins";

    /**
     * 投币
     */
    String DONATE_COIN = "http://api.bilibili.com/x/web-interface/coin/add";

    /**
     * 硬币换银瓜子.
     */
    String SILVER_2_COIN = "http://api.live.bilibili.com/xlive/revenue/v1/wallet/silver2coin";

    /**
     * 查询直播间钱包
     */
    String BILI_LIVE_WALLET = "http://api.live.bilibili.com/xlive/revenue/v1/wallet/myWallet?need_bp=1&need_metal=1&platform=pc";

    /**
     * 点赞视频
     */
    String LIKE_VIDEO = "http://api.bilibili.com/x/web-interface/archive/like";

    /**
     * 直播签到
     */
    String BILI_LIVE_CHECK_IN = "http://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign";

    /**
     * 获取礼物背包
     */
    String LIST_GIFTS = "http://api.live.bilibili.com/xlive/web-room/v1/gift/bag_list";

    /**
     * 获取直播间信息
     */
    String GET_LIVE_ROOM_INFO = "http://api.live.bilibili.com/room/v1/Room/getRoomInfoOld";

    /**
     * 送出直播间礼物
     */
    String SEND_GIFT = "http://api.live.bilibili.com/gift/v2/live/bag_send";

    /**
     * 充电
     */
    String CHARGE = "http://api.bilibili.com/x/ugcpay/web/v2/trade/elec/pay/quick";

    /**
     * 获取充电信息
     */
    String GET_CHARGE_INFO = "http://api.bilibili.com/x/ugcpay/web/v2/trade/elec/panel";

    /**
     * 充电留言.
     */
    String COMMIT_CHARGE_COMMENT = "http://api.bilibili.com/x/ugcpay/trade/elec/message";

    /**
     * 领取大会员权益
     */
    String GET_VIP_REWARD = "http://api.bilibili.com/x/vip/privilege/receive";

    /**
     * 领取大会员漫画奖励
     */
    String GET_MANGA_VIP_REWARD = "https://manga.bilibili.com/twirp/user.v1.User/GetVipReward";

    /**
     * 阅读漫画
     */
    String READ_MANGA = "https://manga.bilibili.com/twirp/bookshelf.v1.Bookshelf/AddHistory?device=pc&platform=web";

    /**
     * 获取勋章墙
     */
    String GET_MEDAL_WALL = "http://api.live.bilibili.com/xlive/web-ucenter/user/MedalWall";

    /**
     * 操作用户关系
     */
    String RELATION_MODIFY = "http://api.bilibili.com/x/relation/modify";

    String GET_JURY_CASE = "http://api.bilibili.com/x/credit/v2/jury/case/next";

    String JURY_VOTE = "http://api.bilibili.com/x/credit/v2/jury/vote";
}
