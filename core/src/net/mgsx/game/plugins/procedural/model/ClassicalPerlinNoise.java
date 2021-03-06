package net.mgsx.game.plugins.procedural.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class ClassicalPerlinNoise 
{
	private RandomXS128 random = new RandomXS128();
	private long seed;
	
	public void seed(long seed){
		this.seed = seed;
	}
	
	/**
	 * Fast floor range from {@code -1<<23-1} = -8388609 to {@code 1<<23} = 8388608
	 * Optimization of {@code v<0 ? (int)v-1 : (int)v}
	 * Performances on OpenJDK seams near to MathUtils.floor
	 * 
	 * TODO pullup to libgdx
	 * 
	 * @param v
	 * @return
	 */
	public static int floor(float v){
		// return (int)v - (Float.floatToIntBits(v)>>>31); 
		return v<0 && (v%1f != 0f) ? (int)v-1 : (int)v;
	}
	
	/**
	 * @param x
	 * @param y
	 * @return a normalized distributed float between -1 and 1
	 */
	public float get(float x, float y)
	{
		// fast integer/fractonal decomposition
		float rx = x % 1f;
		int ix;
		if(rx < 0f){
			rx += 1f;
			ix = (int)x-1;
		}else{
			ix = (int)x;
		}
		
		float ry = y % 1f;
		int iy;
		if(ry < 0f){
			ry += 1f;
			iy = (int)y-1;
		}else{
			iy = (int)y;
		}
		
		float v00 = value(ix, iy);
		float v10 = value(ix+1, iy);
		float v01 = value(ix, iy+1);
		float v11 = value(ix+1, iy+1);

		if(rx<0f){
			rx = 0;
		}
		if(ry<0f){
			ry = 0;
		}
		
		return MathUtils.lerp(
				MathUtils.lerp(v00, v10, rx), 
				MathUtils.lerp(v01, v11, rx), ry);

	}

	private float value(int ix, int iy) 
	{
		random.setSeed(seed + ix);
		random.setSeed(random.nextInt() + iy);
		return random.nextFloat() * 2 - 1;
	}

	public Vector2 get(Vector2 result, float x, float y) {
		// TODO normal distributed angle doesn't work due to lerp average, need to wrap angle
		float angleRad = MathUtils.PI * get(x, y);
		result.x = MathUtils.cos(angleRad);
		result.y = MathUtils.sin(angleRad);
		return result;
	}
	
	public Vector3 get(Vector3 v, float x){
		v.x = get(seed + 0, x);
		v.y = get(seed + 1, x);
		v.z = get(seed + 2, x);
		return v;
	}
	
	private float get(long seed, float x){
		float rx = x % 1f;
		int ix;
		if(rx < 0f){
			rx += 1f;
			ix = (int)x-1;
		}else{
			ix = (int)x;
		}
		float v0 = value(seed + ix);
		float v1 = value(seed + ix+1);
		return MathUtils.lerp(v0, v1, rx);
	}
	
	private float value(long seed) 
	{
		random.setSeed(seed);
		return random.nextFloat() * 2 - 1;
	}
}
