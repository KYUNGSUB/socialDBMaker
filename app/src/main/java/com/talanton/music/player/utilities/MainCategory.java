package com.talanton.music.player.utilities;

import java.util.ArrayList;

public class MainCategory extends MusicCategory {
	private ArrayList<MusicCategory> subCategory = new ArrayList<MusicCategory>();
	public ArrayList<MusicCategory> getSubCategory() {
		return subCategory;
	}
	public void setSubCategory(ArrayList<MusicCategory> subCategory) {
		this.subCategory = subCategory;
	}
}