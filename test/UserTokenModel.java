

import java.util.Date;

import com.YaNan.frame.jdb.database.annotation.Column;
import com.YaNan.frame.jdb.database.annotation.Tab;

@Tab(DB="YaNan_Account",name="user_token")
public class UserTokenModel {
	@Column(type="datetime")
	private Date createTime;
	@Column(Primary_Key=true,Annotations="主键")
	private int id;
	@Column(unique = true,type="varchar",length=255)
	private String token;
	@Column(type="varchar",length=255)
	private String user;
	private int status;
	@Override
	public String toString() {
		return "UserTokenModel [createTime=" + createTime + ", id=" + id + ", token=" + token + ", user=" + user
				+ ", status=" + status + ", note=" + note + "]";
	}
	@Column(type="varchar",length=255)
	private String note;
	
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
}
