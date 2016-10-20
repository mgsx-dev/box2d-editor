package net.mgsx.game.plugins.box2dold.model;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpriteItem {
	public SpriteItem(String id, String path, Sprite sprite) {
		super();
		this.id = id;
		this.path = path;
		this.sprite = sprite;
	}
	public String id;
	public String path;
	public Sprite sprite;
}