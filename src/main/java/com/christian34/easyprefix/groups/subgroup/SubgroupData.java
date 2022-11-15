package com.christian34.easyprefix.groups.subgroup;

import org.jetbrains.annotations.Nullable;

/**
 * EasyPrefix 2022.
 *
 * @author Christian34
 */
public abstract class SubgroupData {

    @Nullable
    public abstract String getPrefix();

    public abstract void setPrefix(@Nullable String prefix);

    @Nullable
    public abstract String getSuffix();

    public abstract void setSuffix(@Nullable String suffix);

    public abstract void delete();

}
