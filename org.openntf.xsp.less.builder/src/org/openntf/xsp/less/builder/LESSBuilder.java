package org.openntf.xsp.less.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.lesscss.LessCompiler;

import com.ibm.commons.util.io.StreamUtil;

public class LESSBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.openntf.xsp.less.builder.lessBuilder";
	private static final String MARKER_TYPE = "org.openntf.xsp.less.builder.lessProblem";
	private static Pattern ERROR_LINE_PATTERN = Pattern.compile(".*on line (\\d+), column (\\d+):.*");

	private LessCompiler lessCompiler_ = new LessCompiler();

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	private void addMarker(IFile file, String message, int lineNumber, int severity) throws CoreException {
		IMarker marker = file.createMarker(MARKER_TYPE);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		if (lineNumber == -1) {
			lineNumber = 1;
		}
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
	}

	class LESSDeltaVisitor implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				processLESSFile(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				processLESSFile(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class LESSResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) {
			try {
				processLESSFile(resource);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			//return true to continue visiting children.
			return true;
		}
	}

	private void deleteMarkers(IFile file) throws CoreException {
		file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		getProject().accept(new LESSResourceVisitor());
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new LESSDeltaVisitor());
	}

	void processLESSFile(IResource resource) throws CoreException {
		String resourceName = resource.getName();
		if (resource instanceof IFile && (resourceName.endsWith(".less") || resourceName.endsWith(".less.css"))) {

			try {
				IFile lessFile = (IFile) resource;
				//				System.out.println("Added/Modified LESS file " + lessFile.getProjectRelativePath());
				deleteMarkers(lessFile);

				InputStream is = lessFile.getContents();
				String lessCode = StreamUtil.readString(is);
				is.close();

				String result;
				synchronized (lessCompiler_) {
					result = lessCompiler_.compile(lessCode);
				}

				// Ending with .less.css = Domino Stylesheet resource
				// Ending with just .less = normal LESS file
				String resultName = resourceName.endsWith(".less.css") ? (resourceName.substring(0, resourceName.lastIndexOf(".less.css")) + ".css") : (resourceName + ".css");

				IFile workspaceBuildFile = ((IFolder) lessFile.getParent()).getFile(resultName);
				ByteArrayInputStream bytes = new ByteArrayInputStream(result.getBytes());

				if (!workspaceBuildFile.exists()) {
					workspaceBuildFile.create(bytes, 0, null);
				} else {
					workspaceBuildFile.setContents(bytes, 0, null);
					workspaceBuildFile.setLocalTimeStamp(Calendar.getInstance().getTimeInMillis());
				}

				//				System.out.println("Created/Modified file " + workspaceBuildFile.getProjectRelativePath());
			} catch (Exception e) {
				//				e.printStackTrace();

				String message = e.getMessage();
				Matcher errorMatcher = ERROR_LINE_PATTERN.matcher(message);
				if (errorMatcher.matches()) {
					System.out.println("Adding marker for line " + errorMatcher.group(1));
					addMarker((IFile) resource, "Exception during LESS file processing: " + e.getMessage(), Integer.parseInt(errorMatcher.group(1)), IMarker.SEVERITY_ERROR);
				} else {
					System.out.println("Adding marker for unknown line");
					System.out.println("Message was " + message);
					addMarker((IFile) resource, "Exception during LESS file processing: " + e.getMessage(), -1, IMarker.SEVERITY_ERROR);
				}
			}
		}
	}
}
