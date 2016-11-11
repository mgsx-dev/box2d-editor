package net.mgsx.game.plugins.box2d.tools.joints;

import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

import net.mgsx.game.core.Editor;
import net.mgsx.game.plugins.box2d.components.Box2DBodyModel;
import net.mgsx.game.plugins.box2d.components.WorldItem;

public class JointMouseTool extends JointTool<MouseJointDef> {
	public JointMouseTool(Editor editor, WorldItem worldItem) {
		super("Mouse", editor, worldItem, 2);
	}

	@Override
	protected MouseJointDef createJoint(Box2DBodyModel bodyA, Box2DBodyModel bodyB) {

		MouseJointDef def = new MouseJointDef();
		def.bodyA = bodyA.body;
		def.bodyB = bodyB.body;
		def.collideConnected = true;
		return def;
	}
}