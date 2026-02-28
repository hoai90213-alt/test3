package org.lwjgl.opengl;

import org.lwjgl.ios.NativeBinding;

public final class GL11 {
	public static final int GL_COLOR_BUFFER_BIT = 0x00004000;
	public static final int GL_CULL_FACE = 0x0B44;
	public static final int GL_BACK = 0x0405;
	public static final int GL_FALSE = 0;
	public static final int GL_COLOR_ARRAY = 0x8076;
	public static final int GL_FLOAT = 0x1406;
	public static final int GL_TRIANGLES = 0x0004;
	public static final int GL_QUADS = 0x0007;
	private static final boolean NATIVE = NativeBinding.AVAILABLE;

	private GL11() {
	}

	private static native void nGlClear(int mask);
	private static native void nGlClearColor(float red, float green, float blue, float alpha);
	private static native void nGlDrawArrays(int mode, int first, int count);
	private static native void nGlViewport(int x, int y, int width, int height);

	public static void glClear(int mask) {
		if (NATIVE) {
			try {
				nGlClear(mask);
				return;
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
	}

	public static void glClearColor(float red, float green, float blue, float alpha) {
		if (NATIVE) {
			try {
				nGlClearColor(red, green, blue, alpha);
				return;
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
	}

	public static void glEnable(int cap) {
	}

	public static void glCullFace(int mode) {
	}

	public static void glEnableClientState(int array) {
	}

	public static void glColorPointer(int size, int type, int stride, long pointer) {
	}

	public static void glDrawArrays(int mode, int first, int count) {
		if (NATIVE) {
			try {
				nGlDrawArrays(mode, first, count);
				return;
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
	}

	public static void glDisableClientState(int array) {
	}

	public static void glColor3f(float red, float green, float blue) {
	}

	public static void glBegin(int mode) {
	}

	public static void glVertex2f(float x, float y) {
	}

	public static void glEnd() {
	}

	public static void glViewport(int x, int y, int width, int height) {
		if (NATIVE) {
			try {
				nGlViewport(x, y, width, height);
				return;
			} catch (UnsatisfiedLinkError ignored) {
			}
		}
	}
}
