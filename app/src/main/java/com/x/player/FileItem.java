package com.x.player;

public class FileItem
{
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_FOLDER = 1;
	public static final int TYPE_FILE = 2;
	public static final int TYPE_PARENT = 3;
	
	public String ID;
	public String Name;
	public int Type = TYPE_UNKNOWN; 

	public FileItem(String id, String name, int type) {
		ID = id;
		Name = name;
		Type = type;
	}
}
