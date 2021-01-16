package com.game.common.util;

public class TupleCode<T> {

	private final ResultCode resultCode;
	private final T data;

	public TupleCode(ResultCode resultCode, T data) {
		this.resultCode = resultCode;
		this.data = data;
	}

	public TupleCode(T data) {
		this(ResultCode.SUCCESS, data);
	}

	public TupleCode(ResultCode resultCode) {
		this(resultCode, null);
	}


	public boolean isSuccess() {
		return resultCode.equals(ResultCode.SUCCESS);
	}

	public ResultCode getCode() {
		return resultCode;
	}

	public T getData() {
		return data;
	}

	@Override
	public String toString() {
		return "{" +
				"resultCode=" + resultCode.id +
				", data=" + data +
				'}';
	}
}
