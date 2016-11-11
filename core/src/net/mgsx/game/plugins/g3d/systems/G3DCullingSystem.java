package net.mgsx.game.plugins.g3d.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

import net.mgsx.game.core.GamePipeline;
import net.mgsx.game.core.annotations.Editable;
import net.mgsx.game.core.annotations.EditableSystem;
import net.mgsx.game.core.components.Hidden;
import net.mgsx.game.plugins.g3d.components.G3DModel;
import net.mgsx.game.plugins.g3d.components.NodeBoundary;

// TODO separate boundary from culling ?
@EditableSystem
public class G3DCullingSystem extends IteratingSystem 
{
	@Editable
	public boolean culling = true;
	
	private final Camera camera;
	public G3DCullingSystem(Camera camera) {
		super(Family.all(G3DModel.class).exclude(Hidden.class).get(), GamePipeline.BEFORE_RENDER);
		this.camera = camera;
	}

	@Override
	public void update(float deltaTime) {
		camera.update(true); // TODO necessary ? if not use frustum only ... instead of camera
		super.update(deltaTime);
	}

	private void scan(Array<NodeBoundary> bounds, Iterable<Node> nodes){
		if(nodes == null) return;
		for(Node node : nodes){
			NodeBoundary b = new NodeBoundary();
			b.node = node;
			b.local = new BoundingBox();
			node.calculateBoundingBox(b.local, false);
			b.global = new BoundingBox();
			bounds.add(b);
			scan(bounds, node.getChildren());
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) 
	{
		G3DModel model = G3DModel.components.get(entity);
		if(model.localBoundary == null){
			model.localBoundary = new BoundingBox();
			model.modelInstance.calculateBoundingBox(model.localBoundary);
			model.globalBoundary = new BoundingBox();
			model.boundary = new Array<NodeBoundary>();
			scan(model.boundary, model.modelInstance.nodes);
		}
		else if(model.culling){
			model.modelInstance.calculateBoundingBox(model.localBoundary);
		}
		if(culling)
		{
			model.globalBoundary.set(model.localBoundary).mul(model.modelInstance.transform);
			model.inFrustum = camera.frustum.boundsInFrustum(model.globalBoundary);
			if(model.inFrustum){
				for(NodeBoundary b : model.boundary)
					b.update(model.modelInstance, camera.frustum);
			}
		}
		else
		{
			model.inFrustum = true;
			for(NodeBoundary b : model.boundary)
				b.show();
		}
	}
}