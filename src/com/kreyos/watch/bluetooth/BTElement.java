package com.kreyos.watch.bluetooth;

import java.io.IOException;
import java.io.OutputStream;

public class BTElement {

	public boolean serialize(OutputStream stream) {
		try {
			stream.write(1);
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}

