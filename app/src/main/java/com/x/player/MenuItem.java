package com.x.player;

import java.util.*;

public class MenuItem {
    public static String Separator = ";";

    public int MenuID;
    public int IconResource;
    public String MenuName;
    public boolean IsDevMenu;
    public String Tag = "tag";

    public static String toListID(List<MenuItem> menus) {
        StringBuilder ret = new StringBuilder();
        for (MenuItem i : menus)
            ret.append(i.MenuID).append(Separator);
        ret = new StringBuilder(ret.substring(0, ret.length() - 1)); // remove last separator char
        return ret.toString();
    }

    public static String toListNames(List<MenuItem> menus) {
        StringBuilder ret = new StringBuilder();
        for (MenuItem i : menus)
            ret.append(i.MenuName).append(Separator);
        ret = new StringBuilder(ret.substring(0, ret.length() - 1)); // remove last separator char
        return ret.toString();
    }

    public static String toListIcons(List<MenuItem> menus) {
        StringBuilder ret = new StringBuilder();
        for (MenuItem i : menus)
            ret.append(i.IconResource).append(Separator);
        ret = new StringBuilder(ret.substring(0, ret.length() - 1)); // remove last separator char
        return ret.toString();
    }

    public static String toListIsDevs(List<MenuItem> menus) {
        StringBuilder ret = new StringBuilder();
        for (MenuItem i : menus)
            ret.append(i.IsDevMenu ? "1" : "0").append(Separator);
        ret = new StringBuilder(ret.substring(0, ret.length() - 1)); // remove last separator char
        return ret.toString();
    }

    public static String toListTags(List<MenuItem> menus) {
        StringBuilder ret = new StringBuilder();
        for (MenuItem i : menus)
            ret.append(i.Tag).append(Separator);
        ret = new StringBuilder(ret.substring(0, ret.length() - 1)); // remove last separator char
        return ret.toString();
    }

    public MenuItem(int id, String name, int iconRes, boolean isDev) {
        this.MenuID = id;
        this.MenuName = name;
        this.IconResource = iconRes;
        this.IsDevMenu = isDev;
    }
}
