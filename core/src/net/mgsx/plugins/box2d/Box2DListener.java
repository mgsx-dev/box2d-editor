package net.mgsx.plugins.box2d;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public interface Box2DListener {

	public void beginContact(Contact contact, Fixture self, Fixture other);	
	public void endContact(Contact contact, Fixture self, Fixture other);	
}
