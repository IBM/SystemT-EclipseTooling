/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
 *******************************************************************************/
package com.ibm.biginsights.project.templates;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;

import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.templates.TemplateFactory.TemplateFactoryKeys;
import com.ibm.biginsights.project.util.BIConstants;

public class TemplateCreatorV13 implements ITemplateCreator {

	@Override
	public void createMapperType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object>data, IProgressMonitor monitor) throws CoreException {
	      
		imports.addImport("java.io.IOException"); //$NON-NLS-1$
		imports.addImport("org.apache.hadoop.mapreduce.Mapper"); //$NON-NLS-1$
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_KEYIN_TYPE_MAPPER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_KEYIN_TYPE_MAPPER));
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_VALUEIN_TYPE_MAPPER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_VALUEIN_TYPE_MAPPER));
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER));
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER));
		
		type.createMethod("" + //$NON-NLS-1$
				"@Override\n"+ //$NON-NLS-1$
				"public void map("+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_KEYIN_TYPE_MAPPER))+" key, "+ //$NON-NLS-1$ //$NON-NLS-2$
								   TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_VALUEIN_TYPE_MAPPER))+" value, "+ //$NON-NLS-1$
								 "Context context) throws IOException, InterruptedException {\n"+ //$NON-NLS-1$
				"}\n", //$NON-NLS-1$
	            null, false, monitor);
	}

	@Override
	public String getMapperSuperClassName() {
		return "org.apache.hadoop.mapreduce.Mapper"; //$NON-NLS-1$
	}
	
	@Override
	public String getMapperClassParameters(Map<TemplateFactoryKeys, Object>data) {
		if (data.get(TemplateFactoryKeys.MR_KEYIN_TYPE_MAPPER)!=null && data.get(TemplateFactoryKeys.MR_VALUEIN_TYPE_MAPPER)!=null &&
				data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER)!=null && data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER)!=null)
		{
			return "<"+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_KEYIN_TYPE_MAPPER))+ //$NON-NLS-1$
					", "+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_VALUEIN_TYPE_MAPPER))+  //$NON-NLS-1$
					", "+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER))+ //$NON-NLS-1$
					", "+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER))+">"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
			return ""; //$NON-NLS-1$
	}

	@Override
	public void createReducerType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object>data, IProgressMonitor monitor) throws CoreException {
		imports.addImport("java.io.IOException"); //$NON-NLS-1$
		imports.addImport("org.apache.hadoop.mapreduce.Reducer");	 //$NON-NLS-1$
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER));
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER));
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_REDUCER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_REDUCER));
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_REDUCER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_REDUCER));
		
		type.createMethod("" +				 //$NON-NLS-1$
				"public void reduce("+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER))+" key, "+ //$NON-NLS-1$ //$NON-NLS-2$
									"Iterable<"+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER))+"> values, "+ //$NON-NLS-1$ //$NON-NLS-2$
									"Context context) throws IOException, InterruptedException {\n"+ //$NON-NLS-1$
				"}\n", //$NON-NLS-1$
	            null, false, monitor);
		
	}

	@Override
	public String getReducerSuperClassName() {		
		return "org.apache.hadoop.mapreduce.Reducer"; //$NON-NLS-1$
	}
	
	@Override
	public String getReducerClassParameters(Map<TemplateFactoryKeys, Object>data) {
		if (data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER)!=null && data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER)!=null &&
			data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_REDUCER)!=null && data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_REDUCER)!=null)
		{
			return "<"+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_MAPPER))+ //$NON-NLS-1$
				", "+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_MAPPER))+  //$NON-NLS-1$
				", "+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_REDUCER))+ //$NON-NLS-1$
				", "+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_REDUCER))+">"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
			return ""; //$NON-NLS-1$
	}
	
	@Override
	public void createDriverType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object>data, IProgressMonitor monitor) throws CoreException {
		imports.addImport("java.io.IOException"); //$NON-NLS-1$
		imports.addImport("org.apache.hadoop.conf.Configuration"); //$NON-NLS-1$
		imports.addImport("org.apache.hadoop.fs.Path"); //$NON-NLS-1$

		imports.addImport("org.apache.hadoop.mapreduce.Job"); //$NON-NLS-1$
		imports.addImport("org.apache.hadoop.mapreduce.lib.input.FileInputFormat"); //$NON-NLS-1$
		imports.addImport("org.apache.hadoop.mapreduce.lib.output.FileOutputFormat"); //$NON-NLS-1$
		imports.addImport("org.apache.hadoop.util.GenericOptionsParser"); //$NON-NLS-1$
		
		// imports for output key and value types
		
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_REDUCER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_REDUCER));
		if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_REDUCER))!=null)
			imports.addImport((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_REDUCER));

		
		// add import for mapper package if different from driver package
		if (!data.get(TemplateFactoryKeys.MR_DRIVER_PACKAGE).equals(data.get(TemplateFactoryKeys.MR_MAPPER_PACKAGE)))
		{
			if (!((String)data.get(TemplateFactoryKeys.MR_MAPPER_PACKAGE)).isEmpty())	// need to check in case the mapper is in the default package but driver class is not
				imports.addImport(data.get(TemplateFactoryKeys.MR_MAPPER_PACKAGE)+"."+data.get(TemplateFactoryKeys.MR_MAPPER_CLASSNAME)); //$NON-NLS-1$
			else
				imports.addImport(""+data.get(TemplateFactoryKeys.MR_MAPPER_CLASSNAME)); //$NON-NLS-1$
		}

		// add import for reducer package if different from driver package
		if (!data.get(TemplateFactoryKeys.MR_DRIVER_PACKAGE).equals(data.get(TemplateFactoryKeys.MR_REDUCER_PACKAGE)))
		{
			if (!((String)data.get(TemplateFactoryKeys.MR_REDUCER_PACKAGE)).isEmpty())
				imports.addImport(data.get(TemplateFactoryKeys.MR_REDUCER_PACKAGE)+"."+data.get(TemplateFactoryKeys.MR_REDUCER_CLASSNAME)); //$NON-NLS-1$
			else
				imports.addImport(""+data.get(TemplateFactoryKeys.MR_REDUCER_CLASSNAME)); //$NON-NLS-1$
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("public static void main(String[] args) throws Exception {\n");		 //$NON-NLS-1$
		buffer.append("\tConfiguration conf = new Configuration();\n"); //$NON-NLS-1$
	
		buffer.append("\t// "+Messages.bind(Messages.TEMPLATECREATORV13_DRIVER_INPUT_ARRAY_COMMENT, "programArgs")+"\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
		buffer.append("\tString[] programArgs = new GenericOptionsParser(conf, args).getRemainingArgs();\n"); //$NON-NLS-1$
		buffer.append("\tJob job = new Job(conf);\n"); //$NON-NLS-1$
		buffer.append("\tjob.setJarByClass("+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_DRIVER_CLASSNAME))+".class);\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\tjob.setMapperClass("+data.get(TemplateFactoryKeys.MR_MAPPER_CLASSNAME)+".class);\n");				 //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\tjob.setReducerClass("+data.get(TemplateFactoryKeys.MR_REDUCER_CLASSNAME)+".class);\n"); //$NON-NLS-1$ //$NON-NLS-2$

		buffer.append("\n");		 //$NON-NLS-1$
		buffer.append("\tjob.setOutputKeyClass("+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_KEYOUT_TYPE_REDUCER))+".class);\n");		 //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\tjob.setOutputValueClass("+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.MR_VALUEOUT_TYPE_REDUCER))+".class);\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\n");		 //$NON-NLS-1$
		buffer.append("\t// TODO: "+Messages.TEMPLATECREATORV13_DRIVER_INPUT_PATH_COMMENT+"\n");  //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\tFileInputFormat.addInputPath(job, new Path(\"[input path]\"));\n"); //$NON-NLS-1$
		buffer.append("\t// TODO: "+Messages.TEMPLATECREATORV13_DRIVER_OUTPUT_PATH_COMMENT+"\n");  //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\tFileOutputFormat.setOutputPath(job, new Path(\"[output path]\"));\n"); //$NON-NLS-1$
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append("\t// "+Messages.TEMPLATECREATORV13_SUBMIT_WAIT_COMMENT+"\n"); //$NON-NLS-1$ //$NON-NLS-2$  
		buffer.append("\tjob.waitForCompletion(true);\n"); //$NON-NLS-1$
		buffer.append("\t// "+Messages.TEMPLATECREATORV13_SUBMIT_RETURN_COMMENT+"\n");  //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("\t// job.submit();\n"); //$NON-NLS-1$
		
		buffer.append("}"); //$NON-NLS-1$
		
		type.createMethod(buffer.toString(),null, false, monitor);		
	}

	@Override
	public void createMacroType(IType type, ImportsManager imports,	Map<TemplateFactoryKeys, Object> data, IProgressMonitor monitor)	throws CoreException {
		imports.addImport("org.apache.m2.exceptions.M2Exception"); //$NON-NLS-1$
		imports.addImport("org.apache.pig.backend.executionengine.ExecException"); //$NON-NLS-1$
		imports.addImport("org.apache.pig.data.Tuple"); //$NON-NLS-1$
		imports.addImport("org.apache.pig.impl.logicalLayer.FrontendException"); //$NON-NLS-1$
		imports.addImport("org.apache.pig.impl.logicalLayer.schema.Schema"); //$NON-NLS-1$
		imports.addImport("com.ibm.bigsheets.macros.AbstractMacro"); //$NON-NLS-1$				
		
		type.createMethod("" + //$NON-NLS-1$
				"@Override\n"+ //$NON-NLS-1$
				"public "+TemplateFactory.extractClassName(((String)data.get(TemplateFactoryKeys.BS_MACRO_RETURN_TYPE)))+" evaluate(Tuple arg0) throws ExecException, M2Exception {\n"+ //$NON-NLS-1$ //$NON-NLS-2$
				"// TODO: "+Messages.TEMPLATECREATORV13_MACRO_EVALUATE_COMMENT_1+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
				"// "+Messages.TEMPLATECREATORV13_MACRO_EVALUATE_COMMENT_2+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$
				"return null;\n"+  //$NON-NLS-1$
				"}\n",   //$NON-NLS-1$
				null, false, monitor);		
		
		type.createMethod("" + //$NON-NLS-1$
				"@Override\n"+ //$NON-NLS-1$
        "public Schema getInputSchema() {\n"+  //$NON-NLS-1$
				"// TODO: "+Messages.TEMPLATECREATORV13_MACRO_INPUTSCHEMA_COMMENT_1+"\n"+//$//$NON-NLS-1$ //$NON-NLS-2$
				"// "+Messages.bind(Messages.TEMPLATECREATORV13_MACRO_INPUTSCHEMA_COMMENT_2,"FieldSchema")+"\n"+//$//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"return null;\n"+ //$NON-NLS-1$
				"}\n",  //$NON-NLS-1$
				null, false, monitor);					

		type.createMethod("" + //$NON-NLS-1$
				"@Override\n"+ //$NON-NLS-1$
				"public Schema getOutputSchema(Schema arg0) throws FrontendException {\n"+ //$NON-NLS-1$
				"// TODO: "+Messages.bind(Messages.TEMPLATECREATORV13_MACRO_OUTPUTSCHEMA_COMMENT,"Schema","FieldSchema")+"\n"+ //$//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"return null;\n"+ //$NON-NLS-1$
				"}\n", //$NON-NLS-1$
				null, false, monitor);					

	}

	@Override
	public String getMacroSuperClassName() {
		return "com.ibm.bigsheets.macros.AbstractMacro"; //$NON-NLS-1$
	}

	@Override
	public String getMacroClassParameters(Map<TemplateFactoryKeys, Object> data) {
		if (data.get(TemplateFactoryKeys.BS_MACRO_RETURN_TYPE)!=null)
			return "<"+(String)data.get(TemplateFactoryKeys.BS_MACRO_RETURN_TYPE)+">"; //$NON-NLS-1$ //$NON-NLS-2$
		else 
			return ""; //$NON-NLS-1$
	}

	@Override
	public String getReaderSuperClassName(Map<TemplateFactoryKeys, Object> data) {
		return (String)data.get(TemplateFactoryKeys.BS_READER_SUPER_CLASS);
	}

	@Override
	public String getReaderClassParameters(Map<TemplateFactoryKeys, Object> data) {
		String result = ""; // for AbstractTextReader always return empty string //$NON-NLS-1$
		if (getReaderSuperClassName(data)!=null && getReaderSuperClassName(data).equals(BIConstants.READER_ABSTRACT_READER)) {
			if (data.get(TemplateFactoryKeys.BS_READER_KEY_TYPE)!=null && data.get(TemplateFactoryKeys.BS_READER_VALUE_TYPE)!=null){
				result = "<"+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.BS_READER_KEY_TYPE))+ //$NON-NLS-1$
						", "+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.BS_READER_VALUE_TYPE))+">"; //$NON-NLS-1$ //$NON-NLS-2$
			}			
		}
		 
		return result;
	}
	@Override
	public void createReaderType(IType type, ImportsManager imports, Map<TemplateFactoryKeys, Object> data, IProgressMonitor monitor) throws CoreException {
		imports.addImport("java.io.IOException"); //$NON-NLS-1$
		imports.addImport("org.apache.hadoop.mapreduce.Job"); //$NON-NLS-1$
		imports.addImport("org.apache.m2.exceptions.M2Exception"); //$NON-NLS-1$
		imports.addImport("org.apache.pig.ResourceSchema"); //$NON-NLS-1$
		imports.addImport("org.apache.pig.data.Tuple"); //$NON-NLS-1$
		if (getReaderSuperClassName(data)!=null && getReaderSuperClassName(data).equals(BIConstants.READER_ABSTRACT_READER)) {
			if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.BS_READER_KEY_TYPE))!=null)
				imports.addImport((String)data.get(TemplateFactoryKeys.BS_READER_KEY_TYPE)); //$NON-NLS-1$
			if (TemplateFactory.extractPackageName((String)data.get(TemplateFactoryKeys.BS_READER_VALUE_TYPE))!=null)
				imports.addImport((String)data.get(TemplateFactoryKeys.BS_READER_VALUE_TYPE)); //$NON-NLS-1$
			imports.addImport("org.apache.hadoop.mapreduce.InputFormat"); //$NON-NLS-1$
		}
		
		type.createMethod("" + //$NON-NLS-1$
				"@Override\n"+ //$NON-NLS-1$
				"public Tuple getNextValue() throws IOException, M2Exception {\n"+  //$NON-NLS-1$
				"// TODO: "+Messages.TEMPLATECREATORV13_READER_NEXTVALUE_COMMENT+"\n"+ ////$NON-NLS-1$ //$NON-NLS-2$
				"return null;\n"+ //$NON-NLS-1$
				"}\n",  //$NON-NLS-1$
				null, false, monitor);					

		type.createMethod("" + //$NON-NLS-1$
				"@Override\n"+ //$NON-NLS-1$
				"public ResourceSchema getResourceSchema(String arg0, Job arg1) {\n"+ //$NON-NLS-1$
				"// TODO: "+Messages.bind(Messages.TEMPLATECREATORV13_READER_RESOURCESCHEMA_COMMENT_1, "getResourceSchema")+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"// "+Messages.bind(Messages.TEMPLATECREATORV13_READER_RESOURCESCHEMA_COMMENT_2, "NumberOfChars", "DataType.INTEGER")+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"// "+Messages.bind(Messages.TEMPLATECREATORV13_READER_RESOURCESCHEMA_COMMENT_3, "LineContent", "DataType.STRING")+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"return null;\n"+ //$NON-NLS-1$
				"}\n",  //$NON-NLS-1$
				null, false, monitor);					

		if (getReaderSuperClassName(data)!=null && getReaderSuperClassName(data).equals(BIConstants.READER_ABSTRACT_READER)) {
			type.createMethod("" + //$NON-NLS-1$
					"@Override\n"+ //$NON-NLS-1$
					"public InputFormat<"+TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.BS_READER_KEY_TYPE))+", "+  //$NON-NLS-1$ //$NON-NLS-2$
										  TemplateFactory.extractClassName((String)data.get(TemplateFactoryKeys.BS_READER_VALUE_TYPE)) +"> getInputFormat() throws IOException {\n"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"// TODO: "+Messages.bind(Messages.TEMPLATECREATORV13_READER_INPUTFORMAT_COMMENT, new String[]{"getInputFormat", "InputFormat", "RecordReader", "InputFormat"})+"\n"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					"return null;\n"+ //$NON-NLS-1$
					"}\n", //$NON-NLS-1$
					null, false, monitor);					
		}
		
	}

}
