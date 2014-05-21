- GlobalResourceProvider is plugin-only and is used to provide a "mount point" of resources
- com.ibm.xsp.RequestParameters can be done in-app
- The parameters can just inject resources and alter urls, not actually respond to requests for a file path

- Potential two-pronged route:
	- use RequestParameters to add a UrlProcessor that converts resource requests for .less files to go through an XSP servlet
	- Find out if the resource aggregator for CSS ("/db.nsf/xsp/.ibmmodres/.css") can be extended/modified to work with LESS
