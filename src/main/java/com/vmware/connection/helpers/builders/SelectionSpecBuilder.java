package com.vmware.connection.helpers.builders;

import com.vmware.vim25.SelectionSpec;

/**
 *
 */
public class SelectionSpecBuilder extends SelectionSpec {
    public SelectionSpecBuilder name(final String name) {
        this.setName(name);
        return this;
    }
}
