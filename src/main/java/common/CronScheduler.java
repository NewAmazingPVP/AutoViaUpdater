package common;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Consumer;

import static com.cronutils.model.CronType.UNIX;

public class CronScheduler {
    private final ExecutionTime executionTime;
    private ZonedDateTime nextExecution;

    public CronScheduler(String cronExpression) {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(UNIX));
        Cron cron = parser.parse(cronExpression);
        this.executionTime = ExecutionTime.forCron(cron);
        this.nextExecution = calculateNextExecution();
    }

    public boolean shouldExecute() {
        ZonedDateTime now = ZonedDateTime.now();
        if (nextExecution.isBefore(now)) {
            nextExecution = calculateNextExecution();
            return true;
        }
        return false;
    }

    private ZonedDateTime calculateNextExecution() {
        Optional<ZonedDateTime> next = executionTime.nextExecution(ZonedDateTime.now());
        return next.orElse(ZonedDateTime.now().plusMinutes(1));
    }

    public void runIfDue(Consumer<Void> task) {
        if (shouldExecute()) {
            task.accept(null);
        }
    }
}
