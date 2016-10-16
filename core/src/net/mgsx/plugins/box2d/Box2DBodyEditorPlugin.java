package net.mgsx.plugins.box2d;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import net.mgsx.core.plugins.EditablePlugin;

public class Box2DBodyEditorPlugin implements EditablePlugin
{

	@Override
	public Actor createEditor(Entity entity, Skin skin) 
	{
		// TODO create contextual editor ...
		
		return new Label("nothing here ...", skin);
	}

}