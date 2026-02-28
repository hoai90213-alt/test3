package org.lwjgl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class BufferUtils {
	private BufferUtils() {
	}

	public static FloatBuffer createFloatBuffer(int size) {
		return ByteBuffer.allocateDirect(size * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
	}

	public static DoubleBuffer createDoubleBuffer(int size) {
		return ByteBuffer.allocateDirect(size * Double.BYTES).order(ByteOrder.nativeOrder()).asDoubleBuffer();
	}

	public static IntBuffer createIntBuffer(int size) {
		return ByteBuffer.allocateDirect(size * Integer.BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
	}
}
