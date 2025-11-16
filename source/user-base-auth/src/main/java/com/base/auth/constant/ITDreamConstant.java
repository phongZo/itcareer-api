package com.base.auth.constant;

import java.io.File;
import java.util.List;

public class ITDreamConstant {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static final String DIRECTORY_GENERAL = File.separator + "general";

    public static final Integer USER_KIND_ADMIN = 1;
    public static final Integer USER_KIND_EDUCATOR = 2;
    public static final Integer USER_KIND_STUDENT = 3;
    public static final Integer USER_KIND_COMPANY = 4;

    public static final Integer TASK_KIND_TASK = 1;
    public static final Integer TASK_KIND_SUBTASK = 2;
    public static final List<Integer> TASK_KINDS = List.of(TASK_KIND_TASK, TASK_KIND_SUBTASK);

    public static final List<Integer> STARS = List.of(1, 2, 3, 4, 5);

    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_WAITING_APPROVE = 2;
    public static final Integer STATUS_WAITING_APPROVE_DELETE = 3;
    public static final Integer STATUS_LOCK = -1;
    public static final Integer STATUS_REJECT = -2;

    public static final Integer STATE_STUDENT_SUBTASK_PROGRESS_COMPLETED = 1;
    public static final Integer STATE_STUDENT_SUBTASK_PROGRESS_IN_PROGRESS = 2;

    public static final Integer STATE_SIMULATION_INIT = 0;
    public static final Integer STATE_SIMULATION_PROCESSING = 1;
    public static final Integer STATE_SIMULATION_DONE = 2;
    public static final Integer STATE_SIMULATION_FAIL = 3;

    public static final Integer STATE_TASK_INIT = 0;
    public static final Integer STATE_TASK_PROCESSING = 1;
    public static final Integer STATE_TASK_DONE = 2;
    public static final Integer STATE_TASK_FAIL = 3;

    public static final Integer KIND_SIMULATION = 1;
    public static final Integer KIND_TASK = 2;

    public static final Integer RESTART_ERROR_COUNT = 0;

    public static final Integer QUESTION_TYPE_FILE = 1;
    public static final Integer QUESTION_TYPE_TEXT = 2;
    public static final Integer QUESTION_TYPE_MULTIPLE_CHOICE = 3;
    public static final List<Integer> QUESTION_TYPES = List.of(QUESTION_TYPE_FILE, QUESTION_TYPE_TEXT, QUESTION_TYPE_MULTIPLE_CHOICE);

    public static final String BACKEND_PROCESS_VIDEO_CMD = "BACKEND_PROCESS_VIDEO";
    public static final String MEDIA_COMPLETED_PROCESS_VIDEO_CMD = "MEDIA_COMPLETED_PROCESS_VIDEO";
    public static final String BACKEND_POST_NOTIFICATION_CMD = "BACKEND_POST_NOTIFICATION";

    public static final Integer GROUP_KIND_ADMIN = 1;
    public static final Integer GROUP_KIND_MANAGER = 2;
    public static final Integer GROUP_KIND_USER=3;

    public static final Integer MAX_ATTEMPT_FORGET_PWD = 5;
    public static final int MAX_TIME_FORGET_PWD = 5 * 60 * 1000; //5 minutes
    public static final Integer MAX_ATTEMPT_LOGIN = 5;

    public static final String PASSWORD_PATTERN = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,15}$";
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PHONE_PATTERN = "^0\\d{9}$";
    public static final String FILE_PATH_PATTERN = "^(https?:\\/\\/|\\/)?([\\w\\-]+\\/?)+\\.[A-Za-z0-9]{2,6}$";


    public static final String NOTIFICATION_TYPE_REVIEW_SUBMISSION = "REVIEW_SUBMISSION";

    private ITDreamConstant(){
        throw new IllegalStateException("Utility class");
    }
}
