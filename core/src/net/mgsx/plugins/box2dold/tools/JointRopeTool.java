package net.mgsx.plugins.box2dold.tools;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;

import net.mgsx.core.Editor;
import net.mgsx.plugins.box2d.model.Box2DBodyModel;
import net.mgsx.plugins.box2dold.model.WorldItem;

public class JointRopeTool extends JointTool<RopeJointDef> {
	public JointRopeTool(Editor editor, WorldItem worldItem) 
	{
		super("Rope", editor, worldItem, 2);
	}
	@Override
	protected RopeJointDef createJoint(Box2DBodyModel bodyA, Box2DBodyModel bodyB) {
		
		RopeJointDef def = new RopeJointDef();
		def.bodyA = bodyA.body;
		def.bodyB = bodyB.body;
		def.collideConnected = true;
		def.localAnchorA.set(new Vector2(dots.get(0)).sub(bodyA.body.getPosition()));
		def.localAnchorB.set(new Vector2(dots.get(1)).sub(bodyB.body.getPosition()));
		
		return def;
	}
}