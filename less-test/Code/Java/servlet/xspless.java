package servlet;

import java.io.*;
import java.util.Arrays;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lesscss.LessCompiler;

import com.ibm.commons.util.io.StreamUtil;

public class xspless extends AbstractXSPServlet {

	@Override
	protected void doService(final HttpServletRequest req, final HttpServletResponse res, final FacesContext facesContext, final ServletOutputStream out) throws Exception {
		//		out.println("okay");
		String[] pathBits = facesContext.getExternalContext().getRequestPathInfo().split("/", 4);
		String fileName = pathBits[3];
		//		out.println("path is " + "/" + fileName);
		//		Object resource = facesContext.getExternalContext().getResource("/" + fileName);
		//		out.println("resource is " + resource);

		InputStream is = facesContext.getExternalContext().getResourceAsStream("/" + fileName);
		String less = StreamUtil.readString(is);
		is.close();

		LessCompiler lessCompiler = new LessCompiler();
		String css = lessCompiler.compile(less);

		res.setContentType("text/css");
		res.setContentLength(css.length());
		out.print(css);
	}

}
