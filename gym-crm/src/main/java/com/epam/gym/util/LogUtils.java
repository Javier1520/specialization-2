package com.epam.gym.util;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class LogUtils {

    public void info(Logger logger, String msg, Object... args) {
        logger.info(msg, args);
    }

    public void warn(Logger logger, String msg, Object... args) {
        logger.warn(msg, args);
    }

    public void error(Logger logger, String msg, Object... args) {
        logger.error(msg, args);
    }

    public void debug(Logger logger, String msg, Object... args) {
        logger.debug(msg, args);
    }
}
