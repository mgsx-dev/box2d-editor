package net.mgsx.game.examples.tactics.logic;

import java.util.Comparator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import net.mgsx.game.examples.tactics.model.Model;
import net.mgsx.game.examples.tactics.model.TeamDef;

public class BattleLogic {

	public static interface BattleListener
	{
		public void onEffectApply(CharacterBattle target, EffectBattle fx);
		public void onDie(CharacterBattle target);
		public void onPlayerChange(CharacterBattle old, CharacterBattle current);
		public void onEnd(boolean victoryA, boolean victoryB);
		public void onTarget(CardBattle card, CharacterBattle target);
		public void onEffectBegin(CharacterBattle target, EffectBattle fx);
		public void onEffectEnd(CharacterBattle target, EffectBattle fx);
	}
	
	public BattleListener listener = new BattleListener() {
		@Override
		public void onPlayerChange(CharacterBattle old, CharacterBattle current) {
		}
		@Override
		public void onEffectApply(CharacterBattle target, EffectBattle fx) {
		}
		@Override
		public void onDie(CharacterBattle target) {
		}
		@Override
		public void onEnd(boolean victoryA, boolean victoryB) {
		}
		@Override
		public void onTarget(CardBattle card, CharacterBattle target) {
		}
		@Override
		public void onEffectBegin(CharacterBattle character, EffectBattle fx) {
		}
		@Override
		public void onEffectEnd(CharacterBattle character, EffectBattle fx) {
		}
	};
	
	
	private static Comparator<CardBattle> cardComparator = new Comparator<CardBattle>() {
		@Override
		public int compare(CardBattle o1, CardBattle o2) {
			return Float.compare(o1.turns, o2.turns);
		}
	};
	private static Comparator<CharacterBattle> characterComparator = new Comparator<CharacterBattle>() {
		@Override
		public int compare(CharacterBattle o1, CharacterBattle o2) {
			return Float.compare(o1.turns, o2.turns);
		}
	};
	
	public TeamBattle teamA, teamB;
	
	public Array<CharacterBattle> characters = new Array<CharacterBattle>();
	
	public CharacterBattle current;
	public Model model;
	
	private int turn = -1;
	
	public static BattleLogic create(Model model, TeamDef a, TeamDef b){
		BattleLogic battle = new BattleLogic();
		battle.model = model;
		battle.teamA = new TeamBattle(a);
		battle.teamB = new TeamBattle(b);
		battle.characters.addAll(battle.teamA.characters);
		battle.characters.addAll(battle.teamB.characters);
		for(CharacterBattle character : battle.characters){
			for(String id : character.def.cards){
				CardBattle card = new CardBattle(model.getCards(id));
				card.turns = card.def.wait;
				character.cards.add(card);
			}
		}
		return battle;
	}
	
	public void nextTurn()
	{
		if(characters.size <= 0) return;
		
		if(turn < 0){
			
			if(!teamA.surprise){
				for(CharacterBattle character : teamA.characters)
				{
					for(CardBattle card : character.cards){
						card.turns = 0;
					}
				}
			}
			if(!teamB.surprise){
				for(CharacterBattle character : teamB.characters)
				{
					for(CardBattle card : character.cards){
						card.turns = 0;
					}
				}
			}
			
			turn = 0;
		}
		
		for(CharacterBattle character : characters)
		{
			character.cards.sort(cardComparator);
			CardBattle card = character.cards.first();
			character.turns = card.turns;
		}
		CharacterBattle old = current;
		characters.sort(characterComparator);
		current = characters.first();
		float turns = current.turns;
		
		System.out.println("turns passed : " + String.valueOf(turns));
		
		// update cards for the next step
		for(CharacterBattle character : characters)
		{
			for(CardBattle card : character.cards){
				card.turns -= turns;
			}
			character.protection = character.def.protection;
		}
		
		// resolve pending effects
		// TODO ordering is based on sorted list !
		for(int i=0 ; i<characters.size ; )
		{
			CharacterBattle character = characters.get(i);
			for(int j=0 ; j<character.effects.size ; ){
				EffectBattle fx = character.effects.get(j);
				if(fx.isNew){
					listener.onEffectBegin(character, fx);
				}
				
				int consumedTurns = Math.max(0, (int)Math.min(turns, fx.turns));
				if(fx.turns == 0)
				consumedTurns = 1;
				fx.turns -= consumedTurns;
				if(fx.life != 0){
					character.life = MathUtils.clamp(character.life + fx.life * consumedTurns, 0, character.def.life);
				}
				if(fx.protection != 0){
					character.protection += fx.protection;
				}
				listener.onEffectApply(character, fx);
				if(fx.turns <= 0){
					character.effects.removeIndex(j);
					listener.onEffectEnd(character, fx);
				}else{
					j++;
				}
				fx.isNew = false;
			}
			character.protection = Math.max(0, character.protection);
			if(character.life <= 0){
				characters.removeIndex(i);
				teamA.characters.removeValue(character, true);
				teamB.characters.removeValue(character, true);
				listener.onDie(character);
			}else{
				i++;
			}
		}
		
		// select again to remove dead entities
		
		current = characters.size > 0 ? characters.first() : null;
		
		// if(old != current){
			listener.onPlayerChange(old, current);
		// }
		
		if(old != null){
			old.control.disable(old);
		}
		if(current != null){
			current.control.enable(current);
		}

		if(teamA.characters.size <= 0 || teamB.characters.size <= 0){
			listener.onEnd(teamA.characters.size > 0, teamB.characters.size > 0);
		}
	}
	
	public void selectAction(CardBattle card, CharacterBattle target){
		selectAction(card, new Array<CharacterBattle>(new CharacterBattle[]{target}));
	}
	public void selectAction(CardBattle card, Array<CharacterBattle> targets)
	{
		float falloff = 1;
		for(CharacterBattle target : targets){
			listener.onTarget(card, target);
			EffectBattle fx = new EffectBattle();
			
			if(card.def.dmg != null){
				if(card.def.dmg.min < 0)
					fx.life = MathUtils.random(-card.def.dmg.min, -card.def.dmg.max);
				else
					fx.life = -MathUtils.random(card.def.dmg.min, card.def.dmg.max);
			}
			if(fx.life < 0 && card.def.turns == null){ // only for attack
				fx.life = -Math.max(0, -fx.life - target.protection);
			}
			
			// protection
			if(card.def.protection != null){
				if(card.def.protection.min < 0)
					fx.protection = -MathUtils.random(-card.def.protection.min, -card.def.protection.max);
				else
					fx.protection = MathUtils.random(card.def.protection.min, card.def.protection.max);
			}
			// critical
			if(targets.size <= 1){
				fx.critical = MathUtils.randomBoolean(card.def.critical/100f);
				if(fx.critical){
					fx.life *= 2;
					fx.protection *= 2;
				}
			}else{
				// targets influence
				fx.life *= falloff;
				falloff /= 2;
			}
			
			
			if(card.def.turns != null){
				fx.turns = MathUtils.random(card.def.turns.min, card.def.turns.max);
			}else{
				fx.turns = 0;
			}
			target.effects.add(fx);
		}
		card.turns = card.def.wait;
		
//		for(CharacterBattle character : characters)
//		{
//			character.cards.sort(cardComparator);
//			CardBattle c = character.cards.first();
//			character.turns = c.turns;
//		}
//		characters.sort(characterComparator);
	}

	public boolean isOver() {
		return teamA.characters.size <= 0 || teamB.characters.size <= 0;
	}
}
