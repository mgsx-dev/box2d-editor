package net.mgsx.game.core.tools;

import com.badlogic.gdx.Input;

import net.mgsx.game.core.Editor;

public class SwitchModeTool extends Tool
{
	public SwitchModeTool(Editor editor) {
		super(editor);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.TAB){
			editor.toggleMode();
			return true;
		}
		return super.keyDown(keycode);
	}
	
}
