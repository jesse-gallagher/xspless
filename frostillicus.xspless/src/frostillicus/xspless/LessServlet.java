package frostillicus.xspless;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lesscss.LessCompiler;
import org.openntf.domino.Database;
import org.openntf.domino.Session;
import org.openntf.domino.design.DatabaseDesign;
import org.openntf.domino.design.StyleSheet;
import org.openntf.domino.types.FactorySchema;
import org.openntf.domino.utils.Factory;

import com.ibm.domino.osgi.core.context.ContextInfo;

public class LessServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	//	private int callCount = 0;
	private LessCompiler lessCompiler_ = new LessCompiler();
	private Map<String, String> cache_ = new ConcurrentHashMap<String, String>();
	private Map<String, Date> cacheModified_ = new ConcurrentHashMap<String, Date>();

	@Override
	public void service(final ServletRequest servletRequest, final ServletResponse servletResponse) throws ServletException, IOException {
		HttpServletResponse res = (HttpServletResponse) servletResponse;
		HttpServletRequest req = (HttpServletRequest) servletRequest;

		Session session = Factory.fromLotus(ContextInfo.getUserSession(), (FactorySchema<Session, lotus.domino.Session, ?>) null, null);
		Database database = Factory.fromLotus(ContextInfo.getUserDatabase(), (FactorySchema<Database, lotus.domino.Database, Session>) null, session);

		// Database database = ContextInfo.getUserDatabase();
		res.setContentType("text/plain");
		ServletOutputStream out = res.getOutputStream();

		try {
			// TODO See if there's a way to get the xspnsf URL route to work without
			// throwing a NotesContext-not-initialized exception
			// TODO Fix cache to check for modifications

			String cacheKey = req.getContextPath() + req.getPathInfo();

			// xspnsf://server:0/tests/res.nsf/foo.less
			String[] pathBits = req.getPathInfo().split("/", 2);
			// String fileName = pathBits[3];
			//			out.println("Call count: " + ++callCount);
			//			out.println("I was called to process " + Arrays.asList(pathBits) + " within " + database.getFilePath());
			//			out.println("Context path is " + req.getContextPath());

			DatabaseDesign design = database.getDesign();
			StyleSheet ss = design.getStyleSheet(pathBits[1].replace("/", "\\"));
			Date lastModified = ss.getDocument().getLastModifiedDate();

			if (!cache_.containsKey(cacheKey) || (lastModified.after(cacheModified_.get(cacheKey)))) {
				// URL url = new URL("xspnsf://server:0" + req.getContextPath() +
				// req.getPathInfo());
				// out.println("URL is " + url);
				// InputStream is = url.openStream();
				// String content = StreamUtil.readString(is);
				// is.close();
				String content = new String(ss.getFileData(), "UTF-8");
				//			out.println("content:\n");
				//			out.println(content);

				cache_.put(cacheKey, lessCompiler_.compile(content));
				cacheModified_.put(cacheKey, new Date());
			}
			String css = cache_.get(cacheKey);
			res.setContentType("text/css");
			res.setContentLength(css.length());
			out.print(css);

			// URL resource = extContext.getResource("/" + fileName);
			// out.println("That resource's URL is " + resource);
		} catch (Exception e) {
			e.printStackTrace(new PrintStream(out));
		} finally {
			out.close();
		}
	}
}
