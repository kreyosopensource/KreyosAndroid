package com.kreyos.kreyosandroid.utilities;

/****************************************************************
 * Collection of constants variables needed for the applications
 **/

public class Constants {

    /** DEBUG SWITCH & VALUES **/
    /**********************************************************************************************/
    public static final boolean DS_SET_ON       = false;
    public static final String DS_DEF_EMAIL     = "mobdev4@kreyos.com";
    public static final String DS_DEF_PASSWORD  = "qqqqqqqq";


    /** LOG TAGS **/
    /**********************************************************************************************/
    public static final String TAG_DEBUG        = "DEBUG";
    public static final String TAG_DEBUG_CONN   = "DEBUG CONNECTION";


    /** ENUMS **/
    /**********************************************************************************************/
    public enum FONT_NAME {
        LEAGUE_GOTHIC_CONDENSED_ITALIC,
        LEAGUE_GOTHIC_CONDENSED_REGULAR,
        LEAGUE_GOTHIC_ITALIC,
        LEAGUE_GOTHIC_REGULAR
    }

    public enum LOGIN_FRAGMENTS {
        FRAGMENT_LOGIN,
        FRAGMENT_SIGNUP_EMAIL,
        FRAGMENT_SIGNUP_USER_INFO
    }

    public enum SETUP_WATCH_FRAGMENTS {
        FRAGMENT_SETUP_WATCH_ON,
        FRAGMENT_SETUP_WATCH_PAIR,
        FRAGMENT_SETUP_WATCH_UPDATE,
        FRAGMENT_SETUP_WATCH_SUCCESS
    }

    public enum WATCH_STATE {
        DISCONNECTED,
        WAITING,
        CONNECTED
    }

    /** FACEBOOK **/
    /**********************************************************************************************/
    public static String[] FB_PERMISSIONS       = new String[] {    "email",
                                                                    //"publish_checkins",
                                                                    "user_birthday",
                                                                    //"user_friends",
                                                                    "user_location",
                                                                    //"friends_location",
                                                                    //"publish_stream",
                                                                    //"read_mailbox",
                                                                    //"read_stream",
                                                                    //"sms"
                                                                };

    /** OTHERS **/
    /**********************************************************************************************/
    public static final int DIALOG_ID_DATE      = 1;
    public static final int DIALOG_ID_TIME      = 2;
    public static final int DIALOG_ID_ALARM     = 3;
    public static final int DIALOG_ID_BIRTHDAY  = 4;

    public static final byte GENDER_NULL    = 0;
    public static final byte GENDER_MALE    = 1;
    public static final byte GENDER_FEMALE  = 2;

    public static final String DBKEY_OVERALL_ACTIVITIES = "USER_ACTIVITIES";


    /** FRAGMENT TAGS **/
    /**********************************************************************************************/
    public static final String FRG_L_1        = "FRAGMENTLEFT1";
    public static final String FRG_L_2        = "FRAGMENTLEFT2";
    public static final String FRG_L_3        = "FRAGMENTLEFT3";
    public static final String FRG_L_4        = "FRAGMENTLEFT4";
    public static final String FRG_L_5        = "FRAGMENTLEFT5";

    public static final String FRG_R_1        = "FRAGMENTRIGHT1";
    public static final String FRG_R_2        = "FRAGMENTRIGHT2";
    public static final String FRG_R_3        = "FRAGMENTRIGHT3";
    public static final String FRG_R_4        = "FRAGMENTRIGHT4";
    public static final String FRG_R_5        = "FRAGMENTRIGHT5";
    public static final String FRG_R_6        = "FRAGMENTRIGHT6";
    public static final String FRG_R_7        = "FRAGMENTRIGHT7";


    /** ACTIVITY RESULT CONSTANTS/REQUEST CODE **/
    /**********************************************************************************************/
    public static final int RC_IMG_FROM_GALLERY     = 1;
    public static final int RC_IMG_FROM_CAMERA      = 2;
    public static final int RC_BLUETOOTH_ENABLE     = 10;



    /** HEIGHT **/
    /**********************************************************************************************/
	public static final int MAX_HEIGHT_INCHES = 11;
	public static final int MIN_HEIGHT_INCHES = 0;
	public static final int MAX_HEIGHT_FEET = 8;
	public static final int MIN_HEIGHT_FEET = 3;
	public static final int MAX_HEIGHT_CENTIMETERS = 274;
	public static final int MIN_HEIGHT_CENTIMETERS = 60;


    /** WEIGHT **/
    /**********************************************************************************************/
	public static final int MAX_WEIGHT_KG = 185;
	public static final int MIN_WEIGHT_KG = 5;
	public static final int MAX_WEIGHT_LBS = 400;
	public static final int MIN_WEIGHT_LBS = 22;


    /** PREFERENCE KEYS **/
    /**********************************************************************************************/
    public static String PREFKEY_AUTO_SYNC_TIME                 = "auto_sync_time";

    public static String PREFKEY_IS_TUTORIAL_MODE 				= "app.is_tutorial_mode";

    public static String PREFKEY_FIRMWARE_VERSION 				= "firmware.version";
    public static String PREFKEY_SPORTS_GOAL_STEPS				= "sports_goals.steps";
    public static String PREFKEY_USER_EMAIL 					= "user.email";
    public static String PREFKEY_USER_EMAIL_PASSWORD 			= "user.email_password";
    public static String PREFKEY_USER_EMAIL_PASSWORD_CONFIRM 	= "user.email_passowrd_confirm";
    public static String PREFKEY_USER_FIRST_VIEW 				= "user.first_view";
    public static String PREFKEY_USER_WATCHED_UNLOCK 			= "user.watch_lock";
    public static String PREFKEY_USER_WATCHED_STATE				= "user.watch_state";
    public static String PREFKEY_USER_WATCH_VERSION             = "watch.version";
    public static String PREFKEY_USER_TUTORIAL_CHECK			= "user.tutorial_Check";
    public static String PREFKEY_USER_IMAGE						= "user.image";
    public static String PREFKEY_USER_FB_TOKEN					= "user.fb_token";
    public static String PREFKEY_USER_KREYOS_TOKEN				= "user.kreyos_token";
    public static String PREFKEY_USER_FIRST_NAME 				= "user.first_name";
    public static String PREFKEY_USER_LAST_NAME					= "user.last_name";
    public static String PREFKEY_USER_BIRTHDAY 					= "user.birthday";
    public static String PREFKEY_USER_GENDER 					= "user.gender";
    public static String PREFKEY_USER_WEIGHT 					= "user.weight";
    public static String PREFKEY_USER_HEIGHT 					= "user.height";
    public static String PREFKEY_USER_FB_ID						= "user.fb_id";
    public static String PREFKEY_USER_FB_IMAGE					= "user.fb_image";
    public static Boolean PREFKEY_IS_DEV_MODE 					= false;
    public static String PREFKEY_LOCAL_IP						= "http://192.168.1.115:3000";
    public static String PREFKEY_URL_CREATE_ACCOUNT 			= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/users" 							: "https://kreyos-members.herokuapp.com/api/users";
    public static String PREFKEY_URL_CHECK_MAIL 				= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/users/check_email" 				: "https://kreyos-members.herokuapp.com/api/users/check_email";
    public static String PREFKEY_URL_LOGIN_CHECK 				= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/sessions" 							: "https://kreyos-members.herokuapp.com/api/sessions";
    public static String PREFKEY_URL_SESSION_KEY				= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/persistence" 						: "https://kreyos-members.herokuapp.com/api/persistence";
    public static String PREFKEY_URL_USER_UPDATE				= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/users/update" 						: "https://kreyos-members.herokuapp.com/api/users/update";
    public static String PREFKEY_URL_USER_ACTIVITIES			= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/activities"						: "https://kreyos-members.herokuapp.com/api/activities";
    public static String PREFKEY_URL_FIRMWARE					= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/firmwares/latest_firmware" 		: "https://kreyos-members.herokuapp.com/api/firmwares/latest_firmware";
    public static String PREFKEY_URL_DELETE_SESSION				= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/logout" 							: "https://kreyos-members.herokuapp.com/api/logout";
    public static String PREFKEY_URL_FACEBOOK_LOGIN				= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/login_via_facebook" 				: "https://kreyos-members.herokuapp.com/api/login_via_facebook";
    public static String PREFKEY_URL_GET_USER_ACTIVITIES		= PREFKEY_IS_DEV_MODE ? PREFKEY_LOCAL_IP + "/api/activities" 						: "https://kreyos-members.herokuapp.com/api/activities";

    public static String PREFKEY_BT_DEVICE_NAME 				= "bluetooth.device_name";
    public static String PREFKEY_DEVICE_ID                      = "device_id";

    public static final String PREFKEY_FB_ACCESS_TOKEN          = "CAAIZCqnLR918BANlpMZBB7aTE4EGU6ecl9b3ZBH9Ya1XYXJkIDa2aHldG6Ud5pYHWKKmbBc9PZB3DiPH7JiNUOcKDZCMrGZBHLjP1633jXjRfSM4oj9qpL4XVvCRARE0jIkr0Cnj8et4uwbvZBdZA3mWVIf0nSZCZBZC5N2pUQKu4sSdTr6wcevq4iZAO4CmN0KYssVtC2d7lZBU12QZDZD";
    public static final String PREFKEY_FB_ACCESS_EXPIRES        = "1396260000";
    public static final String PREFKEY_TW_ACCESS_TOKEN          = "twitter_access_token";
    public static final String PREFKEY_TW_SECRET_TOKEN          = "twitter_secret_token";

    public static final String PREFKEY_NOTIF_FACEBOOK           = "notification.facebook";
    public static final String PREFKEY_NOTIF_WEATHER            = "notification.weather";
    public static final String PREFKEY_NOTIF_TWITTER            = "notification.twitter";
    public static final String PREFKEY_NOTIF_REMINDER           = "notification.reminder";
    public static final String PREFKEY_NOTIF_SMS                = "notification.sms";
    public static final String PREFKEY_NOTIF_CALL               = "notification.call";
    public static final String PREFKEY_NOTIF_LOW_BATTERY        = "notification.low_battery";
    public static final String PREFKEY_NOTIF_OUT_OF_RANGE       = "notification.bt_outof_range";

}
