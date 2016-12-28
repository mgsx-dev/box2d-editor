package net.mgsx.game.examples.platformer.inputs;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;

import net.mgsx.game.core.GamePipeline;
import net.mgsx.game.examples.platformer.animations.WalkingComponent;
import net.mgsx.game.plugins.box2d.components.Box2DBodyModel;
import net.mgsx.game.plugins.box2d.helper.Box2DHelper;
import net.mgsx.game.plugins.box2d.helper.RayCast;
import net.mgsx.game.plugins.box2d.helper.RayCastResult;

public class WalkingSystem extends IteratingSystem
{
	private final static RayCast ray = new RayCast();
	
	public WalkingSystem() {
		super(Family.all(Box2DBodyModel.class, PlayerController.class, WalkingComponent.class).get(), GamePipeline.LOGIC);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		PlayerController player = PlayerController.components.get(entity);
		Box2DBodyModel physics = Box2DBodyModel.components.get(entity);
		Vector2 velocity = physics.body.getLinearVelocity().cpy();
		
		player.lastGroundFixture = null;
		
		if(physics.bounds.area() <= 0){
			Box2DHelper.computeBoundary(physics.bounds, physics.body);
		}
		
		float groundSpace = 0.25f;
		float groundBefore = .5f;
		float groundAfter = groundBefore + .2f;
		
		// ray cast for ground
		ray.start.set(physics.body.getPosition()).add(
				physics.bounds.x + physics.bounds.width/2, 
				physics.bounds.y + groundBefore);
		ray.end.set(ray.start).y -= groundAfter;
		ray.end.x -= physics.bounds.width * groundSpace;
		
		RayCastResult result;
		
		result = physics.context.provider.rayCastFirst(ray);
		Fixture leftFixture = result == null ? null : result.fixture;
		if(player.lastGroundFixture == null) player.lastGroundFixture = leftFixture;
		boolean onGround1 = leftFixture != null;
		
		ray.start.set(physics.body.getPosition()).add(
				physics.bounds.x + physics.bounds.width/2, 
				physics.bounds.y + groundBefore);
		ray.end.set(ray.start).y -= groundAfter;
		ray.end.x += physics.bounds.width * groundSpace;
		
		result = physics.context.provider.rayCastFirst(ray);
		Fixture rightFixture = result == null ? null : result.fixture;
		if(player.lastGroundFixture == null) player.lastGroundFixture = rightFixture;
		boolean onGround2 = rightFixture != null;
		
		boolean onGround = onGround1 || onGround2;
		
		player.onGroundLeft = onGround1;
		player.onGroundRight = onGround2;
		
		player.onGround = onGround;
		
		// ray cast for walls
		ray.start.set(physics.body.getPosition()).add(
				physics.bounds.x + 0.1f, 
				physics.bounds.y + physics.bounds.height / 2);
		ray.end.set(ray.start).x -= 0.2f;
		
		boolean wallLeft = physics.context.provider.rayCastFirst(ray) != null;
		
		ray.start.set(physics.body.getPosition()).add(
				physics.bounds.x + physics.bounds.width - 0.1f, 
				physics.bounds.y + physics.bounds.height / 2);
		ray.end.set(ray.start).x += 0.2f;
		
		boolean wallRight = physics.context.provider.rayCastFirst(ray) != null;
		
		// ray cast for head (grab ceil)
		ray.start.set(physics.body.getPosition()).add(
				physics.bounds.x + physics.bounds.width / 2, 
				physics.bounds.y + physics.bounds.height - 0.1f);
		ray.end.set(ray.start).y += 0.2f;
		
		// TODO boolean ceil = physics.context.provider.rayCastFirst(ray) != null;
		
		player.onWallLeft = wallLeft;
		player.onWallRight = wallRight;
		
		
		
		float duration = 0.15f;
		if(player.jump){
			player.jumpTime += deltaTime;
			float vlc = 100;
			if(player.jumpTime > duration){
				if(wallLeft && velocity.y < vlc  && player.justJump){
					player.jumpTime = duration/2;
					velocity.x = 15;
					physics.body.setLinearVelocity(Vector2.Zero);
					physics.body.applyLinearImpulse(new Vector2(8,8), physics.body.getWorldCenter(), true);
				}
				else if(wallRight && velocity.y < vlc && player.justJump){
					player.jumpTime = duration/2;
					velocity.x = -15;
					physics.body.setLinearVelocity(Vector2.Zero);
					physics.body.applyLinearImpulse(new Vector2(-8,8), physics.body.getWorldCenter(), true);
				}
				else if(velocity.y < -2){
					velocity.y = -2;
					physics.body.setLinearVelocity(velocity);
					//physics.body.applyLinearImpulse(new Vector2(0,0.3f), physics.body.getWorldCenter(), true);
				}
			}else if(velocity.y < 8){
				physics.body.applyLinearImpulse(new Vector2(0,1.5f), physics.body.getWorldCenter(), true);
			}
		}else{
			if(onGround){
				player.jumpTime = 0;
			}else{
				player.jumpTime = duration;
			}
			
		}
		float hforce = onGround ? .4f : .18f; // .18 just enough to not climb on single side.
		if(player.left){
			physics.body.applyLinearImpulse(new Vector2(-hforce,0), physics.body.getWorldCenter(), true);
		}else if(player.right){
			physics.body.applyLinearImpulse(new Vector2(hforce,0), physics.body.getWorldCenter(), true);
		}else{
			physics.body.applyLinearImpulse(new Vector2(-velocity.x * 0.1f,0), physics.body.getWorldCenter(), true);
		}

		
		velocity = physics.body.getLinearVelocity();
		
		float hlimit = 4;
		if(velocity.x < -hlimit) velocity.x = -hlimit; else if(velocity.x > hlimit) velocity.x = hlimit;
		
		physics.body.setLinearVelocity(velocity);
		
		// TODO set friction at 2.0 for player to not slide !
		
//		if(player.justJump){
//			velocity.y = 7;
//		}
//		else if(player.jump){
//			if(velocity.y < 0) velocity.y = -0.2f;
//		}
		
		if(player.grab){
		}else{
			if(physics.body.getJointList().size > 0){
				physics.context.world.destroyJoint(physics.body.getJointList().get(0).joint);
			}
		}
	}

}
