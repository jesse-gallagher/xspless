package servlet;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lesscss.LessCompiler;

public class LessTest extends AbstractXSPServlet {

	@Override
	protected void doService(final HttpServletRequest req, final HttpServletResponse res, final FacesContext facesContext, final ServletOutputStream out) throws Exception {
		out.println("okay");
		LessCompiler lessCompiler = new LessCompiler();
		String css = lessCompiler.compile("@color: #abc123; #header { color: @color; }");
		out.println(css);

		out.println("done");
	}

}
