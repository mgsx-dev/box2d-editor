package net.mgsx.game.examples.platformer.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector2;

import net.mgsx.game.core.GamePipeline;
import net.mgsx.game.core.GameScreen;
import net.mgsx.game.core.helpers.BlenderHelper;
import net.mgsx.game.core.helpers.EntityHelper;
import net.mgsx.game.examples.platformer.components.EnvComponent;
import net.mgsx.game.examples.platformer.components.PlayerComponent;
import net.mgsx.game.plugins.g3d.components.G3DModel;

public class EnvSystem extends IteratingSystem
{
	private GameScreen gameEngine;

	public EnvSystem(GameScreen gameEngine) {
		super(Family.all(G3DModel.class, EnvComponent.class).get(), GamePipeline.LOGIC); // TODO before render but not before VBO !
		this.gameEngine = gameEngine;
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) 
	{
		EnvComponent env = entity.getComponent(EnvComponent.class);
		
		if(env.enabled)
		{
			G3DModel model = entity.getComponent(G3DModel.class);
			Node camera = model.modelInstance.getNode("Camera.001", true);
			env.cameraOffset.set(camera.translation);
			gameEngine.getCullingCamera().direction.set(1, 0, 0);
			camera.rotation.transform(gameEngine.getCullingCamera().direction);
			
			// find player entity
			Entity player = EntityHelper.first(getEngine(), Family.one(PlayerComponent.class).get());
			
			if(player != null){
				// update camera
				Vector2 v = player.getComponent(PlayerComponent.class).physics.body.getPosition();
				gameEngine.getCullingCamera().position.set(v.x, v.y, 0);
				gameEngine.getCullingCamera().position.add(env.cameraOffset);
				//gameEngine.camera.direction.setFromSpherical(MathUtils.degreesToRadians * 90, MathUtils.degreesToRadians * -178);
				
				float blenderFov = 49.134f;
				((PerspectiveCamera) gameEngine.getCullingCamera()).fieldOfView = BlenderHelper.fov(blenderFov, 1920f / 1080f);
				((PerspectiveCamera) gameEngine.getCullingCamera()).near = 0.1f;
				((PerspectiveCamera) gameEngine.getCullingCamera()).far = 200f;
				
			}
		}
		
	}
}