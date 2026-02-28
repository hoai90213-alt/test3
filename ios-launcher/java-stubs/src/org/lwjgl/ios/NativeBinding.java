package org.lwjgl.ios;

public final class NativeBinding {
	public static final boolean AVAILABLE;

	static {
		boolean loaded = false;
		try {
			System.loadLibrary("lwjgldummy");
			loaded = true;
		} catch (Throwable ignored) {
			loaded = false;
		}
		AVAILABLE = loaded;
	}

	private NativeBinding() {
	}
}
