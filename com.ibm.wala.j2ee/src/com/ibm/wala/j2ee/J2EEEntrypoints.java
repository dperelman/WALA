/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.j2ee;

import java.util.Iterator;

import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.ComposedEntrypoints;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.strings.Atom;

/**
 * Entrypoints for an EAR file.
 */
public class J2EEEntrypoints implements Iterable<Entrypoint> {

  private final static boolean USE_STRUTS_ACTIONS = true;

  private Iterable<Entrypoint> entrypoints;

  private final AppClientEntrypoints appClientEntrypoints;

  private final StrutsEntrypoints strutsEntrypoints;

  /**
   * @param scope representation of the analysis scope
   * @param cha governing class hierarchy
   * @param useEjbEntrypoints should the analysis assume external callers on the EJB interfaces?
   */
  public J2EEEntrypoints(J2EEAnalysisScope scope, DeploymentMetaData dmd, IClassHierarchy cha, boolean useEjbEntrypoints) {
    ServletEntrypoints servletEntrypoints = new ServletEntrypoints(scope, cha);
    J2EEClassTargetSelector classTargetSelector = new J2EEClassTargetSelector(null, dmd, cha, cha.getLoader(scope.getLoader(Atom
        .findOrCreateUnicodeAtom("Synthetic"))));

    EJBEntrypoints ejbEntrypoints = null;
    if (useEjbEntrypoints) {
      // pick up all ejb entrypoints
      ejbEntrypoints = new EJBEntrypoints(cha, scope, dmd, false, classTargetSelector);
    } else {
      // pick up only MDB EJB entrypoints
      ejbEntrypoints = new EJBEntrypoints(cha, scope, dmd, true, classTargetSelector);
    }
    entrypoints = new ComposedEntrypoints(servletEntrypoints, ejbEntrypoints);

    appClientEntrypoints = new AppClientEntrypoints(scope, cha);
    entrypoints = new ComposedEntrypoints(entrypoints, appClientEntrypoints);

    if (USE_STRUTS_ACTIONS) {
      strutsEntrypoints = new StrutsEntrypoints(scope, cha);
      entrypoints = new ComposedEntrypoints(entrypoints, strutsEntrypoints);
    } else {
      strutsEntrypoints = null;
    }
  }

  public Iterator<Entrypoint> iterator() {
    return entrypoints.iterator();
  }

  public AppClientEntrypoints getAppClientEntrypoints() {
    return appClientEntrypoints;
  }

  public StrutsEntrypoints getStrutsEntrypoints() {
    return strutsEntrypoints;
  }

}