package io.cruii.execution.component;

import cn.hutool.core.io.FileUtil;
import io.cruii.component.BilibiliDelegate;
import io.cruii.component.TaskExecutor;
import io.cruii.context.BilibiliUserContext;
import io.cruii.execution.feign.BilibiliFeignService;
import io.cruii.execution.feign.PushFeignService;
import io.cruii.pojo.po.BilibiliUser;
import io.cruii.pojo.po.TaskConfig;
import io.cruii.pojo.vo.BilibiliUserVO;
import ma.glasnost.orika.MapperFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2022/10/17
 */
@Component
public class TaskRunner {

    private final ThreadPoolTaskExecutor taskExecutor;
    private final ThreadPoolTaskExecutor pushExecutor;
    private final BilibiliFeignService bilibiliFeignService;

    private final PushFeignService pushFeignService;

    private final MapperFactory mapperFactory;

    public TaskRunner(ThreadPoolTaskExecutor taskExecutor,
                      ThreadPoolTaskExecutor pushExecutor,
                      BilibiliFeignService bilibiliFeignService,
                      PushFeignService pushFeignService,
                      MapperFactory mapperFactory) {
        this.taskExecutor = taskExecutor;
        this.pushExecutor = pushExecutor;
        this.bilibiliFeignService = bilibiliFeignService;
        this.pushFeignService = pushFeignService;
        this.mapperFactory = mapperFactory;
    }

    public void run(TaskConfig taskConfig) {
        taskExecutor.execute(() -> {
            try {
                BilibiliDelegate delegate = new BilibiliDelegate(taskConfig);
                BilibiliUser user = delegate.getUser();
                BilibiliUserContext.set(user);
                TaskExecutor executor = new TaskExecutor(delegate);
                BilibiliUser retUser = executor.execute();
                retUser.setLastRunTime(LocalDateTime.now());
                BilibiliUserContext.set(retUser);

            } catch (Exception e) {
                throw new RuntimeException("执行任务发生异常", e);
            } finally {
                String traceId = MDC.get("traceId");
                // 推送
                push(taskConfig.getDedeuserid(), traceId);
                MDC.clear();
            }
        });
    }

    private void push(String dedeuserid, String traceId) {
        pushExecutor.execute(() -> {
            BilibiliUser retUser = BilibiliUserContext.get();
            BilibiliUserVO bilibiliUserVO = mapperFactory.getMapperFacade().map(retUser, BilibiliUserVO.class);
            bilibiliFeignService.updateUser(bilibiliUserVO);

            BilibiliUserContext.remove();
            // 日志收集
            String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
            File logFile = new File("logs/execution/all-" + date + ".log");
            String content = null;
            if (logFile.exists()) {
                List<String> logs = FileUtil.readLines(logFile, StandardCharsets.UTF_8);

                content = logs
                        .stream()
                        .filter(line -> line.contains(traceId) && (line.contains("INFO") || line.contains("ERROR")))
                        .map(line -> line.split("\\|\\|")[1])
                        .collect(Collectors.joining("\n"));
            }
            pushFeignService.push(dedeuserid, content);
        });
    }
}
