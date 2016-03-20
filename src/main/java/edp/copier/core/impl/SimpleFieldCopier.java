package edp.copier.core.impl;

import edp.copier.core.api.FieldCopier;

public class SimpleFieldCopier extends AbstractFieldCopier {
    // singleton
    private static final SimpleFieldCopier INSTANCE = new SimpleFieldCopier();
    public static FieldCopier getInstance() { return INSTANCE; }
    private SimpleFieldCopier() {}
}
