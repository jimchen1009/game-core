package com.game.common.util;

public class IncInteger {

	private int value;

	public IncInteger() {
		this(0);
	}

	public IncInteger(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void increaseValue(int inc){
		value += inc;
	}

	public void decreaseValue(int dec){
		value -= dec;
	}
}
