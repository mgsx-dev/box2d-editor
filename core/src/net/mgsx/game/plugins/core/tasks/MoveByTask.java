package net.mgsx.game.plugins.core.tasks;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.btree.annotation.TaskAttribute;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import net.mgsx.game.plugins.btree.BTreePlugin.EntityLeafTask;
import net.mgsx.game.plugins.btree.annotations.TaskAlias;
import net.mgsx.game.plugins.core.components.Transform2DComponent;

@TaskAlias("moveBy")
public class MoveByTask extends EntityLeafTask
{
	@TaskAttribute
	public float tx, ty, tz;
	
	@TaskAttribute
	public float duration;
	
	public Interpolation interpolation;
	
	private Vector2 origin = new Vector2();
	private float time, depthOrigin;
	
	@Override
	public void start() {
		time = 0;
		if(interpolation == null) interpolation = Interpolation.linear;
		Transform2DComponent transform = Transform2DComponent.components.get(getEntity());
		if(transform != null){
			origin.set(transform.position);
			depthOrigin = transform.depth;
		}
	}
	
	@Override
	public Status execute() {
		time += GdxAI.getTimepiece().getDeltaTime();
		Transform2DComponent transform = Transform2DComponent.components.get(getEntity());
		if(transform != null){
			if(time > duration){
				transform.position.set(origin).add(tx, ty);
				transform.depth = tz;
				time = 0;
				return Status.SUCCEEDED;
			}
			float t = time / duration;
			transform.position.set(origin).add(tx * interpolation.apply(t), ty * interpolation.apply(t));
			transform.depth = interpolation.apply(depthOrigin, tz, t);
		}
		return Status.RUNNING;
	}
}
