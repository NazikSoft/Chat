package com.chat.utils;

import java.text.SimpleDateFormat;

/**
 * Created by m on 22.09.2017.
 */

public class ChatConst {

    public static final String TAG = "log_tag";
    public static final String USER_DATABASE_PATH = "users";
    public static final String CHAT_DATABASE_PATH = "chats";

    public static final String COLUMN_CHAT_ROOMS = "chatRooms";
    public static final String COLUMN_MESSAGES = "messages";
    public static final String COLUMN_USER_READ_MESSAGE_COUNT = "userReadMessageCount";
    public static final String COLUMN_IMAGE_URL = "imageUrl";


    public final static int HANDLER_RECEIVE_MSG = 10101;
    public final static int HANDLER_USERS_LIST = 10102;
    public final static int HANDLER_CLICK_RECYCLER_ITEM = 10103;
    public final static int HANDLER_RESULT_ERR = 10111;
    public final static int HANDLER_RESULT_OK = 10112;
    public final static int HANDLER_RESULT_COMPAMION_USER = 10113;
    public final static int HANDLER_RESULT_CURRENT_USER = 10114;
    public final static int HANDLER_RESULT_CHAT = 10115;
    public final static int HANDLER_CHAT_LIST = 10116;
    public final static int IMAGEDAO_RESULT_PATH_OK = 10117;
    public final static int ACTION_SELECT_IMAGE = 10118;
    public final static int ACTION_IMAGE_CAPTURE = 10119;
    public final static int HANDLER_IMAGE_SAVE_OK = 10120;
    public final static int RECYCLER_LIST_CLICKED = 10121;

    public static final String DATE_PARSE_REG_EXP = "%1$te %1$tm %1$tY";
    public static final String TIME_PARSE_REG_EXP = "%1$tH:%1$tM";
    public static final SimpleDateFormat sdf=new SimpleDateFormat("dd MMM HH:mm:ss");


    public static final int DEFAULT_MSG_LENGTH_LIMIT = 100;
    public static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final String NOTIFICATION_ID = "generalNotificationChanel";
    public static final String EXTRA_CHAT_ID = "chatRoomIdExtra";
}
