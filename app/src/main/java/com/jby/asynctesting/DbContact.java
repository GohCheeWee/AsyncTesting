package com.jby.asynctesting;

public class DbContact {
    public static final String DATABASE_NAME = "DB_Contact";
    public static final int DATABASE_VERSION = 1;
    public static final String USER_STATUS_FAILED = "Failed";
    public static final String USER_STATUS_SUCCESSFUL = "Successful";
    public static final String URL="http://188.166.186.198/~cheewee/testing/aysnc_testing.php";
    public static final String UI_UPDATE_BROADCAST="GOOD";

    public static final String TB_USER = "tb_user";

    public static final String CREATE_USER_TABLE = "CREATE TABLE "+ TB_USER +
            "(user_id INTEGER PRIMARY KEY, " +
            "username Text, " +
            "status Text)";
}
