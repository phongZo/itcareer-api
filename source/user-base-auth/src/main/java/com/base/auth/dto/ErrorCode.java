package com.base.auth.dto;

public class ErrorCode {
    /**
     * General error code
     * */
    public static final String GENERAL_ERROR_REQUIRE_PARAMS = "ERROR-GENERAL-0000";
    public static final String GENERAL_ERROR_STORE_LOCKED = "ERROR-GENERAL-0001";
    public static final String GENERAL_ERROR_ACCOUNT_LOCKED = "ERROR-GENERAL-0002";
    public static final String GENERAL_ERROR_SHOP_LOCKED = "ERROR-GENERAL-0003";
    public static final String GENERAL_ERROR_STORE_NOT_FOUND = "ERROR-GENERAL-0004";
    public static final String GENERAL_ERROR_ACCOUNT_NOT_FOUND = "ERROR-GENERAL-0005";

    /**
     * Starting error code Account
     * */
    public static final String ACCOUNT_ERROR_UNKNOWN = "ERROR-ACCOUNT-0000";
    public static final String ACCOUNT_ERROR_USERNAME_EXIST = "ERROR-ACCOUNT-0001";
    public static final String ACCOUNT_ERROR_NOT_FOUND = "ERROR-ACCOUNT-0002";
    public static final String ACCOUNT_ERROR_WRONG_PASSWORD = "ERROR-ACCOUNT-0003";
    public static final String ACCOUNT_ERROR_WRONG_HASH_RESET_PASS = "ERROR-ACCOUNT-0004";
    public static final String ACCOUNT_ERROR_LOCKED = "ERROR-ACCOUNT-0005";
    public static final String ACCOUNT_ERROR_OPT_INVALID = "ERROR-ACCOUNT-0006";
    public static final String ACCOUNT_ERROR_LOGIN = "ERROR-ACCOUNT-0007";
    public static final String ACCOUNT_ERROR_MERCHANT_LOGIN_ERROR_DEVICE = "ERROR-ACCOUNT-0008";
    public static final String ACCOUNT_ERROR_MERCHANT_LOGIN_ERROR_STORE = "ERROR-ACCOUNT-0009";
    public static final String ACCOUNT_ERROR_MERCHANT_LOGIN_WRONG_STORE = "ERROR-ACCOUNT-0010";
    public static final String ACCOUNT_ERROR_MERCHANT_SERVICE_NOT_REGISTER = "ERROR-ACCOUNT-0011";
    public static final String ACCOUNT_ERROR_NOT_ALLOW_DELETE_SUPPER_ADMIN = "ERROR-ACCOUNT-0012";
    public static final String ACCOUNT_ERROR_EMAIL_EXIST = "ERROR-ACCOUNT-0013";
    public static final String ACCOUNT_ERROR_PHONE_EXIST = "ERROR-ACCOUNT-0014";
    public static final String ACCOUNT_ERROR_NOT_ACTIVE = "ERROR-ACCOUNT-0015";
    public static final String ACCOUNT_ERROR_INCORRECT_HASH_VERIFICATION = "ERROR-ACCOUNT-0016";


    /**
     * Starting error code Customer
     * */
    public static final String CUSTOMER_ERROR_UNKNOWN = "ERROR-CUSTOMER-0000";
    public static final String CUSTOMER_ERROR_EXIST = "ERROR-CUSTOMER-0002";
    public static final String CUSTOMER_ERROR_UPDATE = "ERROR-CUSTOMER-0003";
    public static final String CUSTOMER_ERROR_NOT_FOUND = "ERROR-CUSTOMER-0004";


    /**
     * Starting error code Store
     * */
    public static final String SERVICE_ERROR_UNKNOWN = "ERROR-SERVICE-0000";
    public static final String SERVICE_ERROR_NOT_FOUND = "ERROR-SERVICE-0001";
    public static final String SERVICE_ERROR_DUPLICATE_PATH = "ERROR-SERVICE-0002";
    public static final String SERVICE_ERROR_USERNAME_EXIST = "ERROR-SERVICE-0003";
    public static final String SERVICE_ERROR_WRONG_OLD_PWD = "ERROR-SERVICE-0004";
    public static final String SERVICE_ERROR_TENANT_ID_EXIST = "ERROR-SERVICE-0005";

    /**
     * Starting error code SHOP ACCOUNT
     * */
    public static final String SHOP_ACCOUNT_ERROR_UNKNOWN = "ERROR-SHOP_ACCOUNT-0000";
    /**
     * Starting error code NATION
     * */
    public static final String NATION_ERROR_NOT_FOUND = "ERROR-NATION-0001";
    public static final String NATION_ERROR_NOT_ALLOW_HAVE_PARENT = "ERROR-NATION-0002";
    public static final String NATION_ERROR_PARENT_INVALID = "ERROR-NATION-0003";
    public static final String NATION_ERROR_NOT_ALLOW_UPDATE_KIND = "ERROR-NATION-0004";
    public static final String NATION_ERROR_CANT_DELETE_RELATIONSHIP_WITH_ADDRESS = "ERROR-NATION-0005";
    /**
     * Starting error code ADDRESS
     * */
    public static final String ADDRESS_ERROR_NOT_FOUND = "ERROR-ADDRESS-0001";

    /**
     * Starting error code CATEGORY
     * */
    public static final String CATEGORY_ERROR_NOT_FOUND = "ERROR-CATEGORY-0000";
    public static final String CATEGORY_ERROR_EXIST = "ERROR-CATEGORY-0001";

    /**
     * Starting error code News
     * */
    public static final String NEWS_ERROR_NOT_FOUND = "ERROR-NEWS-0000";
    public static final String NEWS_ERROR_EXISTED = "ERROR-NEWS-0001";
    /**
     * Starting error code Settings
     * */
    public static final String SETTINGS_ERROR_NOT_FOUND = "ERROR-SETTING-0000";
    public static final String SETTINGS_ERROR_SETTING_KEY_EXISTED = "ERROR-SETTING-0001";

    /**
     * Starting error code USER
     *
     */
    public static final String USER_ERROR_EXIST = "ERROR-USER-0000";
    public static final String USER_ERROR_NOT_FOUND = "ERROR-USER-0001";
    public static final String USER_ERROR_LOGIN_FAILED = "ERROR-USER-0002";
    public static final String USER_ERROR_VERIFY_FAILED = "ERROR-USER-0003";
    public static final String USER_ERROR_NOT_APPROVE = "ERROR-USER-0004";
    public static final String USER_ERROR_NOT_REJECT = "ERROR-USER-0005";
    public static final String USER_ERROR_NOT_EDUCATOR = "ERROR-USER-0006";
    public static final String USER_ERROR_NOT_STUDENT = "ERROR-USER-0007";

    /**
     * Starting error code DATABASE_ERROR
     *
     */
    public static final String  ERROR_DB_QUERY = "ERROR-DB-QUERY-0000";

    /**
     * Starting error code FILE_ERROR
     *
     */
    public static final String FILE_ERROR_UPLOAD_TYPE_INVALID = "ERROR-FILE-QUERY-0000";
    public static final String FILE_ERROR_UPLOAD_FORMAT_INVALID = "ERROR-FILE-QUERY-0001";

    /**
     * Starting error code Specialization
     * */
    public static final String SPECIALIZATION_ERROR_NOT_FOUND = "SPECIALIZATION-ERROR-0000";
    public static final String SPECIALIZATION_ERROR_EXIST = "SPECIALIZATION-ERROR-0001";
    public static final String SPECIALIZATION_ERROR_DELETE = "SPECIALIZATION-ERROR-0002";

    /**
     * Starting error code Simulation
     * */
    public static final String SIMULATION_ERROR_NOT_FOUND = "SPECIALIZATION-ERROR-0000";
    public static final String SIMULATION_ERROR_EXIST = "SPECIALIZATION-ERROR-0001";
    public static final String SIMULATION_ERROR_NOT_DELETE = "SPECIALIZATION-ERROR-0002";
    public static final String SIMULATION_ERROR_APPROVE = "SPECIALIZATION-ERROR-0003";
    public static final String SIMULATION_ERROR_NOT_AUTHORIZED = "SPECIALIZATION-ERROR-0004";
    public static final String SIMULATION_ERROR_NOT_ACTIVE = "SPECIALIZATION-ERROR-0005";
}
