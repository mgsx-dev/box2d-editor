package net.mgsx.box2d.editor.tools;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;

import net.mgsx.box2d.editor.model.BodyItem;
import net.mgsx.box2d.editor.model.WorldItem;

public class JointFrictionTool extends JointTool<FrictionJointDef> {
	public JointFrictionTool(Camera camera, WorldItem worldItem) {
		super("Friction", camera, worldItem, 2);
	}

	@Override
	protected FrictionJointDef createJoint(BodyItem bodyA, BodyItem bodyB) {

		FrictionJointDef def = new FrictionJointDef();
		def.bodyA = bodyA.body;
		def.bodyB = bodyB.body;
		def.collideConnected = true;
		def.localAnchorA.set(new Vector2(dots.get(0)).sub(bodyA.body
				.getPosition()));
		def.localAnchorB.set(new Vector2(dots.get(1)).sub(bodyB.body
				.getPosition()));

		return def;
	}
}
