package net.mgsx.box2d.editor.tools;

import net.mgsx.box2d.editor.BodyItem;
import net.mgsx.box2d.editor.WorldItem;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;

public class JointGearTool extends JointTool<GearJointDef> {
	public JointGearTool(Camera camera, WorldItem worldItem) {
		super("Gear", camera, worldItem, 2);
	}

	@Override
	protected GearJointDef createJoint(BodyItem bodyA, BodyItem bodyB) {

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
