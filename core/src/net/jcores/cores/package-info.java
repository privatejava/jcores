/**
 * <b>START HERE</b>, contains all available cores and their functionalities. However, please note 
 * that you don't create classes of this package directly, but rather through the 
 * {@link net.jcores.CoreKeeper} (accessible as <code>$</code>, also see the 
 * <a href="http://code.google.com/p/jcores/wiki/EclipseIntegration">Eclipse Integration Guide</a>). For 
 * example, to create a CoreObject for a number of objects, you would write:<br/><br/>
 * 
 * <code>$(o1, o2, o3)</code><br/><br/>
 * 
 * which returns a {@link net.jcores.cores.CoreObject}. The <code>$</code>-function always picks the most suitable {@link net.jcores.cores.Core} 
 * (better said: subclass of Core) for the elements you passed. Similar to the example above example, 
 * when you call<br/><br/>
 * 
 * <code>$("a", "b", "c")</code><br/><br/>
 * 
 * a {@link net.jcores.cores.CoreString} will be returned. In addition to the the cores in this package
 * also have a look at the {@link net.jcores.CommonCore} (accessible through <code>$</code> as well), 
 * which contains common helper functions that are not directly bound to a set of objects.
 *
 * @since 1.0
 */
package net.jcores.cores;