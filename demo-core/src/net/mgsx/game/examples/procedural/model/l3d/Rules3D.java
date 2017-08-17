package net.mgsx.game.examples.procedural.model.l3d;

import net.mgsx.game.examples.procedural.model.Generator;
import net.mgsx.game.examples.procedural.model.Parameter;
import net.mgsx.game.examples.procedural.model.Rule;
import net.mgsx.game.examples.procedural.model.Rule.RuleBase;

public class Rules3D 
{
	public static class RuleBase extends Rule.RuleBase<Context3D>{}
	
	public static final Rule size = new RuleBase() {
		private Parameter size;
		@Override
		public void init(Generator<Context3D> context){
			super.init(context);
			size = context.system.parameter("size");
		}
		@Override
		public void execute(int depth) {
			context.current.scale.scl(size.value);
		}
	};
}