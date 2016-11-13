package net.mgsx.game.plugins.camera.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import net.mgsx.game.core.EditorScreen;
import net.mgsx.game.core.GamePipeline;
import net.mgsx.game.core.helpers.RenderDebugHelper;
import net.mgsx.game.plugins.camera.components.CameraComponent;

public class CameraDebugSystem extends IteratingSystem
{
	private EditorScreen editor;
	private Entity currentCamera;

	public CameraDebugSystem(EditorScreen editor) {
		super(Family.all(CameraComponent.class).get(), GamePipeline.RENDER_DEBUG);
		this.editor = editor;
	}
	
	@Override
	public void update(float deltaTime) 
	{
		currentCamera = getEngine().getSystem(CameraSystem.class).getRenderCamera();
		editor.shapeRenderer.setColor(0, 0, 1, 1f);
		editor.shapeRenderer.begin(ShapeType.Line);
		super.update(deltaTime);
		editor.shapeRenderer.end();
		currentCamera = null;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) 
	{
		if(entity != currentCamera){
			CameraComponent camera = CameraComponent.components.get(entity);
			RenderDebugHelper.frustum(editor.shapeRenderer, camera.camera.frustum);
		}
	}

}
