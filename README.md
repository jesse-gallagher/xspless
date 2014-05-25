xspless
=======

LESS CSS Plugin for XPages

This takes the form (currently, at least) of a servlet aliased to "/xspless" within an NSF (behavior outside of an NSF is not handled). By specifying a path to a LESS file in the NSF as "/xspless/file.less" (can be a Stylesheet, a File Resource, or stored in WebContent), the servlet will automatically compile the LESS file to standard CSS to serve to the browser.

Note: this does not currently work with resource aggregation. If using aggregation, it is important to not include the LESS file with a normal xp:styleSheet resource, but instead use xp:linkResource.

TODO
====

* Provide integration with CSS aggregation or provide servlet-specific aggregation
* Allow normal reference as "/foo.less" instead of "/xspless/foo.less" or standardize on using a UrlProcessor to translate
* Investigate also supporting SASS
* Investigate initializing NotesContext to allow using xspnsf:// URLs instead of requiring the OpenNTF Domino API