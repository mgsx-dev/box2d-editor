package net.mgsx.game.examples.td.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.game.core.GamePipeline;
import net.mgsx.game.examples.td.components.Enemy;
import net.mgsx.game.examples.td.components.Speed;
import net.mgsx.game.plugins.core.components.Orientation3D;
import net.mgsx.game.plugins.core.components.Transform2DComponent;
import net.mgsx.game.plugins.g3d.components.G3DModel;

public class EnemyModelSystem extends IteratingSystem
{
	public EnemyModelSystem() {
		super(Family.all(G3DModel.class, Enemy.class).get(), GamePipeline.AFTER_LOGIC);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		G3DModel model = G3DModel.components.get(entity);
		Transform2DComponent transform = Transform2DComponent.components.get(entity);
		Speed speed = Speed.components.get(entity);
		model.modelInstance.transform.idt();
		model.modelInstance.transform.rotate(Vector3.X, -90);
		model.modelInstance.transform.translate(transform.position.x, transform.position.y, transform.depth);
//		model.modelInstance.transform.rotate(Vector3.X, 90);
//		model.modelInstance.transform.mul(new Matrix4().setToLookAt(Vector3.Z.cpy().scl(-1), transform.normal));
		model.modelInstance.transform.rotate(Vector3.X, 90);

		Orientation3D orientation = Orientation3D.components.get(entity);
		if(orientation != null){
			model.modelInstance.transform.rotate(new Quaternion().setFromCross(orientation.normal, orientation.direction));
		}
//		model.modelInstance.transform.rotate(Vector3.Y, transform.angle);
//		model.modelInstance.transform.rotate(Vector3.Z, 90 - MathUtils.radiansToDegrees * (float)Math.acos(transform.normal.z));
		//model.modelInstance.transform.rotate(Vector3.X, 90);
		
		float s = 0.15f;
		model.modelInstance.transform.scale(s, s, s);
		if(model.animationController.current == null){
			model.animationController.animate("Armature|walk", -1, null, 0);
		}
		model.animationController.current.speed = speed.current * 8;
		
		Material mat = model.modelInstance.getMaterial("M_Light");
		if(mat != null){
			ColorAttribute diffuse = (ColorAttribute)mat.get(ColorAttribute.Diffuse);
			diffuse.color.a = 0.5f;
		}
	}
}
