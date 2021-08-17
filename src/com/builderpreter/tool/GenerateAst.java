package com.builderpreter.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException{
        if(args.length != 1){
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];
//        Each line of string is a type of syntax
        defineAst(outputDir, "Expr", Arrays.asList(
           "Binary : Expr left, Token operator, Expr right",
           "Grouping : Expr expressions",
           "Literal : Object value",
           "Unary : Token operator, Expr right"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException{
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");
//        Start writing the Expr class file
        writer.println("package com.builderpreter.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");
//        Start writing each syntax class type
        for(String type : types){
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }
        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList){
//        Class definition
        writer.println("  static class " + className + " extends " + baseName + " {");

//        Class constructor
        writer.println("    " + className + "(" + fieldList + ") {");

//        Extract each parameter for each class
        String[] fields = fieldList.split(", ");
        for(String field : fields){
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

//        Define each field
        writer.println();
        for(String field : fields){
            writer.println("    final " + field + ";");
        }

        writer.println("  }");
    }
}
