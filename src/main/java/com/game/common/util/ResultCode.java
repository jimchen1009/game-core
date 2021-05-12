package com.game.common.util;

public class ResultCode {

	public final short id;
	public final String comment;

	private ResultCode(int id, String comment) {
		this.id = (short) id;
		this.comment = comment;
	}

	public boolean isSuccess(){
		return this.id == SUCCESS.id;
	}

	public static final ResultCode SUCCESS = new ResultCode(0, "成功");
	public static final ResultCode FAILURE = new ResultCode(1, "失败");
	public static final ResultCode PARAM_ERROR = new ResultCode(2, "参数错误");
	public static final ResultCode BATTLE_STAGE_ERROR = new ResultCode(101, "战斗阶段错误");
	public static final ResultCode BATTLE_PARAM_SUPPORT = new ResultCode(102, "战斗不支持操作");
	public static final ResultCode BATTLE_CONTROL_DELAY = new ResultCode(103, "战斗控制完成");
	public static final ResultCode BATTLE_CONTROL_END = new ResultCode(104, "战斗控制循环结束");
	public static final ResultCode BATTLE_CONTROL_TAG = new ResultCode(105, "战斗控制已经结束");
	public static final ResultCode BATTLE_CONTROL_REQUEST = new ResultCode(106, "战斗控制重复请求");
	public static final ResultCode BATTLE_CONTROL_NOT_SUPPORT = new ResultCode(107, "战斗不支持操作");
}
