package org.lwjgl.glfw;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.ios.NativeBinding;

public final class GLFW {
	public static final int GLFW_PRESS = 1;
	public static final int GLFW_MOUSE_BUTTON_LEFT = 0;
	public static final int GLFW_KEY_W = 87;
	public static final int GLFW_KEY_A = 65;
	public static final int GLFW_KEY_S = 83;
	public static final int GLFW_KEY_D = 68;
	public static final int GLFW_SAMPLES = 0x2100;
	public static final int GLFW_OPENGL_PROFILE = 0x2208;
	public static final int GLFW_OPENGL_ANY_PROFILE = 0;
	public static final int GLFW_RESIZABLE = 0x00020003;

	private static final int DEFAULT_WIDTH = 1280;
	private static final int DEFAULT_HEIGHT = 720;
	private static final int MAX_FRAMES = Integer.getInteger("lwjgl.stub.maxFrames", 900);
	private static final long START_NANOS = System.nanoTime();
	private static final boolean NATIVE = NativeBinding.AVAILABLE;
	private static long nextWindowId = 1;
	private static final Map<Long, Integer> frameCounter = new HashMap<Long, Integer>();
	private static final Map<Long, Boolean> closedWindows = new HashMap<Long, Boolean>();

	private GLFW() {
	}

	private static native int nGlfwInit();
	private static native long nGlfwCreateWindow(int width, int height, String title, long monitor, long share);
	private static native void nGlfwMakeContextCurrent(long window);
	private static native void nGlfwPollEvents();
	private static native int nGlfwWindowShouldClose(long window);

	public static boolean glfwInit() {
		if (NATIVE) {
			try {
				return nGlfwInit() != 0;
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
		return true;
	}

	public static void glfwWindowHint(int hint, int value) {
	}

	public static long glfwCreateWindow(int width, int height, CharSequence title, long monitor, long share) {
		if (NATIVE) {
			try {
				return nGlfwCreateWindow(width, height, title == null ? "" : title.toString(), monitor, share);
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
		long id = nextWindowId++;
		frameCounter.put(id, Integer.valueOf(0));
		closedWindows.put(id, Boolean.FALSE);
		return id;
	}

	public static void glfwMakeContextCurrent(long window) {
		if (NATIVE) {
			try {
				nGlfwMakeContextCurrent(window);
				return;
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
	}

	public static void glfwSwapInterval(int interval) {
	}

	public static void glfwShowWindow(long window) {
	}

	public static double glfwGetTime() {
		long deltaNanos = System.nanoTime() - START_NANOS;
		return deltaNanos / 1_000_000_000.0;
	}

	public static boolean glfwWindowShouldClose(long window) {
		if (NATIVE) {
			try {
				return nGlfwWindowShouldClose(window) != 0;
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
		Boolean explicitlyClosed = closedWindows.get(window);
		if (explicitlyClosed == null) {
			return true;
		}
		if (explicitlyClosed.booleanValue()) {
			return true;
		}
		int frames = frameCounter.containsKey(window) ? frameCounter.get(window).intValue() : 0;
		return frames >= MAX_FRAMES;
	}

	public static void glfwPollEvents() {
		if (NATIVE) {
			try {
				nGlfwPollEvents();
				return;
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
	}

	public static void glfwSwapBuffers(long window) {
		Integer frames = frameCounter.get(window);
		if (frames == null) {
			return;
		}
		frameCounter.put(window, Integer.valueOf(frames.intValue() + 1));
	}

	public static void glfwDestroyWindow(long window) {
		closedWindows.put(window, Boolean.TRUE);
		frameCounter.remove(window);
	}

	public static void glfwTerminate() {
		frameCounter.clear();
		closedWindows.clear();
	}

	public static void glfwGetCursorPos(long window, DoubleBuffer xpos, DoubleBuffer ypos) {
		if (xpos != null && xpos.capacity() > 0) {
			xpos.put(0, DEFAULT_WIDTH / 2.0);
		}
		if (ypos != null && ypos.capacity() > 0) {
			ypos.put(0, DEFAULT_HEIGHT / 2.0);
		}
	}

	public static void glfwGetWindowSize(long window, IntBuffer width, IntBuffer height) {
		if (width != null && width.capacity() > 0) {
			width.put(0, DEFAULT_WIDTH);
		}
		if (height != null && height.capacity() > 0) {
			height.put(0, DEFAULT_HEIGHT);
		}
	}

	public static int glfwGetMouseButton(long window, int button) {
		return 0;
	}

	public static int glfwGetKey(long window, int key) {
		return 0;
	}
}
