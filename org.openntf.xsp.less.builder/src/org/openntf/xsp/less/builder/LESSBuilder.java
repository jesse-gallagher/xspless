package org.openntf.xsp.less.builder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.lesscss.LessCompiler;

import com.ibm.commons.util.io.StreamUtil;

public class LESSBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.openntf.xsp.less.builder.lessBuilder";
	private static final String MARKER_TYPE = "org.openntf.xsp.less.builder.lessProblem";

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

	@SuppressWarnings("unused")
	private void addMarker(IFile file, String message, int lineNumber, int severity) {
		try {
			IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		} catch (CoreException e) {
		}
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

	private void deleteMarkers(IFile file) {
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		getProject().accept(new LESSResourceVisitor());
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		delta.accept(new LESSDeltaVisitor());
	}

	void processLESSFile(IResource resource) throws CoreException {
		if (resource instanceof IFile && resource.getName().endsWith(".less")) {
			try {
				IFile lessFile = (IFile) resource;
				System.out.println("Added/Modified LESS file " + lessFile.getProjectRelativePath());
				deleteMarkers(lessFile);

				InputStream is = lessFile.getContents();
				String lessCode = StreamUtil.readString(is);
				is.close();

				String result = lessCompiler_.compile(lessCode);
				IFile workspaceBuildFile = ((IFolder) lessFile.getParent()).getFile(lessFile.getName() + ".css");
				ByteArrayInputStream bytes = new ByteArrayInputStream(result.getBytes());

				if (!workspaceBuildFile.exists()) {
					workspaceBuildFile.create(bytes, 0, null);
				} else {
					workspaceBuildFile.setContents(bytes, 0, null);
					workspaceBuildFile.setLocalTimeStamp(Calendar.getInstance().getTimeInMillis());
				}

				System.out.println("Created/Modified file " + workspaceBuildFile.getProjectRelativePath());
			} catch (Exception e) {
				e.printStackTrace();
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Exception during LESS file processing", e));
			}
		}
	}
}
