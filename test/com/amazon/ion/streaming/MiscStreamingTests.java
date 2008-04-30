
/*
 * Copyright (c) 2008 Amazon.com, Inc.  All rights reserved.
 */

package com.amazon.ion.streaming;

import com.amazon.ion.IonDatagram;
import com.amazon.ion.IonStruct;
import com.amazon.ion.IonSystem;
import com.amazon.ion.IonTestCase;
import com.amazon.ion.IonType;
import com.amazon.ion.IonValue;
import com.amazon.ion.system.SystemFactory;

/**
 *
 */
public class MiscStreamingTests
	extends IonTestCase
{
	static final boolean _debug_flag = false;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
    }
    

    //=========================================================================
    // Test cases
   
    // need the extra \\'s to get at least one slash to the ion parser
    static final String _QuotingString1_ion  = "s\\\"t1";
    static final String _QuotingString1_java = "s\"t1";
    static final String _QuotingString2_ion  = "s\\\'t2";
    static final String _QuotingString2_java = "s\'t2";
    public void testQuoting()
    throws Exception
    {
    	String s = " \""+_QuotingString1_ion+"\" '"+_QuotingString2_ion+"' ";
    	// buffer should be 23 bytes long
    	//		cookie (4 bytes)
    	//		local symbol table (12 bytes):
    	//			annotation (ann.len=11, 1, $ion_1_0=2, struct.len=8 )
    	//			member struct 'symbol' with: (2 bytes)
    	//				$10:str2 (6 bytes)
    	//		value1 string "str1" (1 typedesc + 4 bytes)
    	//		value2 symbol 'str2' (1 typedesc + 1 byte)
    	IonIterator ir = IonIterator.makeIterator(s);
    	
    	IonWriter wr = new IonBinaryWriter();
    	wr.writeIonEvents(ir);
    	
        byte[] buffer = wr.getBytes();
        assertSame("this buffer length is known to be 23", buffer.length, 23);
        
        IonIterator sir = IonIterator.makeIterator(s);
        IonIterator bir = IonIterator.makeIterator(s);

        checkIteratorForQuotingTest("string", sir);
        checkIteratorForQuotingTest("binary", bir);
        
    	return;
    }
    void checkIteratorForQuotingTest(String title, IonIterator ir) {
    	IonType t = ir.next();
    	assertTrue("first value is string for "+title, t.equals(IonType.STRING) );
    	String s = ir.getString();
    	assertTrue("checking first value "+title, s.equals( _QuotingString1_java ) );
    	
    	t = ir.next();
    	assertTrue("first value is string for "+title, t.equals(IonType.SYMBOL) );
    	s = ir.getString();
    	assertTrue("checking 2nd value "+title, s.equals( _QuotingString2_java ) );
    }
	

    public void testValue2()
    throws Exception
    {
    	String s = 
    		 "item_view::{item_id:\"B00096H8Q4\",marketplace_id:2,"
    		+"product:{item_name:["
    		+"{value:'''Method 24CT Leather Wipes''',lang:EN_CA},"
    		+"{value:'''Method 24CT Chiffons de Cuir''',lang:FR_CA}],"
    		+"list_price:{value:18.23,unit:EUR},}"
    		+",index_suppressed:true,"
    		+"offline_store_only:true,version:2,}";

    	IonSystem sys = SystemFactory.newSystem();
    	IonDatagram dg = sys.getLoader().load(s);
    	IonValue v = dg.get(0);
    	IonType t = v.getType();
    	assertSame( "should be a struct", t, IonType.STRUCT );
    	
    	int tree_count = ((IonStruct)v).size();
    	
    	IonIterator it = IonIterator.makeIterator(s);
    	
    	t = it.next();
    	assertSame( "should be a struct", t, IonType.STRUCT );
    	
    	int string_count = it.getContainerSize();
    	assertSame("tree and string iterator should have the same size", string_count, tree_count );
    	
    	byte[] buf = dg.toBytes();
    	it = IonIterator.makeIterator(buf);
    	
    	t = it.next();
    	assertSame( "should be a struct", t, IonType.STRUCT );
    	
    	int bin_count = it.getContainerSize();
    	assertSame("tree and binary iterator should have the same size", bin_count, tree_count );
    	
    	return;
    }

    public void testBinaryAnnotation()
    throws Exception
    {
    	String s = 
    		 "item_view::{item_id:\"B00096H8Q4\",marketplace_id:2,"
    		+"product:{item_name:["
    		+"{value:'''Method 24CT Leather Wipes''',lang:EN_CA},"
    		+"{value:'''Method 24CT Chiffons de Cuir''',lang:FR_CA}],"
    		+"list_price:{value:18.23,unit:EUR},}"
    		+",index_suppressed:true,"
    		+"offline_store_only:true,version:2,}";

    	IonSystem sys = SystemFactory.newSystem();
    	IonDatagram dg = sys.getLoader().load(s);
    	IonValue v = dg.get(0);
    	IonType t = v.getType();
    	assertSame( "should be a struct", t, IonType.STRUCT );
    	
    	// first make sure the ion tree got it right
    	assertTrue(v.hasTypeAnnotation("item_view"));
    	String[] ann = v.getTypeAnnotations(); 
    	assertTrue(ann.length == 1 && ann[0].equals("item_view"));

    	// now take the string and get a text iterator and
    	// make sure it got the annotation right
    	IonIterator it = IonIterator.makeIterator(s);
    	t = it.next();
    	assertSame( "should be a struct", t, IonType.STRUCT );
    	ann = it.getAnnotations();
    	assertTrue(ann.length == 1 && ann[0].equals("item_view"));
    	
    	// finally get the byte array from the tree, make a
    	// binary iterator and check its annotation handling
    	byte[] buf = dg.toBytes();
    	it = IonIterator.makeIterator(buf);
    	t = it.next();
    	assertSame( "should be a struct", t, IonType.STRUCT );
    	ann = it.getAnnotations();
    	assertTrue(ann.length == 1 && ann[0].equals("item_view"));
    	
    	return;
    }

}