package jointyc.jdlc.exception;

public class NotDeclaredTerminalException extends RuntimeException {

	public final String terminalName;
	public NotDeclaredTerminalException(String terminalName) {
		super("Terminal \"" + terminalName + "\" not declared in the lexicon");
		this.terminalName = terminalName;
	}

}
