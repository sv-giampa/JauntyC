package jointyc.tutorial.list;

import java.io.IOException;
import java.util.List;

import jointyc.analysis.StandardCompiler;
import jointyc.analysis.parser.exception.UnexpectedSymbolException;
import jointyc.analysis.semantic.exception.SemanticException;
import jointyc.jdlc.JdlCompiler;

public class ListCompiler {
	
	private StandardCompiler compiler;
	
	public ListCompiler() {
		JdlCompiler jdlc = new JdlCompiler();
		try {
			compiler = jdlc.compileResource("list.jdl", new ListInterpreter());
			return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnexpectedSymbolException e) {
			e.printStackTrace();
		} catch (SemanticException e) {
			e.printStackTrace();
		}
		System.exit(-1);
	}
	
	@SuppressWarnings("unchecked")
	public List<Object> compile(String source) throws UnexpectedSymbolException{
		try {
			return (List<Object>)compiler.compile(source);
		}catch (SemanticException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) {
		ListCompiler listc = new ListCompiler();
		try {
			List<Object> list;
			
			list = listc.compile("<'list', 'of', 'strings'>");
			System.out.println(list);
			list = listc.compile("<2, 3.001, 5.4>");
			System.out.println(list);
			list = listc.compile("{<2,3>,{'abc',8,'6'}, <<5,6>,<7,8,9>>}");
			System.out.println(list);
			list = listc.compile("{{2,3.5},{<'list'>,<'of','lists'>}, 7, 'hello'}");
			System.out.println(list);
		} catch (UnexpectedSymbolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}






























