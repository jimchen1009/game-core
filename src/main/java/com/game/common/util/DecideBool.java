package com.game.common.util;

public class DecideBool {

	private boolean aBoolean;
	private boolean hasDecided = false;

	public DecideBool(boolean defaultValue) {
		this.aBoolean = defaultValue;
	}

	public boolean expectedValue(boolean expectedValue) {
		if (hasDecided) {
			return false;
		}
		this.aBoolean = expectedValue;
		return true;
	}

	public boolean decideValue(boolean decideValue) {
		if (this.hasDecided) {
			return false;
		}
		this.aBoolean = decideValue;
		this.hasDecided = true;
		return true;

	}

	public boolean decideCurrent() {
		if (this.hasDecided) {
			return false;
		}
		this.hasDecided = true;
		return true;
	}

	public boolean finalDecideAndGet() {
		decideCurrent();
		return aBoolean;
	}
}
