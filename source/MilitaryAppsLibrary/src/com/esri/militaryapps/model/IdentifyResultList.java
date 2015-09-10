/*******************************************************************************
 * Copyright 2012-2015 Esri
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.militaryapps.model;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An ordered list of IdentifyResult objects with references to the layers from which they
 * came. Implementers should provide a way to access the layer for their specific platform, as well
 * as a way to add a result.
 */
public abstract class IdentifyResultList {

    private final ArrayList<IdentifiedItem> results = new ArrayList<IdentifiedItem>();

    public int size() {
        return results.size();
    }

    public IdentifiedItem get(int index) {
        return results.get(index);
    }

    public void clear() {
        results.clear();
        clearResultToLayer();
    }

    /**
     * Adds a result. Implementers should also map this result to a layer.
     * @param result the result to add.
     */
    protected void addResult(IdentifiedItem result) {
        results.add(result);
    }

    /**
     * Clears the list's result-to-layer mapping.
     */
    protected abstract void clearResultToLayer();

    /**
     * Returns the layer from which the item came.
     * @param item the item.
     * @return the layer from which the item came.
     */
    public abstract Object getLayer(IdentifiedItem item);

    /**
     * Adds a result and maps it to a layer.
     * @param item the result to add.
     * @param layer the layer to which the result is mapped.
     */
    public abstract void add(IdentifiedItem item, Object layer);

}
