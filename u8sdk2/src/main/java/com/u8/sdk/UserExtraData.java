package com.u8.sdk;

/***
 * 用户扩展数据
 * 已经登录的角色相关数据
 * 有的渠道需要统计角色相关数据
 * @author xiaohei
 *
 */
public class UserExtraData {

	public static final int TYPE_SELECT_SERVER = 1;			//选择服务器
	public static final int TYPE_CREATE_ROLE = 2;			//创建角色
	public static final int TYPE_ENTER_GAME = 3;			//进入游戏
	public static final int TYPE_LEVEL_UP = 4;				//等级提升
	public static final int TYPE_EXIT_GAME = 5;				//退出游戏
	
	private int dataType;					//上报类型
	private String roleID;					//角色ID
	private String roleName;				//角色名称
	private String roleLevel;				//角色等级
	private int serverID;					//角色所在服务器ID
	private String serverName;				//角色所在服务器名称
	private int moneyNum;					//角色身上拥有的游戏币数量
	private long roleCreateTime;			//角色创建时间，从1970年到现在的时间，单位秒
	private long roleLevelUpTime;			//角色等级变化时间，从1970年到现在的时间，单位秒
	
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	public String getRoleID() {
		return roleID;
	}
	public void setRoleID(String roleID) {
		this.roleID = roleID;
	}
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getRoleLevel() {
		return roleLevel;
	}
	public void setRoleLevel(String roleLevel) {
		this.roleLevel = roleLevel;
	}
	public int getServerID() {
		return serverID;
	}
	public void setServerID(int serverID) {
		this.serverID = serverID;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public int getMoneyNum() {
		return moneyNum;
	}
	public void setMoneyNum(int moneyNum) {
		this.moneyNum = moneyNum;
	}
	public long getRoleCreateTime() {
		return roleCreateTime;
	}
	public void setRoleCreateTime(long roleCreateTime) {
		this.roleCreateTime = roleCreateTime;
	}
	public long getRoleLevelUpTime() {
		return roleLevelUpTime;
	}
	public void setRoleLevelUpTime(long roleLevelUpTime) {
		this.roleLevelUpTime = roleLevelUpTime;
	}
	@Override
	public String toString() {
		return "UserExtraData [dataType=" + dataType + ", roleID=" + roleID + ", roleName=" + roleName + ", roleLevel="
				+ roleLevel + ", serverID=" + serverID + ", serverName=" + serverName + ", moneyNum=" + moneyNum
				+ ", roleCreateTime=" + roleCreateTime + ", roleLevelUpTime=" + roleLevelUpTime + "]";
	}
	
	
}
