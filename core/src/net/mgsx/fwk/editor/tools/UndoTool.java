package net.mgsx.fwk.editor.tools;

import com.badlogic.gdx.Input;

import net.mgsx.fwk.editor.CommandHistory;

public class UndoTool extends ToolBase
{
	private CommandHistory history;
	
	public UndoTool(CommandHistory history) {
		this.history = history;
	}

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.Z)
		{
			if(ctrl())
			{
				if(shift()){
					history.redo();
				}else{
					history.undo();
				}
				return true;
			}
		}
		return false;
	}
}
