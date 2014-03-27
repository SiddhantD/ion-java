// Copyright (c) 2011-2014 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion.system;

import com.amazon.ion.IonCatalog;
import com.amazon.ion.IonWriter;
import com.amazon.ion.SymbolTable;


/**
 *
 */
abstract class IonWriterBuilderBase<T extends IonWriterBuilderBase>
    extends IonWriterBuilder
{
    // TODO reuseImportsAfterFinish

    private IonCatalog    myCatalog;
    private SymbolTable[] myImports;


    /** NOT FOR APPLICATION USE! */
    protected IonWriterBuilderBase()
    {
    }

    /** NOT FOR APPLICATION USE! */
    protected IonWriterBuilderBase(IonWriterBuilderBase that)
    {
        this.myCatalog = that.myCatalog;
        this.myImports = that.myImports;
    }


    //=========================================================================

    /**
     * Creates a mutable copy of this builder.
     *
     * @return a new builder with the same configuration as {@code this}.
     */
    abstract T copy();


    /**
     * Returns an immutable builder configured exactly like this one.
     *
     * @return this instance, if immutable;
     * otherwise an immutable copy of this instance.
     */
    abstract T immutable();


    /**
     * Returns a mutable builder configured exactly like this one.
     *
     * @return this instance, if mutable;
     * otherwise a mutable copy of this instance.
     */
    abstract T mutable();


    /** NOT FOR APPLICATION USE! */
    protected void mutationCheck()
    {
        throw new UnsupportedOperationException("This builder is immutable");
    }


    //=========================================================================

    /**
     * Gets the catalog to use when building an {@link IonWriter}.
     * The catalog is needed to resolve manually-written imports (not common).
     * By default, this property is null.
     *
     * @see #setCatalog(IonCatalog)
     * @see #withCatalog(IonCatalog)
     */
    public final IonCatalog getCatalog()
    {
        return myCatalog;
    }

    /**
     * Sets the catalog to use when building an {@link IonWriter}.
     *
     * @param catalog the catalog to use in built writers.
     *  If null, the writer will be unable to resolve manually-written imports
     *  and may throw an exception.
     *
     * @see #getCatalog()
     * @see #withCatalog(IonCatalog)
     *
     * @throws UnsupportedOperationException if this is immutable.
     */
    public final void setCatalog(IonCatalog catalog)
    {
        mutationCheck();
        myCatalog = catalog;
    }

    /**
     * Declares the catalog to use when building an {@link IonWriter},
     * returning a new mutable builder if this is immutable.
     *
     * @param catalog the catalog to use in built writers.
     *  If null, the writer will be unable to resolve manually-written imports
     *  and may throw an exception.
     *
     * @return this instance, if mutable;
     * otherwise a mutable copy of this instance.
     *
     * @see #getCatalog()
     * @see #setCatalog(IonCatalog)
     */
    public T withCatalog(IonCatalog catalog)
    {
        T b = mutable();
        b.setCatalog(catalog);
        return b;
    }

    //-------------------------------------------------------------------------


    private static SymbolTable[] safeCopy(SymbolTable[] imports)
    {
        if (imports != null && imports.length != 0)
        {
            imports = imports.clone();
        }
        return imports;
    }


    /**
     * Gets the imports that will be used to construct the initial local
     * symbol table.
     *
     * @return may be null or empty.
     *
     * @see #setImports(SymbolTable...)
     * @see #withImports(SymbolTable...)
     */
    public final SymbolTable[] getImports()
    {
        return safeCopy(myImports);
    }

    /**
     * Sets the shared symbol tables that will be used to construct the
     * initial local symbol table.
     * <p>
     * If the imports sequence is not null and not empty, the output stream
     * will be bootstrapped with a local symbol table that uses the given
     * {@code imports}.
     *
     * @param imports a sequence of shared symbol tables.
     * The first (and only the first) may be a system table.
     *
     * @see #getImports()
     * @see #withImports(SymbolTable...)
     *
     * @throws UnsupportedOperationException if this is immutable.
     */
    public void setImports(SymbolTable... imports)
    {
        mutationCheck();
        myImports = safeCopy(imports);
    }

    /**
     * Declares the imports to use when building an {@link IonWriter},
     * returning a new mutable builder if this is immutable.
     * <p>
     * If the imports sequence is not null and not empty, the output stream
     * will be bootstrapped with a local symbol table that uses the given
     * {@code imports}.
     *
     * @param imports a sequence of shared symbol tables.
     * The first (and only the first) may be a system table.
     *
     * @return this instance, if mutable;
     * otherwise a mutable copy of this instance.
     *
     * @see #getImports()
     * @see #setImports(SymbolTable...)
     */
    public T withImports(SymbolTable... imports)
    {
        T b = mutable();
        b.setImports(imports);
        return b;
    }
}