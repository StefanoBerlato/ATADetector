package it.unitn.atadetector.javalevel.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static org.objectweb.asm.Opcodes.ASM4;


/**
 * This class will simply go through the imported classes, their methods, fields
 * and Strings present in the class that the Visitor is analyzing
 */
public class ClassFileASMVisitor extends ClassVisitor {

    // these are the object that collects all the elements found in the class
    // (classes in [0], methods in [1], attributes in [2], strings in [3])
    private final ArrayList<HashMap<String, int[]>> classProperties;

    // the name of the class this parser is analyzing and the flag
    private String className;
    private String flag;


    /**
     * Constructor
     * @param classProperties array where to put classes in [0], methods in [1], attributes in [2], strings in [3]
     * @param flag flag to insert along with the found occurrences
     */
    public ClassFileASMVisitor(ArrayList<HashMap<String, int[]>> classProperties, String flag) {

        // the super constructor
        super(ASM4);

        // assign the variables
        this.classProperties = classProperties;
        this.flag = flag;
    }


    // here we are just interested in getting the class name
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        // assign the class name
        this.className = name;
    }


    // visit a field to get the value, if a string
    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {

        // if the field is a String and the value is not null
        if (desc.equals("Ljava/lang/String;") && value != null) {

            int[] oldValues = classProperties.get(3).getOrDefault(value, new int[] {0, 0});
            int[] newValues = new int[] {oldValues[0] + 1, (flag.equals("1") ? oldValues[1] : oldValues[1] + 1)};
            String valueAsString = (String) value;
            classProperties.get(3).put(valueAsString, newValues);
        }

        // return null, we don't need a field visitor
        return null;
    }


    // invoke the method visitor
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        // analyze the method, passing as parameter the HashMap
        return new MethodFileASMVisitor(ASM4, className, classProperties, flag);
    }
}