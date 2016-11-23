package net.mgsx.game.plugins.camera.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Camera;

import net.mgsx.game.core.annotations.Editable;
import net.mgsx.game.core.annotations.EditableComponent;

@EditableComponent(autoTool=false)
public class CameraComponent implements Component
{
	
	public static final ComponentMapper<CameraComponent> components = ComponentMapper.getFor(CameraComponent.class);
	
	@Editable public Camera camera;
	public boolean frustumDirty;
}
