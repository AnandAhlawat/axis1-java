/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  Runtime state of the engine
 */
package org.apache.axis.modules;

import org.apache.axis.context.SystemContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.ExecutionChain;

public interface Module {
    public void init(SystemContext moduleContext) throws AxisFault;

    public void engage(ExecutionChain exeChain) throws AxisFault;

    public void shutDown() throws AxisFault;
}