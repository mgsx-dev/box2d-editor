package net.mgsx.core.tools;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

import net.mgsx.core.Editor;
import net.mgsx.core.components.Duplicable;

public class DuplicateTool extends SelectTool
{
	public DuplicateTool(Editor editor) {
		super("Duplicate", editor);
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// skip super implementation ...
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return super.touchDragged(screenX, screenY, 0); // move has drag ... TODO ugly
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.D && shift())
		{
			Array<Entity> duplicates = new Array<Entity>();
			for(Entity entity : editor.selection)
			{
				Entity newEntity = editor.createEntity();
				editor.entityEngine.addEntity(newEntity);
				for(Component component : entity.getComponents()){
					if(component instanceof Duplicable)
					{
						Component newComponent = ((Duplicable) component).duplicate();
						newEntity.add(newComponent);
					}
				}
				duplicates.add(newEntity);
			}
			editor.selection.clear();
			editor.selection.addAll(duplicates);
			editor.invalidateSelection();
			moving = true;
			prev = unproject(Gdx.input.getX(), Gdx.input.getY());
			return true;
		}
		return false;
	}
	
	
}