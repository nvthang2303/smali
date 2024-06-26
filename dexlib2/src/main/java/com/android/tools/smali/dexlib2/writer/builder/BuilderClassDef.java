/*
 * Copyright 2013, Google LLC
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google LLC nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.android.tools.smali.dexlib2.writer.builder;

import com.android.tools.smali.dexlib2.iface.ClassDef;
import com.android.tools.smali.dexlib2.util.MethodUtil;
import com.android.tools.smali.dexlib2.writer.DexWriter;
import com.android.tools.smali.dexlib2.base.reference.BaseTypeReference;
import com.android.tools.smali.dexlib2.writer.builder.BuilderEncodedValues.BuilderArrayEncodedValue;
import com.android.tools.smali.util.ArraySortedSet;
import com.android.tools.smali.util.CollectionUtils;
import com.android.tools.smali.util.IteratorUtils;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderClassDef extends BaseTypeReference implements ClassDef {
    @Nonnull final BuilderTypeReference type;
    final int accessFlags;
    @Nullable final BuilderTypeReference superclass;
    @Nonnull final BuilderTypeList interfaces;
    @Nullable final BuilderStringReference sourceFile;
    @Nonnull final BuilderAnnotationSet annotations;
    @Nonnull final SortedSet<BuilderField> staticFields;
    @Nonnull final SortedSet<BuilderField> instanceFields;
    @Nonnull final SortedSet<BuilderMethod> directMethods;
    @Nonnull final SortedSet<BuilderMethod> virtualMethods;
    @Nullable final BuilderArrayEncodedValue staticInitializers;

    int classDefIndex = DexWriter.NO_INDEX;
    int annotationDirectoryOffset = DexWriter.NO_OFFSET;

    BuilderClassDef(@Nonnull BuilderTypeReference type,
                    int accessFlags,
                    @Nullable BuilderTypeReference superclass,
                    @Nonnull BuilderTypeList interfaces,
                    @Nullable BuilderStringReference sourceFile,
                    @Nonnull BuilderAnnotationSet annotations,
                    @Nullable SortedSet<BuilderField> staticFields,
                    @Nullable SortedSet<BuilderField> instanceFields,
                    @Nullable Iterable<? extends BuilderMethod> methods,
                    @Nullable BuilderArrayEncodedValue staticInitializers) {
        if (methods == null) {
            methods = Collections.emptyList();
        }
        if (staticFields == null) {
            staticFields = Collections.emptySortedSet();
        }
        if (instanceFields == null) {
            instanceFields = Collections.emptySortedSet();
        }

        this.type = type;
        this.accessFlags = accessFlags;
        this.superclass = superclass;
        this.interfaces = interfaces;
        this.sourceFile = sourceFile;
        this.annotations = annotations;
        this.staticFields = staticFields;
        this.instanceFields = instanceFields;
        this.directMethods = ArraySortedSet.copyOf(CollectionUtils.naturalOrdering(), IteratorUtils.toList(
                (Iterator<? extends BuilderMethod>)IteratorUtils.filter(methods, MethodUtil.METHOD_IS_DIRECT)));
        this.virtualMethods = ArraySortedSet.copyOf(CollectionUtils.naturalOrdering(), IteratorUtils.toList(
                (Iterator<? extends BuilderMethod>)IteratorUtils.filter(methods, MethodUtil.METHOD_IS_VIRTUAL)));
        this.staticInitializers = staticInitializers;
    }

    @Nonnull @Override public String getType() { return type.getType(); }
    @Override public int getAccessFlags() { return accessFlags; }
    @Nullable @Override public String getSuperclass() { return superclass==null?null:superclass.getType(); }
    @Nullable @Override public String getSourceFile() { return sourceFile==null?null:sourceFile.getString(); }
    @Nonnull @Override public BuilderAnnotationSet getAnnotations() { return annotations; }
    @Nonnull @Override public SortedSet<BuilderField> getStaticFields() { return staticFields; }
    @Nonnull @Override public SortedSet<BuilderField> getInstanceFields() { return instanceFields; }
    @Nonnull @Override public SortedSet<BuilderMethod> getDirectMethods() { return directMethods; }
    @Nonnull @Override public SortedSet<BuilderMethod> getVirtualMethods() { return virtualMethods; }

    @Nonnull @Override
    public List<String> getInterfaces() {
        return this.interfaces.stream()
                .map(iface -> iface.toString())
                .collect(Collectors.toList());
    }

    @Nonnull @Override public Collection<BuilderField> getFields() {
        return new AbstractCollection<BuilderField>() {
            @Nonnull @Override public Iterator<BuilderField> iterator() {
                ArrayList<BuilderField> fields = new ArrayList<>(staticFields);
                fields.addAll(instanceFields);
                Collections.sort(fields, CollectionUtils.naturalOrdering());
                return fields.iterator();
            }

            @Override public int size() {
                return staticFields.size() + instanceFields.size();
            }
        };
    }

    @Nonnull @Override public Collection<BuilderMethod> getMethods() {
        return new AbstractCollection<BuilderMethod>() {
            @Nonnull @Override public Iterator<BuilderMethod> iterator() {
                ArrayList<BuilderMethod> methods = new ArrayList<>(directMethods);
                methods.addAll(virtualMethods);
                Collections.sort(methods, CollectionUtils.naturalOrdering());
                return methods.iterator();
            }

            @Override public int size() {
                return directMethods.size() + virtualMethods.size();
            }
        };
    }
}
