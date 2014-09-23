package com.kreyos.watch.bluetooth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import com.kreyos.watch.KreyosActivity;
import com.kreyos.watch.bluetooth.BluetoothAgent.JobDesc;
import com.kreyos.watch.objectdata.ActivityDataDoc;
import com.kreyos.watch.objectdata.ActivityDataRow;
import com.kreyos.watch.objectdata.SportsDataRow;
import com.kreyos.watch.objectdata.TodayActivity;
import com.kreyos.watch.utils.Utils;



public class Protocol {

	public static class MessageID {
		public static final int MSG_BLUETOOTH_START = 0;
		public static final int MSG_FILE_RECEIVED = 1;
		public static final int MSG_FILE_LISTED = 2;
		public static final int MSG_DEVICE_ID_GOT = 3;
		public static final int MSG_DEVICE_STATUS_GOT = 4;
		public static final int MSG_ACTIVITY_DATA_GOT = 5;
		public static final int MSG_GRID_GOT = 6;
		public static final int MSG_ACTIVITY_DATA_END = 7;
		public static final int MSG_ACTIVITY_PREPARE = 8;
		public static final int MSG_ACTIVITY_SYNC = 9;
		public static final int MSG_LAUNCH_GOOGLENOW = 10;
		public static final int MSG_FIRMWARE_VERSION = 11;
		public static final int MSG_FILE_UPLOAD_STATUS = 12;
		public static final int MSG_TODAY_ACTIVITY = 13;

		public static final int MSG_BLUETOOTH_STATUS = 20;
	}

	public static final int headVersion = 0;
	public static final int headFlag = 1;
	public static final int bodyLength = 2;
	public static final int packSequence = 3;

	public static final int elementType = 0;
	public static final int elementLength = 1;

	public static final String elementTypeEcho = "E";
	public static final String elementTypeClock = "C";
	public static final String elementTypeMsgSMS = "MS";
	public static final String elementTypeMsgFB = "MF";
	public static final String elementTypeMsgTWI = "MT";

	public static final String msgSubTypeIdentity = "i";
	public static final String msgSubTypeMessage = "d";

	public static final int maxBodySize = 200;
	public static final int headSize = 4;
	public static final int elementHeadSize = 2;
	public static final byte continueElementTypeMarker = (byte) 0x80;

	private static final int STLV_INVALID_HANDLE = -1;
	private static final int STLV_PACKET_MAX_BODY_SIZE = 240;
	private static final int STLV_HEAD_SIZE = 4;
	private static final int STLV_PACKET_MAX_SIZE = (STLV_PACKET_MAX_BODY_SIZE + STLV_HEAD_SIZE);
	private static final int MAX_ELEMENT_NESTED_LAYER = 4;
	private static final int MIN_ELEMENT_SIZE = 2;
	private static final int MAX_ELEMENT_TYPE_SIZE = 3;
	private static final int MAX_ELEMENT_TYPE_BUFSIZE = (MAX_ELEMENT_TYPE_SIZE + 1);
	private static final int HEADFIELD_VERSION = 0;
	private static final int HEADFIELD_FLAG = 1;
	private static final int HEADFIELD_BODY_LENGTH = 2;
	private static final int HEADFIELD_SEQUENCE = 3;
	private static final int ELEMENT_TYPE_CLOCK = 'C';
	private static final int ELEMENT_TYPE_ECHO = 'E';
	private static final int ELEMENT_TYPE_SPORT_HEARTBEAT = 'H';
	private static final int ELEMENT_TYPE_GET_FILE = 'G';
	private static final int ELEMENT_TYPE_GET_DATA = 'A';
	private static final int SUB_TYPE_SPORTS_DATA_ID = 'i';
	private static final int SUB_TYPE_SPORTS_DATA_DATA = 'd';
	private static final int SUB_TYPE_SPORTS_DATA_FLAG = 'f';

	private static final int ELEMENT_TYPE_GET_GRID = 'R';
	private static final int ELEMENT_TYPE_SN = 'S';
	private static final int ELEMENT_TYPE_WATCHFACE = 'W';
	private static final int ELEMENT_TYPE_ANDROID_VER = '&';

	private static final int ELEMENT_TYPE_FILE = 'F';
	private static final int SUB_TYPE_FILE_NAME = 'n';
	private static final int SUB_TYPE_FILE_DATA = 'd';
	private static final int SUB_TYPE_FILE_END = 'e';
	private static final int ELEMENT_TYPE_MESSAGE = 'M';
	private static final int ELEMENT_TYPE_MESSAGE_SMS = 'S';
	private static final int ELEMENT_TYPE_MESSAGE_FB = 'F';
	private static final int ELEMENT_TYPE_MESSAGE_TW = 'T';
	private static final int SUB_TYPE_MESSAGE_IDENTITY = 'i';
	private static final int SUB_TYPE_MESSAGE_MESSAGE = 'd';

	private static final int ELEMENT_TYPE_ACTIVITY = 'Z';
	private static final int SUB_TYPE_ACTIVITY_UTC = 't';
	private static final int SUB_TYPE_ACTIVITY_LAT = 'l';
	private static final int SUB_TYPE_ACTIVITY_LON = 'n';
	private static final int SUB_TYPE_ACTIVITY_ALT = 'a';
	private static final int SUB_TYPE_ACTIVITY_SPD = 's';
	private static final int SUB_TYPE_ACTIVITY_DIS = 'd';
	private static final int SUB_TYPE_ACTIVITY_HRT = 'h';
	private static final int SUB_TYPE_ACTIVITY_CAL = 'c';
	private static final int SUB_TYPE_ACTIVITY_ID = 'i';

	private static final int ELEMENT_TYPE_GOOGLENOW = '/';
	private static final int ELEMENT_TYPE_FIRMWARE_VERSION = 'V';
	private static final int ELEMENT_TYPE_ACTIVITY_DATA = 'N';
	private static final int ELEMENT_TYPE_UNLOCK_WATCH = 'U';

	private static final int ELEMENT_TYPE_DAILY_ACTIVITY = '0';
	private static final int SUB_TYPE_TODAY_ATIME = '1';
	private static final int SUB_TYPE_TODAY_STEPS = '2';
	private static final int SUB_TYPE_TODAY_CAL = '3';
	private static final int SUB_TYPE_TODAY_DIST = '4';

	private BluetoothAgent btAgent = null;
	private Handler mMsgHandler = null;
	private Handler mServiceMsgHandler = null;
	private Context mContext = null;

	public Protocol(BluetoothAgent btAgent, Context context) {
		if (btAgent != null)
			this.btAgent = btAgent;
		mContext = context;
	}

	public void bindMessageHandler(Handler msgHandler) {
		mMsgHandler = msgHandler;
	}

	public void bindServiceMsgHandler(Handler handler) {
		mServiceMsgHandler = handler;
	}

	private int buildPacketHead(byte[] packet) {
		byte[] header = packet;
		header[headVersion] = (byte) 0x01;
		header[headFlag] = (byte) 0x80;
		header[bodyLength] = (byte) (packet.length - headSize);
		header[packSequence] = (byte) 0x00;
		return headSize;
	}

	private int buildElementHead(byte[] packet, int offset, String type,
			byte len) {
		byte[] typeBuffer = type.getBytes();
		for (int i = 0; i < typeBuffer.length; ++i) {
			if (i != typeBuffer.length - 1) {
				packet[offset + i] = (byte) (typeBuffer[i] | continueElementTypeMarker);
			} else {
				packet[offset + i] = typeBuffer[i];
			}
		}

		packet[offset + typeBuffer.length] = len;
		return typeBuffer.length + 1;
	}

	private int buildElement(byte[] packet, int offset, String type, byte[] data) {
		int cursor = offset;
		cursor += buildElementHead(packet, cursor, type, (byte) data.length);
		cursor += fillElementData(packet, cursor, data, 0, data.length);
		return cursor - offset;
	}

	private int fillElementData(byte[] packet, int offset, byte[] data,
			int from, int len) {
		System.arraycopy(data, from, packet, offset, len);
		return len;
	}

	private int estimateElementHeadSize(String type) {
		return estimateStringSize(type) + 1;
	}

	private int estimateStringSize(String type) {
		byte[] typeBuffer = type.getBytes();
		return typeBuffer.length;
	}

	private byte[] buildSingleElementPacket(String type, byte[] data) {
		int packetSize = headSize + estimateElementHeadSize(type) + data.length;
		int buildOffset = 0;
		byte[] packet = new byte[packetSize];
		buildOffset = buildPacketHead(packet);
		buildOffset += buildElementHead(packet, buildOffset, type,
				(byte) data.length);
		buildOffset += fillElementData(packet, buildOffset, data, 0,
				data.length);
		return packet;
	}

	public void echo(String hint) {
		byte[] data = hint.getBytes();
		byte[] packet = buildSingleElementPacket(elementTypeEcho, data);
		btAgent.sendBytes(packet);
	}

	public void readFile(String filename) {
		byte[] data = filename.getBytes();
		byte[] packet = buildSingleElementPacket("G", data);
		btAgent.sendBytes(packet);
	}

	public void getSportsData() {
		byte[] data = "dummy".getBytes();
		byte[] packet = buildSingleElementPacket("A", data);
		btAgent.sendBytes(packet);
	}

	public void getSportsGrid() {
		byte[] data = "dummy".getBytes();
		byte[] packet = buildSingleElementPacket("R", data);
		btAgent.sendBytes(packet);
	}

	public void syncTime() {
		byte[] packet = buildSyncTimePack();
		btAgent.sendBytes(packet);
	}

	public byte[] buildSyncTimePack() {
		Calendar ca = Calendar.getInstance();
		byte[] data = new byte[8];
		data[0] = (byte) (ca.get(Calendar.YEAR) % 100);
		data[1] = (byte) ca.get(Calendar.MONTH);
		data[2] = (byte) ca.get(Calendar.DATE);
		data[3] = (byte) ca.get(Calendar.HOUR);
		if (ca.get(Calendar.AM_PM) == Calendar.PM)
			data[3] += 12;
		data[4] = (byte) ca.get(Calendar.MINUTE);
		data[5] = (byte) ca.get(Calendar.SECOND);

		data[6] = (byte) 0x01; // 0x01 - android, 0x02 - windows phone
		data[7] = (byte) android.os.Build.VERSION.SDK_INT; // android version
		byte[] packet = buildSingleElementPacket(elementTypeClock, data);
		return packet;
	}

	public void notifyMessage(String type, String identity, String message) {

		byte[] identityData = identity.getBytes();
		byte[] messageData = message.getBytes();

		int elementDataLength = estimateElementHeadSize(msgSubTypeIdentity)
				+ identityData.length
				+ estimateElementHeadSize(msgSubTypeIdentity)
				+ messageData.length;

		int packetLength = headSize + estimateElementHeadSize(type)
				+ elementDataLength;
		if (packetLength > 240) {
			Log.e("Protocol", "Notification Body size exceed max size");
			return;
		}

		int cursor = 0;
		byte[] packet = new byte[packetLength];
		cursor += buildPacketHead(packet);
		cursor += buildElementHead(packet, cursor, type,
				(byte) elementDataLength);
		cursor += buildElement(packet, cursor, msgSubTypeIdentity, identityData);
		cursor += buildElement(packet, cursor, msgSubTypeMessage, messageData);
		btAgent.sendBytes(packet);

	}

	public void getDeviceID() {
		byte[] data = "dummy".getBytes();
		byte[] packet = buildSingleElementPacket("S", data);
		btAgent.sendBytes(packet);
	}

	public void activityHeartbeat(String activityId) {
		byte[] data = activityId.getBytes();
		byte[] packet = buildSingleElementPacket("H", data);
		btAgent.sendBytes(packet);
	}

	public boolean sendStream(String name, InputStream is) {
		byte[] buffer = new byte[300 * 1024]; // max 100k file
		int streamLen = 0;
		while (true) {
			byte[] temp = new byte[1024];
			try {
				int byteRead = is.read(temp);
				if (byteRead == -1) {
					Log.v("Protocol", String.format(
							"Read Stream Completed: %d bytes Read", streamLen));
					break;
				} else if (byteRead + streamLen < buffer.length) {
					System.arraycopy(temp, 0, buffer, streamLen, byteRead);
					streamLen += byteRead;
				} else {
					Log.e("Protocol", String.format(
							"Read Stream Failed: %d bytes Read", streamLen));
					return false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}

		return sendFileBuffer(name, buffer, streamLen);
	}

	public boolean sendFile(String fileName) {

		if (fileName == null) {
			return false;
		}

		File file = new File(mContext.getFilesDir().getPath() + fileName);
		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return false;
		}

		byte[] buffer = new byte[(int) file.length()];
		try {
			if (fileStream.read(buffer) != buffer.length) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}

		return sendFileBuffer(fileName, buffer, (int) file.length());
	}

	private boolean sendFileBuffer(String fileName, byte[] buffer, int fileLen) {
		final int packetSize = 200;
		final String fileEndFlag = "\0";
		ArrayList<JobDesc> jobList = new ArrayList<JobDesc>();
		int sendPos = 0;
		while (sendPos < fileLen) {

			int toSentSize = fileLen - sendPos > packetSize ? packetSize
					: fileLen - sendPos;
			int elementDataLength = 2 + toSentSize;

			if (sendPos == 0) {
				// send file begin
				elementDataLength += (2 + fileName.getBytes().length);
			}

			if (sendPos + packetSize >= fileLen) {
				// send file end
				elementDataLength += (2 + fileEndFlag.getBytes().length);
			}

			int packetLength = headSize + 2 + elementDataLength;

			int cursor = 0;
			byte[] packet = new byte[packetLength];
			cursor += buildPacketHead(packet);
			cursor += buildElementHead(packet, cursor, "F",
					(byte) elementDataLength);
			if (sendPos == 0) {
				// send file begin
				cursor += buildElement(packet, cursor, "n", fileName.getBytes());
			}

			byte[] temp = new byte[toSentSize];
			System.arraycopy(buffer, sendPos, temp, 0, toSentSize);
			cursor += buildElement(packet, cursor, "d", temp);
			if (sendPos + packetSize >= fileLen) {
				// send file end
				cursor += buildElement(packet, cursor, "e",
						fileEndFlag.getBytes());
			}

			JobDesc job = btAgent.new JobDesc(packet, null);
			jobList.add(job);

			sendPos += toSentSize;
		}

		btAgent.batchRegisterJob(jobList);

		return true;
	}

	public void deleteFile(String fileName) {
		byte[] data = fileName.getBytes();
		byte[] packet = buildSingleElementPacket("X", data);
		btAgent.sendBytes(packet);
	}

	public void getActivityData() {
		byte[] data = new byte[1];
		data[0] = 'N';
		byte[] packet = buildSingleElementPacket("N", data);
		btAgent.sendBytes(packet);
	}

	public void listFile(String prefix) {
		byte[] data = prefix.getBytes();
		byte[] packet = buildSingleElementPacket("X", data);
		btAgent.sendBytes(packet);
	}

	public void setWatchAlarm(final int index, final int mode,
			final int monthday, final int weekday, final int hour,
			final int minute) {
		byte[] data = new byte[6];
		data[0] = (byte) index;
		data[1] = (byte) mode;
		data[2] = (byte) monthday;
		data[3] = (byte) weekday;
		data[4] = (byte) hour;
		data[5] = (byte) minute;
		byte[] packet = buildSingleElementPacket("I", data);
		btAgent.sendBytes(packet);
	}

	public void setGestureControl(final boolean enable,
			final boolean isLeftHand, final int[] action_map) {
		byte[] data = new byte[1];
		if (enable) {
			data[0] = 0x01;
		}
		if (isLeftHand) {
			data[0] |= 0x02;
		}
		byte[] packet = buildSingleElementPacket("D", data);
		btAgent.sendBytes(packet);
	}

	public void setWatchGrid() {
		// TODO
	}

	public byte[] buildCompleteSyncPacket() {
		Calendar ca = Calendar.getInstance();
		byte[] data = new byte[6];
		data[0] = (byte) (ca.get(Calendar.YEAR) % 100);
		data[1] = (byte) ca.get(Calendar.MONTH);
		data[2] = (byte) ca.get(Calendar.DATE);
		data[3] = (byte) ca.get(Calendar.HOUR);
		if (ca.get(Calendar.AM_PM) == Calendar.PM)
			data[3] += 12;
		data[4] = (byte) ca.get(Calendar.MINUTE);
		data[5] = (byte) ca.get(Calendar.SECOND);

		return data;
	}

	public void syncWatchConfig(final String[] worldClocks,
			final int[] worldClockOffset, final boolean isDigitalClock,
			final int digitalClock, final int analogClock,
			final int sportsGrid, final int[] sportsGrids, final int[] goals,
			final int weight, final int height, final boolean enableGesture,
			final boolean isLeftHandGesture, final int[] gestureActionsTable,
			final boolean isUkUnit) {
		byte[] data = buildWatchConfig(worldClocks, worldClockOffset,
				isDigitalClock, digitalClock, analogClock, sportsGrid,
				sportsGrids, goals, weight, height, enableGesture,
				isLeftHandGesture, gestureActionsTable, isUkUnit);

		byte[] packet = buildSingleElementPacket("P", data);
		btAgent.sendBytes(packet);
	}

	private static final int confSignature = 0xFACE0001;

	public byte[] buildWatchConfig(final String[] worldClocks,
			final int[] worldClockOffset, final boolean isDigitalClock,
			final int digitalClock, final int analogClock,
			final int sportsGrid, final int[] sportsGrids, final int[] goals,
			final int weight, final int height, final boolean enableGesture,
			final boolean isLeftHandGesture, final int[] gestureActionsTable,
			final boolean isUkUnit) {

		byte[] placeholder = new byte[1];
		placeholder[0] = (byte) 0xab;

		byte[] signatureBytes = intToByteArray(confSignature);
		byte[] worldClocksTable = new byte[6 * 10];
		for (int i = 0; i < 6; ++i) {
			byte[] worldClockName = worldClocks[i].getBytes();
			int size = worldClockName.length < 10 ? worldClockName.length : 10;
			System.arraycopy(worldClockName, 0, worldClocksTable, i * 10, size);
		}

		byte[] worldClocksOffsetTable = new byte[6];
		for (int i = 0; i < 6; ++i) {
			worldClocksOffsetTable[i] = (byte) worldClockOffset[i];
		}

		byte[] clockSettings = new byte[3];
		clockSettings[0] = (byte) (isDigitalClock ? 0x01 : 0x00);
		clockSettings[1] = (byte) digitalClock;
		clockSettings[2] = (byte) analogClock;

		byte[] grid = new byte[1];
		grid[0] = (byte) sportsGrid;

		byte[] gridTable = new byte[5];
		for (int i = 0; i < 5; ++i) {
			gridTable[i] = (byte) sportsGrids[i];
		}

		byte[] profile = new byte[3];
		profile[0] = (byte) weight;
		profile[1] = (byte) height;
		profile[2] = (byte) 80; // TODO

		byte[] goalSteps = shortToByteArray((short) goals[0]);
		byte[] goalCalories = shortToByteArray((short) goals[1]);
		byte[] goalDistance = shortToByteArray((short) goals[2]);

		byte[] gestureFlag = new byte[1];
		gestureFlag[0] = 0;
		if (enableGesture)
			gestureFlag[0] |= 0x01;
		if (isLeftHandGesture)
			gestureFlag[0] |= 0x2;

		byte[] gestureActions = new byte[4];
		for (int i = 0; i < 4; ++i) {
			gestureActions[i] = (byte) (gestureActionsTable[i]);
		}

		byte[] lapLenBuf = shortToByteArray((short) 400);

		byte[] isUkUnitBuf = new byte[1];
		isUkUnitBuf[0] = isUkUnit ? (byte) 0x01 : 0x00;

		byte[] data = new byte[placeholder.length + signatureBytes.length
				+ worldClocksTable.length + worldClocksOffsetTable.length
				+ clockSettings.length + grid.length + gridTable.length
				+ goalSteps.length + goalCalories.length + goalDistance.length
				+ profile.length + gestureFlag.length + gestureActions.length
				+ lapLenBuf.length + isUkUnitBuf.length + 1];

		int cursor = 0;

		System.arraycopy(placeholder, 0, data, cursor, placeholder.length);
		cursor += placeholder.length;

		System.arraycopy(signatureBytes, 0, data, cursor, signatureBytes.length);
		cursor += signatureBytes.length;

		System.arraycopy(goalSteps, 0, data, cursor, goalSteps.length);
		cursor += goalSteps.length;

		System.arraycopy(goalCalories, 0, data, cursor, goalCalories.length);
		cursor += goalCalories.length;

		System.arraycopy(goalDistance, 0, data, cursor, goalDistance.length);
		cursor += goalDistance.length;

		System.arraycopy(lapLenBuf, 0, data, cursor, lapLenBuf.length);
		cursor += lapLenBuf.length;

		System.arraycopy(worldClocksTable, 0, data, cursor,
				worldClocksTable.length);
		cursor += worldClocksTable.length;

		System.arraycopy(worldClocksOffsetTable, 0, data, cursor,
				worldClocksOffsetTable.length);
		cursor += worldClocksOffsetTable.length;

		System.arraycopy(clockSettings, 0, data, cursor, clockSettings.length);
		cursor += clockSettings.length;

		System.arraycopy(grid, 0, data, cursor, grid.length);
		cursor += grid.length;

		System.arraycopy(gridTable, 0, data, cursor, gridTable.length);
		cursor += gridTable.length;

		System.arraycopy(isUkUnitBuf, 0, data, cursor, isUkUnitBuf.length);
		cursor += isUkUnitBuf.length;

		System.arraycopy(profile, 0, data, cursor, profile.length);
		cursor += profile.length;

		System.arraycopy(gestureFlag, 0, data, cursor, gestureFlag.length);
		cursor += gestureFlag.length;

		System.arraycopy(gestureActions, 0, data, cursor, gestureActions.length);
		cursor += gestureActions.length;

		return data;
	}

	public static byte[] longToByteArray(long s) {
		byte[] targets = new byte[8];
		for (int i = 0; i < 8; i++) {
			int offset = (targets.length - 1 - i) * 8;
			targets[i] = (byte) ((s >>> offset) & 0xff);
		}
		return targets;
	}

	public static byte[] intToByteArray(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) ((i >> 24) & 0xFF);
		result[1] = (byte) ((i >> 16) & 0xFF);
		result[2] = (byte) ((i >> 8) & 0xFF);
		result[3] = (byte) ((i >> 0) & 0xFF);
		return result;
	}

	public static byte[] shortToByteArray(short s) {
		byte[] targets = new byte[2];
		targets[0] = (byte) ((s >> 8) & 0xff);
		targets[1] = (byte) ((s >> 0) & 0xff);
		return targets;
	}

	public void unlockWatch() {
		byte[] data = new byte[1];
		data[0] = 1;
		byte[] packet = buildSingleElementPacket("U", data);
		btAgent.sendBytes(packet);
	}

	public void sendRawGPSInfo(short spd, short alt, int distance) {
		byte[] spdBuf = shortToByteArray(spd);
		byte[] altBuf = shortToByteArray(alt);
		byte[] disBuf = intToByteArray(distance);
		byte[] calBuf = intToByteArray(0);

		int elementDataLength = 0;
		if (spd != -1)
			elementDataLength += (2 + spdBuf.length);
		if (alt != -1)
			elementDataLength += (2 + altBuf.length);
		elementDataLength += (2 + disBuf.length + 2 + calBuf.length);

		int packetLength = headSize + estimateElementHeadSize("Z")
				+ elementDataLength;

		int cursor = 0;
		byte[] packet = new byte[packetLength];
		cursor += buildPacketHead(packet);
		cursor += buildElementHead(packet, cursor, "Z",
				(byte) elementDataLength);
		if (spd != -1)
			cursor += buildElement(packet, cursor, "s", spdBuf);
		if (alt != -1)
			cursor += buildElement(packet, cursor, "a", altBuf);
		cursor += buildElement(packet, cursor, "d", disBuf);
		cursor += buildElement(packet, cursor, "c", calBuf);
		btAgent.sendBytes(packet);
	}

	public void sendGPSInfo(Location location, int distance, int calories) {
		short spd = (short) (location.getSpeed() * 100);
		short alt = (short) location.getAltitude();

		if (location.hasSpeed())
			spd = -1;
		if (location.hasAltitude())
			alt = -1;

		sendRawGPSInfo(spd, alt, distance);
	}

	public void sendDailyActivityRequest() {
		byte[] dymmData = new byte[2];
		dymmData[0] = 0;
		dymmData[1] = 0;

		byte[] packet = buildSingleElementPacket("0", dymmData);
		btAgent.sendBytes(packet);
	}

	private String getDataFileName(String pathName) {
		int pos = pathName.lastIndexOf('/');
		return pathName.substring(pos + 1);
	}

	private FileOutputStream mOFileStream = null;
	private String mOFileName = "";
	private ArrayList<byte[]> mFileRawData = new ArrayList<byte[]>();
	public boolean readingFile = false;

	public void handle_file(byte[] pack, int handle) {

		int element = get_first_sub_element(pack, handle);
		while (element != -1) {
			byte[] type_buf = get_element_type(pack, element);
			switch (type_buf[0]) {
			case SUB_TYPE_FILE_NAME: {
				readingFile = true;
				byte[] file_name_data = get_element_data(pack, element);
				String path_name = new String(file_name_data);
				String file_name = getDataFileName(path_name);
				Log.v("Protocol",
						String.format("handle_file(fn=%s)", file_name));
				if (mOFileName != file_name) {
					/*
					 * if (mOFileStream != null) { try { mOFileStream.close(); }
					 * catch (IOException e) { } mOFileStream = null; } )
					 */
					if (mFileRawData != null) {
						mFileRawData = null;
					}
					mOFileName = file_name;
				}
				if (mFileRawData == null) {
					mFileRawData = new ArrayList<byte[]>();
				}
				/*
				 * if (mOFileStream == null){ File file = new
				 * File(mContext.getFilesDir().getPath() + file_name); try {
				 * mOFileStream = new FileOutputStream(file); } catch
				 * (FileNotFoundException e) { return; } }
				 */
			}
				break;

			case SUB_TYPE_FILE_DATA: {
				byte[] file_data = get_element_data(pack, element);
				Log.v("Protocol",
						String.format("handle_file(size=%d)", file_data.length));
				if (mFileRawData == null) {
					mFileRawData = new ArrayList<byte[]>();
				}

				mFileRawData.add(file_data);

				/*
				 * if (mOFileStream == null) return;
				 * 
				 * try { mOFileStream.write(file_data); mOFileStream.flush(); }
				 * catch (IOException e) { }
				 */
			}
				break;

			case SUB_TYPE_FILE_END:
				Log.v("Protocol", String.format("handle_file(end)"));

				/*
				 * if (mOFileStream == null) return;
				 * 
				 * try { mOFileStream.close(); } catch (IOException e) { } if
				 * (mMsgHandler != null) {
				 * mMsgHandler.obtainMessage(MessageID.MSG_FILE_RECEIVED,
				 * mOFileName).sendToTarget(); } if (mServiceMsgHandler != null)
				 * {
				 * mServiceMsgHandler.obtainMessage(MessageID.MSG_FILE_RECEIVED,
				 * mOFileName).sendToTarget(); } mOFileStream = null;
				 */
				byte[] buffer = buildDataBuffer(mFileRawData);
				ActivityDataDoc doc = LoadDataFromBuffer(buffer);
				if (doc == null) {
					this.deleteFile(String.format("/DATA/%s", mOFileName));
				} else {
					if (mMsgHandler != null) {
						mMsgHandler.obtainMessage(MessageID.MSG_FILE_RECEIVED,
								doc).sendToTarget();
					}
					if (mServiceMsgHandler != null) {
						mServiceMsgHandler.obtainMessage(
								MessageID.MSG_FILE_RECEIVED, doc)
								.sendToTarget();
					}
				}
				mOFileName = "";
				mFileRawData = null;

				readingFile = false;
				break;
			}
			element = get_next_sub_element(pack, handle, element);
		}

	}

	private byte[] buildDataBuffer(ArrayList<byte[]> buflist) {
		int size = 0;
		for (byte[] buf : buflist) {
			size += buf.length;
		}

		int cursor = 0;
		byte[] ret = new byte[size];
		for (byte[] buf : buflist) {
			System.arraycopy(buf, 0, ret, cursor, buf.length);
			cursor += buf.length;
		}
		return ret;
	}

	public void handle_today_activity(byte[] pack, int handle) {

		TodayActivity ta = new TodayActivity();

		int element = get_first_sub_element(pack, handle);
		while (element != -1) {
			byte[] type_buf = get_element_type(pack, element);
			byte[] value_buf = get_element_data(pack, element);
			switch (type_buf[0]) {
			case SUB_TYPE_TODAY_ATIME:
				ta.time = bytesToShort(value_buf, 0);
				break;
			case SUB_TYPE_TODAY_STEPS:
				ta.steps = bytesToShort(value_buf, 0);
				break;
			case SUB_TYPE_TODAY_CAL:
				ta.calories = bytesToInt(value_buf, 0) / 100;
				break;
			case SUB_TYPE_TODAY_DIST:
				ta.distance = (double) bytesToInt(value_buf, 0) / 100;
				break;
			}
			element = get_next_sub_element(pack, handle, element);
		}

		if (mMsgHandler != null) {
			mMsgHandler.obtainMessage(MessageID.MSG_TODAY_ACTIVITY, ta)
					.sendToTarget();
		}
		if (mServiceMsgHandler != null) {
			mServiceMsgHandler.obtainMessage(MessageID.MSG_TODAY_ACTIVITY, ta)
					.sendToTarget();
		}
	}

	public void handle_activity(byte[] pack, int handle) {

		int activityId = 0;
		int sports_type = 0;
		byte[] sportsData = null;
		SportsDataRow sportsDatRow = null;
		boolean isEnd = false;
		int msgId = MessageID.MSG_ACTIVITY_DATA_GOT;

		int element = get_first_sub_element(pack, handle);
		while (element != -1) {
			byte[] type_buf = get_element_type(pack, element);
			switch (type_buf[0]) {
			case SUB_TYPE_SPORTS_DATA_ID: {
				byte[] buf = get_element_data(pack, element);
				activityId = (int) buf[0];
			}
				break;

			case SUB_TYPE_SPORTS_DATA_DATA: {
				sportsData = get_element_data(pack, element);
				sportsDatRow = SportsDataRow.loadFromBuffer(sportsData);
			}
				break;

			case SUB_TYPE_SPORTS_DATA_FLAG: {
				byte[] buf = get_element_data(pack, element);
				if ((buf[0] & 0x02) != 0) {
					msgId = MessageID.MSG_ACTIVITY_DATA_END;
				} else if ((buf[0] & 0x04) != 0) {
					msgId = MessageID.MSG_ACTIVITY_PREPARE;
				} else if ((buf[0] & 0x08) != 0) {
					msgId = MessageID.MSG_ACTIVITY_SYNC;
				}

				if ((buf[0] & 0x10) != 0) {
					sports_type = SportsDataRow.DataType.SPORTS_MODE_BIKING;
				} else if ((buf[0] & 0x20) != 0) {
					sports_type = SportsDataRow.DataType.SPORTS_MODE_RUNNING;
				}
			}
				break;
			}
			element = get_next_sub_element(pack, handle, element);
		}

		if (sportsDatRow != null) {
			sportsDatRow.sports_mode = sports_type;
		}

		if (mMsgHandler != null && msgId != MessageID.MSG_ACTIVITY_SYNC) {
			mMsgHandler.obtainMessage(msgId, sportsDatRow).sendToTarget();
		}
		if (mServiceMsgHandler != null) {
			mServiceMsgHandler.obtainMessage(msgId, sportsDatRow)
					.sendToTarget();
		}

	}

	public void handlePacket(byte[] packet) {
		byte[] pack = packet;

		int handle = get_first_element(packet);
		while (handle != -1) {
			byte[] type_buf = get_element_type(pack, handle);
			switch (type_buf[0]) {
			case ELEMENT_TYPE_FILE:
				handle_file(pack, handle);
				break;
			case ELEMENT_TYPE_GET_DATA: {
				byte[] data_buf = get_element_data(pack, handle);
				handle_activity(pack, handle);
			}
				break;

			case ELEMENT_TYPE_GET_GRID: {
				byte[] data_buf = get_element_data(pack, handle);
				if (mMsgHandler != null) {
					mMsgHandler.obtainMessage(MessageID.MSG_GRID_GOT, data_buf)
							.sendToTarget();
				}
				if (mServiceMsgHandler != null) {
					mServiceMsgHandler.obtainMessage(MessageID.MSG_GRID_GOT,
							data_buf).sendToTarget();
				}
			}
				break;

			case ELEMENT_TYPE_SN: {
				byte[] data_buf = get_element_data(pack, handle);
				if (mMsgHandler != null) {
					mMsgHandler.obtainMessage(MessageID.MSG_DEVICE_ID_GOT,
							data_buf).sendToTarget();
				}
				if (mServiceMsgHandler != null) {
					mServiceMsgHandler.obtainMessage(
							MessageID.MSG_DEVICE_ID_GOT, data_buf)
							.sendToTarget();
				}
			}
				break;

			case ELEMENT_TYPE_GOOGLENOW: {
				if (mServiceMsgHandler != null) {
					mServiceMsgHandler.obtainMessage(
							MessageID.MSG_LAUNCH_GOOGLENOW).sendToTarget();
				}
			}
				break;

			case ELEMENT_TYPE_FIRMWARE_VERSION: {
				byte[] data_buf = get_element_data(pack, handle);
				String version = new String(data_buf);
								
				if (mMsgHandler != null) {
					mMsgHandler.obtainMessage(MessageID.MSG_FIRMWARE_VERSION,
							version).sendToTarget();
				}
				if (mServiceMsgHandler != null) {
					mServiceMsgHandler.obtainMessage(
							MessageID.MSG_FIRMWARE_VERSION, version)
							.sendToTarget();
				}
			}
				break;

			case ELEMENT_TYPE_DAILY_ACTIVITY: {
				handle_today_activity(pack, handle);
			}
				break;

			}

			handle = get_next_element(pack, handle);

		}

	}

	int GET_PACKET_END(byte[] pack) {
		return (STLV_HEAD_SIZE + get_body_length(pack));
	}

	int get_version(byte[] pack) {
		return pack[HEADFIELD_VERSION];
	}

	int get_body_length(byte[] pack) {
		return (int) (pack[HEADFIELD_BODY_LENGTH]) & 0xff;
	}

	int get_sequence(byte[] pack) {
		return pack[HEADFIELD_SEQUENCE];
	}

	int get_flag(byte[] pack) {
		return pack[HEADFIELD_FLAG];
	}

	int get_first_element(byte[] pack) {
		if (get_body_length(pack) >= MIN_ELEMENT_SIZE)
			return STLV_HEAD_SIZE;
		else
			return STLV_INVALID_HANDLE;
	}

	int get_next_element(byte[] pack, int handle) {
		byte[] typebuf = get_element_type(pack, handle);
		int len = get_element_data_size(pack, handle);
		if (GET_PACKET_END(pack) - (handle + len + typebuf.length) >= MIN_ELEMENT_SIZE)
			return handle + len;
		else
			return STLV_INVALID_HANDLE;
	}

	int get_first_sub_element(byte[] pack, int parent) {
		byte[] elementType = get_element_type(pack, parent);
		return parent + elementType.length + 1;
	}

	int get_next_sub_element(byte[] pack, int parent, int handle) {
		byte[] parentType = get_element_type(pack, parent);
		int parent_body_len = get_element_data_size(pack, parent);

		byte[] elementType = get_element_type(pack, handle);
		int element_body_len = get_element_data_size(pack, handle);

		int parent_end = parent + parentType.length + parent_body_len;
		int element_end = handle + elementType.length + element_body_len;

		if (parent_end - element_end < MIN_ELEMENT_SIZE)
			return STLV_INVALID_HANDLE;
		else
			return element_end + 1;

	}

	byte[] get_element_type(byte[] pack, int handle) {
		byte[] buf = new byte[8];
		int cursor = 0;
		int pos = handle;
		while ((pack[pos] & 0x80) != 0) {
			if (cursor < buf.length)
				buf[cursor] = (byte) (pack[pos] & ~0x80);
			pos++;
			cursor++;
		}
		if (cursor < buf.length)
			buf[cursor] = (byte) (pack[pos] & ~0x80);

		byte[] result = new byte[cursor + 1];
		System.arraycopy(buf, 0, result, 0, cursor + 1);
		return result;
	}

	int get_element_data_size(byte[] pack, int handle) {
		int pos = handle + get_element_type(pack, handle).length;
		return ((int) pack[pos]) & 0x000000ff;
	}

	byte[] get_element_data(byte[] pack, int handle) {
		int pos = handle + get_element_type(pack, handle).length;
		int size = ((int) pack[pos]) & 0x000000ff;
		byte[] result = new byte[size];
		System.arraycopy(pack, pos + 1, result, 0, size);
		return result;
	}

	private void hexdump(byte[] buffer) {
		for (int i = 0; i < buffer.length;) {
			String line = "";
			for (int j = 0; j < 16; ++j, ++i) {
				if (i >= buffer.length) {
					break;
				} else {
					line += String.format("%02x ", buffer[i]);
				}
			}
			Log.v("BTAgent.Hexdump", line);
		}
	}

	public ActivityDataDoc LoadDataFromBuffer(byte[] buffer) {

		hexdump(buffer);

		ActivityDataDoc ret = new ActivityDataDoc();
		ret.data = new ArrayList<ActivityDataRow>();

		int cursor = 4; // jump over signature

		ret.version = ((int) buffer[cursor++]) & 0x000000ff;
		ret.year = ((int) buffer[cursor++]) & 0x000000ff;
		ret.month = ((int) buffer[cursor++]) & 0x000000ff;
		ret.day = ((int) buffer[cursor++]) & 0x000000ff;

		Log.v("BTAgent.ActivityDataParser", String.format(
				"Head: version:%d, time:%d-%d-%d", ret.version,
				ret.year + 2000, ret.month, ret.day));

		while (cursor + 8 < buffer.length) {
			ActivityDataRow row = new ActivityDataRow();

			row.mode = buffer[cursor++];
			row.hour = buffer[cursor++];
			row.minute = buffer[cursor++];
			row.data = new SparseArray<Double>();

			Log.v("BTAgent.ActivityDataParser",
					String.format("Row[%d]: mode:%d, time:%02d:%02d",
							ret.data.size(), row.mode, row.hour, row.minute));

			int size = buffer[cursor++];
			int[] meta = new int[size];

			int meta_cursor = 0;
			int meta_cnt = 0;
			for (; meta_cnt < 4 && meta_cursor < size; ++meta_cnt) {
				int _2d = (int) buffer[cursor + meta_cnt];
				_2d = _2d & 0x000000ff;
				int left = _2d >> 4;
				int right = _2d & 0x0f;
				if (left != 0) {
					meta[meta_cursor++] = left;
				} else {
					break;
				}
				if (right != 0) {
					meta[meta_cursor++] = right;
				} else {
					break;
				}
			}
			if (meta_cursor != size) {
				return null;
			}
			cursor += 4;

			for (int i = 0; i < size; ++i) {
				int dataType = meta[i];
				if (cursor + 4 >= buffer.length)
					break;

				int rawValue = bytesToInt(buffer, cursor);
				double value = 0;
				switch (dataType) {
				case ActivityDataRow.DataType.DATA_COL_STEP:
				case ActivityDataRow.DataType.DATA_COL_CADN:
				case ActivityDataRow.DataType.DATA_COL_HR:
					value = rawValue;
					break;
				case ActivityDataRow.DataType.DATA_COL_DIST:
				case ActivityDataRow.DataType.DATA_COL_CALS:
					value = rawValue % 100;
					value /= 100.0;
					value += rawValue / 100;
					break;
				}

				row.data.put(dataType, value);
				cursor += 4;

				Log.v("BTAgent.ActivityDataParser", String.format(
						"Row[%d]: %d - %f", ret.data.size(), dataType, value));
			}

			ret.data.add(row);
		}

		return ret;
	}

	public ActivityDataDoc LoadDataFile(String fileName) {

		if (fileName == null) {
			return null;
		}

		File file = new File(mContext.getFilesDir().getPath() + fileName);
		FileInputStream fileStream = null;
		try {
			fileStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return null;
		}

		byte[] buffer = new byte[(int) file.length()];
		try {
			if (fileStream.read(buffer) != buffer.length) {
				return null;
			}
		} catch (IOException e) {
			return null;
		}

		return LoadDataFromBuffer(buffer);
	}

	public static int bytesToInt(byte[] buf, int start) {
		int b0 = buf[start + 0];
		b0 = b0 & 0x000000ff;
		int b1 = buf[start + 1];
		b1 = b1 & 0x000000ff;
		int b2 = buf[start + 2];
		b2 = b2 & 0x000000ff;
		int b3 = buf[start + 3];
		b3 = b3 & 0x000000ff;

		// TODO: verify whether it is big-endian or little-endian
		return (b0 | b1 << 8 | b2 << 16 | b3 << 24);
	}
	
	public static int bytesToShort(byte[] buf, int start) {
		int b0 = buf[start + 0];
		b0 = b0 & 0x00ff;
		int b1 = buf[start + 1];
		b1 = b1 & 0x00ff;

		// TODO: verify whether it is big-endian or little-endian
		return (b0 | b1 << 8);
	}
	
	// Added Methods
	public void syncTimeFromInput( Date p_date, Date p_time ) {
		byte[] packet = buildSyncTimePackFromInput(p_date, p_time );
		btAgent.sendBytes(packet);
	}

	public byte[] buildSyncTimePackFromInput( Date p_date, Date p_time ) {

		Calendar ca = Utils.calendar();
		byte[] data = new byte[8];
		data[0] = (byte) (p_date.getYear()% 100);
		data[1] = (byte) (p_date.getMonth());	//ca.get(Calendar.MONTH);
		data[2] = (byte) (p_date.getDate());	// ca.get(Calendar.DATE);
		data[3] = (byte) (p_time.getHours());	//ca.get(Calendar.HOUR); 
//		if (ca.get(Calendar.AM_PM) == Calendar.PM)
//			data[3] += 12;
		data[4] = (byte) (p_time.getMinutes()); //ca.get(Calendar.MINUTE);
		data[5] = (byte) (p_time.getSeconds()); //ca.get(Calendar.SECOND);

		data[6] = (byte) 0x01; //0x01 - android, 0x02 - windows phone
		data[7] = (byte) android.os.Build.VERSION.SDK_INT; //android version
		byte[] packet = buildSingleElementPacket(elementTypeClock, data);
		return packet;
	}
}