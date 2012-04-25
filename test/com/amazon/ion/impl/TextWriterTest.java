// Copyright (c) 2009-2012 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion.impl;

import static com.amazon.ion.SystemSymbols.ION_1_0;
import static com.amazon.ion.system.IonWriterBuilder.InitialIvmHandling.SUPPRESS;

import com.amazon.ion.IonBinaryWriter;
import com.amazon.ion.IonDatagram;
import com.amazon.ion.IonReader;
import com.amazon.ion.IonSequence;
import com.amazon.ion.IonStruct;
import com.amazon.ion.IonWriter;
import com.amazon.ion.SymbolTable;
import com.amazon.ion.Symtabs;
import com.amazon.ion.SystemSymbols;
import com.amazon.ion.system.IonTextWriterBuilder;
import com.amazon.ion.system.IonTextWriterBuilder.LstMinimizing;
import com.amazon.ion.system.IonWriterBuilder.InitialIvmHandling;
import com.amazon.ion.system.IonWriterBuilder.IvmMinimizing;
import java.io.OutputStream;
import org.junit.Test;

/**
 *
 */
public class TextWriterTest
    extends OutputStreamWriterTestCase
{
    private IonTextWriterBuilder options;

    @Override
    protected IonWriter makeWriter(OutputStream out, SymbolTable... imports)
        throws Exception
    {
        myOutputForm = OutputForm.TEXT;

        if (options != null) return options.withImports(imports).build(out);

        return system().newTextWriter(out, imports);
    }

    protected String outputString()
        throws Exception
    {
        byte[] utf8Bytes = outputByteArray();
        return _Private_Utils.utf8(utf8Bytes);
    }

    @Test
    public void testNotWritingSymtab()
        throws Exception
    {
        iw = makeWriter();
        iw.writeSymbol("holla");
        String ionText = outputString();

        assertEquals("holla", ionText);
    }


    @Test
    public void testEnsureInitialIvm()
        throws Exception
    {
        options = IonTextWriterBuilder.standard();
        iw = makeWriter();
        iw.writeNull();
        String ionText = outputString();
        assertEquals("null", ionText);

        options.setInitialIvmHandling(InitialIvmHandling.ENSURE);
        iw = makeWriter();
        iw.writeNull();
        ionText = outputString();
        assertEquals(ION_1_0 + " null", ionText);

        iw = makeWriter();
        iw.writeSymbol(SystemSymbols.ION_1_0);
        iw.writeNull();
        ionText = outputString();
        assertEquals(ION_1_0 + " null", ionText);
    }


    @Test
    public void testIvmMinimizing()
        throws Exception
    {
        options = IonTextWriterBuilder.standard();
        iw = makeWriter();
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol("foo");
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol("bar");
        iw.writeSymbol(ION_1_0);

        String ionText = outputString();
        assertEquals(ION_1_0 + " " + ION_1_0 + " foo " +
                     ION_1_0 + " " + ION_1_0 + " " + ION_1_0 + " bar " +
                     ION_1_0,
                     ionText);

        options.setIvmMinimizing(IvmMinimizing.ADJACENT);
        iw = makeWriter();
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol("foo");
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol("bar");
        iw.writeSymbol(ION_1_0);

        ionText = outputString();
        assertEquals(ION_1_0 + " foo " + ION_1_0 + " bar " + ION_1_0,
                     ionText);
    }

    @Test
    public void testLstMinimizing()
        throws Exception
    {
        SymbolTable fred1 = Symtabs.register("fred",   1, catalog());

        IonBinaryWriter binaryWriter = system().newBinaryWriter(fred1);
        binaryWriter.writeSymbol("fred_1");
        binaryWriter.writeSymbol("ginger");
        binaryWriter.finish();
        byte[] binaryData = binaryWriter.getBytes();

        options = IonTextWriterBuilder.standard();

        // TODO User reader still transfers local symtabs!
        IonReader binaryReader = system().newReader(binaryData);
        iw = makeWriter();
        iw.writeValues(binaryReader);

        String ionText = outputString();
        assertEquals(// TODO "$ion_1_0 " +
                     "$ion_symbol_table::{imports:[{name:\"fred\",version:1,max_id:2}],"
                     +                   "symbols:[\"ginger\"]} " +
                     "fred_1 ginger",
                     ionText);

        options.setLstMinimizing(LstMinimizing.LOCALS);
        binaryReader = system().newReader(binaryData);
        iw = makeWriter();
        iw.writeValues(binaryReader);
        ionText = outputString();
        assertEquals(// TODO "$ion_1_0 " +
                     "$ion_symbol_table::{imports:[{name:\"fred\",version:1,max_id:2}]} " +
                     "fred_1 ginger",
                     ionText);

        options.setLstMinimizing(LstMinimizing.EVERYTHING);
        binaryReader = system().newReader(binaryData);
        iw = makeWriter();
        iw.writeValues(binaryReader);
        ionText = outputString();
        assertEquals(// TODO "$ion_1_0 " +
                     "$ion_1_0 fred_1 ginger",
                     ionText);

        options.setInitialIvmHandling(InitialIvmHandling.SUPPRESS);
        binaryReader = system().newReader(binaryData);
        iw = makeWriter();
        iw.writeValues(binaryReader);
        ionText = outputString();
        assertEquals("fred_1 ginger",
                     ionText);
    }


    private void expectRendering(String expected, IonDatagram original)
        throws Exception
    {
        iw = makeWriter();
        original.writeTo(iw);

        assertEquals(original, reload());

        String actual = outputString();
        assertEquals(expected, actual);
    }

    @Test
    public void testWritingLongStrings()
        throws Exception
    {
        options = IonTextWriterBuilder.standard();
        options.setInitialIvmHandling(SUPPRESS);
        options.setLongStringThreshold(5);

        // This is tricky because we must avoid triple-quoting multiple
        // long strings in such a way that they'd get concatenated together!
        IonDatagram dg = system().newDatagram();
        dg.add().newNullString();
        dg.add().newString("hello");
        dg.add().newString("hello\nnurse");
        dg.add().newString("goodbye").addTypeAnnotation("a");
        dg.add().newString("what's\nup\ndoc");

        expectRendering("null.string \"hello\" '''hello\nnurse''' a::'''goodbye''' \"what's\\nup\\ndoc\"",
                        dg);

        dg.clear();
        dg.add().newString("looong");
        IonSequence seq = dg.add().newEmptySexp();
        seq.add().newString("looong");
        seq.add().newString("hello");
        seq.add().newString("hello\nnurse");
        seq.add().newString("goodbye").addTypeAnnotation("a");
        seq.add().newString("what's\nup\ndoc");

        expectRendering("'''looong''' ('''looong''' \"hello\" '''hello\nnurse''' a::'''goodbye''' \"what's\\nup\\ndoc\")",
                        dg);

        dg.clear();
        dg.add().newString("looong");
        seq = dg.add().newEmptyList();
        seq.add().newString("looong");
        seq.add().newString("hello");
        seq.add().newString("hello\nnurse");
        seq.add().newString("what's\nup\ndoc");

        expectRendering("'''looong''' ['''looong''',\"hello\",'''hello\n" +
                        "nurse''','''what\\'s\n" +
                        "up\n" +
                        "doc''']",
                        dg);

        options.setLongStringThreshold(0);
        expectRendering("\"looong\" [\"looong\",\"hello\",\"hello\\nnurse\",\"what's\\nup\\ndoc\"]",
            dg);

        options.setLongStringThreshold(5);
        dg.clear();
        dg.add().newString("looong");
        IonStruct struct = dg.add().newEmptyStruct();
        struct.add("a").newString("looong");
        struct.add("b").newString("hello");
        struct.add("c").newString("hello\nnurse");
        struct.add("d").newString("what's\nup\ndoc");

        expectRendering("'''looong''' {a:'''looong''',b:\"hello\",c:'''hello\n" +
                        "nurse''',d:'''what\\'s\n" +
                        "up\n" +
                        "doc'''}",
                        dg);

        options.withPrettyPrinting();
        expectRendering("\n" +
                "'''looong'''\n" +
                "{\n" +
                        "  a:'''looong''',\n" +
                "  b:\"hello\",\n" +
                "  c:'''hello\n" +
                        "nurse''',\n" +
                        "  d:'''what\\'s\n" +
                        "up\n" +
                        "doc'''\n" +
                        "}",
            dg);
    }

    @Test
    public void testJsonLongStrings()
        throws Exception
    {
        options = IonTextWriterBuilder.json();
        options.setLongStringThreshold(5);

        IonDatagram dg = system().newDatagram();
        dg.add().newString("hello");
        dg.add().newString("hello!");
        dg.add().newString("goodbye");
        dg.add().newString("what's\nup\ndoc");

        expectRendering("\"hello\" '''hello!''' \"goodbye\" '''what\\'s\nup\ndoc'''",
                        dg);
    }

    @Test
    public void testJsonSystemMinimization()
        throws Exception
    {
        SymbolTable fred1 = Symtabs.register("fred",   1, catalog());

        options = IonTextWriterBuilder.json();
        iw = makeWriter(fred1);
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol("fred_1");

        // TODO ION-283 fix distant IVMs

        assertEquals("\"fred_1\"", outputString());
    }

    @Test
    public void testWritingLongClobs()
        throws Exception
    {
        options = IonTextWriterBuilder.standard();
        options.setInitialIvmHandling(SUPPRESS);
        options.setLongStringThreshold(3);


        IonDatagram dg = system().newDatagram();
        dg.add().newClob(new byte[]{'a', 'b', '\n'});

        expectRendering("{{\"ab\\n\"}}", dg);

        dg.clear();
        dg.add().newClob(new byte[]{'a', 'b', '\n', 'c'});
        expectRendering("{{'''ab\n" +
                        "c'''}}",
                        dg);
    }

    @Test
    public void testSuppressInitialIvm()
        throws Exception
    {
        iw = makeWriter();
        iw.writeSymbol(ION_1_0);
//        iw.writeSymbol(ION_1_0);  // TODO User writer always minimizes adjacent
        iw.writeNull();
        iw.writeSymbol(ION_1_0);
        iw.writeNull();

        assertEquals(/*ION_1_0 + " " +*/ ION_1_0 + " null " + ION_1_0 + " null",
                     outputString());

        options = IonTextWriterBuilder.standard().withInitialIvmHandling(SUPPRESS);

        iw = makeWriter();
        iw.writeSymbol(ION_1_0);
        iw.writeSymbol(ION_1_0);
        iw.writeNull();
        iw.writeSymbol(ION_1_0);
        iw.writeNull();

        assertEquals("null " + ION_1_0 + " null", outputString());
    }

    @Test @Override
    public void testWritingLob()
        throws Exception
    {
        super.testWritingLob();

        options = IonTextWriterBuilder.standard();
        super.testWritingLob();

        options = IonTextWriterBuilder.pretty();
        super.testWritingLob();

        options.setLongStringThreshold(2);
        super.testWritingLob();
    }
}
