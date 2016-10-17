package net.mgsx.plugins.box2dold.tools;

import com.badlogic.gdx.physics.box2d.joints.GearJointDef;

import net.mgsx.core.Editor;
import net.mgsx.plugins.box2d.model.Box2DBodyModel;
import net.mgsx.plugins.box2dold.model.WorldItem;

public class JointGearTool extends JointTool<GearJointDef> {
	public JointGearTool(Editor editor, WorldItem worldItem) {
		super("Gear", editor, worldItem, 2);
	}

	@Override
	protected GearJointDef createJoint(Box2DBodyModel bodyA, Box2DBodyModel bodyB) {

		GearJointDef def = new GearJointDef();
		def.bodyA = bodyA.body;
		def.bodyB = bodyB.body;
		def.collideConnected = true;
//		def.localAnchorA.set(new Vector2(dots.get(0)).sub(bodyA.body
//				.getPosition()));
//		def.localAnchorB.set(new Vector2(dots.get(1)).sub(bodyB.body
//				.getPosition()));
		// TODO joint 1 and 2 ... ?
		return def;
	}
}