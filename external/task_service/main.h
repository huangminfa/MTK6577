#ifndef __TASK_MAIN_H__
#define __TASK_MAIN_H__

enum exit_code {
    USAGE_ERROR = 1,
    SYSTEM_ERROR = 2,
    NETWORK_ERROR = 3,
    PROTOCOL_ERROR = 4,
    CHALLENGE_FAILED = 5,
    USER_REQUESTED = 6,
    REMOTE_REQUESTED = 7,
    PPPD_EXITED = 32,
};

enum log_level {
    DEBUG = 0,
    INFO = 1,
    WARNING = 2,
    ERROR = 3,
    FATAL = 4,
    LOG_MAX = 4,
};
#endif
