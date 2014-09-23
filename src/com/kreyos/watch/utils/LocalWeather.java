package com.kreyos.watch.utils;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;

public class LocalWeather extends WwoApi {
	public static final String FREE_API_ENDPOINT = "http://api.worldweatheronline.com/free/v1/weather.ashx";
	public static final String PREMIUM_API_ENDPOINT = "http://api.worldweatheronline.com/premium/v1/premium-weather-V2.ashx";

	public LocalWeather(boolean freeAPI) {
		super(freeAPI);
		if(freeAPI) {
			apiEndPoint = FREE_API_ENDPOINT;
		} else {
			apiEndPoint = PREMIUM_API_ENDPOINT;
		}
	}

	public Data callAPI(String query) {
		return getLocalWeatherData(getInputStream(apiEndPoint + query));
	}

	Data getLocalWeatherData(InputStream is) {
		Data weather = null;

		try {
			Log.d("WWO", "getLocalWeatherData");

	        XmlPullParser xpp = getXmlPullParser(is);

	        weather = new Data();
	        CurrentCondition cc = new CurrentCondition();
	        weather.current_condition = cc;

	        cc.temp_C = getTextForTag(xpp, "temp_C");
	        cc.weatherIconUrl = getDecode(getTextForTag(xpp, "weatherIconUrl"));
	        cc.weatherDesc = getDecode(getTextForTag(xpp, "weatherDesc"));

	        Log.d("WWO", "getLocalWeatherData:"+cc.temp_C);
	        Log.d("WWO", "getLocalWeatherData:"+cc.weatherIconUrl);
	        Log.d("WWO", "getLocalWeatherData:"+cc.weatherDesc);
		} catch (Exception e) {

		}

		return weather;
	}

	public class Params extends RootParams {
		String q;					//required
		String extra;
		String num_of_days="1";		//required
		String date;
		String fx="no";
		String cc;					//default "yes"
		String includeLocation;		//default "no"
		String format;				//default "xml"
		String show_comments="no";
		String callback;
		String key;					//required

		public Params(String key) {
			num_of_days = "1";
			fx = "no";
			show_comments = "no";
			this.key = key;
		}

		public Params setQ(String q) {
			this.q = q;
			return this;
		}

		Params setExtra(String extra) {
			this.extra = extra;
			return this;
		}

		Params setNumOfDays(String num_of_days) {
			this.num_of_days = num_of_days;
			return this;
		}

		Params setDate(String date) {
			this.date = date;
			return this;
		}

		Params setFx(String fx) {
			this.fx = fx;
			return this;
		}

		Params setCc(String cc) {
			this.cc = cc;
			return this;
		}

		Params setIncludeLocation(String includeLocation) {
			this.includeLocation = includeLocation;
			return this;
		}

		Params setFormat(String format) {
			this.format = format;
			return this;
		}

		Params setShowComments(String showComments) {
			this.show_comments = showComments;
			return this;
		}

		Params setCallback(String callback) {
			this.callback = callback;
			return this;
		}

		Params setKey(String key) {
			this.key = key;
			return this;
		}
	}

	public class Data {
		Request request;
		public CurrentCondition current_condition;
		Weather weather;
	}

	class Request {
		String type;
		String query;
	}

	public class CurrentCondition {
	    public String observation_time;
	    public String temp_C;
	    String weatherCode;
	    String weatherIconUrl;
	    public String weatherDesc;
	    String windspeedMiles;
	    public String windspeedKmph;
	    String winddirDegree;
	    String winddir16Point;
	    String precipMM;
	    public String humidity;
	    String visibility;
	    String pressure;
	    String cloudcover;
	}

	class Weather {
	    String date;
	    String tempMaxC;
	    String tempMaxF;
	    String tempMinC;
	    String tempMinF;
	    String windspeedMiles;
	    String windspeedKmph;
	    String winddirection;
	    String weatherCode;
	    String weatherIconUrl;
	    String weatherDesc;
	    String precipMM;
	}
}
