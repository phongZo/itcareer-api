package com.base.auth.constant;


import io.swagger.models.auth.In;
import java.util.List;

public class UserBaseConstant {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";


    public static final Integer USER_KIND_ADMIN = 1;
    public static final Integer USER_KIND_MANAGER = 2;
    public static final Integer USER_KIND_STUDENT = 3;
    public static final Integer USER_KIND_EDUCATOR = 5;
    public static final Integer USER_KIND_ENTERISE = 6;

    public static final Integer TASK_KIND_TASK = 1;
    public static final Integer TASK_KIND_SUBTASK = 2;
    public static final List<Integer> TASK_KINDS = List.of(TASK_KIND_TASK, TASK_KIND_SUBTASK);

    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_WAITING_APPROVE = 2;
    public static final Integer STATUS_LOCK = -1;
    public static final Integer STATUS_REJECT = -2;

    public static final Integer STATE_COMPLETED = 1;
    public static final Integer STATE_IN_PROGRESS = 2;
    public static final Integer STATE_PROCESSING = 3;
    public static final Integer STATE_DONE = 4;
    public static final Integer STATE_FAIL = 5;

    public static final Integer RESTART_ERROR_COUNT = 0;

    public static final Integer QUESTION_TYPE_FILE = 1;
    public static final Integer QUESTION_TYPE_TEXT = 2;
    public static final Integer QUESTION_TYPE_MULTIPLE_CHOICE = 3;
    public static final List<Integer> QUESTION_TYPES = List.of(QUESTION_TYPE_FILE, QUESTION_TYPE_TEXT, QUESTION_TYPE_MULTIPLE_CHOICE);

    public static final String MEDIA_COMPLETED_PROCESS_VIDEO = "MEDIA_COMPLETED_PROCESS_VIDEO";

    public static final Integer NATION_KIND_PROVINCE = 1;
    public static final Integer NATION_KIND_DISTRICT = 2;
    public static final Integer NATION_KIND_COMMUNE = 3;

    public static final Integer GROUP_KIND_ADMIN = 1;
    public static final Integer GROUP_KIND_MANAGER = 2;
    public static final Integer GROUP_KIND_USER=3;

    public static final Integer MAX_ATTEMPT_FORGET_PWD = 5;
    public static final int MAX_TIME_FORGET_PWD = 5 * 60 * 1000; //5 minutes
    public static final Integer MAX_ATTEMPT_LOGIN = 5;

    public static final Integer CATEGORY_KIND_NEWS = 1;

    public static final String PASSWORD_PATTERN = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
    public static final String EMAIL_PATTERN = "^\\S+@\\S+\\.\\S+$";
    public static final String PHONE_PATTERN = "^0\\d{9}$";

    public static final String[] UPLOAD_TYPES = new String[]{"LOGO", "AVATAR", "IMAGE"};
    public static final String[] AVATAR_EXTENSION = new String[]{"jpeg", "jpg", "gif", "bmp", "png"};

    private UserBaseConstant(){
        throw new IllegalStateException("Utility class");
    }
}
