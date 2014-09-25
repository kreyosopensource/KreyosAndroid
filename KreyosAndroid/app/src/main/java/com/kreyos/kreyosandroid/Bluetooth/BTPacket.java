package com.kreyos.kreyosandroid.bluetooth;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class BTPacket {
	
	public static final int version = 0;
	public static final int flag = 1;
	public static final int length = 2;
	public static final int sequnce = 3;
	
	public static final byte defaultVersion = 1;
	public static final byte flagRequest =  (byte) 0x80;
	public static final byte flagResponse = (byte) 0x40;
	
	private byte[] header = new byte[4];
	private List<BTElement> body = new LinkedList<BTElement>();

	public BTPacket() {
	}
	
	public int getLength() {
		return header[length];
	}
	
	public boolean isRequest() {
		return (header[flag] & flagRequest) == flagRequest;
	}
	
	public boolean isResponse() {
		return (header[flag] & flagResponse) == flagResponse;
	}
	
	public void setAsRequest(boolean enable) {
		if (enable)
			header[flag] |= flagRequest;
		else
			header[flag] &= ~flagRequest;
	}
	
	public void setAsResponse(boolean enable) {
		if (enable)
			header[flag] |= flagResponse;
		else
			header[flag] &= ~flagResponse;
	}

	public void setVersion(int versionNumber) {
		header[version] = (byte) versionNumber;
	}
	
	public int getVersion() {
		return header[version];
	}

	public BTElement addElement(String type) {
		BTElement result = new BTElement();
		body.add(result);
		return result;
	}
	
	public boolean serialize(OutputStream stream) {
		try {
			stream.write(header);
		} catch (IOException e) {
			return false;
		}
		for (BTElement i : this.body) {
			if (!i.serialize(stream))
				return false;
		}
		return true;
	}
}
