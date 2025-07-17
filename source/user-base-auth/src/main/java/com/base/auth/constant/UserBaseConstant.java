package com.base.auth.constant;


import io.swagger.models.auth.In;

public class UserBaseConstant {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss";


    public static final Integer USER_KIND_ADMIN = 1;
    public static final Integer USER_KIND_MANAGER = 2;
    public static final Integer USER_KIND_STUDENT = 3;
    public static final Integer USER_KIND_EDUCATOR = 5;
    public static final Integer USER_KIND_ENTERISE = 6;

    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_WAITING_APPROVE = 2;
    public static final Integer STATUS_LOCK = -1;
    public static final Integer STATUS_DELETE = -2;

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
