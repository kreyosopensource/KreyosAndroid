package com.kreyos.watch;

public class KreyosPrefKeys 
{
	// + ET 04232014 : PREF KEYS
	public static String FIRMWARE_VERSION 				= "firmware.version";
	public static String SPORTS_GOAL_STEPS				= "sports_goals.steps";
	public static String USER_EMAIL 					= "user.email";
	public static String USER_EMAIL_PASSWORD 			= "user.email_password";
	public static String USER_EMAIL_PASSWORD_CONFIRM 	= "user.email_passowrd_confirm";
	public static String USER_FIRST_VIEW 				= "user.first_view";
	public static String USER_WATCHED_UNLOCK 			= "user.watch_lock";
	public static String USER_WATCHED_STATE				= "user.watch_state";
	public static String USER_TUTORIAL_CHECK			= "user.tutorial_Check"; 
	public static String USER_IMAGE						= "user.image";
	public static String USER_FB_TOKEN					= "user.fb_token";
	public static String USER_KREYOS_TOKEN				= "user.kreyos_token";
	public static String USER_FIRST_NAME 				= "user.first_name";
	public static String USER_LAST_NAME					= "user.last_name";
	public static String USER_BIRTHDAY 					= "user.birthday";
	public static String USER_GENDER 					= "user.gender";
	public static String USER_WEIGHT 					= "user.weight";
	public static String USER_HEIGHT 					= "user.height";
	public static String USER_FB_ID						= "user.fb_id";
	public static String USER_FB_IMAGE					= "user.fb_image";
	public static Boolean IS_DEV_MODE 					= false;
	public static Boolean IS_LIVE_ON 					= true;
	public static String LOCAL_IP						= "http://192.168.0.105:3000";
	public static String LIVE_URL						= IS_LIVE_ON ? "https://members.kreyos.com/" 						: "https://kreyos-members.herokuapp.com/";
	public static String URL_CREATE_ACCOUNT 			= IS_DEV_MODE ? LOCAL_IP + "/api/users" 							: LIVE_URL + "api/users";
	public static String URL_CHECK_MAIL 				= IS_DEV_MODE ? LOCAL_IP + "/api/users/check_email" 				: LIVE_URL + "api/users/check_email";
	public static String URL_LOGIN_CHECK 				= IS_DEV_MODE ? LOCAL_IP + "/api/sessions" 							: LIVE_URL + "api/sessions";
	public static String URL_SESSION_KEY				= IS_DEV_MODE ? LOCAL_IP + "/api/persistence" 						: LIVE_URL + "api/persistence";
	public static String URL_USER_UPDATE				= IS_DEV_MODE ? LOCAL_IP + "/api/users/update" 						: LIVE_URL + "users/update";
	public static String URL_USER_ACTIVITIES			= IS_DEV_MODE ? LOCAL_IP + "/api/activities"						: LIVE_URL + "api/activities";
	public static String URL_FIRMWARE					= IS_DEV_MODE ? LOCAL_IP + "/api/firmwares/latest_firmware" 		: LIVE_URL + "api/firmwares/latest_firmware";
	public static String URL_DELETE_SESSION				= IS_DEV_MODE ? LOCAL_IP + "/api/logout" 							: LIVE_URL + "api/logout";
	public static String URL_FACEBOOK_LOGIN				= IS_DEV_MODE ? LOCAL_IP + "/api/login_via_facebook" 				: LIVE_URL + "api/login_via_facebook";
	public static String URL_GET_USER_ACTIVITIES		= IS_DEV_MODE ? LOCAL_IP + "/api/activities" 						: LIVE_URL + "api/activities";
}
