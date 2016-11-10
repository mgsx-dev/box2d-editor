package net.mgsx.game.plugins.ashley.editors;

import java.lang.reflect.Field;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.game.core.Editor;
import net.mgsx.game.core.GamePipeline;
import net.mgsx.game.core.annotations.EditableSystem;
import net.mgsx.game.core.helpers.ReflectionHelper;
import net.mgsx.game.core.plugins.GlobalEditorPlugin;
import net.mgsx.game.core.ui.EntityEditor;

public class AshleySystemsEditor implements GlobalEditorPlugin
{
	@Override
	public Actor createEditor(final Editor editor, final Skin skin) 
	{
		Table table = new Table(skin);
		table.setBackground(skin.getDrawable("default-window-body-right"));
		
		rebuildUI(editor, table, skin);
		
		return table;
	}
	
	private void rebuildUI(final Editor editor, final Table table, final Skin skin) 
	{
		table.clearChildren();
		
		TextButton btRefresh = new TextButton("refresh", skin);
		
		table.add(btRefresh).colspan(3).row();
		
		btRefresh.addListener(new ChangeListener() {
			
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				rebuildUI(editor, table, skin);
			}
		});
		
		table.add("System");
		table.add("Priority");
		table.add("Entities");
		table.add("Processing").height(Value.percentHeight(1.5f));
		table.row();
		
		
		ObjectMap<Integer, String> pipelineLegend = new ObjectMap<Integer, String>();
		for(Field field : GamePipeline.class.getFields()){
			Integer priority = (Integer)ReflectionHelper.get(null, field);
			if(priority != null) pipelineLegend.put(priority, field.getName());
		}
		for(EntitySystem system : editor.entityEngine.getSystems())
		{
			String systemName = system.getClass().getSimpleName();
			if(system.getClass().getEnclosingClass() != null){
				systemName = system.getClass().getEnclosingClass().getSimpleName();
			}
			
			String priorityName = pipelineLegend.get(system.priority);
			if(priorityName == null) priorityName = "undefined (" + String.valueOf(system.priority) + ")";
			
			String description = "";
			if(system instanceof IteratingSystem){
				int size = ((IteratingSystem) system).getEntities().size();
				description += String.valueOf(size);
			}else{
				description = "-";
			}
			
			table.add(systemName).left();
			table.add(priorityName);
			table.add(description);
			
			if(system.getClass().getAnnotation(EditableSystem.class) != null)
				EntityEditor.createControl(table, system, new EntityEditor.MethodAccessor(system, "processing", "checkProcessing", "setProcessing"));
			else
				table.add(String.valueOf(system.checkProcessing()));
			table.row();
		}
	}

}
