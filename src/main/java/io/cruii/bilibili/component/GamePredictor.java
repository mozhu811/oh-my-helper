package io.cruii.bilibili.component;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tencentcloudapi.scf.v20180416.models.Function;
import com.tencentcloudapi.scf.v20180416.models.ListFunctionsResponse;
import io.cruii.bilibili.config.TencentApiConfig;
import io.cruii.bilibili.util.ScfUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/6/12
 */
@Log4j2
@Component
public class GamePredictor {

    @Resource
    private Executor predictExecutor;

    @Resource
    public TencentApiConfig apiConfig;

    public static final String QUESTIONS_API = "https://api.bilibili.com/x/esports/guess/collection/question";

    public static final int REQUEST_COINS = 10;
    @Scheduled(cron = "0 0/1 * * * ?")
    public void runGuessTask() {
        // 获取所有用户提交的cookie
        ListFunctionsResponse listFunctionsResponse = ScfUtil.listFunctions(apiConfig);
        LinkedBlockingQueue<String[]> users = Arrays.stream(listFunctionsResponse.getFunctions())
                .map(Function::getDescription)
                .map(s -> s.split(";")).collect(Collectors.toCollection(LinkedBlockingQueue::new));
        // 查询当日竞猜
        JSONArray contestsAndQuestions = queryQuestions().getJSONArray("list");
        if (contestsAndQuestions.isEmpty()) {
            log.info("本日赛事预测已经截止");
            return;
        }

        log.info(">>>>>>>>>> 开始获取今日赛事信息 <<<<<<<<<<");
        for (Object contestsAndQuestion : contestsAndQuestions) {
            JSONObject jsonObject = JSONUtil.parseObj(contestsAndQuestion);
            JSONObject contest = jsonObject.getJSONObject("contest");
            JSONObject question = JSONUtil.parseObj(jsonObject.getJSONArray("questions").get(0));
            int contestId = contest.getInt("id");
            String contestGameStage = contest.getStr("game_stage");
            int questionId = question.getInt("id");
            String title = question.getStr("title");
            String seasonName = contest.getByPath("season.title", String.class);
            log.info(seasonName + " " + contestGameStage + ":" + title);

            if (question.getInt("is_guess") == 1) {
                log.info("本次竞猜已投注");
                continue;
            }

            JSONObject blueTeam = JSONUtil.parseObj(question.getJSONArray("details").get(0));
            JSONObject redTeam = JSONUtil.parseObj(question.getJSONArray("details").get(1));

            Double oddsBlue = blueTeam.getDouble("odds");
            Double oddsRed = redTeam.getDouble("odds");

            JSONObject selected = oddsBlue >= oddsRed ? blueTeam : redTeam;
            log.info("投注队伍: {}，当前赔率收益: {}", selected.getStr("option"), selected.getDouble("odds") * REQUEST_COINS);
            log.info(">>>>>>>>>> 开始进行投注 <<<<<<<<<<");
            doPredict(contestId, questionId, selected.getInt("detail_id"), users);
        }

//        gamePredictExecutor.execute(() -> {
//
//        });

    }

    private void doPredict(int contestId, int questionId, int selectedTeamId, Queue<String[]> users) {

    }

    private JSONObject queryQuestions() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String gid = "";
        String sids = "";

        String url = UriComponentsBuilder.fromHttpUrl(QUESTIONS_API)
                .queryParam("pn", 1)
                .queryParam("ps", 50)
                .queryParam("gid", gid)
                .queryParam("sids", sids)
                .queryParam("stime", today + " 00:00:00")
                .queryParam("etime", today + " 23:59:59")
                .queryParam("pn", 1)
                .queryParam("ps", 50)
                .queryParam("stime", today + "+00:00:00")
                .queryParam("etime", today + "+23:59:59").toUriString();

        String response = HttpRequest.get(URLUtil.normalize(url, true)).execute().body();
        log.debug("赛事信息返回结果: {}", response);
        JSONObject responseJson = JSONUtil.parseObj(response);
        if (0 != responseJson.getInt("code")) {
            throw new RuntimeException("获取赛事信息失败");
        }
        return responseJson.getJSONObject("data");
    }
}
