xspless
=======

LESS CSS Plugin for XPages

This takes the form of two projects:

* A servlet aliased to "/xspless" within an NSF (behavior outside of an NSF is not handled). By specifying a path to a LESS file in the NSF as "/xspless/file.less" (can be a Stylesheet, a File Resource, or stored in WebContent), the servlet will automatically compile the LESS file to standard CSS to serve to the browser. Note: this does not currently work with resource aggregation. If using aggregation, it is important to not include the LESS file with a normal xp:styleSheet resource, but instead use xp:linkResource.
* An Eclipse builder plugin for Designer. By installing this plugin, right-clicking on an application, and choosing "Add/Remove LESS CSS Nature", it allows Designer to automatically compile LESS files to normal CSS versions. It matches two types: stylesheet resources with names in the form "foo.less.css" (because Designer automatically appends the ".css"), in which case it creates a file named "foo.css" next to it, and file resources elsewhere (e.g. File Resources or WebContent) with names in the form "foo.less", in which case it creates a new file next to it named "foo.less.css" (to avoid overwriting similarly-named normal-CSS files).

TODO
====

* Provide integration with CSS aggregation or provide servlet-specific aggregation
* Allow normal reference as "/foo.less" instead of "/xspless/foo.less" or standardize on using a UrlProcessor to translate
* Investigate also supporting SASS
* Investigate initializing NotesContext to allow using xspnsf:// URLs instead of requiring the OpenNTF Domino API