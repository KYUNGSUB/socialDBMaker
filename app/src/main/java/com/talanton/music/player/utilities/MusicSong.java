package com.talanton.music.player.utilities;

public class MusicSong {
	private String pid;				// product ID
	private String title;
	private String author;
	private int genre;
	private String fileinfo;
	private String timeinfo;
	private int file_size;
	private String uuid;
	private String download;
	private String download_date;
	private String expire_flag;
	private boolean want_flag;
	private int playOrder;
	private int id;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getPid() {
		return pid;
	}
	
	public void setPid(String pid) {
		this.pid = pid;
	}
	
	public boolean getWant_flag() {
		return want_flag;
	}
	
	public String getWant_flagString() {
		if(want_flag == true)
			return "O";
		else
			return "X";
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public int getGenre() {
		return genre;
	}
	
	public void setGenre(int genre) {
		this.genre = genre;
	}
	
	public String getFileinfo() {
		return fileinfo;
	}
	
	public void setFileinfo(String fileinfo) {
		this.fileinfo = fileinfo;
	}
	
	public String getTimeinfo() {
		return timeinfo;
	}
	
	public void setTimeinfo(String timeinfo) {
		this.timeinfo = timeinfo;
	}
	
	public String getDownload() {
		return download;
	}
	
	public void setDownload(String download) {
		this.download = download;
	}
	
	public String getDownload_date() {
		return download_date;
	}
	
	public void setDownload_date(String download_date) {
		this.download_date = download_date;
	}
	
	public String getExpire_flag() {
		return expire_flag;
	}
	
	public void setExpire_flag(String expire_flag) {
		this.expire_flag = expire_flag;
	}
	
	public void setWant_flag(boolean want_flag) {
		this.want_flag = want_flag;
	}
	
	public void setWant_flag(String checked) {
		if(checked.equals("O")) {
			this.want_flag = true;
		}
		else {
			this.want_flag = false;
		}
	}
	
	public int getPlayOrder() {
		return playOrder;
	}
	
	public void setPlayOrder(int playOrder) {
		this.playOrder = playOrder;
	}
	
	public int getFile_size() {
		return file_size;
	}
	
	public void setFile_size(int file_size) {
		this.file_size = file_size;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}