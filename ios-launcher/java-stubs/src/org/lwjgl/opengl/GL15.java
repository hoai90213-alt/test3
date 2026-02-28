package org.lwjgl.opengl;

import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public final class GL15 {
	public static final int GL_ARRAY_BUFFER = 0x8892;
	public static final int GL_STATIC_DRAW = 0x88E4;

	private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

	private GL15() {
	}

	public static int glGenBuffers() {
		return NEXT_ID.getAndIncrement();
	}

	public static void glBindBuffer(int target, int buffer) {
	}

	public static void glBufferData(int target, FloatBuffer data, int usage) {
	}

	public static void glDeleteBuffers(int buffer) {
	}
}
