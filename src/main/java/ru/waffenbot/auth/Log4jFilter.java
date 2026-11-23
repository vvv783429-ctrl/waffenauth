package ru.waffenbot.auth;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

public class Log4jFilter extends AbstractFilter {

    @Override
    public Result filter(LogEvent event) {
        if (event == null || event.getMessage() == null) {
            return Result.NEUTRAL;
        }

        String message = event.getMessage().getFormattedMessage();
        if (message == null) {
            return Result.NEUTRAL;
        }

        String lower = message.toLowerCase();
        if (lower.contains("issued server command:") &&
           (lower.contains("/login") || lower.contains("/register") || lower.contains("/changepass"))) {
            return Result.DENY;
        }

        return Result.NEUTRAL;
    }
}
