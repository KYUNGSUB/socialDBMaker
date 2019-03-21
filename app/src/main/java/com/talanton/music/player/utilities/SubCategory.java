package com.talanton.music.player.utilities;

public class SubCategory extends MusicCategory {
	private int mCode;	// main code
	private MusicCategory subCategory;
	public int getmCode() {
		return mCode;
	}
	public void setmCode(int mCode) {
		this.mCode = mCode;
	}
	public MusicCategory getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(MusicCategory subCategory) {
		this.subCategory = subCategory;
	}
}