package com.kreyos.watch.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BluetoothAgent {

	// sub type definition:
	public enum ErrorCode {
		Success, NoBluetoothSupport, BluetoothDisabled, StatusError,
	}

	public enum Status {
		UnInitialized, Ready, Connecting, Connected, SenderStarting, SenderStarted, ReaderStarting, ReaderStarted, Running, Stopping, SenderStopping, SenderStopped, ReaderStopping, ReaderStopped, Stopped, Quit,
	}

	public enum Mode {
		Unknown, Server, Client,
	}

	public class DeviceDesc {
		public String name;
		public String tag;
		public String hint;

		public DeviceDesc() {

		}
	}

	public static String GetErrorCodeString(ErrorCode err) {
		switch (err) {
		case Success:
			return "Success";
		case NoBluetoothSupport:
			return "NoBluetoothSupport";
		case BluetoothDisabled:
			return "BluetoothDisabled";
		case StatusError:
			return "StatusError";
		}
		return "Unknown";
	}

	public class JobDesc {
		public Handler msgHandler = null;
		public byte[] bytesToSent = null;
		public boolean isStopSig = false;

		public JobDesc(byte[] buffer, Handler msgProc) {
			msgHandler = msgProc;
			bytesToSent = buffer;
		}
	}

	// constants:
	// Protocol schemas:
	public static final String PROTOCOL_SCHEME_L2CAP = "btl2cap";
	public static final String PROTOCOL_SCHEME_RFCOMM = "btspp";
	public static final String PROTOCOL_SCHEME_BT_OBEX = "btgoep";
	public static final String PROTOCOL_SCHEME_TCP_OBEX = "tcpobex";
	public static final String SDP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
	
	public static final int SPP_PACKET_SIZE = 64; 
	public static final int SPP_PACKET_DATA_SIZE = SPP_PACKET_SIZE - 1;
	public static final int UNIT_BUF_SIZE = SPP_PACKET_SIZE;
	public static final int READDER_BUF_SIZE = 256;

	public static final int BLUETOOTH_MSG_DATA = 1;

	private BluetoothAdapter embedBTAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private Status sessionStatus = Status.UnInitialized;

	private BluetoothDevice targetDevice = null;

	private ServerThread serverThread = new ServerThread();
	private ClientThread clientThread = new ClientThread();
	private ReaderThread readerThread = new ReaderThread();
	private WriterThread writerThread = new WriterThread();

	private BluetoothServerSocket listenSocket = null;
	private BluetoothSocket clientSocket = null;

	private Queue<JobDesc> writerJobQueue = new LinkedList<JobDesc>();
	private Handler uiMsgHandler = null;
	private Handler serviceMsgHandler = null;
	private Context mContext = null;

	private static BluetoothAgent instance = null;

	public static void initBluetoothAgent(Context context) {
		instance = new BluetoothAgent(context);
	}

	public static BluetoothAgent getInstance(Handler uiHandler) {
		if (instance != null && uiHandler != null) {
			instance.bindMessageHandler(uiHandler);
		}
		return instance;
	}

	private BluetoothAgent(Context context) {
		mContext = context;
	}

	public BluetoothDevice getTargetDevice() {
		return targetDevice;
	}

	public ErrorCode initialize() {
		if (sessionStatus != Status.UnInitialized)
			return ErrorCode.StatusError;

		if (embedBTAdapter == null)
			return ErrorCode.NoBluetoothSupport;

		if (!embedBTAdapter.isEnabled())
			return ErrorCode.BluetoothDisabled;

		transistStatus(Status.Ready);
		return ErrorCode.Success;
	}

	public ArrayList<BluetoothDevice> getPairedDevices() {
		if (sessionStatus == Status.UnInitialized)
			return null;

		Set<BluetoothDevice> bondedDevices = embedBTAdapter.getBondedDevices();
		ArrayList<BluetoothDevice> bondedWatches = new ArrayList<BluetoothDevice>();

		for (BluetoothDevice i : bondedDevices) {
			if (i.getName().startsWith("Meteor") || i.getName().startsWith("Kreyos")) {
				bondedWatches.add(i);
			}
		}
		return bondedWatches;
	}

	public ErrorCode bindDevice(BluetoothDevice device) {
		if (sessionStatus == Status.Running)
			return ErrorCode.StatusError;

		targetDevice = device;
		return ErrorCode.Success;
	}

	public ErrorCode bindDevice(String devicename) {
		ArrayList<BluetoothDevice> devices = getPairedDevices();
		if (devices == null)
			return ErrorCode.StatusError;

		for (BluetoothDevice device : devices) {
			if (device.getName().equals(devicename))
				return bindDevice(device);
		}

		return ErrorCode.StatusError;
	}

	public void bindMessageHandler(Handler msgHandler) {
		uiMsgHandler = msgHandler;
	}

	public void bindServiceHandler(Handler msgHandler) {
		this.serviceMsgHandler = msgHandler;
	}

	public ErrorCode startPassiveSession() {
		if (getStatus() != Status.Stopped && getStatus() != Status.Ready
				&& getStatus() != Status.Quit && targetDevice == null)
			return ErrorCode.StatusError;

		startThread(serverThread);
		startThread(writerThread);
		startThread(readerThread);
		return ErrorCode.Success;
	}

	public ErrorCode startActiveSession() {
		if (getStatus() != Status.Stopped && getStatus() != Status.Ready
				&& getStatus() != Status.Quit)
			return ErrorCode.StatusError;

		if (targetDevice == null) {
			return ErrorCode.StatusError;
		}

		startThread(clientThread);
		startThread(writerThread);
		startThread(readerThread);

		return ErrorCode.Success;
	}

	public ErrorCode forceStopSession() {
		transistStatus(Status.Quit);
		new Thread() {
			public void run() {
				stopThread(serverThread);
				stopThread(clientThread);
				stopThread(readerThread);
				stopThread(writerThread);

				if (listenSocket != null) {
					try {
						listenSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					listenSocket = null;
				}

				if (clientSocket != null) {
					try {
						clientSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					clientSocket = null;
				}
			};
		}.start();

		return ErrorCode.Success;
	}

	public ErrorCode restartSession() {
		if (getStatus() != Status.Running)
			return ErrorCode.StatusError;

		if (listenSocket == null)
			return ErrorCode.StatusError;

		try {
			listenSocket.close();

		} catch (IOException e1) {
			Log.e("BTAgent", "Close Server Socket Failed");
		}
		try {
			clientSocket.close();

		} catch (IOException e1) {
			Log.e("BTAgent", "Close Client Socket Failed");
		}

		return ErrorCode.Success;
	}

	public ErrorCode sendString(String string) {
		if (getStatus() != Status.Running)
			return ErrorCode.StatusError;

		registerStringToSend(string);

		return ErrorCode.Success;
	}

	public ErrorCode sendBytes(byte[] data) {
		if (getStatus() != Status.Running)
			return ErrorCode.StatusError;
		registerBufferToSend(data);

		return ErrorCode.Success;
	}

	private void registerJob(JobDesc job) {
		synchronized (writerJobQueue) {
			writerJobQueue.offer(job);
			writerJobQueue.notifyAll();
		}
	}

	private void registerBufferToSend(byte[] buffer) {
		JobDesc job = new JobDesc(buffer, null);
		registerJob(job);
	}

	public void batchRegisterJob(ArrayList<JobDesc> jobList) {
		synchronized (writerJobQueue) {
			for (JobDesc job : jobList) {
				writerJobQueue.offer(job);
			}
			writerJobQueue.notifyAll();
		}
	}

	private void registerStringToSend(String string) {
		byte[] buffer = string.getBytes();
		registerBufferToSend(buffer);
	}

	private void stopThread(Thread thread) {
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	private void startThread(Thread thread) {
		if (!thread.isAlive())
			thread.start();
	}

	private synchronized ErrorCode transistStatus(Status targetStatus) {

		Log.v("BTAgent", String.format("Status Changed: %s=>%s", sessionStatus,
				targetStatus));
		if (uiMsgHandler != null) {
			uiMsgHandler.obtainMessage(
					Protocol.MessageID.MSG_BLUETOOTH_STATUS, 
					targetStatus).sendToTarget();
		}
		if (serviceMsgHandler != null) {
			serviceMsgHandler.obtainMessage(
					Protocol.MessageID.MSG_BLUETOOTH_STATUS, 
					targetStatus).sendToTarget();
		}
		sessionStatus = targetStatus;
		notifyAll();
		return ErrorCode.Success;
	}

	public synchronized Status getStatus() {
		return sessionStatus;
	}

	private synchronized boolean WaitStatus(Status expect) {
		while (true) {
			try {
				if (getStatus() == Status.Stopping) {
					return false;
				} else if (getStatus() == expect) {
					return true;
				} else {
					wait();
				}
			} catch (InterruptedException e) {
				transistStatus(Status.Quit);
				return false;
			}
		}

	}

	private synchronized boolean testStatusArray(Status[] expect) {
		for (Status s : expect) {
			if (getStatus() == s) {
				return true;
			}
		}
		return false;
	}

	private synchronized boolean WaitStatus(Status[] expect) {
		while (true) {
			try {
				if (getStatus() == Status.Quit) {
					return false;
				} else if (testStatusArray(expect)) {
					return true;
				} else {
					//give it a timeout so that the client thread can be wake up from time to time
					//and the back-end service will not be killed in some vendor's phone
					wait(8 * 60 * 1000);
				}
			} catch (InterruptedException e) {
				return false;
			}
		}

	}

	private class ClientThread extends Thread {
		private Status[] watchStatus = { Status.ReaderStopped,
				Status.SenderStopped, Status.Stopped, Status.UnInitialized,
				Status.Ready };
		private int waitTime = 3 * 1000;

		private boolean tryConnect() {
			try {
				clientSocket = targetDevice
						.createRfcommSocketToServiceRecord(UUID
								.fromString(SDP_UUID));
				clientSocket.connect();
			} catch (IOException e) {
				Log.e("BTAgent", String.format(
						"tryConnect(%s) failed, cause=%s",
						targetDevice.getName(), e.getMessage()));
				return false;
			}
			return true;
		}

		public void run() {
			while (getStatus() != Status.Quit) {

				transistStatus(Status.Connecting);

				if (tryConnect()) {
					transistStatus(Status.Connected);
					WaitStatus(watchStatus);
					waitTime = 3 * 1000;
				} else {
					waitTime += waitTime / 2;
				}

				try {
					sleep(waitTime);
				} catch (InterruptedException e) {
					return;
				}
			}

		}
	};

	private class ServerThread extends Thread {

		private Status[] watchStatus = { Status.ReaderStopped,
				Status.SenderStopped, Status.Stopped, Status.UnInitialized,
				Status.Ready };

		public void run() {

			while (getStatus() != Status.Quit) {
				transistStatus(Status.Connecting);
				try {
					listenSocket = embedBTAdapter
							.listenUsingRfcommWithServiceRecord(
									PROTOCOL_SCHEME_RFCOMM,
									UUID.fromString(SDP_UUID));
					clientSocket = listenSocket.accept();

				} catch (IOException e) {
					transistStatus(Status.Stopping);
				}
				transistStatus(Status.Connected);

				WaitStatus(watchStatus);
				try {
					sleep(5 * 1000);
				} catch (InterruptedException e) {
					return;
				}

			}

		}
	};

	private class ReaderThread extends Thread {
		private InputStream inStream = null;
		private byte[] unitBuffer = new byte[UNIT_BUF_SIZE];
		private byte[] readerBuffer = new byte[READDER_BUF_SIZE];
		private int byteReceived = 0;
		private int stlvPacketLen = 0;

		private Protocol protocol = new Protocol(instance, mContext);

		public void handleIncomeData(byte[] buffer, int length) {
			
			if (length == 0) {
				Log.v("BTAgent", String.format("handleIncomeData: Length = 0"));
				return;
			}
			
			int payloadLen = ((int) buffer[0] & 0x000000ff) >> 2;
					
			if ((buffer[0] & 0x01) != 0) {
				// begin of packet
				byteReceived = 0;
				
				// TODO: remove magic number
				stlvPacketLen = ((int) buffer[3] & 0x000000ff) + 4;				
				if (stlvPacketLen >= READDER_BUF_SIZE) {
					byteReceived = 0;
					Log.v("BTAgent", String.format("Transport Recv: Drop Malformat packet"));
					return;
				}
			}
			
			System.arraycopy(buffer, 1, readerBuffer, byteReceived, payloadLen);
			byteReceived += payloadLen;

			if ((buffer[0] & 0x02) != 0) {
				// end of packet
				
				Log.v("BTAgent", String.format("Transport Recv: %d/%d bytes", byteReceived, stlvPacketLen));
				
				if (byteReceived != stlvPacketLen) {
					return;
				}
				
				byte[] packet = new byte[byteReceived];
				System.arraycopy(readerBuffer, 0, packet, 0, byteReceived);
				byteReceived = 0;

				protocol.bindMessageHandler(uiMsgHandler);
				protocol.bindServiceMsgHandler(serviceMsgHandler);
				protocol.handlePacket(packet);
			}
		}

		private void readerLoop() {
			if (WaitStatus(Status.Connected)) {
				transistStatus(Status.ReaderStarting);
			} else {
				transistStatus(Status.Quit);
				return;
			}

			try {
				inStream = clientSocket.getInputStream();
			} catch (IOException e) {
				transistStatus(Status.ReaderStopped);
				return;
			}

			transistStatus(Status.ReaderStarted);

			// reader loop
			while (true) {
				try {
					int byteRead = inStream.read(unitBuffer);
					if (byteRead <= 0) {
						Log.v("BTAgent", String.format("Transport Recv: read() return -1"));
						continue;
					}

					Log.v("BTAgent", String.format("Transport Recv: read() return %d", byteRead));
					handleIncomeData(unitBuffer, byteRead);
				} catch (IOException e) {
					Log.e("BTAgent", "Reader Quit!");

					JobDesc stopJob = new JobDesc(null, null);
					stopJob.isStopSig = true;
					registerJob(stopJob);
					break;
				}
			}

			try {
				inStream.close();
			} catch (IOException e) {
			}
			transistStatus(Status.ReaderStopped);
		}

		public void run() {

			while (getStatus() != Status.Quit) {
				readerLoop();
			}
		}
	}

	private class WriterThread extends Thread {
		private OutputStream outStream = null;

		public void splitSend(byte[] p) throws IOException {
			int byte_sent = 0;
			int byte_to_send = p.length;

			Log.v("BTAgent", String.format("STLV: Begin send %d byte Packet", byte_to_send));
			while (byte_to_send - byte_sent > 0) {
				int byteLeft = byte_to_send - byte_sent;
				int subPacketSize = byteLeft;
				if (subPacketSize > SPP_PACKET_DATA_SIZE)
					subPacketSize = SPP_PACKET_DATA_SIZE;

				byte[] temp = new byte[subPacketSize + 1];
				temp[0] = 0;
				if (byte_sent == 0)
					temp[0] |= 0x01;
				if (subPacketSize == byteLeft)
					temp[0] |= 0x02;
				System.arraycopy(p, byte_sent, temp, 1, subPacketSize);
				outStream.write(temp);
				outStream.flush();
				
				Log.v("BTAgent", String.format("Transport Send: %d bytes sent", temp.length));
				
				byte_sent += subPacketSize;
			}
			Log.v("BTAgent", String.format("STLV Send: End send %d byte Packet", byte_to_send));
		}

		public void writerLoop() {
			if (WaitStatus(Status.ReaderStarted)) {
				transistStatus(Status.SenderStarting);
			} else {
				transistStatus(Status.SenderStopped);
				return;
			}

			try {
				outStream = clientSocket.getOutputStream();
			} catch (IOException e) {
				transistStatus(Status.SenderStopped);
				return;
			}
			transistStatus(Status.SenderStarted);
			transistStatus(Status.Running);

			byte[] syncPack = new Protocol(null, null).buildSyncTimePack();
			try {
				splitSend(syncPack);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// writer loop
			boolean stopFlag = false;
			while (!stopFlag) {
				synchronized (writerJobQueue) {
					try {
						writerJobQueue.wait();
						JobDesc job = null;
						while ((job = writerJobQueue.poll()) != null) {
							if (job.isStopSig) {
								Log.v("BTAgent",
										"Receive Quit Signal, Stop Writer Loop!");
								stopFlag = true;
								break;
							} else {
								splitSend(job.bytesToSent);
							}
						}

					} catch (IOException e) {
						Log.e("BTAgent", "Writer Quit!");
						break;
					} catch (InterruptedException e) {
						Log.e("BTAgent", "Writer Quit Interrupted!");
						break;
					}
				}
			}

			try {
				outStream.close();
			} catch (IOException e) {

			}

			transistStatus(Status.SenderStopped);
		}

		public void run() {

			while (getStatus() != Status.Quit) {
				writerLoop();
			}

		}
	}
}